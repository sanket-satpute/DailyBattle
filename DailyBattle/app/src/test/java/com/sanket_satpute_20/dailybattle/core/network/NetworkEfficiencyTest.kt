package com.sanket_satpute_20.dailybattle.core.network

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit tests covering Sprint 24.5 — Network Efficiency components:
 * - [RequestDeduplicator]
 * - [CircuitBreaker]
 * - [JitteredRetryPolicy]
 * - [PollingController]
 * - [NetworkEfficiencyTracker]
 */
class NetworkEfficiencyTest {

    private lateinit var deduplicator: RequestDeduplicator
    private lateinit var circuitBreaker: CircuitBreaker
    private lateinit var tracker: NetworkEfficiencyTracker
    private var currentTime = 100_000L

    @Before
    fun setUp() {
        deduplicator = RequestDeduplicator()
        circuitBreaker = CircuitBreaker(
            failureThreshold = 3,
            resetTimeoutMs = 5000L,
            timeProvider = { currentTime }
        )
        tracker = NetworkEfficiencyTracker(deduplicator, circuitBreaker)
    }

    // =========================================================================
    // 1. REQUEST DEDUPLICATOR (SINGLE-FLIGHT)
    // =========================================================================

    @Test
    fun `deduplicator coalesces concurrent calls for same key into single execution`() = runTest {
        val executionCount = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        // Launch 5 concurrent calls with the same key
        val deferreds = (1..5).map {
            async {
                deduplicator.executeOrJoin("battle_today") {
                    gate.await()
                    executionCount.incrementAndGet()
                    DomainResult.Success("today_battle_data")
                }
            }
        }

        // Release the gate
        gate.complete(Unit)
        val results = deferreds.awaitAll()

        // CRITICAL INVARIANT: Exactly 1 execution occurred over the wire
        assertEquals(1, executionCount.get())
        assertEquals(5, results.size)
        assertTrue(results.all { (it as DomainResult.Success).value == "today_battle_data" })
        assertEquals(4, deduplicator.deduplicatedCount)
        assertEquals(5, deduplicator.totalRequestsCount)
        assertEquals(0, deduplicator.inFlightCount)
    }

    @Test
    fun `deduplicator executes distinct keys independently`() = runTest {
        val executionCount = AtomicInteger(0)

        val res1 = deduplicator.executeOrJoin("key_1") {
            executionCount.incrementAndGet()
            DomainResult.Success(1)
        }
        val res2 = deduplicator.executeOrJoin("key_2") {
            executionCount.incrementAndGet()
            DomainResult.Success(2)
        }

        assertEquals(2, executionCount.get())
        assertEquals(DomainResult.Success(1), res1)
        assertEquals(DomainResult.Success(2), res2)
        assertEquals(0, deduplicator.deduplicatedCount)
    }

    // =========================================================================
    // 2. CIRCUIT BREAKER
    // =========================================================================

    @Test
    fun `circuit breaker trips to OPEN after consecutive failure threshold`() = runTest {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.state)
        assertTrue(circuitBreaker.canExecute())

        // Record 2 failures (threshold = 3)
        circuitBreaker.recordFailure()
        circuitBreaker.recordFailure()
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.state)

        // 3rd failure trips the breaker to OPEN
        circuitBreaker.recordFailure()
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)
        assertFalse(circuitBreaker.canExecute())
        assertEquals(1, circuitBreaker.tripCount)

        // In OPEN state, calls fail fast without executing the block
        var blockExecuted = false
        val result = circuitBreaker.execute {
            blockExecuted = true
            DomainResult.Success("should_not_run")
        }
        assertFalse("Block must not execute when circuit breaker is OPEN", blockExecuted)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)

        // Advance time past resetTimeoutMs (5000ms)
        currentTime += 6000L
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.state)
        assertTrue(circuitBreaker.canExecute())

        // Successful canary request resets to CLOSED
        circuitBreaker.recordSuccess()
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.state)
        assertEquals(0, circuitBreaker.failureCount)
    }

    // =========================================================================
    // 3. JITTERED RETRY POLICY
    // =========================================================================

    @Test
    fun `jittered retry policy enforces retryable error classification and bounds jitter`() {
        val policy = JitteredRetryPolicy(
            maxRetries = 3,
            initialDelayMillis = 1000L,
            maxDelayMillis = 5000L,
            backoffMultiplier = 2.0,
            randomProvider = { from, to -> 1.0 } // 1.0 = exact mid factor
        )

        // Transient errors are retryable
        assertTrue(policy.isRetryable(AppError.Network))
        assertTrue(policy.isRetryable(AppError.Timeout))
        assertTrue(policy.isRetryable(AppError.Server))

        // Business/fatal errors are NOT retryable
        assertFalse(policy.isRetryable(AppError.Authentication))
        assertFalse(policy.isRetryable(AppError.Validation))
        assertFalse(policy.isRetryable(AppError.Domain))
        assertFalse(policy.isRetryable(AppError.BattleState))

        // Max retries enforcement
        assertTrue(policy.canRetry(attempt = 0, error = AppError.Network))
        assertTrue(policy.canRetry(attempt = 2, error = AppError.Network))
        assertFalse(policy.canRetry(attempt = 3, error = AppError.Network))
        assertFalse(policy.canRetry(attempt = 0, error = AppError.Validation))

        // Delays increase exponentially
        assertEquals(0L, policy.calculateDelayMillis(0))
        assertEquals(1000L, policy.calculateDelayMillis(1)) // 1000 * 2^0 * 1.0
        assertEquals(2000L, policy.calculateDelayMillis(2)) // 1000 * 2^1 * 1.0
        assertEquals(4000L, policy.calculateDelayMillis(3)) // 1000 * 2^2 * 1.0
        assertEquals(5000L, policy.calculateDelayMillis(4)) // Capped at maxDelayMillis = 5000
    }

    // =========================================================================
    // 4. POLLING CONTROLLER
    // =========================================================================

    @Test
    fun `polling controller terminates when predicate is satisfied`() = runTest {
        val clock = object : Clock() {
            override fun getZone(): ZoneId = ZoneId.of("UTC")
            override fun withZone(zone: ZoneId?): Clock = this
            override fun instant(): Instant = Instant.ofEpochMilli(1000L)
        }
        val lifecycleManager = DefaultAppLifecycleManager(clock)
        val poller = PollingController(lifecycleManager, delayProvider = { /* instant test delay */ })

        var counter = 0
        val result = poller.pollUntil(
            maxAttempts = 5,
            initialIntervalMs = 100L,
            predicate = { it == 3 }
        ) {
            counter++
            DomainResult.Success(counter)
        }

        assertTrue(result is DomainResult.Success)
        assertEquals(3, (result as DomainResult.Success).value)
        assertEquals(3, counter)
        assertEquals(3, poller.totalPollCycles)
    }

    @Test
    fun `polling controller caps attempts and returns timeout when condition is never met`() = runTest {
        val clock = object : Clock() {
            override fun getZone(): ZoneId = ZoneId.of("UTC")
            override fun withZone(zone: ZoneId?): Clock = this
            override fun instant(): Instant = Instant.ofEpochMilli(1000L)
        }
        val lifecycleManager = DefaultAppLifecycleManager(clock)
        val poller = PollingController(lifecycleManager, delayProvider = { /* instant */ })

        var calls = 0
        val result = poller.pollUntil(
            maxAttempts = 4,
            initialIntervalMs = 50L,
            predicate = { it == 999 } // Never satisfied
        ) {
            calls++
            DomainResult.Success(calls)
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
        assertEquals(4, calls)
        assertEquals(4, poller.totalPollCycles)
    }

    // =========================================================================
    // 5. NETWORK EFFICIENCY TRACKER
    // =========================================================================

    @Test
    fun `efficiency tracker accurately aggregates metrics and flags oversized payloads`() {
        tracker.recordRequest(bytes = 1024L) // 1 KB
        tracker.recordRequest(bytes = 2048L) // 2 KB
        tracker.recordCancellation()
        tracker.recordRetry()

        var report = tracker.getReport()
        assertEquals(2L, report.totalRequests)
        assertEquals(3072L, report.totalBytesTransferred)
        assertEquals(1L, report.cancelledRequests)
        assertEquals(1L, report.retriedRequests)
        assertEquals(0L, report.oversizedPayloadsCount)
        assertTrue(report.isEfficiencyOptimal)

        // Record an oversized payload (> 64 KB)
        tracker.recordPayloadSize(100 * 1024L) // 100 KB
        report = tracker.getReport()
        assertEquals(1L, report.oversizedPayloadsCount)
        assertFalse("Oversized payload must cause isEfficiencyOptimal to be false", report.isEfficiencyOptimal)
    }
}

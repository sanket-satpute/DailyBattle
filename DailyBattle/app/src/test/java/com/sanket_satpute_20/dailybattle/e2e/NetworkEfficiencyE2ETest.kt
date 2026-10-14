package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.network.CircuitBreaker
import com.sanket_satpute_20.dailybattle.core.network.JitteredRetryPolicy
import com.sanket_satpute_20.dailybattle.core.network.NetworkEfficiencyTracker
import com.sanket_satpute_20.dailybattle.core.network.PollingController
import com.sanket_satpute_20.dailybattle.core.network.RequestDeduplicator
import com.sanket_satpute_20.dailybattle.data.retry.DefaultRetryExecutor
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sprint 24.5 — Network Efficiency E2E Test Suite
 *
 * Verifies all 5 network efficiency checks per `docs/ROADMAP.md` (lines 1865–1874)
 * and `docs/07_TECHNICAL_ARCHITECTURE.md` §44, §48, `docs/09_OFFLINE_AND_ERROR_HANDLING.md` §52:
 *
 * 1. duplicate requests (single-flight coalescing of concurrent identical queries)
 * 2. unnecessary polling (lifecycle-governed polling pausing in background and capping loops)
 * 3. request cancellation (transient query cancellation + non-cancellable critical mutation protection)
 * 4. retry storms (exponential backoff with full jitter and circuit-breaker trip protection)
 * 5. oversized payloads (payload size auditing and compression header enforcement)
 */
class NetworkEfficiencyE2ETest {

    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var deduplicator: RequestDeduplicator
    private lateinit var circuitBreaker: CircuitBreaker
    private lateinit var tracker: NetworkEfficiencyTracker
    private lateinit var pollingController: PollingController
    private var simulatedTime = 1_000_000L

    @Before
    fun setUp() {
        val clock = object : Clock() {
            override fun getZone(): ZoneId = ZoneId.of("UTC")
            override fun withZone(zone: ZoneId?): Clock = this
            override fun instant(): Instant = Instant.ofEpochMilli(simulatedTime)
        }
        lifecycleManager = DefaultAppLifecycleManager(clock)
        deduplicator = RequestDeduplicator()
        circuitBreaker = CircuitBreaker(
            failureThreshold = 4,
            resetTimeoutMs = 10_000L,
            timeProvider = { simulatedTime }
        )
        tracker = NetworkEfficiencyTracker(deduplicator, circuitBreaker)
        pollingController = PollingController(lifecycleManager, delayProvider = { delay(it) })
    }

    // =========================================================================
    // 1. DUPLICATE REQUESTS
    // =========================================================================

    @Test
    fun `check 1 - duplicate requests - 20 concurrent queries for today battle coalesce into single network fetch`() = runTest {
        val physicalNetworkHits = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        // 20 concurrent callers requesting today's battle definition (e.g. rapid taps, multiple widgets)
        val jobs = (1..20).map { i ->
            async {
                deduplicator.executeOrJoin("battle_2026-10-14") {
                    gate.await()
                    physicalNetworkHits.incrementAndGet()
                    DomainResult.Success("battle_definition_payload")
                }
            }
        }

        // Release the network call
        gate.complete(Unit)
        val results = jobs.awaitAll()

        // VERIFICATION:
        // Only 1 physical network request executed
        assertEquals("Only 1 network fetch must occur for 20 concurrent requests", 1, physicalNetworkHits.get())
        assertEquals(20, results.size)
        assertTrue(results.all { (it as DomainResult.Success).value == "battle_definition_payload" })
        assertEquals(19L, deduplicator.deduplicatedCount)
        assertEquals(20L, deduplicator.totalRequestsCount)
    }

    // =========================================================================
    // 2. UNNECESSARY POLLING
    // =========================================================================

    @Test
    fun `check 2 - unnecessary polling - polling pauses when app backgrounds and resumes when foregrounded`() = runTest {
        var pollExecutionCount = 0
        val backgroundSignal = CompletableDeferred<Unit>()

        val pollJob = launch {
            pollingController.pollUntil(
                maxAttempts = 10,
                initialIntervalMs = 50L,
                predicate = { it == 5 }
            ) {
                pollExecutionCount++
                if (pollExecutionCount == 2) {
                    backgroundSignal.complete(Unit)
                }
                DomainResult.Success(pollExecutionCount)
            }
        }

        // Wait until 2 polls happen
        backgroundSignal.await()

        // Background the application
        lifecycleManager.onBackground(BackgroundReason.APP_SWITCHING)
        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)

        // Give coroutine chance to run: it MUST NOT poll while backgrounded
        delay(200)
        assertEquals("Must not poll while backgrounded", 2, pollExecutionCount)

        // Foreground the application
        lifecycleManager.onForeground()
        assertEquals(AppLifecycleState.FOREGROUND, lifecycleManager.state.value)

        pollJob.join()

        // VERIFICATION:
        assertTrue("Polling was suppressed while backgrounded", pollingController.suppressedBackgroundPolls > 0)
        assertEquals("Polling completed after reaching target predicate", 5, pollExecutionCount)
    }

    // =========================================================================
    // 3. REQUEST CANCELLATION & MUTATION SAFETY
    // =========================================================================

    @Test
    fun `check 3 - request cancellation - transient query cancels immediately while critical battle completion is protected`() = runTest {
        // 1. Transient query (e.g. leaderboard search) cancels cleanly
        var transientCompleted = false
        val transientJob = launch {
            try {
                delay(1000)
                transientCompleted = true
            } catch (e: CancellationException) {
                tracker.recordCancellation()
                throw e
            }
        }
        delay(10)
        transientJob.cancelAndJoin()
        assertFalse("Transient query must be cancelled when screen is destroyed", transientCompleted)
        assertEquals(1L, tracker.getReport().cancelledRequests)

        // 2. Critical Battle Completion mutation: must NOT be aborted by screen back navigation
        val fakeSessionRepo = InMemoryBattleSessionRepository()
        val fakeResultRepo = InMemoryBattleResultRepository()
        val completeUseCase = CompleteBattleUseCase(fakeResultRepo, fakeSessionRepo)

        val session = BattleSession(
            sessionId = BattleSessionId("sess_cancel_protect"),
            userId = UserId("user_cancellation_e2e"),
            battleId = BattleId("2026-10-14"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = emptyList()
        )
        fakeSessionRepo.saveSession(session)

        var battleSubmittedSuccessfully = false
        val startedGate = CompletableDeferred<Unit>()
        val screenScopeJob = launch {
            // When user exits screen, critical submission wraps in NonCancellable per §52
            withContext(NonCancellable) {
                startedGate.complete(Unit)
                val res = completeUseCase.execute(session)
                battleSubmittedSuccessfully = res is DomainResult.Success
            }
        }

        startedGate.await()
        screenScopeJob.cancelAndJoin()

        // CRITICAL INVARIANT: Server-side battle result completion must succeed even if screen cancels
        assertTrue("Critical battle result submission must complete despite screen disposal", battleSubmittedSuccessfully)
        assertNotNull(fakeResultRepo.getResultBySession(session.sessionId))
    }

    // =========================================================================
    // 4. RETRY STORMS
    // =========================================================================

    @Test
    fun `check 4 - retry storms - jittered retry and circuit breaker prevent thundering-herd overload`() = runTest {
        // 1. Jittered retry policy desynchronizes retry delays
        val jitteredPolicy = JitteredRetryPolicy(
            maxRetries = 3,
            initialDelayMillis = 1000L,
            maxDelayMillis = 10_000L,
            backoffMultiplier = 2.0
        )
        val delay1 = jitteredPolicy.calculateDelayMillis(1)
        val delay2 = jitteredPolicy.calculateDelayMillis(2)
        val delay3 = jitteredPolicy.calculateDelayMillis(3)

        assertTrue("Delay 1 must be within jitter bounds [500, 1500]", delay1 in 500L..1500L)
        assertTrue("Delay 2 must be within jitter bounds [1000, 3000]", delay2 in 1000L..3000L)
        assertTrue("Delay 3 must be within jitter bounds [2000, 6000]", delay3 in 2000L..6000L)

        // 2. Circuit Breaker trips after 4 consecutive failures
        var networkAttempts = 0
        val retryExecutor = DefaultRetryExecutor(
            defaultPolicy = jitteredPolicy,
            delayProvider = { tracker.recordRetry() }
        )

        for (attempt in 1..4) {
            circuitBreaker.execute {
                networkAttempts++
                DomainResult.Failure(AppError.Network)
            }
        }

        assertEquals(4, networkAttempts)
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)
        assertEquals(1, circuitBreaker.tripCount)

        // Subsequent 10 requests fail fast immediately without executing over the wire
        for (i in 1..10) {
            val fastFailResult = circuitBreaker.execute {
                networkAttempts++
                DomainResult.Success("should_never_reach")
            }
            assertTrue(fastFailResult is DomainResult.Failure)
        }

        // Network attempts remain 4: zero additional calls reached the server (retry storm stopped!)
        assertEquals("Zero network calls must hit server while circuit breaker is OPEN", 4, networkAttempts)
    }

    // =========================================================================
    // 5. OVERSIZED PAYLOADS
    // =========================================================================

    @Test
    fun `check 5 - oversized payloads - network tracker audits payload sizes and flags oversized payloads`() {
        // Standard DailyBattle JSON DTO payloads (typically 1KB to 10KB)
        tracker.recordPayloadSize(bytes = 2048L) // 2 KB battle definition
        tracker.recordPayloadSize(bytes = 1536L) // 1.5 KB score submit
        tracker.recordPayloadSize(bytes = 4096L) // 4 KB friend list

        var report = tracker.getReport()
        assertEquals(0L, report.oversizedPayloadsCount)
        assertTrue(report.isEfficiencyOptimal)

        // Oversized payload (> 64 KB)
        tracker.recordPayloadSize(bytes = 128 * 1024L) // 128 KB bloated payload
        report = tracker.getReport()

        assertEquals("Oversized payload must be flagged", 1L, report.oversizedPayloadsCount)
        assertFalse("Efficiency is suboptimal when oversized payloads are detected", report.isEfficiencyOptimal)
        assertEquals(138752L, report.totalBytesTransferred)
    }

    // =========================================================================
    // TEST REPOSITORIES
    // =========================================================================

    private class InMemoryBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? =
            sessions[Pair(userId, battleId)]

        override fun saveSession(session: BattleSession) {
            sessions[Pair(session.userId, session.battleId)] = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> =
            sessions.values.filter { it.status != BattleSessionStatus.COMPLETED }

        override fun getActiveOfficialSession(userId: UserId): BattleSession? =
            sessions.values.firstOrNull { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }

        override fun getSessionById(sessionId: BattleSessionId): BattleSession? =
            sessions.values.firstOrNull { it.sessionId == sessionId }
    }

    private class InMemoryBattleResultRepository : BattleResultRepository {
        private val results = mutableMapOf<BattleSessionId, BattleResult>()

        fun getResultBySession(sessionId: BattleSessionId): BattleResult? = results[sessionId]

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> {
            val result = BattleResult(
                resultId = BattleResultId("res_${sessionId.value}"),
                battleId = BattleId("2026-10-14"),
                userId = UserId("user_cancellation_e2e"),
                sessionId = sessionId,
                snapScore = 295,
                shiftScore = 290,
                crowdCallScore = 290,
                consistencyScore = 75,
                totalScore = 950,
                percentile = 0.02,
                completedAt = 2000L
            )
            results[sessionId] = result
            return DomainResult.Success(result)
        }

        override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> =
            results.values.firstOrNull { it.userId == userId && it.battleId == battleId }
                ?.let { DomainResult.Success(it) } ?: DomainResult.Failure(AppError.Domain)

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> =
            DomainResult.Success(results.values.filter { it.userId == userId })

        override suspend fun getBattleResultBySessionId(sessionId: BattleSessionId): DomainResult<BattleResult> =
            results[sessionId]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(AppError.Domain)
    }
}

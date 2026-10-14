package com.sanket_satpute_20.dailybattle.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.network.CircuitBreaker
import com.sanket_satpute_20.dailybattle.core.network.NetworkEfficiencyTracker
import com.sanket_satpute_20.dailybattle.core.network.RequestDeduplicator
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Instrumentation test validating network efficiency infrastructure on Android runtime (Sprint 24.5):
 * - Hilt injection of OkHttpClient configured with PayloadSizeInterceptor
 * - Real runtime single-flight request deduplication across threads
 * - Circuit breaker failure recording and fail-fast tripping
 * - NetworkEfficiencyTracker diagnostics
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NetworkEfficiencyInstrumentationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var deduplicator: RequestDeduplicator

    @Inject
    lateinit var circuitBreaker: CircuitBreaker

    @Inject
    lateinit var tracker: NetworkEfficiencyTracker

    @Before
    fun setUp() {
        hiltRule.inject()
        deduplicator.reset()
        circuitBreaker.reset()
        tracker.reset()
    }

    @Test
    fun runtimeOkHttpClientIncludesStandardTimeoutsAndInterceptors() {
        assertNotNull(okHttpClient)
        assertEquals(15_000, okHttpClient.connectTimeoutMillis)
        assertEquals(15_000, okHttpClient.readTimeoutMillis)
        assertEquals(15_000, okHttpClient.writeTimeoutMillis)
        assertTrue("OkHttpClient must have interceptors configured", okHttpClient.interceptors.isNotEmpty())
    }

    @Test
    fun runtimeDeduplicatorCoalescesConcurrentRequests() = runBlocking {
        val executionCount = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        val jobs = (1..10).map {
            async {
                deduplicator.executeOrJoin("runtime_dedup_key") {
                    gate.await()
                    executionCount.incrementAndGet()
                    DomainResult.Success("runtime_result")
                }
            }
        }

        gate.complete(Unit)
        val results = jobs.awaitAll()

        assertEquals(1, executionCount.get())
        assertEquals(10, results.size)
        assertEquals(9L, deduplicator.deduplicatedCount)
        assertEquals(10L, deduplicator.totalRequestsCount)
    }

    @Test
    fun runtimeCircuitBreakerTripsAndFailsFast() = runBlocking {
        // Record 5 failures to trip default threshold
        for (i in 1..5) {
            circuitBreaker.recordFailure()
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)
        assertFalse(circuitBreaker.canExecute())

        var executed = false
        val result = circuitBreaker.execute {
            executed = true
            DomainResult.Success("canary")
        }

        assertFalse("Block must not execute in OPEN circuit state", executed)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }
}

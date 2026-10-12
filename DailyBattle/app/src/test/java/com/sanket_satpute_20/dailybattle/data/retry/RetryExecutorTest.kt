package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RetryExecutorTest {

    private val recordedDelays = mutableListOf<Long>()
    private val delayProvider: suspend (Long) -> Unit = { delayMs ->
        recordedDelays.add(delayMs)
    }

    private val defaultPolicy = DefaultRetryPolicy(
        maxRetries = 3,
        initialDelayMillis = 1000L,
        maxDelayMillis = 10_000L,
        backoffMultiplier = 2.0,
    )

    private val executor = DefaultRetryExecutor(
        defaultPolicy = defaultPolicy,
        delayProvider = delayProvider,
    )

    @Test
    fun executeWithRetrySucceedsOnFirstAttemptWithoutDelay() = runTest {
        var attemptsCalled = 0
        val result = executor.executeWithRetry(defaultPolicy) { attempt ->
            attemptsCalled++
            DomainResult.Success("success_val")
        }

        assertTrue(result is DomainResult.Success)
        assertEquals("success_val", (result as DomainResult.Success).value)
        assertEquals(1, attemptsCalled)
        assertTrue(recordedDelays.isEmpty())
    }

    @Test
    fun executeWithRetryRecoversAfterTransientFailure() = runTest {
        var attemptsCalled = 0
        val result = executor.executeWithRetry(defaultPolicy) { attempt ->
            attemptsCalled++
            if (attempt == 0) {
                DomainResult.Failure(AppError.Network)
            } else {
                DomainResult.Success("recovered")
            }
        }

        assertTrue(result is DomainResult.Success)
        assertEquals("recovered", (result as DomainResult.Success).value)
        assertEquals(2, attemptsCalled)
        assertEquals(listOf(1000L), recordedDelays)
    }

    @Test
    fun executeWithRetryExhaustsMaxRetriesAndReturnsFailureWithoutInfiniteLoop() = runTest {
        var attemptsCalled = 0
        val result = executor.executeWithRetry(defaultPolicy) { attempt ->
            attemptsCalled++
            DomainResult.Failure(AppError.Network)
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
        // Attempt 0 (initial) + 3 retries = 4 total attempts
        assertEquals(4, attemptsCalled)
        assertEquals(listOf(1000L, 2000L, 4000L), recordedDelays)
    }

    @Test
    fun executeWithRetryFailsImmediatelyOnNonRetryableError() = runTest {
        var attemptsCalled = 0
        val result = executor.executeWithRetry(defaultPolicy) { attempt ->
            attemptsCalled++
            DomainResult.Failure(AppError.Validation)
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
        // Must fail immediately on attempt 0 without retrying (§133)
        assertEquals(1, attemptsCalled)
        assertTrue(recordedDelays.isEmpty())
    }

    @Test
    fun executeWithRetryFailsImmediatelyOnBattleStateError() = runTest {
        var attemptsCalled = 0
        val result = executor.executeWithRetry(defaultPolicy) { attempt ->
            attemptsCalled++
            DomainResult.Failure(AppError.BattleState)
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.BattleState, (result as DomainResult.Failure).error)
        assertEquals(1, attemptsCalled)
        assertTrue(recordedDelays.isEmpty())
    }

    @Test
    fun executeWithRetryPropagatesCoroutineCancellationDuringDelay() = runTest {
        val cancellingDelayProvider: suspend (Long) -> Unit = {
            throw CancellationException("Operation cancelled by caller")
        }
        val cancellingExecutor = DefaultRetryExecutor(
            defaultPolicy = defaultPolicy,
            delayProvider = cancellingDelayProvider,
        )

        try {
            cancellingExecutor.executeWithRetry(defaultPolicy) { _ ->
                DomainResult.Failure(AppError.Network)
            }
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertEquals("Operation cancelled by caller", e.message)
        }
    }

    @Test
    fun executeWithRetryAppliesCustomPolicyDelays() = runTest {
        val customPolicy = DefaultRetryPolicy(
            maxRetries = 2,
            initialDelayMillis = 200L,
            maxDelayMillis = 1000L,
            backoffMultiplier = 2.0,
        )

        var attemptsCalled = 0
        val result = executor.executeWithRetry(customPolicy) { _ ->
            attemptsCalled++
            DomainResult.Failure(AppError.Timeout)
        }

        assertTrue(result is DomainResult.Failure)
        // Initial + 2 retries = 3 attempts
        assertEquals(3, attemptsCalled)
        assertEquals(listOf(200L, 400L), recordedDelays)
    }
}

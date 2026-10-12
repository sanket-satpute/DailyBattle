package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.core.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {

    private val policy = DefaultRetryPolicy()

    @Test
    fun isRetryableReturnsTrueForTransientErrors() {
        assertTrue(policy.isRetryable(AppError.Network))
        assertTrue(policy.isRetryable(AppError.Offline))
        assertTrue(policy.isRetryable(AppError.Timeout))
        assertTrue(policy.isRetryable(AppError.Server))
        assertTrue(policy.isRetryable(AppError.Synchronization))
    }

    @Test
    fun isRetryableReturnsFalseForNonRetryableErrors() {
        assertFalse(policy.isRetryable(AppError.Authentication))
        assertFalse(policy.isRetryable(AppError.Authorization))
        assertFalse(policy.isRetryable(AppError.Validation))
        assertFalse(policy.isRetryable(AppError.Persistence))
        assertFalse(policy.isRetryable(AppError.Domain))
        assertFalse(policy.isRetryable(AppError.BattleState))
        assertFalse(policy.isRetryable(AppError.Unknown))
    }

    @Test
    fun canRetryRespectsMaxRetriesLimit() {
        assertTrue(policy.canRetry(0))
        assertTrue(policy.canRetry(1))
        assertTrue(policy.canRetry(2))
        assertFalse(policy.canRetry(3))
        assertFalse(policy.canRetry(4))
    }

    @Test
    fun canRetryReturnsFalseForNonRetryableErrorEvenIfAttemptsRemain() {
        // Non-retryable errors must fail immediately on attempt 0 without looping (§133)
        assertFalse(policy.canRetry(0, AppError.Validation))
        assertFalse(policy.canRetry(1, AppError.Authentication))
        assertFalse(policy.canRetry(2, AppError.Domain))
        assertFalse(policy.canRetry(0, AppError.BattleState))
    }

    @Test
    fun canRetryReturnsTrueForRetryableErrorWhenAttemptsRemain() {
        assertTrue(policy.canRetry(0, AppError.Network))
        assertTrue(policy.canRetry(1, AppError.Timeout))
        assertTrue(policy.canRetry(2, AppError.Server))
        assertTrue(policy.canRetry(1, AppError.Synchronization))
    }

    @Test
    fun canRetryReturnsFalseWhenAttemptsExhaustedEvenForRetryableError() {
        // Controlled retry stops when maxRetries is reached (§133)
        assertFalse(policy.canRetry(3, AppError.Network))
        assertFalse(policy.canRetry(4, AppError.Timeout))
        assertFalse(policy.canRetry(5, AppError.Server))
    }

    @Test
    fun calculateDelayMillisComputesExponentialBackoff() {
        assertEquals(0L, policy.calculateDelayMillis(0))
        assertEquals(1000L, policy.calculateDelayMillis(1)) // 1000 * 2^0
        assertEquals(2000L, policy.calculateDelayMillis(2)) // 1000 * 2^1
        assertEquals(4000L, policy.calculateDelayMillis(3)) // 1000 * 2^2
        assertEquals(8000L, policy.calculateDelayMillis(4)) // 1000 * 2^3
        assertEquals(10000L, policy.calculateDelayMillis(5)) // capped at maxDelay (10_000)
        assertEquals(10000L, policy.calculateDelayMillis(10)) // capped
    }

    @Test
    fun customPolicyParametersAreRespected() {
        val customPolicy = DefaultRetryPolicy(
            maxRetries = 5,
            initialDelayMillis = 500L,
            maxDelayMillis = 8000L,
            backoffMultiplier = 3.0,
        )

        assertEquals(5, customPolicy.maxRetries)
        assertTrue(customPolicy.canRetry(4, AppError.Network))
        assertFalse(customPolicy.canRetry(5, AppError.Network))

        assertEquals(0L, customPolicy.calculateDelayMillis(0))
        assertEquals(500L, customPolicy.calculateDelayMillis(1)) // 500 * 3^0
        assertEquals(1500L, customPolicy.calculateDelayMillis(2)) // 500 * 3^1
        assertEquals(4500L, customPolicy.calculateDelayMillis(3)) // 500 * 3^2
        assertEquals(8000L, customPolicy.calculateDelayMillis(4)) // 500 * 3^3 = 13500 -> capped at 8000
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeMaxRetriesThrowsException() {
        DefaultRetryPolicy(maxRetries = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun maxDelayLessThanInitialDelayThrowsException() {
        DefaultRetryPolicy(initialDelayMillis = 5000L, maxDelayMillis = 2000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun backoffMultiplierLessThanOneThrowsException() {
        DefaultRetryPolicy(backoffMultiplier = 0.5)
    }
}

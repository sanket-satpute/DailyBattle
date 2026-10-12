package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.core.error.AppError
import kotlin.math.min
import kotlin.math.pow

/**
 * Defines policy rules governing automated retry behavior, retryable error classifications,
 * and exponential backoff calculations.
 *
 * Conforms to:
 * - 15_ANTIGRAVITY_RULES.md Rule 133 (No Infinite Retry Rule: retries must be controlled)
 * - 07_TECHNICAL_ARCHITECTURE.md §24 (Retries MUST NOT be applied blindly)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §4, §6 (Recoverable vs unrecoverable error categories)
 */
interface RetryPolicy {
    val maxRetries: Int
    val initialDelayMillis: Long
    val maxDelayMillis: Long
    val backoffMultiplier: Double

    /**
     * Determines whether an error is transient/recoverable via retry.
     */
    fun isRetryable(error: AppError): Boolean

    /**
     * Determines if a subsequent retry attempt is permissible given the current attempt
     * count and the preceding failure error (if available).
     */
    fun canRetry(attempt: Int, error: AppError? = null): Boolean

    /**
     * Calculates the exponential backoff delay in milliseconds for the given attempt.
     */
    fun calculateDelayMillis(attempt: Int): Long
}

/**
 * Production retry policy:
 * - Capped at [maxRetries] attempts (default 3) to prevent infinite loops (§133).
 * - Exponential backoff with configurable initial delay and ceiling.
 * - Transient networking/server/timeout errors are retryable; validation/auth/domain errors fail immediately.
 */
class DefaultRetryPolicy(
    override val maxRetries: Int = 3,
    override val initialDelayMillis: Long = 1000L,
    override val maxDelayMillis: Long = 10_000L,
    override val backoffMultiplier: Double = 2.0,
) : RetryPolicy {

    init {
        require(maxRetries >= 0) { "maxRetries cannot be negative" }
        require(initialDelayMillis >= 0) { "initialDelayMillis cannot be negative" }
        require(maxDelayMillis >= initialDelayMillis) { "maxDelayMillis cannot be less than initialDelayMillis" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be >= 1.0" }
    }

    override fun isRetryable(error: AppError): Boolean = when (error) {
        AppError.Network,
        AppError.Offline,
        AppError.Timeout,
        AppError.Server,
        AppError.Synchronization -> true

        AppError.Authentication,
        AppError.Authorization,
        AppError.Validation,
        AppError.Persistence,
        AppError.Domain,
        AppError.BattleState,
        AppError.Unknown -> false
    }

    override fun canRetry(attempt: Int, error: AppError?): Boolean {
        if (attempt >= maxRetries) return false
        if (error != null && !isRetryable(error)) return false
        return true
    }

    override fun calculateDelayMillis(attempt: Int): Long {
        if (attempt <= 0) return 0L
        val multiplier = backoffMultiplier.pow((attempt - 1).toDouble())
        val calculated = (initialDelayMillis * multiplier).toLong()
        return min(calculated, maxDelayMillis)
    }
}

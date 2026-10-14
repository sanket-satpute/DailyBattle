package com.sanket_satpute_20.dailybattle.core.network

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.retry.RetryPolicy
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Exponential backoff retry policy with randomized full jitter to prevent retry storms.
 *
 * Implements Sprint 24.5 (retry storms check) per `docs/ROADMAP.md` and `docs/15_ANTIGRAVITY_RULES.md` Rule 42 & 133:
 * - Exponential backoff: delay = initialDelay * backoffMultiplier^(attempt-1)
 * - Full jitter: scales delay by a randomized factor in [0.5, 1.5] to desynchronize concurrent clients
 * - Caps maximum delay at [maxDelayMillis]
 * - Caps maximum attempts at [maxRetries]
 */
class JitteredRetryPolicy(
    override val maxRetries: Int = 3,
    override val initialDelayMillis: Long = 1000L,
    override val maxDelayMillis: Long = 10_000L,
    override val backoffMultiplier: Double = 2.0,
    private val randomProvider: (Double, Double) -> Double = { from, to -> Random.nextDouble(from, to) },
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
        val exponential = initialDelayMillis * backoffMultiplier.pow((attempt - 1).toDouble())
        val boundedExponential = min(exponential, maxDelayMillis.toDouble())
        val jitterFactor = randomProvider(0.5, 1.5)
        val jittered = (boundedExponential * jitterFactor).toLong()
        return min(jittered.coerceAtLeast(0L), maxDelayMillis)
    }
}

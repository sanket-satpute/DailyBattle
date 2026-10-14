package com.sanket_satpute_20.dailybattle.core.network

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * Coordinates and governs polling operations to eliminate unnecessary network polling.
 *
 * Implements Sprint 24.5 (unnecessary polling check) per `docs/ROADMAP.md` and `docs/07_TECHNICAL_ARCHITECTURE.md` §48:
 * - Automatically pauses or suppresses polling when the application is backgrounded.
 * - Enforces progressive backoff intervals between polls instead of tight continuous polling loops.
 * - Enforces strict upper bounds on polling attempts and duration to prevent battery drain.
 */
@Singleton
class PollingController(
    private val lifecycleManager: AppLifecycleManager,
    private val delayProvider: suspend (Long) -> Unit = { delay(it) },
) {
    @Inject
    constructor(lifecycleManager: AppLifecycleManager) : this(lifecycleManager, { delay(it) })
    private val _totalPollCycles = AtomicInteger(0)
    private val _suppressedBackgroundPolls = AtomicInteger(0)

    val totalPollCycles: Int
        get() = _totalPollCycles.get()

    val suppressedBackgroundPolls: Int
        get() = _suppressedBackgroundPolls.get()

    /**
     * Executes [action] up to [maxAttempts] times until [predicate] returns true or [maxAttempts] is reached.
     * Pauses during background states, resuming when the app returns to foreground.
     */
    suspend fun <T> pollUntil(
        maxAttempts: Int = 10,
        initialIntervalMs: Long = 1000L,
        maxIntervalMs: Long = 10_000L,
        backoffMultiplier: Double = 1.5,
        predicate: (T) -> Boolean,
        action: suspend () -> DomainResult<T>,
    ): DomainResult<T> {
        require(maxAttempts > 0) { "maxAttempts must be > 0" }
        require(initialIntervalMs >= 0) { "initialIntervalMs must be >= 0" }

        var attempt = 0
        while (attempt < maxAttempts) {
            // Check app lifecycle: if backgrounded, wait until foregrounded before polling
            if (lifecycleManager.state.value == AppLifecycleState.BACKGROUND) {
                _suppressedBackgroundPolls.incrementAndGet()
                // Suspend until foreground returns
                lifecycleManager.state.first { it == AppLifecycleState.FOREGROUND }
            }

            _totalPollCycles.incrementAndGet()
            attempt++

            val result = try {
                action()
            } catch (e: CancellationException) {
                throw e
            }

            when (result) {
                is DomainResult.Success -> {
                    if (predicate(result.value)) {
                        return result
                    }
                }
                is DomainResult.Failure -> {
                    // Fatal errors stop polling
                    if (result.error is AppError.Authentication ||
                        result.error is AppError.Authorization ||
                        result.error is AppError.Validation ||
                        result.error is AppError.Domain
                    ) {
                        return result
                    }
                }
            }

            if (attempt < maxAttempts) {
                val delayTime = (initialIntervalMs * backoffMultiplier.pow((attempt - 1).toDouble()))
                    .toLong()
                    .coerceAtMost(maxIntervalMs)
                try {
                    delayProvider(delayTime)
                } catch (e: CancellationException) {
                    throw e
                }
            }
        }

        return DomainResult.Failure(AppError.Timeout)
    }

    fun reset() {
        _totalPollCycles.set(0)
        _suppressedBackgroundPolls.set(0)
    }
}

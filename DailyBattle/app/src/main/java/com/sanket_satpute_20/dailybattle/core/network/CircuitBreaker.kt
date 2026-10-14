package com.sanket_satpute_20.dailybattle.core.network

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Circuit breaker state machine to prevent network retry storms against failing backends.
 *
 * Implements Sprint 24.5 (retry storms check) per `docs/ROADMAP.md` and `docs/15_ANTIGRAVITY_RULES.md` Rule 42:
 * - CLOSED: Normal operation; requests pass through.
 * - OPEN: Failure threshold exceeded; requests fail fast immediately without dispatching over the wire.
 * - HALF_OPEN: Probe state allowing a single canary request to test backend recovery.
 */
@Singleton
class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val resetTimeoutMs: Long = 15_000L,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
) {
    @Inject
    constructor() : this(5, 15_000L, { System.currentTimeMillis() })
    enum class State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private val currentState = AtomicReference(State.CLOSED)
    private val consecutiveFailures = AtomicInteger(0)
    private val lastStateChangeTime = AtomicLong(timeProvider())
    private val tripCounter = AtomicInteger(0)

    val state: State
        get() {
            val now = timeProvider()
            val state = currentState.get()
            if (state == State.OPEN && now - lastStateChangeTime.get() >= resetTimeoutMs) {
                if (currentState.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    lastStateChangeTime.set(now)
                    return State.HALF_OPEN
                }
            }
            return currentState.get()
        }

    val failureCount: Int
        get() = consecutiveFailures.get()

    val tripCount: Int
        get() = tripCounter.get()

    fun canExecute(): Boolean {
        return state != State.OPEN
    }

    fun recordSuccess() {
        consecutiveFailures.set(0)
        currentState.set(State.CLOSED)
        lastStateChangeTime.set(timeProvider())
    }

    fun recordFailure() {
        val failures = consecutiveFailures.incrementAndGet()
        val now = timeProvider()
        if (failures >= failureThreshold) {
            if (currentState.compareAndSet(State.CLOSED, State.OPEN) ||
                currentState.compareAndSet(State.HALF_OPEN, State.OPEN)
            ) {
                tripCounter.incrementAndGet()
                lastStateChangeTime.set(now)
            }
        }
    }

    suspend fun <T> execute(block: suspend () -> DomainResult<T>): DomainResult<T> {
        if (!canExecute()) {
            return DomainResult.Failure(AppError.Network)
        }

        val result = block()
        when (result) {
            is DomainResult.Success -> recordSuccess()
            is DomainResult.Failure -> {
                if (result.error is AppError.Network ||
                    result.error is AppError.Timeout ||
                    result.error is AppError.Server
                ) {
                    recordFailure()
                }
            }
        }
        return result
    }

    fun reset() {
        consecutiveFailures.set(0)
        currentState.set(State.CLOSED)
        lastStateChangeTime.set(timeProvider())
        tripCounter.set(0)
    }
}

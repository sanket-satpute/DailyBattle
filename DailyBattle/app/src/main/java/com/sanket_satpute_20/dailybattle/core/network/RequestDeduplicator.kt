package com.sanket_satpute_20.dailybattle.core.network

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coalesces concurrent in-flight requests for the same resource key into a single remote execution.
 *
 * Implements Sprint 24.5 (duplicate requests check) per `docs/ROADMAP.md` and `docs/08_DATA_AND_API.md` §78:
 * - When multiple components or rapid user taps request the same key concurrently, only one request executes.
 * - All other callers join and await the active in-flight Deferred.
 * - Prevents duplicate network bandwidth usage and redundant backend load.
 */
@Singleton
class RequestDeduplicator @Inject constructor() {

    private val inFlight = ConcurrentHashMap<String, Deferred<Any>>()
    private val _deduplicatedCount = AtomicLong(0L)
    private val _totalRequestsCount = AtomicLong(0L)

    val deduplicatedCount: Long
        get() = _deduplicatedCount.get()

    val totalRequestsCount: Long
        get() = _totalRequestsCount.get()

    val inFlightCount: Int
        get() = inFlight.size

    /**
     * Executes [block] for [key], or joins an existing in-flight execution if one is already active.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> executeOrJoin(
        key: String,
        block: suspend () -> DomainResult<T>,
    ): DomainResult<T> {
        _totalRequestsCount.incrementAndGet()

        // Fast-path: check if already in-flight
        val existing = inFlight[key]
        if (existing != null) {
            _deduplicatedCount.incrementAndGet()
            return existing.await() as DomainResult<T>
        }

        return coroutineScope {
            val deferred = async {
                try {
                    block()
                } finally {
                    inFlight.remove(key)
                }
            }

            val previous = inFlight.putIfAbsent(key, deferred as Deferred<Any>)
            if (previous != null) {
                // Another thread raced and installed its deferred first; cancel ours and join previous
                deferred.cancel()
                _deduplicatedCount.incrementAndGet()
                previous.await() as DomainResult<T>
            } else {
                deferred.await() as DomainResult<T>
            }
        }
    }

    fun reset() {
        inFlight.clear()
        _deduplicatedCount.set(0L)
        _totalRequestsCount.set(0L)
    }
}

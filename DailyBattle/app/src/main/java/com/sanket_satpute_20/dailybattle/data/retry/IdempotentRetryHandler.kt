package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deduplicates concurrent and repeated operations sharing the same idempotency key.
 *
 * Conforms to:
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (Retry must be safe; prevent duplicate requests, sessions, submissions, friend actions)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §14, §22 (Duplicate Start & Challenge Submission protection)
 */
interface IdempotentRetryHandler {
    suspend fun <T> executeIdempotent(
        idempotencyKey: String,
        block: suspend () -> DomainResult<T>,
    ): DomainResult<T>

    fun isInFlight(idempotencyKey: String): Boolean
}

@Singleton
class DefaultIdempotentRetryHandler(
    private val scope: CoroutineScope,
) : IdempotentRetryHandler {

    @Inject
    constructor() : this(CoroutineScope(SupervisorJob() + Dispatchers.Default))

    private val activeOperations = ConcurrentHashMap<String, Deferred<DomainResult<*>>>()
    private val mutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> executeIdempotent(
        idempotencyKey: String,
        block: suspend () -> DomainResult<T>,
    ): DomainResult<T> {
        val deferred = mutex.withLock {
            val existing = activeOperations[idempotencyKey]
            if (existing != null) {
                existing
            } else {
                val newDeferred = scope.async {
                    try {
                        block()
                    } finally {
                        activeOperations.remove(idempotencyKey)
                    }
                }
                activeOperations[idempotencyKey] = newDeferred
                newDeferred
            }
        }

        return try {
            deferred.await() as DomainResult<T>
        } catch (e: CancellationException) {
            throw e
        }
    }

    override fun isInFlight(idempotencyKey: String): Boolean {
        return activeOperations.containsKey(idempotencyKey)
    }
}

package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Execution coordinator that executes an operation and applies controlled exponential backoff retries.
 *
 * Conforms to:
 * - 15_ANTIGRAVITY_RULES.md Rule 133 (No Infinite Retry Rule)
 * - 07_TECHNICAL_ARCHITECTURE.md §24 (Retries MUST NOT be applied blindly)
 */
interface RetryExecutor {
    suspend fun <T> executeWithRetry(
        policy: RetryPolicy = DefaultRetryPolicy(),
        block: suspend (attempt: Int) -> DomainResult<T>,
    ): DomainResult<T>
}

@Singleton
class DefaultRetryExecutor(
    private val defaultPolicy: RetryPolicy,
    private val delayProvider: suspend (Long) -> Unit,
) : RetryExecutor {

    @Inject
    constructor(defaultPolicy: RetryPolicy) : this(defaultPolicy, { delay(it) })

    override suspend fun <T> executeWithRetry(
        policy: RetryPolicy,
        block: suspend (attempt: Int) -> DomainResult<T>,
    ): DomainResult<T> {
        val effectivePolicy = policy
        var retryCount = 0
        while (true) {
            val result = try {
                block(retryCount)
            } catch (e: CancellationException) {
                throw e
            }

            when (result) {
                is DomainResult.Success -> return result
                is DomainResult.Failure -> {
                    val error = result.error
                    if (!effectivePolicy.canRetry(retryCount, error)) {
                        return result
                    }
                    retryCount++
                    val delayMs = effectivePolicy.calculateDelayMillis(retryCount)
                    if (delayMs > 0) {
                        try {
                            delayProvider(delayMs)
                        } catch (e: CancellationException) {
                            throw e
                        }
                    }
                }
            }
        }
    }
}

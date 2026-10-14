package com.sanket_satpute_20.dailybattle.core.network

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks and audits network operations across the application for efficiency governance.
 *
 * Implements Sprint 24.5 per `docs/ROADMAP.md` (lines 1865–1874):
 * - Duplicate request prevention tracking
 * - Request cancellation accounting
 * - Retry monitoring
 * - Payload size and bytes transferred tracking
 */
@Singleton
class NetworkEfficiencyTracker @Inject constructor(
    private val deduplicator: RequestDeduplicator,
    private val circuitBreaker: CircuitBreaker,
) {
    companion object {
        const val MAX_RECOMMENDED_PAYLOAD_BYTES = 64 * 1024L // 64 KB per JSON payload
    }

    private val _totalRequests = AtomicLong(0L)
    private val _cancelledRequests = AtomicLong(0L)
    private val _retriedRequests = AtomicLong(0L)
    private val _oversizedPayloads = AtomicLong(0L)
    private val _totalBytesTransferred = AtomicLong(0L)

    fun recordRequest(bytes: Long = 0L) {
        _totalRequests.incrementAndGet()
        if (bytes > 0) {
            _totalBytesTransferred.addAndGet(bytes)
            if (bytes > MAX_RECOMMENDED_PAYLOAD_BYTES) {
                _oversizedPayloads.incrementAndGet()
            }
        }
    }

    fun recordCancellation() {
        _cancelledRequests.incrementAndGet()
    }

    fun recordRetry() {
        _retriedRequests.incrementAndGet()
    }

    fun recordPayloadSize(bytes: Long) {
        if (bytes > 0) {
            _totalBytesTransferred.addAndGet(bytes)
            if (bytes > MAX_RECOMMENDED_PAYLOAD_BYTES) {
                _oversizedPayloads.incrementAndGet()
            }
        }
    }

    fun getReport(): NetworkEfficiencyReport {
        val totalReq = _totalRequests.get() + deduplicator.totalRequestsCount
        val deduped = deduplicator.deduplicatedCount
        val cancelled = _cancelledRequests.get()
        val retried = _retriedRequests.get()
        val trips = circuitBreaker.tripCount
        val oversized = _oversizedPayloads.get()
        val bytes = _totalBytesTransferred.get()

        val isOptimal = oversized == 0L && circuitBreaker.state != CircuitBreaker.State.OPEN

        return NetworkEfficiencyReport(
            totalRequests = totalReq,
            deduplicatedRequests = deduped,
            cancelledRequests = cancelled,
            retriedRequests = retried,
            circuitBreakerTrippedCount = trips,
            oversizedPayloadsCount = oversized,
            totalBytesTransferred = bytes,
            isEfficiencyOptimal = isOptimal,
        )
    }

    fun reset() {
        _totalRequests.set(0L)
        _cancelledRequests.set(0L)
        _retriedRequests.set(0L)
        _oversizedPayloads.set(0L)
        _totalBytesTransferred.set(0L)
        deduplicator.reset()
        circuitBreaker.reset()
    }
}

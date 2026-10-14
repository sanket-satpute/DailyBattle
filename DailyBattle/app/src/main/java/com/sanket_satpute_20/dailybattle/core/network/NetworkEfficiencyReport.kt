package com.sanket_satpute_20.dailybattle.core.network

/**
 * Diagnostic metrics snapshot representing application network efficiency.
 *
 * Implements Sprint 24.5 per `docs/ROADMAP.md` (lines 1865–1874):
 * - Duplicate requests tracking and prevention
 * - Unnecessary polling suppression
 * - Request cancellation accounting
 * - Retry storm protection via circuit breaker
 * - Oversized payload detection
 */
data class NetworkEfficiencyReport(
    val totalRequests: Long,
    val deduplicatedRequests: Long,
    val cancelledRequests: Long,
    val retriedRequests: Long,
    val circuitBreakerTrippedCount: Int,
    val oversizedPayloadsCount: Long,
    val totalBytesTransferred: Long,
    val isEfficiencyOptimal: Boolean,
)

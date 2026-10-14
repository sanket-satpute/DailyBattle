package com.sanket_satpute_20.dailybattle.core.memory

/**
 * Diagnostic snapshot of current application memory state.
 *
 * Implements Sprint 24.4 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §57:
 * - Current heap utilization
 * - Ratio against max heap limit
 * - Retained object/screen leak tracking
 * - Overall memory health assessment
 */
data class MemoryReport(
    val usedHeapBytes: Long,
    val maxHeapBytes: Long,
    val totalHeapBytes: Long,
    val heapUtilizationRatio: Double,
    val activeRetainedObjectsCount: Int,
    val unreleasedObjectKeys: List<String>,
    val isMemoryHealthy: Boolean,
)

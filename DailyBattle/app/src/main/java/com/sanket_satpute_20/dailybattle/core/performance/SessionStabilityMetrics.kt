package com.sanket_satpute_20.dailybattle.core.performance

/**
 * Immutable snapshot of application stability metrics over an extended session.
 *
 * Tracks:
 * - Total session runtime duration
 * - Repeated battle cycles
 * - Repeated practice cycles
 * - Repeated navigation transitions
 * - Background/foreground lifecycle cycles
 * - Repeated results views
 * - Heap memory bounds and heap creep
 * - Unhandled exceptions / crash resilience
 *
 * Implements Sprint 24.3 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §57, §58.
 */
data class SessionStabilityMetrics(
    val sessionStartTimestampMs: Long = 0L,
    val sessionDurationMs: Long = 0L,
    val battleCyclesCompleted: Long = 0L,
    val practiceCyclesCompleted: Long = 0L,
    val navigationTransitions: Long = 0L,
    val backgroundForegroundCycles: Long = 0L,
    val resultsViewCount: Long = 0L,
    val startingHeapBytes: Long = 0L,
    val peakHeapBytes: Long = 0L,
    val currentHeapBytes: Long = 0L,
    val unhandledExceptionsCount: Long = 0L,
) {
    /** Total heap growth since session start, non-negative. */
    val heapCreepBytes: Long
        get() = (currentHeapBytes - startingHeapBytes).coerceAtLeast(0L)

    /**
     * Verifies that heap memory growth is within safe operational thresholds.
     * Default threshold is 50MB of creep over prolonged multi-hour sessions.
     */
    fun isMemoryHealthy(maxAllowedCreepBytes: Long = 50 * 1024 * 1024L): Boolean {
        return heapCreepBytes < maxAllowedCreepBytes
    }

    /**
     * Verifies overall session stability:
     * - Zero unhandled exceptions
     * - Memory growth within bounds
     */
    fun isSessionStable(): Boolean {
        return unhandledExceptionsCount == 0L && isMemoryHealthy()
    }
}

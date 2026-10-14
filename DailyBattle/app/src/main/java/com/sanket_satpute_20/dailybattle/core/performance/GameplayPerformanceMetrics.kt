package com.sanket_satpute_20.dailybattle.core.performance

import kotlin.math.max

/**
 * Immutable snapshot of gameplay performance measurements covering:
 * 1. Frame Rendering (total frames, jank frames, frozen frames, avg/max frame time, jank %)
 * 2. Input Latency (tap-to-dispatch latency, min/max/average latency)
 * 3. Animation Performance (animation durations, dropped frames during animation)
 * 4. Timer Accuracy (authoritative monotonic time vs observed timer drift)
 * 5. Memory during Gameplay (start heap, peak heap, end heap, heap delta)
 *
 * Implements Sprint 24.2 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §56.
 */
data class GameplayPerformanceMetrics(
    // 1. Frame Rendering
    val totalFrames: Long = 0L,
    val jankFrames: Long = 0L,
    val frozenFrames: Long = 0L,
    val totalFrameTimeMs: Double = 0.0,
    val maxFrameTimeMs: Double = 0.0,

    // 2. Input Latency
    val totalInputsProcessed: Long = 0L,
    val totalInputLatencyMs: Long = 0L,
    val minInputLatencyMs: Long? = null,
    val maxInputLatencyMs: Long? = null,

    // 3. Animation Performance
    val totalAnimationsTracked: Long = 0L,
    val droppedFramesDuringAnimation: Long = 0L,
    val totalAnimationDurationDeltaMs: Long = 0L,

    // 4. Timer Accuracy
    val totalTimerChecks: Long = 0L,
    val totalTimerDriftMs: Long = 0L,
    val maxTimerDriftMs: Long = 0L,

    // 5. Memory
    val startHeapBytes: Long = 0L,
    val peakHeapBytes: Long = 0L,
    val endHeapBytes: Long? = null,
) {
    val averageFrameTimeMs: Double
        get() = if (totalFrames > 0L) totalFrameTimeMs / totalFrames else 0.0

    val jankPercentage: Double
        get() = if (totalFrames > 0L) (jankFrames.toDouble() / totalFrames) * 100.0 else 0.0

    val averageInputLatencyMs: Double
        get() = if (totalInputsProcessed > 0L) totalInputLatencyMs.toDouble() / totalInputsProcessed else 0.0

    val averageTimerDriftMs: Double
        get() = if (totalTimerChecks > 0L) totalTimerDriftMs.toDouble() / totalTimerChecks else 0.0

    val heapDeltaBytes: Long
        get() = max(0L, (endHeapBytes ?: peakHeapBytes) - startHeapBytes)

    companion object {
        /** Standard 60fps frame target: 16.667ms per frame */
        const val TARGET_FRAME_DURATION_MS: Double = 16.667
        /** Frames taking longer than 16.667ms are considered dropped / jank */
        const val JANK_THRESHOLD_MS: Double = 16.667
        /** Frames taking longer than 700ms are considered frozen */
        const val FROZEN_THRESHOLD_MS: Double = 700.0

        /** Target maximum jank frame percentage during gameplay (<= 5.0%) */
        const val TARGET_MAX_JANK_PERCENTAGE: Double = 5.0
        /** Target maximum input latency (<= 50ms) */
        const val TARGET_MAX_INPUT_LATENCY_MS: Long = 50L
        /** Target maximum timer drift (<= 50ms) */
        const val TARGET_MAX_TIMER_DRIFT_MS: Long = 50L
        /** Target maximum heap delta during a gameplay session (<= 30 MB) */
        const val TARGET_MAX_HEAP_DELTA_BYTES: Long = 30 * 1024 * 1024L
    }

    fun isFrameRenderingSmooth(thresholdJankPercentage: Double = TARGET_MAX_JANK_PERCENTAGE): Boolean =
        jankPercentage <= thresholdJankPercentage

    fun isInputLatencyAcceptable(thresholdMs: Long = TARGET_MAX_INPUT_LATENCY_MS): Boolean =
        (maxInputLatencyMs ?: 0L) <= thresholdMs

    fun isAnimationPerformant(maxDroppedFrames: Long = 5L): Boolean =
        droppedFramesDuringAnimation <= maxDroppedFrames

    fun isTimerAccurate(maxDriftMs: Long = TARGET_MAX_TIMER_DRIFT_MS): Boolean =
        maxTimerDriftMs <= maxDriftMs

    fun isMemoryUsageHealthy(maxHeapDeltaBytes: Long = TARGET_MAX_HEAP_DELTA_BYTES): Boolean =
        heapDeltaBytes <= maxHeapDeltaBytes
}

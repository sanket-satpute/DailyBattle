package com.sanket_satpute_20.dailybattle.core.performance

import kotlinx.coroutines.flow.StateFlow

/**
 * Authoritative contract for measuring gameplay runtime performance:
 * 1. Frame rendering (duration, jank rate, dropped frames)
 * 2. Input latency (touch-to-response duration)
 * 3. Animation performance (animation duration vs target, dropped frames)
 * 4. Timer accuracy (authoritative monotonic time vs observed timer drift)
 * 5. Memory (heap utilization and delta during gameplay)
 *
 * Implements Sprint 24.2 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §56.
 */
interface GameplayPerformanceTracker {

    /** Current observable snapshot of gameplay performance metrics. */
    val metrics: StateFlow<GameplayPerformanceMetrics>

    /** Marks the start of a gameplay performance measuring session. */
    fun startSession(sessionTimestampMs: Long? = null)

    /** Marks the end of a gameplay session and returns the final immutable metrics. */
    fun endSession(sessionTimestampMs: Long? = null): GameplayPerformanceMetrics

    /** Records a single frame duration in milliseconds. */
    fun recordFrame(frameDurationMs: Double)

    /** Records a batch of frame durations in milliseconds. */
    fun recordFrames(durationsMs: List<Double>)

    /** Records an input latency sample in milliseconds. */
    fun recordInputLatency(latencyMs: Long)

    /** Measures and records the execution duration of an input action lambda. */
    fun <T> measureInputLatency(inputAction: () -> T): T

    /** Records animation performance metrics. */
    fun recordAnimationPerformance(
        animationName: String,
        actualDurationMs: Long,
        expectedDurationMs: Long,
        droppedFrames: Long = 0L,
    )

    /** Records a timer check comparing authoritative monotonic time against observed timer display time. */
    fun recordTimerCheck(authoritativeElapsedMs: Long, observedTimerMs: Long)

    /** Records a heap memory sample in bytes (null uses Runtime current heap). */
    fun recordMemorySample(usedHeapBytes: Long? = null)

    /** Resets metrics for testing isolation. */
    fun resetForTesting()
}

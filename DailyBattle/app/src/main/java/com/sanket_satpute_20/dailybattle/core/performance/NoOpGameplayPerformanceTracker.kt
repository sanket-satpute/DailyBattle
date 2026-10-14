package com.sanket_satpute_20.dailybattle.core.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Safe no-op implementation of [GameplayPerformanceTracker] for fallbacks and default constructor arguments.
 */
object NoOpGameplayPerformanceTracker : GameplayPerformanceTracker {

    private val _metrics = MutableStateFlow(GameplayPerformanceMetrics())
    override val metrics: StateFlow<GameplayPerformanceMetrics> = _metrics.asStateFlow()

    override fun startSession(sessionTimestampMs: Long?) {}

    override fun endSession(sessionTimestampMs: Long?): GameplayPerformanceMetrics = _metrics.value

    override fun recordFrame(frameDurationMs: Double) {}

    override fun recordFrames(durationsMs: List<Double>) {}

    override fun recordInputLatency(latencyMs: Long) {}

    override fun <T> measureInputLatency(inputAction: () -> T): T = inputAction()

    override fun recordAnimationPerformance(
        animationName: String,
        actualDurationMs: Long,
        expectedDurationMs: Long,
        droppedFrames: Long,
    ) {}

    override fun recordTimerCheck(authoritativeElapsedMs: Long, observedTimerMs: Long) {}

    override fun recordMemorySample(usedHeapBytes: Long?) {}

    override fun resetForTesting() {
        _metrics.value = GameplayPerformanceMetrics()
    }
}

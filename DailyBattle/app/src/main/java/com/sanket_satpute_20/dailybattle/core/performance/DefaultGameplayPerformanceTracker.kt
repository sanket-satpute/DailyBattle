package com.sanket_satpute_20.dailybattle.core.performance

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.performance.GameplayPerformanceMetrics.Companion.FROZEN_THRESHOLD_MS
import com.sanket_satpute_20.dailybattle.core.performance.GameplayPerformanceMetrics.Companion.JANK_THRESHOLD_MS
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Default singleton implementation of [GameplayPerformanceTracker] that measures:
 * 1. Frame Rendering (60/120fps frame timing, jank frame detection, frozen frames)
 * 2. Input Latency (touch-to-response duration)
 * 3. Animation Performance (animation durations, dropped frames during transitions)
 * 4. Timer Accuracy (authoritative monotonic time vs observed timer drift)
 * 5. Memory during Gameplay (heap utilization, delta, peak memory)
 *
 * Implements Sprint 24.2 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §56.
 */
@Singleton
class DefaultGameplayPerformanceTracker @Inject constructor(
    private val timeProvider: TimeProvider,
    private val analyticsTracker: AnalyticsTracker,
) : GameplayPerformanceTracker {

    private val _metrics = MutableStateFlow(GameplayPerformanceMetrics())
    override val metrics: StateFlow<GameplayPerformanceMetrics> = _metrics.asStateFlow()

    override fun startSession(sessionTimestampMs: Long?) {
        val currentHeap = getCurrentUsedHeapBytes()
        _metrics.update {
            it.copy(
                startHeapBytes = currentHeap,
                peakHeapBytes = currentHeap,
                endHeapBytes = null
            )
        }
    }

    override fun endSession(sessionTimestampMs: Long?): GameplayPerformanceMetrics {
        val currentHeap = getCurrentUsedHeapBytes()
        _metrics.update {
            it.copy(
                endHeapBytes = currentHeap,
                peakHeapBytes = max(it.peakHeapBytes, currentHeap)
            )
        }
        return _metrics.value
    }

    override fun recordFrame(frameDurationMs: Double) {
        val dur = max(0.0, frameDurationMs)
        val isJank = dur > JANK_THRESHOLD_MS
        val isFrozen = dur > FROZEN_THRESHOLD_MS

        _metrics.update { current ->
            current.copy(
                totalFrames = current.totalFrames + 1L,
                totalFrameTimeMs = current.totalFrameTimeMs + dur,
                maxFrameTimeMs = max(current.maxFrameTimeMs, dur),
                jankFrames = if (isJank) current.jankFrames + 1L else current.jankFrames,
                frozenFrames = if (isFrozen) current.frozenFrames + 1L else current.frozenFrames,
            )
        }
    }

    override fun recordFrames(durationsMs: List<Double>) {
        if (durationsMs.isEmpty()) return

        var addedFrames = 0L
        var addedTotalTime = 0.0
        var localMaxTime = 0.0
        var addedJank = 0L
        var addedFrozen = 0L

        for (d in durationsMs) {
            val dur = max(0.0, d)
            addedFrames += 1L
            addedTotalTime += dur
            if (dur > localMaxTime) localMaxTime = dur
            if (dur > JANK_THRESHOLD_MS) addedJank += 1L
            if (dur > FROZEN_THRESHOLD_MS) addedFrozen += 1L
        }

        _metrics.update { current ->
            current.copy(
                totalFrames = current.totalFrames + addedFrames,
                totalFrameTimeMs = current.totalFrameTimeMs + addedTotalTime,
                maxFrameTimeMs = max(current.maxFrameTimeMs, localMaxTime),
                jankFrames = current.jankFrames + addedJank,
                frozenFrames = current.frozenFrames + addedFrozen,
            )
        }
    }

    override fun recordInputLatency(latencyMs: Long) {
        val lat = max(0L, latencyMs)
        _metrics.update { current ->
            current.copy(
                totalInputsProcessed = current.totalInputsProcessed + 1L,
                totalInputLatencyMs = current.totalInputLatencyMs + lat,
                minInputLatencyMs = min(current.minInputLatencyMs ?: lat, lat),
                maxInputLatencyMs = max(current.maxInputLatencyMs ?: lat, lat),
            )
        }
    }

    override fun <T> measureInputLatency(inputAction: () -> T): T {
        val start = timeProvider.elapsedRealtime()
        val result = inputAction()
        val lat = max(0L, timeProvider.elapsedRealtime() - start)
        recordInputLatency(lat)
        return result
    }

    override fun recordAnimationPerformance(
        animationName: String,
        actualDurationMs: Long,
        expectedDurationMs: Long,
        droppedFrames: Long,
    ) {
        val delta = max(0L, abs(actualDurationMs - expectedDurationMs))
        val dropped = max(0L, droppedFrames)

        _metrics.update { current ->
            current.copy(
                totalAnimationsTracked = current.totalAnimationsTracked + 1L,
                droppedFramesDuringAnimation = current.droppedFramesDuringAnimation + dropped,
                totalAnimationDurationDeltaMs = current.totalAnimationDurationDeltaMs + delta,
            )
        }
    }

    override fun recordTimerCheck(authoritativeElapsedMs: Long, observedTimerMs: Long) {
        val drift = max(0L, abs(authoritativeElapsedMs - observedTimerMs))
        _metrics.update { current ->
            current.copy(
                totalTimerChecks = current.totalTimerChecks + 1L,
                totalTimerDriftMs = current.totalTimerDriftMs + drift,
                maxTimerDriftMs = max(current.maxTimerDriftMs, drift),
            )
        }
    }

    override fun recordMemorySample(usedHeapBytes: Long?) {
        val heap = usedHeapBytes ?: getCurrentUsedHeapBytes()
        _metrics.update { current ->
            val start = if (current.startHeapBytes == 0L) heap else current.startHeapBytes
            current.copy(
                startHeapBytes = start,
                peakHeapBytes = max(current.peakHeapBytes, heap),
                endHeapBytes = heap,
            )
        }
    }

    override fun resetForTesting() {
        _metrics.value = GameplayPerformanceMetrics()
    }

    private fun getCurrentUsedHeapBytes(): Long {
        val runtime = Runtime.getRuntime()
        return max(0L, runtime.totalMemory() - runtime.freeMemory())
    }
}

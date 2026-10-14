package com.sanket_satpute_20.dailybattle.core.performance

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent
import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdEvent
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdInputCollector
import com.sanket_satpute_20.dailybattle.domain.shift.ShiftEvent
import com.sanket_satpute_20.dailybattle.domain.shift.ShiftInputCollector
import com.sanket_satpute_20.dailybattle.domain.snap.SnapEvent
import com.sanket_satpute_20.dailybattle.domain.snap.SnapInputCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit test suite for Gameplay Performance (Sprint 24.2):
 * 1. Frame Rendering (FPS, frame timings, jank frames, frozen frames)
 * 2. Input Latency (touch-to-response duration)
 * 3. Animation Performance (transition durations, dropped frames)
 * 4. Timer Accuracy (drift between authoritative monotonic time and timer observer)
 * 5. Memory (heap utilization, delta, peak memory)
 * 6. Integration with Snap, Shift, and Crowd Call engines
 */
class GameplayPerformanceTrackerTest {

    private class FakeTimeProvider(var currentTime: Long = 1000L) : TimeProvider {
        override fun elapsedRealtime(): Long = currentTime
    }

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val trackedEvents = mutableListOf<AnalyticsEvent>()
        override fun track(event: AnalyticsEvent) {
            trackedEvents.add(event)
        }
        override fun track(eventName: String, properties: Map<String, Any?>) {
            trackedEvents.add(AnalyticsEvent(eventName, properties))
        }
        override fun setUserProperty(name: String, value: String?) {}
        override fun setUserId(userId: String?) {}
    }

    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var analyticsTracker: FakeAnalyticsTracker
    private lateinit var tracker: DefaultGameplayPerformanceTracker

    @Before
    fun setUp() {
        timeProvider = FakeTimeProvider(currentTime = 1000L)
        analyticsTracker = FakeAnalyticsTracker()
        tracker = DefaultGameplayPerformanceTracker(timeProvider, analyticsTracker)
    }

    // ========================================================================
    // 1. FRAME RENDERING
    // ========================================================================

    @Test
    fun `smooth 60fps frames record zero jank and pass smoothness evaluation`() {
        val smoothFrames = listOf(16.0, 16.2, 15.9, 16.5, 16.1)
        tracker.recordFrames(smoothFrames)

        val metrics = tracker.metrics.value
        assertEquals(5L, metrics.totalFrames)
        assertEquals(0L, metrics.jankFrames)
        assertEquals(0L, metrics.frozenFrames)
        assertEquals(0.0, metrics.jankPercentage, 0.001)
        assertEquals(16.14, metrics.averageFrameTimeMs, 0.05)
        assertEquals(16.5, metrics.maxFrameTimeMs, 0.001)
        assertTrue(metrics.isFrameRenderingSmooth(5.0))
    }

    @Test
    fun `dropped and frozen frames are identified accurately`() {
        tracker.recordFrame(16.0)  // normal
        tracker.recordFrame(28.0)  // jank (> 16.667ms)
        tracker.recordFrame(15.5)  // normal
        tracker.recordFrame(750.0) // frozen (> 700ms)

        val metrics = tracker.metrics.value
        assertEquals(4L, metrics.totalFrames)
        assertEquals(2L, metrics.jankFrames)   // both 28.0 and 750.0 are > 16.667ms
        assertEquals(1L, metrics.frozenFrames) // 750.0 is > 700ms
        assertEquals(50.0, metrics.jankPercentage, 0.001)
        assertEquals(750.0, metrics.maxFrameTimeMs, 0.001)
        assertFalse(metrics.isFrameRenderingSmooth(5.0))
    }

    // ========================================================================
    // 2. INPUT LATENCY
    // ========================================================================

    @Test
    fun `input latency tracking records min max and average latency`() {
        tracker.recordInputLatency(12L)
        tracker.recordInputLatency(8L)
        tracker.recordInputLatency(24L)

        val metrics = tracker.metrics.value
        assertEquals(3L, metrics.totalInputsProcessed)
        assertEquals(44L, metrics.totalInputLatencyMs)
        assertEquals(8L, metrics.minInputLatencyMs)
        assertEquals(24L, metrics.maxInputLatencyMs)
        assertEquals(14.66, metrics.averageInputLatencyMs, 0.05)
        assertTrue(metrics.isInputLatencyAcceptable(50L))
    }

    @Test
    fun `high input latency fails target evaluation`() {
        tracker.recordInputLatency(85L)

        val metrics = tracker.metrics.value
        assertEquals(1L, metrics.totalInputsProcessed)
        assertEquals(85L, metrics.maxInputLatencyMs)
        assertFalse(metrics.isInputLatencyAcceptable(50L))
    }

    @Test
    fun `measureInputLatency helper records execution duration of action`() {
        timeProvider.currentTime = 2000L
        val result = tracker.measureInputLatency {
            timeProvider.currentTime = 2018L
            "input_processed"
        }

        assertEquals("input_processed", result)
        val metrics = tracker.metrics.value
        assertEquals(1L, metrics.totalInputsProcessed)
        assertEquals(18L, metrics.maxInputLatencyMs)
    }

    // ========================================================================
    // 3. ANIMATION PERFORMANCE
    // ========================================================================

    @Test
    fun `animation performance tracks duration delta and dropped frames`() {
        // Animation took 320ms instead of expected 300ms, dropped 2 frames
        tracker.recordAnimationPerformance("SnapPulse", actualDurationMs = 320L, expectedDurationMs = 300L, droppedFrames = 2L)
        // Animation took 200ms as expected, 0 dropped frames
        tracker.recordAnimationPerformance("ShiftTransition", actualDurationMs = 200L, expectedDurationMs = 200L, droppedFrames = 0L)

        val metrics = tracker.metrics.value
        assertEquals(2L, metrics.totalAnimationsTracked)
        assertEquals(2L, metrics.droppedFramesDuringAnimation)
        assertEquals(20L, metrics.totalAnimationDurationDeltaMs)
        assertTrue(metrics.isAnimationPerformant(maxDroppedFrames = 5L))
    }

    @Test
    fun `excessive animation frame drops fail performance target`() {
        tracker.recordAnimationPerformance("HeavyTransition", actualDurationMs = 450L, expectedDurationMs = 300L, droppedFrames = 8L)
        assertFalse(tracker.metrics.value.isAnimationPerformant(maxDroppedFrames = 5L))
    }

    // ========================================================================
    // 4. TIMER ACCURACY
    // ========================================================================

    @Test
    fun `timer accuracy computes drift against monotonic authoritative time`() {
        // Authoritative monotonic elapsed: 1000L, Observed timer: 1010L -> 10ms drift
        tracker.recordTimerCheck(authoritativeElapsedMs = 1000L, observedTimerMs = 1010L)
        // Authoritative: 2000L, Observed: 1985L -> 15ms drift
        tracker.recordTimerCheck(authoritativeElapsedMs = 2000L, observedTimerMs = 1985L)

        val metrics = tracker.metrics.value
        assertEquals(2L, metrics.totalTimerChecks)
        assertEquals(25L, metrics.totalTimerDriftMs)
        assertEquals(15L, metrics.maxTimerDriftMs)
        assertEquals(12.5, metrics.averageTimerDriftMs, 0.001)
        assertTrue(metrics.isTimerAccurate(maxDriftMs = 50L))
    }

    @Test
    fun `excessive timer drift fails accuracy target`() {
        // Timer drifted by 120ms due to UI stalls
        tracker.recordTimerCheck(authoritativeElapsedMs = 5000L, observedTimerMs = 5120L)
        assertFalse(tracker.metrics.value.isTimerAccurate(maxDriftMs = 50L))
    }

    // ========================================================================
    // 5. MEMORY DURING GAMEPLAY
    // ========================================================================

    @Test
    fun `memory session tracks start peak end and heap delta`() {
        tracker.recordMemorySample(usedHeapBytes = 20 * 1024 * 1024L) // 20 MB start
        tracker.recordMemorySample(usedHeapBytes = 35 * 1024 * 1024L) // 35 MB peak
        tracker.recordMemorySample(usedHeapBytes = 25 * 1024 * 1024L) // 25 MB end

        val metrics = tracker.metrics.value
        assertEquals(20 * 1024 * 1024L, metrics.startHeapBytes)
        assertEquals(35 * 1024 * 1024L, metrics.peakHeapBytes)
        assertEquals(25 * 1024 * 1024L, metrics.endHeapBytes)
        assertEquals(5 * 1024 * 1024L, metrics.heapDeltaBytes) // 25 MB - 20 MB = 5 MB
        assertTrue(metrics.isMemoryUsageHealthy(30 * 1024 * 1024L))
    }

    @Test
    fun `excessive heap allocation delta fails memory health check`() {
        tracker.recordMemorySample(usedHeapBytes = 10 * 1024 * 1024L)
        tracker.recordMemorySample(usedHeapBytes = 55 * 1024 * 1024L)

        val metrics = tracker.metrics.value
        assertEquals(45 * 1024 * 1024L, metrics.heapDeltaBytes)
        assertFalse(metrics.isMemoryUsageHealthy(30 * 1024 * 1024L))
    }

    // ========================================================================
    // 6. INTEGRATION WITH CHALLENGE ENGINES
    // ========================================================================

    @Test
    fun `SnapInputCollector records input latency on tap event`() {
        var currentTime = 1000L
        val snapCollector = SnapInputCollector(
            timeProvider = { currentTime },
            performanceTracker = tracker
        )
        snapCollector.prepare()
        snapCollector.start()

        // User tapped at 1000L, engine receives tap at 1015L (15ms touch-to-dispatch latency)
        currentTime = 1015L
        snapCollector.handleInput(SnapEvent.Tap(elementId = "elem_1", timestampMs = 1000L))

        val metrics = tracker.metrics.value
        assertEquals(1L, metrics.totalInputsProcessed)
        assertEquals(15L, metrics.maxInputLatencyMs)
        assertTrue(metrics.isInputLatencyAcceptable(50L))
    }

    @Test
    fun `ShiftInputCollector records input latency on cell tap`() {
        var currentTime = 2000L
        val shiftCollector = ShiftInputCollector(
            timeProvider = { currentTime },
            performanceTracker = tracker
        )
        shiftCollector.prepare()
        shiftCollector.start()
        shiftCollector.handleInput(ShiftEvent.BeginShift)
        shiftCollector.handleInput(ShiftEvent.PresentAnswer)

        // User tapped cell at 2000L, engine receives tap at 2022L (22ms latency)
        currentTime = 2022L
        shiftCollector.handleInput(ShiftEvent.TapCell(cellId = "c1", timestampMs = 2000L))

        val metrics = tracker.metrics.value
        assertEquals(1L, metrics.totalInputsProcessed)
        assertEquals(22L, metrics.maxInputLatencyMs)
        assertTrue(metrics.isInputLatencyAcceptable(50L))
    }

    @Test
    fun `CrowdInputCollector records input latency on prediction submission`() {
        var currentTime = 3000L
        val crowdCollector = CrowdInputCollector(
            timeProvider = { currentTime },
            performanceTracker = tracker
        )
        crowdCollector.prepare()
        crowdCollector.start()

        // User tapped prediction at 3000L, engine receives tap at 3012L (12ms latency)
        currentTime = 3012L
        crowdCollector.handleInput(CrowdEvent.SubmitPrediction(choiceId = "c1", timestampMs = 3000L))

        val metrics = tracker.metrics.value
        assertEquals(1L, metrics.totalInputsProcessed)
        assertEquals(12L, metrics.maxInputLatencyMs)
        assertTrue(metrics.isInputLatencyAcceptable(50L))
    }

    // ========================================================================
    // 7. CONCURRENCY & ROBUSTNESS
    // ========================================================================

    @Test
    fun `concurrent metric recording maintains state integrity`() = runBlocking {
        val jobs = List(10) { index ->
            launch(Dispatchers.Default) {
                tracker.recordFrame(16.0 + index)
                tracker.recordInputLatency(10L + index)
                tracker.recordTimerCheck(1000L + index * 100L, 1005L + index * 100L)
            }
        }
        jobs.forEach { it.join() }

        val metrics = tracker.metrics.value
        assertEquals(10L, metrics.totalFrames)
        assertEquals(10L, metrics.totalInputsProcessed)
        assertEquals(10L, metrics.totalTimerChecks)
        assertNotNull(metrics.averageFrameTimeMs)
        assertNotNull(metrics.averageInputLatencyMs)
    }

    @Test
    fun `resetForTesting clears all gameplay performance metrics`() {
        tracker.recordFrame(33.0)
        tracker.recordInputLatency(25L)
        tracker.recordTimerCheck(1000L, 1020L)
        tracker.recordAnimationPerformance("Anim", 300L, 300L, 2L)
        tracker.recordMemorySample(50 * 1024 * 1024L)

        tracker.resetForTesting()

        val resetMetrics = tracker.metrics.value
        assertEquals(0L, resetMetrics.totalFrames)
        assertEquals(0L, resetMetrics.totalInputsProcessed)
        assertEquals(0L, resetMetrics.totalTimerChecks)
        assertEquals(0L, resetMetrics.totalAnimationsTracked)
        assertEquals(0L, resetMetrics.startHeapBytes)
        assertNull(resetMetrics.minInputLatencyMs)
        assertNull(resetMetrics.maxInputLatencyMs)
    }
}

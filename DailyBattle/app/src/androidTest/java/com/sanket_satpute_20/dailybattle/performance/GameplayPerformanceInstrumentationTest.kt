package com.sanket_satpute_20.dailybattle.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.core.performance.GameplayPerformanceTracker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumentation tests validating runtime gameplay performance tracking (Sprint 24.2):
 * - Frame rendering metrics
 * - Input latency measurement
 * - Timer accuracy checks
 * - Memory heap sampling on Android runtime
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GameplayPerformanceInstrumentationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var tracker: GameplayPerformanceTracker

    @Before
    fun setUp() {
        hiltRule.inject()
        tracker.resetForTesting()
    }

    @Test
    fun runtimeGameplayMetricsTrackAndSampleMemoryCleanly() {
        tracker.startSession()

        // Record a sequence of typical 60fps frames
        tracker.recordFrames(listOf(16.1, 16.3, 15.9, 16.4, 16.0))
        tracker.recordInputLatency(14L)
        tracker.recordTimerCheck(authoritativeElapsedMs = 3000L, observedTimerMs = 3008L)
        tracker.recordAnimationPerformance(
            animationName = "SnapTransition",
            actualDurationMs = 205L,
            expectedDurationMs = 200L,
            droppedFrames = 0L
        )

        val endMetrics = tracker.endSession()

        assertEquals(5L, endMetrics.totalFrames)
        assertEquals(0L, endMetrics.jankFrames)
        assertTrue(endMetrics.isFrameRenderingSmooth())
        assertEquals(1L, endMetrics.totalInputsProcessed)
        assertTrue(endMetrics.isInputLatencyAcceptable())
        assertTrue(endMetrics.isTimerAccurate())
        assertTrue(endMetrics.isAnimationPerformant())
        assertTrue("Heap delta must be non-negative", endMetrics.heapDeltaBytes >= 0L)
        assertTrue(endMetrics.isMemoryUsageHealthy())
    }
}

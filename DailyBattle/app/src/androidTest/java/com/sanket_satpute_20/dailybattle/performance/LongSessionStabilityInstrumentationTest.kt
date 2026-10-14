package com.sanket_satpute_20.dailybattle.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.performance.GameplayPerformanceTracker
import com.sanket_satpute_20.dailybattle.core.performance.SessionStabilityTracker
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
 * Instrumentation test validating runtime session stability (Sprint 24.3) on Android OS:
 * - 50 background/foreground cycles via [AppLifecycleManager]
 * - Repeated battle cycles and practice cycles
 * - Repeated navigation and results viewing
 * - Real runtime JVM heap sampling and creep detection
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LongSessionStabilityInstrumentationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var stabilityTracker: SessionStabilityTracker

    @Inject
    lateinit var gameplayTracker: GameplayPerformanceTracker

    @Inject
    lateinit var lifecycleManager: AppLifecycleManager

    @Before
    fun setUp() {
        hiltRule.inject()
        stabilityTracker.resetForTesting()
        gameplayTracker.resetForTesting()
    }

    @Test
    fun runtimeLongSessionCyclesAndMemoryRemainHealthyOnDevice() {
        stabilityTracker.startSession()

        // 1. Simulate 25 battle cycles and 25 practice cycles
        for (i in 1..25) {
            stabilityTracker.recordBattleCycle()
            stabilityTracker.recordPracticeCycle()
            stabilityTracker.recordNavigationTransition("cycle_$i")
            stabilityTracker.recordResultsView()
        }

        val initialListeners = lifecycleManager.listenerCount

        // 2. Simulate 50 background/foreground cycles
        val reasons = listOf(
            BackgroundReason.USER_NAVIGATION,
            BackgroundReason.SCREEN_LOCK,
            BackgroundReason.NOTIFICATION_INTERRUPT,
            BackgroundReason.SYSTEM_INTERRUPT,
            BackgroundReason.APP_SWITCHING
        )

        for (i in 1..50) {
            val reason = reasons[(i - 1) % reasons.size]
            lifecycleManager.onBackground(reason)
            lifecycleManager.onForeground()
        }

        // Sample real JVM heap
        stabilityTracker.recordHeapSample()

        val snapshot = stabilityTracker.getSnapshot()

        assertEquals(25L, snapshot.battleCyclesCompleted)
        assertEquals(25L, snapshot.practiceCyclesCompleted)
        assertEquals(25L, snapshot.navigationTransitions)
        assertEquals(25L, snapshot.resultsViewCount)
        assertEquals(50L, snapshot.backgroundForegroundCycles)
        assertEquals(0L, snapshot.unhandledExceptionsCount)

        // Memory and stability invariants on Android runtime
        assertTrue("Memory must remain within healthy threshold", snapshot.isMemoryHealthy())
        assertTrue("Session must remain fully stable", snapshot.isSessionStable())
        assertEquals(
            "Lifecycle listeners must not leak across 50 background/foreground cycles",
            initialListeners,
            lifecycleManager.listenerCount
        )
    }
}

package com.sanket_satpute_20.dailybattle.core.performance

import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleEvent
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleListener
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DefaultSessionStabilityTracker] and [SessionStabilityMetrics].
 *
 * Validates Sprint 24.3 requirements:
 * - Tracking long session duration monotonically
 * - Monitoring battle cycles, practice cycles, navigation transitions
 * - Responding to lifecycle background/foreground events
 * - Heap memory bounds and creep tracking
 * - Unhandled exception and overall stability reporting
 */
class SessionStabilityTrackerTest {

    private class FakeTimeProvider(var currentTime: Long = 10_000L) : TimeProvider {
        override fun elapsedRealtime(): Long = currentTime
    }

    private class FakeAppLifecycleManager : AppLifecycleManager {
        private val _state = MutableStateFlow(AppLifecycleState.FOREGROUND)
        override val state: StateFlow<AppLifecycleState> = _state
        private val _events = MutableSharedFlow<AppLifecycleEvent>(extraBufferCapacity = 8)
        override val events: SharedFlow<AppLifecycleEvent> = _events
        override val lastBackgroundAtMillis: Long? = null
        override val lastForegroundAtMillis: Long? = null
        override val currentReason: BackgroundReason? = null

        val listeners = mutableListOf<AppLifecycleListener>()

        override val listenerCount: Int get() = listeners.size

        override fun onBackground(reason: BackgroundReason, timestamp: Long?) {
            val event = AppLifecycleEvent.Background(timestamp ?: 0L, reason)
            listeners.forEach { it.onLifecycleEvent(event) }
        }

        override fun onForeground(timestamp: Long?) {
            val event = AppLifecycleEvent.Foreground(timestamp ?: 0L, 100L)
            listeners.forEach { it.onLifecycleEvent(event) }
        }

        override fun onRecreation(isProcessRecreation: Boolean, timestamp: Long?) {
            val event = AppLifecycleEvent.Recreated(timestamp ?: 0L, isProcessRecreation)
            listeners.forEach { it.onLifecycleEvent(event) }
        }

        override fun addListener(listener: AppLifecycleListener) {
            listeners.add(listener)
        }

        override fun removeListener(listener: AppLifecycleListener) {
            listeners.remove(listener)
        }
    }

    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var lifecycleManager: FakeAppLifecycleManager
    private lateinit var tracker: DefaultSessionStabilityTracker

    @Before
    fun setUp() {
        timeProvider = FakeTimeProvider(currentTime = 1_000_000L)
        lifecycleManager = FakeAppLifecycleManager()
        tracker = DefaultSessionStabilityTracker(timeProvider, lifecycleManager)
    }

    @Test
    fun tracksInitialSessionStateAndDurationCleanly() {
        tracker.startSession(startTimestampMs = 500_000L)

        // Advance simulated time by 5 minutes (300,000 ms)
        timeProvider.currentTime += 300_000L
        tracker.recordNavigationTransition("home")

        val snapshot = tracker.getSnapshot()
        assertEquals(500_000L, snapshot.sessionStartTimestampMs)
        assertEquals(300_000L, snapshot.sessionDurationMs)
        assertEquals(1L, snapshot.navigationTransitions)
        assertTrue(snapshot.isSessionStable())
    }

    @Test
    fun recordsBattleCyclesAndPracticeCyclesCorrectly() {
        tracker.startSession()

        tracker.recordBattleCycle()
        tracker.recordBattleCycle()
        tracker.recordPracticeCycle()
        tracker.recordPracticeCycle()
        tracker.recordPracticeCycle()

        val snapshot = tracker.getSnapshot()
        assertEquals(2L, snapshot.battleCyclesCompleted)
        assertEquals(3L, snapshot.practiceCyclesCompleted)
        assertTrue(snapshot.isSessionStable())
    }

    @Test
    fun recordsNavigationTransitionsAndResultsView() {
        tracker.startSession()

        tracker.recordNavigationTransition("results")
        tracker.recordResultsView()
        tracker.recordNavigationTransition("rival")
        tracker.recordResultsView()

        val snapshot = tracker.getSnapshot()
        assertEquals(2L, snapshot.navigationTransitions)
        assertEquals(2L, snapshot.resultsViewCount)
    }

    @Test
    fun tracksBackgroundForegroundCyclesViaLifecycleListener() {
        tracker.startSession()

        assertEquals("Tracker must register as lifecycle listener", 1, lifecycleManager.listenerCount)

        // Trigger 5 background/foreground cycles via lifecycle manager
        for (i in 1..5) {
            lifecycleManager.onBackground(BackgroundReason.APP_SWITCHING)
            lifecycleManager.onForeground()
        }

        val snapshot = tracker.getSnapshot()
        assertEquals(5L, snapshot.backgroundForegroundCycles)
    }

    @Test
    fun recordsExceptionAndReflectsInstability() {
        tracker.startSession()

        assertTrue(tracker.getSnapshot().isSessionStable())

        tracker.recordException(RuntimeException("Simulated crash"))

        val snapshot = tracker.getSnapshot()
        assertEquals(1L, snapshot.unhandledExceptionsCount)
        assertFalse("Session with unhandled exceptions must not be stable", snapshot.isSessionStable())
    }

    @Test
    fun evaluatesMemoryHealthAndHeapCreep() {
        val initialHeap = 20 * 1024 * 1024L // 20 MB
        val healthyHeap = 30 * 1024 * 1024L // 30 MB (10 MB creep)
        val excessiveHeap = 80 * 1024 * 1024L // 80 MB (60 MB creep > 50 MB limit)

        tracker.startSession()
        tracker.recordHeapSample(initialHeap)

        val metricsHealthy = tracker.getSnapshot().copy(
            startingHeapBytes = initialHeap,
            currentHeapBytes = healthyHeap
        )
        assertEquals(10 * 1024 * 1024L, metricsHealthy.heapCreepBytes)
        assertTrue(metricsHealthy.isMemoryHealthy())
        assertTrue(metricsHealthy.isSessionStable())

        val metricsLeaked = tracker.getSnapshot().copy(
            startingHeapBytes = initialHeap,
            currentHeapBytes = excessiveHeap
        )
        assertEquals(60 * 1024 * 1024L, metricsLeaked.heapCreepBytes)
        assertFalse(metricsLeaked.isMemoryHealthy())
        assertFalse(metricsLeaked.isSessionStable())
    }

    @Test
    fun resetForTestingReinitializesCleanly() {
        tracker.startSession()
        tracker.recordBattleCycle()
        tracker.recordPracticeCycle()
        tracker.recordNavigationTransition()
        tracker.recordException()

        assertEquals(1L, tracker.getSnapshot().battleCyclesCompleted)
        assertEquals(1L, tracker.getSnapshot().unhandledExceptionsCount)

        tracker.resetForTesting()

        val resetSnapshot = tracker.getSnapshot()
        assertEquals(0L, resetSnapshot.battleCyclesCompleted)
        assertEquals(0L, resetSnapshot.practiceCyclesCompleted)
        assertEquals(0L, resetSnapshot.navigationTransitions)
        assertEquals(0L, resetSnapshot.unhandledExceptionsCount)
        assertTrue(resetSnapshot.isSessionStable())
    }
}

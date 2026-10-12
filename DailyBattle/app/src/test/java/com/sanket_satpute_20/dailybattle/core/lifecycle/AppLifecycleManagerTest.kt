package com.sanket_satpute_20.dailybattle.core.lifecycle

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AppLifecycleManagerTest {

    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var lifecycleManager: DefaultAppLifecycleManager

    @Before
    fun setup() {
        lifecycleManager = DefaultAppLifecycleManager(clock)
    }

    @Test
    fun initialStateIsForeground() {
        assertEquals(AppLifecycleState.FOREGROUND, lifecycleManager.state.value)
        assertNull(lifecycleManager.lastBackgroundAtMillis)
        assertNull(lifecycleManager.lastForegroundAtMillis)
        assertNull(lifecycleManager.currentReason)
    }

    @Test
    fun onBackgroundUpdatesStateAndRecordsReasonForUserNavigation() {
        currentTime = 1_005_000L
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)

        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)
        assertEquals(1_005_000L, lifecycleManager.lastBackgroundAtMillis)
        assertEquals(BackgroundReason.USER_NAVIGATION, lifecycleManager.currentReason)
    }

    @Test
    fun onBackgroundHandlesScreenLockReason() {
        currentTime = 1_010_000L
        lifecycleManager.onBackground(BackgroundReason.SCREEN_LOCK)

        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)
        assertEquals(BackgroundReason.SCREEN_LOCK, lifecycleManager.currentReason)
        assertEquals(1_010_000L, lifecycleManager.lastBackgroundAtMillis)
    }

    @Test
    fun onBackgroundHandlesNotificationInterruptionReason() {
        currentTime = 1_015_000L
        lifecycleManager.onBackground(BackgroundReason.NOTIFICATION_INTERRUPT)

        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)
        assertEquals(BackgroundReason.NOTIFICATION_INTERRUPT, lifecycleManager.currentReason)
    }

    @Test
    fun onBackgroundHandlesAppSwitchingReason() {
        currentTime = 1_020_000L
        lifecycleManager.onBackground(BackgroundReason.APP_SWITCHING)

        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)
        assertEquals(BackgroundReason.APP_SWITCHING, lifecycleManager.currentReason)
    }

    @Test
    fun onBackgroundHandlesSystemInterruptionReason() {
        currentTime = 1_025_000L
        lifecycleManager.onBackground(BackgroundReason.SYSTEM_INTERRUPT)

        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)
        assertEquals(BackgroundReason.SYSTEM_INTERRUPT, lifecycleManager.currentReason)
    }

    @Test
    fun onForegroundUpdatesStateAndCalculatesBackgroundDuration() {
        currentTime = 1_000_000L
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)

        currentTime = 1_008_500L // 8.5 seconds later
        lifecycleManager.onForeground()

        assertEquals(AppLifecycleState.FOREGROUND, lifecycleManager.state.value)
        assertEquals(1_008_500L, lifecycleManager.lastForegroundAtMillis)
    }

    @Test
    fun listenersReceiveLifecycleEventsInOrder() = runTest {
        val recordedEvents = mutableListOf<AppLifecycleEvent>()
        val listener = AppLifecycleListener { recordedEvents.add(it) }

        lifecycleManager.addListener(listener)

        currentTime = 1000L
        lifecycleManager.onBackground(BackgroundReason.SCREEN_LOCK)

        currentTime = 5000L
        lifecycleManager.onForeground()

        assertEquals(2, recordedEvents.size)

        val bgEvent = recordedEvents[0]
        assertTrue(bgEvent is AppLifecycleEvent.Background)
        assertEquals(BackgroundReason.SCREEN_LOCK, (bgEvent as AppLifecycleEvent.Background).reason)
        assertEquals(1000L, bgEvent.timestamp)

        val fgEvent = recordedEvents[1]
        assertTrue(fgEvent is AppLifecycleEvent.Foreground)
        assertEquals(5000L, (fgEvent as AppLifecycleEvent.Foreground).timestamp)
        assertEquals(4000L, fgEvent.backgroundDurationMs)

        // Remove listener
        lifecycleManager.removeListener(listener)
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)
        // No new events received after removal
        assertEquals(2, recordedEvents.size)
    }

    @Test
    fun repeatedBackgroundCallsWithSameReasonAreIdempotent() {
        currentTime = 1000L
        lifecycleManager.onBackground(BackgroundReason.SCREEN_LOCK)

        val recordedEvents = mutableListOf<AppLifecycleEvent>()
        lifecycleManager.addListener { recordedEvents.add(it) }

        // Second call with same reason is idempotent
        lifecycleManager.onBackground(BackgroundReason.SCREEN_LOCK)
        assertTrue(recordedEvents.isEmpty())
    }

    @Test
    fun rapidBackgroundForegroundCyclesHandledSafely() {
        repeat(5) { cycle ->
            currentTime += 1000L
            lifecycleManager.onBackground(BackgroundReason.APP_SWITCHING)
            assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)

            currentTime += 500L
            lifecycleManager.onForeground()
            assertEquals(AppLifecycleState.FOREGROUND, lifecycleManager.state.value)
        }
    }
}

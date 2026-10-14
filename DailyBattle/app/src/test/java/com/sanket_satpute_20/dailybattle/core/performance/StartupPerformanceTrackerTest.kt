package com.sanket_satpute_20.dailybattle.core.performance

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent
import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.analytics.CoreAnalyticsEvents
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleEvent
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleListener
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Unit tests verifying startup performance measurement per Sprint 24.1:
 * - Cold start latency measurement, idempotency, and analytics dispatch
 * - Warm start latency measurement (recreation & foreground return)
 * - First screen rendering latency measurement, idempotency, and analytics dispatch
 * - Baseline threshold evaluations
 * - Concurrency and lifecycle integration
 */
class StartupPerformanceTrackerTest {

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

    private class FakeAppLifecycleManager : AppLifecycleManager {
        private val _state = MutableStateFlow(AppLifecycleState.FOREGROUND)
        override val state: StateFlow<AppLifecycleState> = _state.asStateFlow()

        private val _events = MutableSharedFlow<AppLifecycleEvent>(extraBufferCapacity = 16)
        override val events: SharedFlow<AppLifecycleEvent> = _events.asSharedFlow()

        override val lastBackgroundAtMillis: Long? = null
        override val lastForegroundAtMillis: Long? = null
        override val currentReason: BackgroundReason? = null

        val listeners = CopyOnWriteArrayList<AppLifecycleListener>()

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
    private lateinit var analyticsTracker: FakeAnalyticsTracker
    private lateinit var lifecycleManager: FakeAppLifecycleManager
    private lateinit var tracker: DefaultStartupPerformanceTracker

    @Before
    fun setUp() {
        DefaultStartupPerformanceTracker.resetInitialProcessStartForTesting()
        timeProvider = FakeTimeProvider(currentTime = 1000L)
        analyticsTracker = FakeAnalyticsTracker()
        lifecycleManager = FakeAppLifecycleManager()
        tracker = DefaultStartupPerformanceTracker(timeProvider, analyticsTracker, lifecycleManager)
    }

    @Test
    fun `initial state has cold startup type and process start time initialized`() {
        val metrics = tracker.metrics.value
        assertEquals(StartupType.COLD, metrics.startupType)
        assertEquals(1000L, metrics.processStartTimeMs)
        assertFalse(metrics.isColdStartCompleted)
        assertFalse(metrics.isWarmStartCompleted)
        assertFalse(metrics.isFirstScreenRendered)
        assertNull(metrics.coldStartDurationMs)
        assertNull(metrics.warmStartDurationMs)
        assertNull(metrics.firstScreenRenderDurationMs)
    }

    @Test
    fun `cold start measurement computes latency and dispatches analytics`() {
        tracker.recordProcessStart(1000L)
        timeProvider.currentTime = 1450L

        val duration = tracker.recordColdStartCompleted(1450L)
        assertEquals(450L, duration)

        val metrics = tracker.metrics.value
        assertTrue(metrics.isColdStartCompleted)
        assertEquals(450L, metrics.coldStartDurationMs)
        assertEquals(StartupType.COLD, metrics.startupType)
        assertTrue(metrics.isColdStartWithinTarget(2000L))

        // Analytics verification
        val startupEvent = analyticsTracker.trackedEvents.find { it.name == CoreAnalyticsEvents.EVENT_APP_STARTUP }
        assertNotNull(startupEvent)
        assertEquals(450L, (startupEvent?.properties?.get(CoreAnalyticsEvents.PARAM_DURATION_MS) as? Number)?.toLong())
    }

    @Test
    fun `cold start measurement is idempotent and does not overwrite original duration`() {
        tracker.recordProcessStart(1000L)
        tracker.recordColdStartCompleted(1300L)

        // Subsequent call at later time
        val secondDuration = tracker.recordColdStartCompleted(1900L)
        assertEquals(300L, secondDuration)
        assertEquals(300L, tracker.metrics.value.coldStartDurationMs)

        // Only one startup event tracked
        val startupEvents = analyticsTracker.trackedEvents.filter { it.name == CoreAnalyticsEvents.EVENT_APP_STARTUP }
        assertEquals(1, startupEvents.size)
    }

    @Test
    fun `warm start measurement computes latency correctly`() {
        tracker.recordWarmStartBegin(2000L)
        timeProvider.currentTime = 2150L

        val duration = tracker.recordWarmStartCompleted(2150L)
        assertEquals(150L, duration)

        val metrics = tracker.metrics.value
        assertTrue(metrics.isWarmStartCompleted)
        assertEquals(150L, metrics.warmStartDurationMs)
        assertEquals(StartupType.WARM, metrics.startupType)
        assertTrue(metrics.isWarmStartWithinTarget(500L))
    }

    @Test
    fun `warm start triggered on lifecycle foreground after cold start completed`() {
        tracker.recordProcessStart(1000L)
        tracker.recordColdStartCompleted(1200L)

        // App goes to background at 3000L
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION, 3000L)

        // App returns to foreground at 3100L
        lifecycleManager.onForeground(3100L)

        val metrics = tracker.metrics.value
        assertTrue(metrics.isWarmStartCompleted)
        assertEquals(100L, metrics.warmStartDurationMs)
        assertEquals(StartupType.WARM, metrics.startupType)
    }

    @Test
    fun `process recreation marks warm startup type`() {
        lifecycleManager.onRecreation(isProcessRecreation = true, timestamp = 5000L)
        assertEquals(StartupType.WARM, tracker.metrics.value.startupType)

        val duration = tracker.recordWarmStartCompleted(5120L)
        assertEquals(120L, duration)
        assertEquals(120L, tracker.metrics.value.warmStartDurationMs)
    }

    @Test
    fun `first screen rendering latency computes time from process start and dispatches analytics`() {
        tracker.recordProcessStart(1000L)
        timeProvider.currentTime = 1180L

        val duration = tracker.recordFirstScreenRenderCompleted("Startup", 1180L)
        assertEquals(180L, duration)

        val metrics = tracker.metrics.value
        assertTrue(metrics.isFirstScreenRendered)
        assertEquals(180L, metrics.firstScreenRenderDurationMs)
        assertEquals("Startup", metrics.firstScreenName)
        assertTrue(metrics.isFirstScreenRenderWithinTarget(1000L))

        // Analytics verification
        val screenLoadEvent = analyticsTracker.trackedEvents.find { it.name == CoreAnalyticsEvents.EVENT_SCREEN_LOAD }
        assertNotNull(screenLoadEvent)
        assertEquals("Startup", screenLoadEvent?.properties?.get(CoreAnalyticsEvents.PARAM_SCREEN_NAME))
        assertEquals(180L, (screenLoadEvent?.properties?.get(CoreAnalyticsEvents.PARAM_DURATION_MS) as? Number)?.toLong())
    }

    @Test
    fun `first screen rendering latency is idempotent and preserves first screen identity`() {
        tracker.recordProcessStart(1000L)
        tracker.recordFirstScreenRenderCompleted("Startup", 1150L)

        // Subsequent screen render call (e.g. Welcome screen)
        val secondDuration = tracker.recordFirstScreenRenderCompleted("Welcome", 1400L)
        assertEquals(150L, secondDuration)
        assertEquals("Startup", tracker.metrics.value.firstScreenName)
        assertEquals(150L, tracker.metrics.value.firstScreenRenderDurationMs)

        // Screen load for Startup is tracked
        val screenLoads = analyticsTracker.trackedEvents.filter { it.name == CoreAnalyticsEvents.EVENT_SCREEN_LOAD }
        assertEquals(1, screenLoads.size)
        assertEquals("Startup", screenLoads.first().properties[CoreAnalyticsEvents.PARAM_SCREEN_NAME])
    }

    @Test
    fun `target evaluations accurately report within or exceeding targets`() {
        val fastMetrics = StartupMetrics(
            coldStartDurationMs = 1200L,
            warmStartDurationMs = 350L,
            firstScreenRenderDurationMs = 600L
        )
        assertTrue(fastMetrics.isColdStartWithinTarget(2000L))
        assertTrue(fastMetrics.isWarmStartWithinTarget(500L))
        assertTrue(fastMetrics.isFirstScreenRenderWithinTarget(1000L))

        val slowMetrics = StartupMetrics(
            coldStartDurationMs = 2500L,
            warmStartDurationMs = 650L,
            firstScreenRenderDurationMs = 1200L
        )
        assertFalse(slowMetrics.isColdStartWithinTarget(2000L))
        assertFalse(slowMetrics.isWarmStartWithinTarget(500L))
        assertFalse(slowMetrics.isFirstScreenRenderWithinTarget(1000L))
    }

    @Test
    fun `negative elapsed time clamped to zero gracefully`() {
        tracker.recordProcessStart(2000L)
        // Clock anomaly returns timestamp before process start
        val duration = tracker.recordColdStartCompleted(1900L)
        assertEquals(0L, duration)
        assertEquals(0L, tracker.metrics.value.coldStartDurationMs)
    }

    @Test
    fun `concurrent calls to recordColdStartCompleted maintain thread-safety`() = runBlocking {
        tracker.recordProcessStart(1000L)
        timeProvider.currentTime = 1250L

        val jobs = List(10) { index ->
            launch(Dispatchers.Default) {
                tracker.recordColdStartCompleted(1250L + index * 10L)
            }
        }
        jobs.forEach { it.join() }

        assertTrue(tracker.metrics.value.isColdStartCompleted)
        assertNotNull(tracker.metrics.value.coldStartDurationMs)
        // Only 1 startup event was dispatched despite 10 concurrent calls
        assertEquals(1, analyticsTracker.trackedEvents.filter { it.name == CoreAnalyticsEvents.EVENT_APP_STARTUP }.size)
    }

    @Test
    fun `resetForTesting clears all tracked state`() {
        tracker.recordProcessStart(1000L)
        tracker.recordColdStartCompleted(1400L)
        tracker.recordFirstScreenRenderCompleted("Startup", 1200L)

        timeProvider.currentTime = 5000L
        tracker.resetForTesting()

        val resetMetrics = tracker.metrics.value
        assertEquals(5000L, resetMetrics.processStartTimeMs)
        assertFalse(resetMetrics.isColdStartCompleted)
        assertFalse(resetMetrics.isFirstScreenRendered)
        assertNull(resetMetrics.coldStartDurationMs)
        assertNull(resetMetrics.firstScreenRenderDurationMs)
    }
}

package com.sanket_satpute_20.dailybattle.core.analytics

import com.sanket_satpute_20.dailybattle.core.analytics.reliability.AnalyticsQueueStore
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.AnalyticsReliabilityManager
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.EventDeduplicator
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.InMemoryAnalyticsQueueStore
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.QueuedAnalyticsEvent
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.SessionFunnelAuditor
import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verification of Sprint 20.3 — Event Reliability.
 *
 * Requirements verified:
 * - Duplicate event (Idempotency / Deduplication)
 * - Missing event (Funnel integrity / Unclosed session recovery)
 * - Offline event (Local FIFO queue buffering)
 * - Delayed event (Original timestamp preserved)
 * - App killed before event delivery (Queue recovery)
 * - Provider unavailable (Safe fallback, retry, and non-blocking failure)
 */
class EventReliabilityTest {

    private lateinit var inMemoryProvider: InMemoryAnalyticsProvider
    private lateinit var queueStore: InMemoryAnalyticsQueueStore
    private lateinit var reliabilityManager: AnalyticsReliabilityManager
    private lateinit var tracker: DefaultAnalyticsTracker

    private class FakeEnv : EnvironmentProvider {
        override val current: AppEnvironment = AppEnvironment.Development
    }

    @Before
    fun setup() {
        inMemoryProvider = InMemoryAnalyticsProvider()
        queueStore = InMemoryAnalyticsQueueStore(maxCapacity = 100)
        reliabilityManager = AnalyticsReliabilityManager(
            deduplicator = EventDeduplicator(windowDurationMs = 2_000L),
            queueStore = queueStore,
            funnelAuditor = SessionFunnelAuditor(),
            maxDeliveryAttempts = 3
        )
        tracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(inMemoryProvider),
            reliabilityManager = reliabilityManager
        )
    }

    // ============================================================================
    // 1. Duplicate Event (Idempotency)
    // ============================================================================

    @Test
    fun `reliability - duplicate battle completed event dropped within time window`() {
        val event1 = CoreAnalyticsEvents.battleCompleted("b-1", "sess-100", 650)
        val event2 = CoreAnalyticsEvents.battleCompleted("b-1", "sess-100", 650)

        tracker.track(event1)
        tracker.track(event2) // Rapid double tap on completion

        // Only 1 event delivered to provider
        val recorded = inMemoryProvider.getRecordedEvents()
        assertEquals(1, recorded.size)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED, recorded.first().name)

        // Reliability state recorded dropped duplicate
        assertEquals(1, tracker.reliabilityState.value.droppedDuplicateCount)
    }

    @Test
    fun `reliability - duplicate challenge completed event dropped for same session and index`() {
        val ch1 = CoreAnalyticsEvents.challengeCompleted("sess-200", "snap", 1, 250)
        val ch1Duplicate = CoreAnalyticsEvents.challengeCompleted("sess-200", "snap", 1, 250)
        val ch2 = CoreAnalyticsEvents.challengeCompleted("sess-200", "shift", 2, 280)

        tracker.track(ch1)
        tracker.track(ch1Duplicate) // Duplicate tap
        tracker.track(ch2)          // Legitimate next challenge

        val recorded = inMemoryProvider.getRecordedEvents()
        assertEquals(2, recorded.size)
        assertEquals(1, recorded[0].properties[CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX])
        assertEquals(2, recorded[1].properties[CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX])
        assertEquals(1, tracker.reliabilityState.value.droppedDuplicateCount)
    }

    @Test
    fun `reliability - explicit deduplication id filters identical duplicates`() {
        val eventA = AnalyticsEvent(
            name = "custom_action",
            properties = mapOf(EventDeduplicator.PARAM_DEDUPLICATION_ID to "unique-action-1")
        )
        val eventADup = AnalyticsEvent(
            name = "custom_action",
            properties = mapOf(EventDeduplicator.PARAM_DEDUPLICATION_ID to "unique-action-1")
        )
        val eventB = AnalyticsEvent(
            name = "custom_action",
            properties = mapOf(EventDeduplicator.PARAM_DEDUPLICATION_ID to "unique-action-2")
        )

        tracker.track(eventA)
        tracker.track(eventADup)
        tracker.track(eventB)

        assertEquals(2, inMemoryProvider.getRecordedEvents().size)
    }

    @Test
    fun `reliability - event accepted after deduplication window expires`() {
        val deduplicator = EventDeduplicator(windowDurationMs = 500L)
        val event = CoreAnalyticsEvents.battleViewed("b-999")

        val t0 = 1000L
        assertTrue(deduplicator.shouldTrack(event, currentTimeMs = t0))
        assertFalse(deduplicator.shouldTrack(event, currentTimeMs = t0 + 200L)) // within window -> dropped
        assertTrue(deduplicator.shouldTrack(event, currentTimeMs = t0 + 600L))  // after window -> accepted
    }

    // ============================================================================
    // 2. Missing Event (Session Funnel Auditing)
    // ============================================================================

    @Test
    fun `reliability - missing event detected when new session starts before previous completes`() {
        // Start session 1
        tracker.trackBattleStarted("battle-1", "session-alpha")
        tracker.trackChallengeStarted("session-alpha", "snap", 1)

        // Session 1 was never completed or abandoned! User starts session 2
        tracker.trackBattleStarted("battle-2", "session-beta")

        val recorded = inMemoryProvider.getRecordedEvents()
        // Expected events:
        // 1. battle_started (session-alpha)
        // 2. challenge_started (session-alpha)
        // 3. battle_abandoned (session-alpha, synthetic recovery)
        // 4. battle_started (session-beta)
        assertEquals(4, recorded.size)

        val abandonedEvent = recorded[2]
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_ABANDONED, abandonedEvent.name)
        assertEquals("session-alpha", abandonedEvent.properties[CoreAnalyticsEvents.PARAM_SESSION_ID])
        assertEquals("abandoned_uncompleted_session", abandonedEvent.properties[CoreAnalyticsEvents.PARAM_REASON])
    }

    @Test
    fun `reliability - app killed mid-battle is recovered cleanly on startup`() {
        tracker.trackBattleStarted("battle-daily", "sess-killed-app")
        tracker.trackChallengeCompleted("sess-killed-app", "snap", 1, 200)

        // Simulate app process restart: recoverInterruptedSession called on startup
        tracker.recoverInterruptedSession()

        val recorded = inMemoryProvider.getRecordedEvents()
        assertEquals(3, recorded.size)

        val recoveredEvent = recorded.last()
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_ABANDONED, recoveredEvent.name)
        assertEquals("sess-killed-app", recoveredEvent.properties[CoreAnalyticsEvents.PARAM_SESSION_ID])
        assertEquals("interrupted_app_exit", recoveredEvent.properties[CoreAnalyticsEvents.PARAM_REASON])
    }

    @Test
    fun `reliability - completed session produces no orphaned recovery event`() {
        tracker.trackBattleStarted("battle-daily", "sess-normal")
        tracker.trackBattleCompleted("battle-daily", "sess-normal", 600)

        tracker.recoverInterruptedSession()

        // Only started + completed, no abandoned event
        val recorded = inMemoryProvider.getRecordedEvents()
        assertEquals(2, recorded.size)
        assertFalse(recorded.any { it.name == CoreAnalyticsEvents.EVENT_BATTLE_ABANDONED })
    }

    // ============================================================================
    // 3. Offline Event & Delayed Event Delivery
    // ============================================================================

    @Test
    fun `reliability - offline events are queued and flushed when online`() {
        // Switch to offline
        tracker.setOnline(false)
        assertFalse(tracker.reliabilityState.value.isOnline)

        val originalTimestamp = 1600000000000L
        val eventOffline = AnalyticsEvent("app_opened", timestamp = originalTimestamp)
        tracker.track(eventOffline)
        tracker.track(CoreAnalyticsEvents.battleViewed("battle-off"))

        // Provider has 0 events while offline
        assertEquals(0, inMemoryProvider.getRecordedEvents().size)
        // Queue holds 2 pending events
        assertEquals(2, tracker.reliabilityState.value.pendingQueueSize)

        // Reconnect online -> auto flushes queued events
        tracker.setOnline(true)

        val recorded = inMemoryProvider.getRecordedEvents()
        assertEquals(2, recorded.size)
        assertEquals(0, tracker.reliabilityState.value.pendingQueueSize)

        // Delayed event: original timestamp must be preserved!
        val appOpenedEvent = recorded.first { it.name == "app_opened" }
        assertEquals(originalTimestamp, appOpenedEvent.timestamp)
    }

    // ============================================================================
    // 4. App Killed Before Event Delivery (Queue Persistence & Recovery)
    // ============================================================================

    @Test
    fun `reliability - pending queue persists across simulated process death and flushes on recovery`() {
        // Queue an event while offline
        tracker.setOnline(false)
        tracker.track(CoreAnalyticsEvents.battleCompleted("b-kill", "s-kill", 800))
        assertEquals(1, queueStore.size())

        // Simulate app process killed and restarted with same queue store
        val newProvider = InMemoryAnalyticsProvider()
        val restoredReliabilityManager = AnalyticsReliabilityManager(
            queueStore = queueStore
        )
        val restoredTracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(newProvider),
            reliabilityManager = restoredReliabilityManager
        )

        // Flush on startup
        restoredTracker.flushQueuedEvents()

        assertEquals(1, newProvider.getRecordedEvents().size)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED, newProvider.getRecordedEvents().first().name)
        assertEquals(0, queueStore.size())
    }

    // ============================================================================
    // 5. Provider Unavailable (Fallback & Retry)
    // ============================================================================

    @Test
    fun `reliability - provider unavailable buffers event for retry without throwing or blocking`() {
        var providerShouldFail = true
        val flakingProvider = object : AnalyticsProvider {
            override val name: String = "FlakingRemote"
            override val isEnabled: Boolean = true
            override fun logEvent(event: AnalyticsEvent) {
                if (providerShouldFail) {
                    throw RuntimeException("503 Service Unavailable")
                }
            }
            override fun setUserProperty(name: String, value: String?) {}
            override fun setUserId(userId: String?) {}
        }

        val failingTracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(flakingProvider),
            reliabilityManager = reliabilityManager
        )

        // Track when provider is failing
        failingTracker.track(CoreAnalyticsEvents.appStartup(300L))

        // No exception thrown to caller!
        // Event buffered in queue for retry
        assertEquals(1, queueStore.size())
        assertEquals(1, failingTracker.reliabilityState.value.failedDeliveryCount)
        assertEquals("503 Service Unavailable", failingTracker.reliabilityState.value.lastError)

        // Provider recovers
        providerShouldFail = false
        val flushed = failingTracker.flushQueuedEvents()

        assertEquals(1, flushed)
        assertEquals(0, queueStore.size())
    }

    @Test
    fun `reliability - permanently failing events evicted after max delivery attempts`() {
        val alwaysFailingProvider = object : AnalyticsProvider {
            override val name: String = "AlwaysFailing"
            override val isEnabled: Boolean = true
            override fun logEvent(event: AnalyticsEvent) {
                throw RuntimeException("Network fatal error")
            }
            override fun setUserProperty(name: String, value: String?) {}
            override fun setUserId(userId: String?) {}
        }

        val failingTracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(alwaysFailingProvider),
            reliabilityManager = reliabilityManager
        )

        failingTracker.track(CoreAnalyticsEvents.appStartup(100L))
        assertEquals(1, queueStore.size())

        // Attempt flush 1 (attempt 2)
        failingTracker.flushQueuedEvents()
        assertEquals(1, queueStore.size())

        // Attempt flush 2 (attempt 3 - reaches max 3)
        failingTracker.flushQueuedEvents()

        // Evicted from queue to prevent infinite loop / unbounded growth
        assertEquals(0, queueStore.size())
    }

    // ============================================================================
    // 6. Bounded Queue Capacity
    // ============================================================================

    @Test
    fun `reliability - queue store evicts oldest events when max capacity exceeded`() {
        val smallQueue = InMemoryAnalyticsQueueStore(maxCapacity = 3)
        smallQueue.enqueue(QueuedAnalyticsEvent(event = AnalyticsEvent("event_1")))
        smallQueue.enqueue(QueuedAnalyticsEvent(event = AnalyticsEvent("event_2")))
        smallQueue.enqueue(QueuedAnalyticsEvent(event = AnalyticsEvent("event_3")))
        assertEquals(3, smallQueue.size())

        // Add 4th event
        smallQueue.enqueue(QueuedAnalyticsEvent(event = AnalyticsEvent("event_4")))

        // Size stays 3, event_1 was evicted
        assertEquals(3, smallQueue.size())
        val allEvents = smallQueue.getAll().map { it.event.name }
        assertFalse(allEvents.contains("event_1"))
        assertTrue(allEvents.contains("event_2"))
        assertTrue(allEvents.contains("event_3"))
        assertTrue(allEvents.contains("event_4"))
    }
}

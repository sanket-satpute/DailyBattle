package com.sanket_satpute_20.dailybattle.core.analytics

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit & Integration verification of Sprint 20.1 — Analytics Abstraction.
 *
 * Requirements verified:
 * - REQ-AN-001: Analytics must not alter core gameplay behavior.
 * - REQ-AN-002: Official and Practice analytics must remain distinguishable.
 * - REQ-AN-003: Analytics failures must not block gameplay or throw to caller.
 * - REQ-AN-005: Analytics must not collect unnecessary sensitive or personal data.
 * - REQ-AN-010: Analytics must be independently testable.
 * - REQ-AN-011: Analytics provider implementation must remain isolated.
 * - REQ-AN-013: Analytics must not expose sensitive Battle Code or token info.
 * - REQ-AN-014: Development/test analytics must support environment attribution.
 * - REQ-AN-015: Analytics instrumentation must not become a dependency for gameplay.
 */
class AnalyticsAbstractionTest {

    private lateinit var fakeEnvironmentProvider: FakeEnvironmentProvider
    private lateinit var inMemoryProvider: InMemoryAnalyticsProvider
    private lateinit var tracker: DefaultAnalyticsTracker

    private class FakeEnvironmentProvider(
        override var current: AppEnvironment = AppEnvironment.Development
    ) : EnvironmentProvider

    @Before
    fun setup() {
        fakeEnvironmentProvider = FakeEnvironmentProvider(AppEnvironment.Development)
        inMemoryProvider = InMemoryAnalyticsProvider()
        tracker = DefaultAnalyticsTracker(
            environmentProvider = fakeEnvironmentProvider,
            initialProviders = listOf(inMemoryProvider)
        )
    }

    // ============================================================================
    // 1. AnalyticsEvent Model Tests
    // ============================================================================

    @Test
    fun `event - valid event names are accepted`() {
        val validNames = listOf(
            "app_opened",
            "battle_started",
            "challenge_completed",
            "practice_viewed",
            "results_viewed",
            "user_action_1"
        )
        for (name in validNames) {
            assertTrue(AnalyticsEvent.isValidEventName(name))
            val event = AnalyticsEvent(name = name)
            assertEquals(name, event.name)
        }
    }

    @Test
    fun `event - invalid event names throw IllegalArgumentException in constructor`() {
        val invalidNames = listOf(
            "AppOpened",
            "battle started",
            "battle-started",
            "battle.started",
            "",
            "   ",
            "play_button_clicked!"
        )
        for (name in invalidNames) {
            assertFalse(AnalyticsEvent.isValidEventName(name))
            assertThrows(IllegalArgumentException::class.java) {
                AnalyticsEvent(name = name)
            }
        }
    }

    @Test
    fun `event - withProperty and withProperties return immutable copies`() {
        val base = AnalyticsEvent("battle_started", mapOf("mode" to "official"))
        val updated = base.withProperty("session_id", "sess-123")
        val batchUpdated = updated.withProperties(mapOf("difficulty" to "standard", "index" to 1))

        assertEquals(mapOf("mode" to "official"), base.properties)
        assertEquals(mapOf("mode" to "official", "session_id" to "sess-123"), updated.properties)
        assertEquals(4, batchUpdated.properties.size)
        assertEquals(1, batchUpdated.properties["index"])
    }

    @Test
    fun `event - createSanitized strips forbidden sensitive keys`() {
        val rawProperties = mapOf(
            "battle_session_id" to "session-999",
            "password" to "secret_pass",
            "token" to "bearer_xyz",
            "battle_code" to "CODE456",
            "auth_token" to "auth_abc",
            "score" to 300
        )
        val sanitized = AnalyticsEvent.createSanitized("battle_completed", rawProperties)

        assertEquals("battle_completed", sanitized.name)
        assertEquals("session-999", sanitized.properties["battle_session_id"])
        assertEquals(300, sanitized.properties["score"])
        assertFalse(sanitized.properties.containsKey("password"))
        assertFalse(sanitized.properties.containsKey("token"))
        assertFalse(sanitized.properties.containsKey("battle_code"))
        assertFalse(sanitized.properties.containsKey("auth_token"))
    }

    // ============================================================================
    // 2. Provider Implementations Tests
    // ============================================================================

    @Test
    fun `noop provider - does not record or throw`() {
        val noop = NoOpAnalyticsProvider()
        assertEquals("NoOp", noop.name)
        assertFalse(noop.isEnabled)

        // Calling methods must not crash
        val event = AnalyticsEvent("app_opened")
        noop.logEvent(event)
        noop.setUserProperty("theme", "dark")
        noop.setUserId("user-1")
    }

    @Test
    fun `inMemory provider - records events and user properties accurately`() {
        val provider = InMemoryAnalyticsProvider()
        assertTrue(provider.isEnabled)

        val event1 = AnalyticsEvent("app_opened")
        val event2 = AnalyticsEvent("battle_started", mapOf("mode" to "official"))
        val event3 = AnalyticsEvent("battle_completed", mapOf("score" to 600))

        provider.logEvent(event1)
        provider.logEvent(event2)
        provider.logEvent(event3)

        assertEquals(3, provider.getRecordedEvents().size)
        assertEquals(listOf(event1, event2, event3), provider.getRecordedEvents())
        assertEquals(listOf(event2), provider.getEventsByName("battle_started"))

        provider.setUserProperty("preferred_theme", "oled")
        provider.setUserId("user-100")
        assertEquals("oled", provider.getUserProperties()["preferred_theme"])
        assertEquals("user-100", provider.getUserId())

        provider.clear()
        assertTrue(provider.getRecordedEvents().isEmpty())
        assertTrue(provider.getUserProperties().isEmpty())
        assertNull(provider.getUserId())
    }

    @Test
    fun `inMemory provider - ignores events when disabled`() {
        val provider = InMemoryAnalyticsProvider(isEnabled = false)
        assertFalse(provider.isEnabled)

        provider.logEvent(AnalyticsEvent("app_opened"))
        provider.setUserProperty("key", "value")
        provider.setUserId("u-1")

        assertTrue(provider.getRecordedEvents().isEmpty())
        assertTrue(provider.getUserProperties().isEmpty())
        assertNull(provider.getUserId())
    }

    @Test
    fun `logging provider - dispatches formatted message to logger`() {
        val logs = mutableListOf<String>()
        val logger = LoggingAnalyticsProvider(logOutput = { logs.add(it) })

        val event = AnalyticsEvent("battle_started", mapOf("mode" to "official"))
        logger.logEvent(event)
        logger.setUserProperty("tier", "pro")
        logger.setUserId("user-xyz")

        assertEquals(3, logs.size)
        assertTrue(logs[0].contains("battle_started"))
        assertTrue(logs[1].contains("tier = pro"))
        assertTrue(logs[2].contains("UserId: user-xyz"))
    }

    // ============================================================================
    // 3. DefaultAnalyticsTracker Integration & Guardrails
    // ============================================================================

    @Test
    fun `tracker - registers and dispatches to multiple providers`() {
        val provider2 = InMemoryAnalyticsProvider(name = "InMemory2")
        tracker.registerProvider(provider2)

        val event = AnalyticsEvent("home_viewed")
        tracker.track(event)

        assertEquals(1, inMemoryProvider.getRecordedEvents().size)
        assertEquals(1, provider2.getRecordedEvents().size)
        assertEquals("home_viewed", inMemoryProvider.getRecordedEvents().first().name)
        assertEquals("home_viewed", provider2.getRecordedEvents().first().name)
    }

    @Test
    fun `tracker - replaces provider with same name`() {
        val replacement = InMemoryAnalyticsProvider(name = "InMemory")
        tracker.registerProvider(replacement)

        assertEquals(1, tracker.getProviders().size)
        tracker.track("home_viewed")

        assertEquals(0, inMemoryProvider.getRecordedEvents().size)
        assertEquals(1, replacement.getRecordedEvents().size)
    }

    @Test
    fun `tracker - unregisters provider`() {
        tracker.unregisterProvider("InMemory")
        assertTrue(tracker.getProviders().isEmpty())

        tracker.track("home_viewed")
        assertTrue(inMemoryProvider.getRecordedEvents().isEmpty())
    }

    @Test
    fun `tracker - enriches event with ambient environment (REQ-AN-014)`() {
        fakeEnvironmentProvider.current = AppEnvironment.Production
        tracker.track("app_opened")

        val recorded = inMemoryProvider.getRecordedEvents().first()
        assertEquals("production", recorded.properties[AnalyticsEvent.PARAM_ENVIRONMENT])
    }

    @Test
    fun `tracker - does not overwrite explicitly supplied environment`() {
        fakeEnvironmentProvider.current = AppEnvironment.Production
        val explicitEvent = AnalyticsEvent(
            name = "app_opened",
            properties = mapOf(AnalyticsEvent.PARAM_ENVIRONMENT to "staging_custom")
        )
        tracker.track(explicitEvent)

        val recorded = inMemoryProvider.getRecordedEvents().first()
        assertEquals("staging_custom", recorded.properties[AnalyticsEvent.PARAM_ENVIRONMENT])
    }

    @Test
    fun `tracker - sanitizes sensitive keys on tracking (REQ-AN-005, REQ-AN-013)`() {
        tracker.track(
            "battle_completed",
            mapOf(
                "score" to 750,
                "password" to "forbidden123",
                "battle_code" to "ABCXYZ",
                "token" to "secret_token",
                "mode" to "official"
            )
        )

        val recorded = inMemoryProvider.getRecordedEvents().first()
        assertEquals(750, recorded.properties["score"])
        assertEquals("official", recorded.properties["mode"])
        assertFalse(recorded.properties.containsKey("password"))
        assertFalse(recorded.properties.containsKey("battle_code"))
        assertFalse(recorded.properties.containsKey("token"))
    }

    @Test
    fun `tracker - strips sensitive keys from setUserProperty`() {
        tracker.setUserProperty("password", "should_not_save")
        tracker.setUserProperty("token", "should_not_save")
        tracker.setUserProperty("theme", "midnight")

        assertFalse(inMemoryProvider.getUserProperties().containsKey("password"))
        assertFalse(inMemoryProvider.getUserProperties().containsKey("token"))
        assertEquals("midnight", inMemoryProvider.getUserProperties()["theme"])
    }

    @Test
    fun `tracker - failure isolation REQ-AN-003 - crashing provider does not throw or block other providers`() {
        val crashingProvider = object : AnalyticsProvider {
            override val name: String = "Crashing"
            override val isEnabled: Boolean = true
            override fun logEvent(event: AnalyticsEvent) {
                throw RuntimeException("Provider internal network failure!")
            }
            override fun setUserProperty(name: String, value: String?) {
                throw IllegalStateException("Provider user property crashed!")
            }
            override fun setUserId(userId: String?) {
                throw NullPointerException("Provider setUserId crashed!")
            }
        }

        tracker.registerProvider(crashingProvider)

        // Tracking must not throw exception!
        tracker.track("battle_started", mapOf("mode" to "official"))
        tracker.setUserProperty("rank", "gold")
        tracker.setUserId("user-safe")

        // Sibling provider must have received the event normally
        val recorded = inMemoryProvider.getRecordedEvents()
        assertEquals(1, recorded.size)
        assertEquals("battle_started", recorded.first().name)
        assertEquals("gold", inMemoryProvider.getUserProperties()["rank"])
        assertEquals("user-safe", inMemoryProvider.getUserId())
    }

    @Test
    fun `tracker - convenience track drops invalid names safely without throwing`() {
        tracker.track("INVALID NAME WITH SPACES", mapOf("data" to 1))
        tracker.track("UPPERCASE", mapOf("data" to 2))
        tracker.track("", mapOf("data" to 3))

        assertTrue(inMemoryProvider.getRecordedEvents().isEmpty())
    }

    // ============================================================================
    // 4. Official vs Practice Separation (REQ-AN-002)
    // ============================================================================

    @Test
    fun `official vs practice - mode distinction is preserved without contamination`() {
        tracker.track(
            "battle_completed",
            mapOf(
                AnalyticsEvent.PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_OFFICIAL,
                "score" to 850
            )
        )
        tracker.track(
            "practice_completed",
            mapOf(
                AnalyticsEvent.PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_PRACTICE,
                "score" to 900
            )
        )

        val events = inMemoryProvider.getRecordedEvents()
        assertEquals(2, events.size)

        val officialEvent = events.first { it.name == "battle_completed" }
        val practiceEvent = events.first { it.name == "practice_completed" }

        assertEquals(AnalyticsEvent.VALUE_MODE_OFFICIAL, officialEvent.properties[AnalyticsEvent.PARAM_BATTLE_MODE])
        assertEquals(AnalyticsEvent.VALUE_MODE_PRACTICE, practiceEvent.properties[AnalyticsEvent.PARAM_BATTLE_MODE])
    }

    // ============================================================================
    // 5. Concurrency / Thread Safety
    // ============================================================================

    @Test
    fun `tracker - concurrent tracking from multiple coroutines executes safely`() = runBlocking {
        val numCoroutines = 20
        val eventsPerCoroutine = 50

        val jobs = List(numCoroutines) { coroutineIndex ->
            launch(Dispatchers.Default) {
                for (i in 0 until eventsPerCoroutine) {
                    tracker.track(
                        "battle_progress",
                        mapOf("coroutine" to coroutineIndex, "iteration" to i)
                    )
                }
            }
        }

        jobs.joinAll()

        val totalRecorded = inMemoryProvider.getRecordedEvents().size
        assertEquals(numCoroutines * eventsPerCoroutine, totalRecorded)
    }
}

package com.sanket_satpute_20.dailybattle.core.analytics

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
 * Verification of Sprint 20.2 — Core Events.
 *
 * Verifies approved events across all 11 categories:
 * - App open
 * - Onboarding
 * - Battle
 * - Challenges
 * - Results
 * - Rival
 * - Friends
 * - Practice
 * - Errors
 * - Network
 * - Performance
 *
 * Invariants tested:
 * - REQ-AN-001: Observational only.
 * - REQ-AN-002: Official vs Practice data separated.
 * - REQ-AN-003: Analytics failures never crash or block.
 * - REQ-AN-005 & REQ-AN-013: Privacy & data minimization (no passwords, tokens, raw codes).
 * - REQ-AN-006 & REQ-AN-007: Official battle and challenge completion measurability.
 * - REQ-AN-008: Abandonment observable without speculative user intent.
 * - REQ-AN-010: Independently testable.
 */
class CoreAnalyticsEventsTest {

    private lateinit var inMemoryProvider: InMemoryAnalyticsProvider
    private lateinit var tracker: DefaultAnalyticsTracker

    private class FakeEnv(override var current: AppEnvironment = AppEnvironment.Development) : EnvironmentProvider

    @Before
    fun setup() {
        inMemoryProvider = InMemoryAnalyticsProvider()
        tracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(inMemoryProvider)
        )
    }

    // ============================================================================
    // 1. App Open & Lifecycle Events
    // ============================================================================

    @Test
    fun `category 1 - app opened event has valid name and attributes`() {
        val event = CoreAnalyticsEvents.appOpened(appVersion = "1.0.0")
        assertEquals(CoreAnalyticsEvents.EVENT_APP_OPENED, event.name)
        assertEquals("1.0.0", event.properties[AnalyticsEvent.PARAM_APP_VERSION])

        val validation = AnalyticsEventRegistry.validateEvent(event)
        assertEquals(EventValidationResult.Valid, validation)

        tracker.trackAppOpened("1.0.0")
        assertEquals(1, inMemoryProvider.getRecordedEvents().size)
    }

    @Test
    fun `category 1 - app backgrounded and foregrounded events`() {
        val bg = CoreAnalyticsEvents.appBackgrounded()
        val fg = CoreAnalyticsEvents.appForegrounded()

        assertEquals(CoreAnalyticsEvents.EVENT_APP_BACKGROUNDED, bg.name)
        assertEquals(CoreAnalyticsEvents.EVENT_APP_FOREGROUNDED, fg.name)

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(bg))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(fg))

        tracker.trackAppBackgrounded()
        tracker.trackAppForegrounded()
        assertEquals(2, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 2. Onboarding Events
    // ============================================================================

    @Test
    fun `category 2 - onboarding events lifecycle`() {
        val welcome = CoreAnalyticsEvents.welcomeViewed()
        val nameStarted = CoreAnalyticsEvents.battleNameStarted()
        val nameCompleted = CoreAnalyticsEvents.battleNameCompleted()
        val completed = CoreAnalyticsEvents.onboardingCompleted()

        assertEquals(CoreAnalyticsEvents.EVENT_WELCOME_VIEWED, welcome.name)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_NAME_STARTED, nameStarted.name)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_NAME_COMPLETED, nameCompleted.name)
        assertEquals(CoreAnalyticsEvents.EVENT_ONBOARDING_COMPLETED, completed.name)

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(welcome))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(nameStarted))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(nameCompleted))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(completed))

        tracker.trackWelcomeViewed()
        tracker.trackBattleNameStarted()
        tracker.trackBattleNameCompleted()
        tracker.trackOnboardingCompleted()
        assertEquals(4, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 3. Battle Events
    // ============================================================================

    @Test
    fun `category 3 - battle viewed started completed and abandoned`() {
        val viewed = CoreAnalyticsEvents.battleViewed("battle-101")
        val started = CoreAnalyticsEvents.battleStarted("battle-101", "sess-1")
        val completed = CoreAnalyticsEvents.battleCompleted("battle-101", "sess-1", totalScore = 750, durationMs = 45000L)
        val abandoned = CoreAnalyticsEvents.battleAbandoned("battle-101", "sess-1", reason = "user_exit")

        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_VIEWED, viewed.name)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_STARTED, started.name)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED, completed.name)
        assertEquals(CoreAnalyticsEvents.EVENT_BATTLE_ABANDONED, abandoned.name)

        assertEquals("battle-101", completed.properties[CoreAnalyticsEvents.PARAM_BATTLE_ID])
        assertEquals("sess-1", completed.properties[CoreAnalyticsEvents.PARAM_SESSION_ID])
        assertEquals(750, completed.properties[CoreAnalyticsEvents.PARAM_TOTAL_SCORE])
        assertEquals(45000L, completed.properties[CoreAnalyticsEvents.PARAM_DURATION_MS])
        assertEquals(AnalyticsEvent.VALUE_MODE_OFFICIAL, completed.properties[CoreAnalyticsEvents.PARAM_BATTLE_MODE])

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(viewed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(started))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(completed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(abandoned))

        tracker.trackBattleViewed("battle-101")
        tracker.trackBattleStarted("battle-101", "sess-1")
        tracker.trackBattleCompleted("battle-101", "sess-1", 750, durationMs = 45000L)
        tracker.trackBattleAbandoned("battle-101", "sess-1", reason = "user_exit")
        assertEquals(4, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 4. Challenges Events
    // ============================================================================

    @Test
    fun `category 4 - challenge lifecycle events`() {
        val started = CoreAnalyticsEvents.challengeStarted("sess-1", "snap", 1)
        val completed = CoreAnalyticsEvents.challengeCompleted("sess-1", "snap", 1, score = 250, durationMs = 15000L)
        val abandoned = CoreAnalyticsEvents.challengeAbandoned("sess-1", "shift", 2, reason = "timeout")

        assertEquals(CoreAnalyticsEvents.EVENT_CHALLENGE_STARTED, started.name)
        assertEquals(CoreAnalyticsEvents.EVENT_CHALLENGE_COMPLETED, completed.name)
        assertEquals(CoreAnalyticsEvents.EVENT_CHALLENGE_ABANDONED, abandoned.name)

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(started))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(completed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(abandoned))

        tracker.trackChallengeStarted("sess-1", "snap", 1)
        tracker.trackChallengeCompleted("sess-1", "snap", 1, 250, durationMs = 15000L)
        tracker.trackChallengeAbandoned("sess-1", "shift", 2, reason = "timeout")
        assertEquals(3, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 5. Results Events
    // ============================================================================

    @Test
    fun `category 5 - results viewed, score revealed, rival comparison, and share`() {
        val results = CoreAnalyticsEvents.resultsViewed("battle-101", "sess-1", totalScore = 750, percentile = 92.5)
        val revealed = CoreAnalyticsEvents.scoreRevealed("crowd_call", 280)
        val comparison = CoreAnalyticsEvents.rivalComparisonViewed(userScore = 750, rivalScore = 710, scoreGap = 40)
        val share = CoreAnalyticsEvents.shareStarted("battle-101", totalScore = 750)

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(results))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(revealed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(comparison))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(share))

        tracker.trackResultsViewed("battle-101", "sess-1", 750, 92.5)
        tracker.trackScoreRevealed("crowd_call", 280)
        tracker.trackRivalComparisonViewed(750, 710, 40)
        tracker.trackShareStarted("battle-101", 750)
        assertEquals(4, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 6. Rival Events
    // ============================================================================

    @Test
    fun `category 6 - rival viewed and rival battle started`() {
        val viewed = CoreAnalyticsEvents.rivalViewed("rival-42", scoreGap = 35)
        val battleStarted = CoreAnalyticsEvents.rivalBattleStarted("rival-42")

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(viewed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(battleStarted))

        tracker.trackRivalViewed("rival-42", 35)
        tracker.trackRivalBattleStarted("rival-42")
        assertEquals(2, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 7. Friends Events (REQ-AN-013: Data Minimization)
    // ============================================================================

    @Test
    fun `category 7 - friends viewed, add started, friend added, and failed`() {
        val viewed = CoreAnalyticsEvents.friendsViewed()
        val addStarted = CoreAnalyticsEvents.addFriendStarted()
        val added = CoreAnalyticsEvents.friendAdded(status = "accepted")
        val failed = CoreAnalyticsEvents.friendRequestFailed(errorCode = "code_not_found")

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(viewed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(addStarted))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(added))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(failed))

        // Ensure raw battle codes are NOT stored
        assertFalse(added.properties.containsKey("battle_code"))
        assertFalse(failed.properties.containsKey("battle_code"))

        tracker.trackFriendsViewed()
        tracker.trackAddFriendStarted()
        tracker.trackFriendAdded("accepted")
        tracker.trackFriendRequestFailed("code_not_found")
        assertEquals(4, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 8. Practice Events (REQ-AN-002: Mode Separation)
    // ============================================================================

    @Test
    fun `category 8 - practice events always enforce practice battle_mode`() {
        val viewed = CoreAnalyticsEvents.practiceViewed()
        val started = CoreAnalyticsEvents.practiceStarted("practice-sess-1")
        val challengeStarted = CoreAnalyticsEvents.practiceChallengeStarted("practice-sess-1", "snap", 1)
        val challengeCompleted = CoreAnalyticsEvents.practiceChallengeCompleted("practice-sess-1", "snap", 1, 300)
        val completed = CoreAnalyticsEvents.practiceCompleted("practice-sess-1", 850, durationMs = 38000L)

        // All must have battle_mode = "practice"
        listOf(viewed, started, challengeStarted, challengeCompleted, completed).forEach { event ->
            assertEquals(AnalyticsEvent.VALUE_MODE_PRACTICE, event.properties[AnalyticsEvent.PARAM_BATTLE_MODE])
            assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(event))
        }

        tracker.trackPracticeViewed()
        tracker.trackPracticeStarted("practice-sess-1")
        tracker.trackPracticeChallengeStarted("practice-sess-1", "snap", 1)
        tracker.trackPracticeChallengeCompleted("practice-sess-1", "snap", 1, 300)
        tracker.trackPracticeCompleted("practice-sess-1", 850, durationMs = 38000L)
        assertEquals(5, inMemoryProvider.getRecordedEvents().size)
    }

    @Test
    fun `category 8 - practice event with official mode fails registry validation (REQ-AN-002)`() {
        // Attempting to forge a practice event with official mode must fail validation
        val invalidPracticeEvent = AnalyticsEvent(
            name = CoreAnalyticsEvents.EVENT_PRACTICE_COMPLETED,
            properties = mapOf(
                CoreAnalyticsEvents.PARAM_SESSION_ID to "sess-1",
                CoreAnalyticsEvents.PARAM_TOTAL_SCORE to 500,
                CoreAnalyticsEvents.PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_OFFICIAL
            )
        )
        val validation = AnalyticsEventRegistry.validateEvent(invalidPracticeEvent)
        assertTrue(validation is EventValidationResult.InvalidMode)
    }

    // ============================================================================
    // 9. Error Events (REQ-AN-005: Sensitive Data Stripping)
    // ============================================================================

    @Test
    fun `category 9 - error events record error code and category without secrets`() {
        val startFailed = CoreAnalyticsEvents.battleStartFailed(errorCategory = "network", errorCode = "ERR_404", retryable = true)
        val challengeFailed = CoreAnalyticsEvents.challengeLoadFailed(challengeType = "shift", errorCode = "ERR_IO", retryable = false)
        val submissionFailed = CoreAnalyticsEvents.battleSubmissionFailed(errorCode = "ERR_TIMEOUT", retryable = true)
        val friendFailed = CoreAnalyticsEvents.friendActionFailed(operation = "send_invite", errorCode = "ERR_LIMIT", retryable = false)
        val dataFailed = CoreAnalyticsEvents.dataLoadFailed(operation = "load_results", errorCode = "ERR_DB", retryable = true)

        listOf(startFailed, challengeFailed, submissionFailed, friendFailed, dataFailed).forEach { event ->
            assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(event))
            // Verify sensitive keys are NOT present
            assertFalse(event.properties.containsKey("token"))
            assertFalse(event.properties.containsKey("password"))
        }

        tracker.trackBattleStartFailed("network", "ERR_404", true)
        tracker.trackChallengeLoadFailed("shift", "ERR_IO", false)
        tracker.trackBattleSubmissionFailed("ERR_TIMEOUT", true)
        tracker.trackFriendActionFailed("send_invite", "ERR_LIMIT", false)
        tracker.trackDataLoadFailed("load_results", "ERR_DB", true)
        assertEquals(5, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 10. Network Events
    // ============================================================================

    @Test
    fun `category 10 - network lifecycle events`() {
        val unavail = CoreAnalyticsEvents.networkUnavailable()
        val timeout = CoreAnalyticsEvents.requestTimeout("/api/battle/submit")
        val failed = CoreAnalyticsEvents.requestFailed("/api/battle/submit", errorCode = "500", retryable = true)
        val recovered = CoreAnalyticsEvents.requestRecovered("/api/battle/submit")

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(unavail))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(timeout))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(failed))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(recovered))

        tracker.trackNetworkUnavailable()
        tracker.trackRequestTimeout("/api/battle/submit")
        tracker.trackRequestFailed("/api/battle/submit", "500", true)
        tracker.trackRequestRecovered("/api/battle/submit")
        assertEquals(4, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 11. Performance Events
    // ============================================================================

    @Test
    fun `category 11 - performance telemetry events`() {
        val startup = CoreAnalyticsEvents.appStartup(durationMs = 450L)
        val screen = CoreAnalyticsEvents.screenLoad("home", durationMs = 120L)
        val battle = CoreAnalyticsEvents.battleLoad(durationMs = 210L)
        val transition = CoreAnalyticsEvents.challengeTransition(fromIndex = 1, toIndex = 2, durationMs = 300L)
        val api = CoreAnalyticsEvents.apiLatency("/api/config", latencyMs = 85L)

        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(startup))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(screen))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(battle))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(transition))
        assertEquals(EventValidationResult.Valid, AnalyticsEventRegistry.validateEvent(api))

        tracker.trackAppStartup(450L)
        tracker.trackScreenLoad("home", 120L)
        tracker.trackBattleLoad(210L)
        tracker.trackChallengeTransition(1, 2, 300L)
        tracker.trackApiLatency("/api/config", 85L)
        assertEquals(5, inMemoryProvider.getRecordedEvents().size)
    }

    // ============================================================================
    // 12. Registry Completeness & Edge Cases
    // ============================================================================

    @Test
    fun `registry - all 11 categories are populated`() {
        for (category in AnalyticsEventRegistry.EventCategory.values()) {
            val events = AnalyticsEventRegistry.getEventsByCategory(category)
            assertTrue("Category $category should have registered events", events.isNotEmpty())
        }
    }

    @Test
    fun `registry - validates missing required property`() {
        val incompleteEvent = AnalyticsEvent(
            name = CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED,
            properties = mapOf(
                CoreAnalyticsEvents.PARAM_BATTLE_ID to "b-1"
                // Missing session_id, total_score, battle_mode
            )
        )
        val validation = AnalyticsEventRegistry.validateEvent(incompleteEvent)
        assertTrue(validation is EventValidationResult.MissingRequiredProperties)
        val missing = (validation as EventValidationResult.MissingRequiredProperties).missing
        assertTrue(missing.contains(CoreAnalyticsEvents.PARAM_SESSION_ID))
        assertTrue(missing.contains(CoreAnalyticsEvents.PARAM_TOTAL_SCORE))
        assertTrue(missing.contains(CoreAnalyticsEvents.PARAM_BATTLE_MODE))
    }

    @Test
    fun `registry - rejects unregistered event`() {
        val unknownEvent = AnalyticsEvent("unknown_random_event")
        val validation = AnalyticsEventRegistry.validateEvent(unknownEvent)
        assertTrue(validation is EventValidationResult.Unregistered)
    }

    @Test
    fun `registry - flags sensitive keys found in event (REQ-AN-005)`() {
        // Constructing raw event with forbidden keys
        val eventWithPassword = AnalyticsEvent(
            name = CoreAnalyticsEvents.EVENT_APP_OPENED,
            properties = mapOf("password" to "secret123")
        )
        val validation = AnalyticsEventRegistry.validateEvent(eventWithPassword)
        assertTrue(validation is EventValidationResult.SensitiveKeysFound)
    }

    // ============================================================================
    // 13. Observational & Failure Isolation (REQ-AN-001, REQ-AN-003)
    // ============================================================================

    @Test
    fun `tracker - all core event extensions execute safely even when provider crashes`() {
        val crashingProvider = object : AnalyticsProvider {
            override val name: String = "Crashing"
            override val isEnabled: Boolean = true
            override fun logEvent(event: AnalyticsEvent) {
                throw RuntimeException("Database error in analytics provider!")
            }
            override fun setUserProperty(name: String, value: String?) {}
            override fun setUserId(userId: String?) {}
        }
        val resilientTracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(crashingProvider, inMemoryProvider)
        )

        // Execute extensions - must not throw exception!
        resilientTracker.trackAppOpened("1.0.0")
        resilientTracker.trackBattleStarted("b-1", "s-1")
        resilientTracker.trackChallengeCompleted("s-1", "snap", 1, 200)
        resilientTracker.trackBattleCompleted("b-1", "s-1", 600)
        resilientTracker.trackPracticeStarted("p-1")
        resilientTracker.trackBattleStartFailed("network", "404", true)

        // InMemory sibling received all events
        assertEquals(6, inMemoryProvider.getRecordedEvents().size)
    }
}

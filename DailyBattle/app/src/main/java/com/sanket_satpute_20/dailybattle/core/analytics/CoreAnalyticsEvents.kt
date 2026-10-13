package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * Authoritative factory functions and event/parameter constants for Core Analytics Events.
 *
 * Implements Sprint 20.2 — Core Events per `docs/ROADMAP.md` and `docs/14_ANALYTICS.md` §8-§41, §74, §88:
 * - App Open / Lifecycle (§9)
 * - Onboarding (§10)
 * - Battle (§12-§14)
 * - Challenges (§15-§17)
 * - Results (§20-§23)
 * - Rival (§24)
 * - Friends (§25-§26)
 * - Practice (§29)
 * - Errors (§38-§39)
 * - Network (§40)
 * - Performance (§74)
 *
 * Core Invariants:
 * - REQ-AN-001: Observational only.
 * - REQ-AN-002: Official vs Practice data separated (`PARAM_BATTLE_MODE`).
 * - REQ-AN-005 & REQ-AN-013: Data minimization — zero passwords, tokens, or raw battle codes.
 */
object CoreAnalyticsEvents {

    // ============================================================================
    // Parameter Keys
    // ============================================================================
    const val PARAM_BATTLE_ID = "battle_id"
    const val PARAM_SESSION_ID = "battle_session_id"
    const val PARAM_BATTLE_MODE = AnalyticsEvent.PARAM_BATTLE_MODE
    const val PARAM_CHALLENGE_TYPE = "challenge_type"
    const val PARAM_CHALLENGE_INDEX = "challenge_index"
    const val PARAM_TOTAL_SCORE = "total_score"
    const val PARAM_SCORE = "score"
    const val PARAM_PERCENTILE = "percentile"
    const val PARAM_DURATION_MS = "duration_ms"
    const val PARAM_REASON = "reason"
    const val PARAM_RIVAL_ID = "rival_id"
    const val PARAM_SCORE_GAP = "score_gap"
    const val PARAM_USER_SCORE = "user_score"
    const val PARAM_RIVAL_SCORE = "rival_score"
    const val PARAM_STATUS = "status"
    const val PARAM_ERROR_CATEGORY = AnalyticsEvent.PARAM_ERROR_CATEGORY
    const val PARAM_ERROR_CODE = AnalyticsEvent.PARAM_ERROR_CODE
    const val PARAM_OPERATION = "operation"
    const val PARAM_RETRYABLE = "retryable"
    const val PARAM_ENDPOINT = "endpoint"
    const val PARAM_SCREEN_NAME = "screen_name"
    const val PARAM_FROM_INDEX = "from_index"
    const val PARAM_TO_INDEX = "to_index"

    // ============================================================================
    // 1. Application Events (§9)
    // ============================================================================
    const val EVENT_APP_OPENED = "app_opened"
    const val EVENT_APP_BACKGROUNDED = "app_backgrounded"
    const val EVENT_APP_FOREGROUNDED = "app_foregrounded"

    fun appOpened(appVersion: String? = null): AnalyticsEvent =
        AnalyticsEvent.createSanitized(
            EVENT_APP_OPENED,
            buildMap {
                if (appVersion != null) put(AnalyticsEvent.PARAM_APP_VERSION, appVersion)
            }
        )

    fun appBackgrounded(): AnalyticsEvent =
        AnalyticsEvent(EVENT_APP_BACKGROUNDED)

    fun appForegrounded(): AnalyticsEvent =
        AnalyticsEvent(EVENT_APP_FOREGROUNDED)

    // ============================================================================
    // 2. Onboarding Events (§10)
    // ============================================================================
    const val EVENT_WELCOME_VIEWED = "welcome_viewed"
    const val EVENT_BATTLE_NAME_STARTED = "battle_name_started"
    const val EVENT_BATTLE_NAME_COMPLETED = "battle_name_completed"
    const val EVENT_ONBOARDING_COMPLETED = "onboarding_completed"

    fun welcomeViewed(): AnalyticsEvent =
        AnalyticsEvent(EVENT_WELCOME_VIEWED)

    fun battleNameStarted(): AnalyticsEvent =
        AnalyticsEvent(EVENT_BATTLE_NAME_STARTED)

    fun battleNameCompleted(): AnalyticsEvent =
        AnalyticsEvent(EVENT_BATTLE_NAME_COMPLETED)

    fun onboardingCompleted(): AnalyticsEvent =
        AnalyticsEvent(EVENT_ONBOARDING_COMPLETED)

    // ============================================================================
    // 3. Battle Events (§12-§14)
    // ============================================================================
    const val EVENT_BATTLE_VIEWED = "battle_viewed"
    const val EVENT_BATTLE_STARTED = "battle_started"
    const val EVENT_BATTLE_COMPLETED = "battle_completed"
    const val EVENT_BATTLE_ABANDONED = "battle_abandoned"

    fun battleViewed(
        battleId: String,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_VIEWED,
        mapOf(
            PARAM_BATTLE_ID to battleId,
            PARAM_BATTLE_MODE to mode
        )
    )

    fun battleStarted(
        battleId: String,
        sessionId: String,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_STARTED,
        mapOf(
            PARAM_BATTLE_ID to battleId,
            PARAM_SESSION_ID to sessionId,
            PARAM_BATTLE_MODE to mode
        )
    )

    fun battleCompleted(
        battleId: String,
        sessionId: String,
        totalScore: Int,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
        durationMs: Long? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_COMPLETED,
        buildMap {
            put(PARAM_BATTLE_ID, battleId)
            put(PARAM_SESSION_ID, sessionId)
            put(PARAM_TOTAL_SCORE, totalScore)
            put(PARAM_BATTLE_MODE, mode)
            if (durationMs != null) put(PARAM_DURATION_MS, durationMs)
        }
    )

    fun battleAbandoned(
        battleId: String,
        sessionId: String,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
        reason: String? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_ABANDONED,
        buildMap {
            put(PARAM_BATTLE_ID, battleId)
            put(PARAM_SESSION_ID, sessionId)
            put(PARAM_BATTLE_MODE, mode)
            if (reason != null) put(PARAM_REASON, reason)
        }
    )

    // ============================================================================
    // 4. Challenge Events (§15-§17)
    // ============================================================================
    const val EVENT_CHALLENGE_STARTED = "challenge_started"
    const val EVENT_CHALLENGE_COMPLETED = "challenge_completed"
    const val EVENT_CHALLENGE_ABANDONED = "challenge_abandoned"

    fun challengeStarted(
        sessionId: String,
        challengeType: String,
        challengeIndex: Int,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_CHALLENGE_STARTED,
        mapOf(
            PARAM_SESSION_ID to sessionId,
            PARAM_CHALLENGE_TYPE to challengeType,
            PARAM_CHALLENGE_INDEX to challengeIndex,
            PARAM_BATTLE_MODE to mode
        )
    )

    fun challengeCompleted(
        sessionId: String,
        challengeType: String,
        challengeIndex: Int,
        score: Int,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
        durationMs: Long? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_CHALLENGE_COMPLETED,
        buildMap {
            put(PARAM_SESSION_ID, sessionId)
            put(PARAM_CHALLENGE_TYPE, challengeType)
            put(PARAM_CHALLENGE_INDEX, challengeIndex)
            put(PARAM_SCORE, score)
            put(PARAM_BATTLE_MODE, mode)
            if (durationMs != null) put(PARAM_DURATION_MS, durationMs)
        }
    )

    fun challengeAbandoned(
        sessionId: String,
        challengeType: String,
        challengeIndex: Int,
        mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
        reason: String? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_CHALLENGE_ABANDONED,
        buildMap {
            put(PARAM_SESSION_ID, sessionId)
            put(PARAM_CHALLENGE_TYPE, challengeType)
            put(PARAM_CHALLENGE_INDEX, challengeIndex)
            put(PARAM_BATTLE_MODE, mode)
            if (reason != null) put(PARAM_REASON, reason)
        }
    )

    // ============================================================================
    // 5. Results Events (§20-§23)
    // ============================================================================
    const val EVENT_RESULTS_VIEWED = "results_viewed"
    const val EVENT_SCORE_REVEALED = "score_revealed"
    const val EVENT_RIVAL_COMPARISON_VIEWED = "rival_comparison_viewed"
    const val EVENT_SHARE_STARTED = "share_started"

    fun resultsViewed(
        battleId: String,
        sessionId: String,
        totalScore: Int,
        percentile: Double? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_RESULTS_VIEWED,
        buildMap {
            put(PARAM_BATTLE_ID, battleId)
            put(PARAM_SESSION_ID, sessionId)
            put(PARAM_TOTAL_SCORE, totalScore)
            if (percentile != null) put(PARAM_PERCENTILE, percentile)
        }
    )

    fun scoreRevealed(
        challengeType: String,
        score: Int
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_SCORE_REVEALED,
        mapOf(
            PARAM_CHALLENGE_TYPE to challengeType,
            PARAM_SCORE to score
        )
    )

    fun rivalComparisonViewed(
        userScore: Int,
        rivalScore: Int,
        scoreGap: Int
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_RIVAL_COMPARISON_VIEWED,
        mapOf(
            PARAM_USER_SCORE to userScore,
            PARAM_RIVAL_SCORE to rivalScore,
            PARAM_SCORE_GAP to scoreGap
        )
    )

    fun shareStarted(
        battleId: String,
        totalScore: Int
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_SHARE_STARTED,
        mapOf(
            PARAM_BATTLE_ID to battleId,
            PARAM_TOTAL_SCORE to totalScore
        )
    )

    // ============================================================================
    // 6. Rival Events (§24)
    // ============================================================================
    const val EVENT_RIVAL_VIEWED = "rival_viewed"
    const val EVENT_RIVAL_BATTLE_STARTED = "rival_battle_started"

    fun rivalViewed(
        rivalId: String? = null,
        scoreGap: Int? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_RIVAL_VIEWED,
        buildMap {
            if (rivalId != null) put(PARAM_RIVAL_ID, rivalId)
            if (scoreGap != null) put(PARAM_SCORE_GAP, scoreGap)
        }
    )

    fun rivalBattleStarted(
        rivalId: String? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_RIVAL_BATTLE_STARTED,
        buildMap {
            if (rivalId != null) put(PARAM_RIVAL_ID, rivalId)
        }
    )

    // ============================================================================
    // 7. Friends Events (§25-§26)
    // Note: NEVER stores raw battle code or personal contact data (REQ-AN-013)
    // ============================================================================
    const val EVENT_FRIENDS_VIEWED = "friends_viewed"
    const val EVENT_ADD_FRIEND_STARTED = "add_friend_started"
    const val EVENT_FRIEND_ADDED = "friend_added"
    const val EVENT_FRIEND_REQUEST_FAILED = "friend_request_failed"

    fun friendsViewed(): AnalyticsEvent =
        AnalyticsEvent(EVENT_FRIENDS_VIEWED)

    fun addFriendStarted(): AnalyticsEvent =
        AnalyticsEvent(EVENT_ADD_FRIEND_STARTED)

    fun friendAdded(
        status: String = "success"
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_FRIEND_ADDED,
        mapOf(PARAM_STATUS to status)
    )

    fun friendRequestFailed(
        errorCode: String? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_FRIEND_REQUEST_FAILED,
        buildMap {
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
        }
    )

    // ============================================================================
    // 8. Practice Events (§29)
    // Mode is ALWAYS "practice" to maintain analytical separation (REQ-AN-002)
    // ============================================================================
    const val EVENT_PRACTICE_VIEWED = "practice_viewed"
    const val EVENT_PRACTICE_STARTED = "practice_started"
    const val EVENT_PRACTICE_CHALLENGE_STARTED = "practice_challenge_started"
    const val EVENT_PRACTICE_CHALLENGE_COMPLETED = "practice_challenge_completed"
    const val EVENT_PRACTICE_COMPLETED = "practice_completed"

    fun practiceViewed(): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_PRACTICE_VIEWED,
        mapOf(PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_PRACTICE)
    )

    fun practiceStarted(
        sessionId: String
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_PRACTICE_STARTED,
        mapOf(
            PARAM_SESSION_ID to sessionId,
            PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_PRACTICE
        )
    )

    fun practiceChallengeStarted(
        sessionId: String,
        challengeType: String,
        challengeIndex: Int
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_PRACTICE_CHALLENGE_STARTED,
        mapOf(
            PARAM_SESSION_ID to sessionId,
            PARAM_CHALLENGE_TYPE to challengeType,
            PARAM_CHALLENGE_INDEX to challengeIndex,
            PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_PRACTICE
        )
    )

    fun practiceChallengeCompleted(
        sessionId: String,
        challengeType: String,
        challengeIndex: Int,
        score: Int
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_PRACTICE_CHALLENGE_COMPLETED,
        mapOf(
            PARAM_SESSION_ID to sessionId,
            PARAM_CHALLENGE_TYPE to challengeType,
            PARAM_CHALLENGE_INDEX to challengeIndex,
            PARAM_SCORE to score,
            PARAM_BATTLE_MODE to AnalyticsEvent.VALUE_MODE_PRACTICE
        )
    )

    fun practiceCompleted(
        sessionId: String,
        totalScore: Int,
        durationMs: Long? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_PRACTICE_COMPLETED,
        buildMap {
            put(PARAM_SESSION_ID, sessionId)
            put(PARAM_TOTAL_SCORE, totalScore)
            put(PARAM_BATTLE_MODE, AnalyticsEvent.VALUE_MODE_PRACTICE)
            if (durationMs != null) put(PARAM_DURATION_MS, durationMs)
        }
    )

    // ============================================================================
    // 9. Error Events (§38-§39)
    // Strips credentials/secrets automatically (REQ-AN-005)
    // ============================================================================
    const val EVENT_BATTLE_START_FAILED = "battle_start_failed"
    const val EVENT_CHALLENGE_LOAD_FAILED = "challenge_load_failed"
    const val EVENT_BATTLE_SUBMISSION_FAILED = "battle_submission_failed"
    const val EVENT_FRIEND_ACTION_FAILED = "friend_action_failed"
    const val EVENT_DATA_LOAD_FAILED = "data_load_failed"

    fun battleStartFailed(
        errorCategory: String? = null,
        errorCode: String? = null,
        retryable: Boolean = false
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_START_FAILED,
        buildMap {
            if (errorCategory != null) put(PARAM_ERROR_CATEGORY, errorCategory)
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
            put(PARAM_RETRYABLE, retryable)
        }
    )

    fun challengeLoadFailed(
        challengeType: String? = null,
        errorCode: String? = null,
        retryable: Boolean = false
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_CHALLENGE_LOAD_FAILED,
        buildMap {
            if (challengeType != null) put(PARAM_CHALLENGE_TYPE, challengeType)
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
            put(PARAM_RETRYABLE, retryable)
        }
    )

    fun battleSubmissionFailed(
        errorCode: String? = null,
        retryable: Boolean = false
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_SUBMISSION_FAILED,
        buildMap {
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
            put(PARAM_RETRYABLE, retryable)
        }
    )

    fun friendActionFailed(
        operation: String? = null,
        errorCode: String? = null,
        retryable: Boolean = false
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_FRIEND_ACTION_FAILED,
        buildMap {
            if (operation != null) put(PARAM_OPERATION, operation)
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
            put(PARAM_RETRYABLE, retryable)
        }
    )

    fun dataLoadFailed(
        operation: String? = null,
        errorCode: String? = null,
        retryable: Boolean = false
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_DATA_LOAD_FAILED,
        buildMap {
            if (operation != null) put(PARAM_OPERATION, operation)
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
            put(PARAM_RETRYABLE, retryable)
        }
    )

    // ============================================================================
    // 10. Network Events (§40)
    // ============================================================================
    const val EVENT_NETWORK_UNAVAILABLE = "network_unavailable"
    const val EVENT_REQUEST_TIMEOUT = "request_timeout"
    const val EVENT_REQUEST_FAILED = "request_failed"
    const val EVENT_REQUEST_RECOVERED = "request_recovered"

    fun networkUnavailable(): AnalyticsEvent =
        AnalyticsEvent(EVENT_NETWORK_UNAVAILABLE)

    fun requestTimeout(
        endpoint: String? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_REQUEST_TIMEOUT,
        buildMap {
            if (endpoint != null) put(PARAM_ENDPOINT, endpoint)
        }
    )

    fun requestFailed(
        endpoint: String? = null,
        errorCode: String? = null,
        retryable: Boolean = false
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_REQUEST_FAILED,
        buildMap {
            if (endpoint != null) put(PARAM_ENDPOINT, endpoint)
            if (errorCode != null) put(PARAM_ERROR_CODE, errorCode)
            put(PARAM_RETRYABLE, retryable)
        }
    )

    fun requestRecovered(
        endpoint: String? = null
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_REQUEST_RECOVERED,
        buildMap {
            if (endpoint != null) put(PARAM_ENDPOINT, endpoint)
        }
    )

    // ============================================================================
    // 11. Performance Events (§74)
    // ============================================================================
    const val EVENT_APP_STARTUP = "app_startup"
    const val EVENT_SCREEN_LOAD = "screen_load"
    const val EVENT_BATTLE_LOAD = "battle_load"
    const val EVENT_CHALLENGE_TRANSITION = "challenge_transition"
    const val EVENT_API_LATENCY = "api_latency"

    fun appStartup(
        durationMs: Long
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_APP_STARTUP,
        mapOf(PARAM_DURATION_MS to durationMs)
    )

    fun screenLoad(
        screenName: String,
        durationMs: Long
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_SCREEN_LOAD,
        mapOf(
            PARAM_SCREEN_NAME to screenName,
            PARAM_DURATION_MS to durationMs
        )
    )

    fun battleLoad(
        durationMs: Long
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_BATTLE_LOAD,
        mapOf(PARAM_DURATION_MS to durationMs)
    )

    fun challengeTransition(
        fromIndex: Int,
        toIndex: Int,
        durationMs: Long
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_CHALLENGE_TRANSITION,
        mapOf(
            PARAM_FROM_INDEX to fromIndex,
            PARAM_TO_INDEX to toIndex,
            PARAM_DURATION_MS to durationMs
        )
    )

    fun apiLatency(
        endpoint: String,
        latencyMs: Long
    ): AnalyticsEvent = AnalyticsEvent.createSanitized(
        EVENT_API_LATENCY,
        mapOf(
            PARAM_ENDPOINT to endpoint,
            PARAM_DURATION_MS to latencyMs
        )
    )
}

package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * Type-safe extension functions on [AnalyticsTracker] for tracking core approved product events.
 */

// 1. App Lifecycle
fun AnalyticsTracker.trackAppOpened(appVersion: String? = null) {
    track(CoreAnalyticsEvents.appOpened(appVersion))
}

fun AnalyticsTracker.trackAppBackgrounded() {
    track(CoreAnalyticsEvents.appBackgrounded())
}

fun AnalyticsTracker.trackAppForegrounded() {
    track(CoreAnalyticsEvents.appForegrounded())
}

// 2. Onboarding
fun AnalyticsTracker.trackWelcomeViewed() {
    track(CoreAnalyticsEvents.welcomeViewed())
}

fun AnalyticsTracker.trackBattleNameStarted() {
    track(CoreAnalyticsEvents.battleNameStarted())
}

fun AnalyticsTracker.trackBattleNameCompleted() {
    track(CoreAnalyticsEvents.battleNameCompleted())
}

fun AnalyticsTracker.trackOnboardingCompleted() {
    track(CoreAnalyticsEvents.onboardingCompleted())
}

// 3. Battle
fun AnalyticsTracker.trackBattleViewed(
    battleId: String,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL
) {
    track(CoreAnalyticsEvents.battleViewed(battleId, mode))
}

fun AnalyticsTracker.trackBattleStarted(
    battleId: String,
    sessionId: String,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL
) {
    track(CoreAnalyticsEvents.battleStarted(battleId, sessionId, mode))
}

fun AnalyticsTracker.trackBattleCompleted(
    battleId: String,
    sessionId: String,
    totalScore: Int,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
    durationMs: Long? = null
) {
    track(CoreAnalyticsEvents.battleCompleted(battleId, sessionId, totalScore, mode, durationMs))
}

fun AnalyticsTracker.trackBattleAbandoned(
    battleId: String,
    sessionId: String,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
    reason: String? = null
) {
    track(CoreAnalyticsEvents.battleAbandoned(battleId, sessionId, mode, reason))
}

// 4. Challenges
fun AnalyticsTracker.trackChallengeStarted(
    sessionId: String,
    challengeType: String,
    challengeIndex: Int,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL
) {
    track(CoreAnalyticsEvents.challengeStarted(sessionId, challengeType, challengeIndex, mode))
}

fun AnalyticsTracker.trackChallengeCompleted(
    sessionId: String,
    challengeType: String,
    challengeIndex: Int,
    score: Int,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
    durationMs: Long? = null
) {
    track(CoreAnalyticsEvents.challengeCompleted(sessionId, challengeType, challengeIndex, score, mode, durationMs))
}

fun AnalyticsTracker.trackChallengeAbandoned(
    sessionId: String,
    challengeType: String,
    challengeIndex: Int,
    mode: String = AnalyticsEvent.VALUE_MODE_OFFICIAL,
    reason: String? = null
) {
    track(CoreAnalyticsEvents.challengeAbandoned(sessionId, challengeType, challengeIndex, mode, reason))
}

// 5. Results
fun AnalyticsTracker.trackResultsViewed(
    battleId: String,
    sessionId: String,
    totalScore: Int,
    percentile: Double? = null
) {
    track(CoreAnalyticsEvents.resultsViewed(battleId, sessionId, totalScore, percentile))
}

fun AnalyticsTracker.trackScoreRevealed(
    challengeType: String,
    score: Int
) {
    track(CoreAnalyticsEvents.scoreRevealed(challengeType, score))
}

fun AnalyticsTracker.trackRivalComparisonViewed(
    userScore: Int,
    rivalScore: Int,
    scoreGap: Int
) {
    track(CoreAnalyticsEvents.rivalComparisonViewed(userScore, rivalScore, scoreGap))
}

fun AnalyticsTracker.trackShareStarted(
    battleId: String,
    totalScore: Int
) {
    track(CoreAnalyticsEvents.shareStarted(battleId, totalScore))
}

// 6. Rival
fun AnalyticsTracker.trackRivalViewed(
    rivalId: String? = null,
    scoreGap: Int? = null
) {
    track(CoreAnalyticsEvents.rivalViewed(rivalId, scoreGap))
}

fun AnalyticsTracker.trackRivalBattleStarted(
    rivalId: String? = null
) {
    track(CoreAnalyticsEvents.rivalBattleStarted(rivalId))
}

// 7. Friends
fun AnalyticsTracker.trackFriendsViewed() {
    track(CoreAnalyticsEvents.friendsViewed())
}

fun AnalyticsTracker.trackAddFriendStarted() {
    track(CoreAnalyticsEvents.addFriendStarted())
}

fun AnalyticsTracker.trackFriendAdded(
    status: String = "success"
) {
    track(CoreAnalyticsEvents.friendAdded(status))
}

fun AnalyticsTracker.trackFriendRequestFailed(
    errorCode: String? = null
) {
    track(CoreAnalyticsEvents.friendRequestFailed(errorCode))
}

// 8. Practice
fun AnalyticsTracker.trackPracticeViewed() {
    track(CoreAnalyticsEvents.practiceViewed())
}

fun AnalyticsTracker.trackPracticeStarted(
    sessionId: String
) {
    track(CoreAnalyticsEvents.practiceStarted(sessionId))
}

fun AnalyticsTracker.trackPracticeChallengeStarted(
    sessionId: String,
    challengeType: String,
    challengeIndex: Int
) {
    track(CoreAnalyticsEvents.practiceChallengeStarted(sessionId, challengeType, challengeIndex))
}

fun AnalyticsTracker.trackPracticeChallengeCompleted(
    sessionId: String,
    challengeType: String,
    challengeIndex: Int,
    score: Int
) {
    track(CoreAnalyticsEvents.practiceChallengeCompleted(sessionId, challengeType, challengeIndex, score))
}

fun AnalyticsTracker.trackPracticeCompleted(
    sessionId: String,
    totalScore: Int,
    durationMs: Long? = null
) {
    track(CoreAnalyticsEvents.practiceCompleted(sessionId, totalScore, durationMs))
}

// 9. Errors
fun AnalyticsTracker.trackBattleStartFailed(
    errorCategory: String? = null,
    errorCode: String? = null,
    retryable: Boolean = false
) {
    track(CoreAnalyticsEvents.battleStartFailed(errorCategory, errorCode, retryable))
}

fun AnalyticsTracker.trackChallengeLoadFailed(
    challengeType: String? = null,
    errorCode: String? = null,
    retryable: Boolean = false
) {
    track(CoreAnalyticsEvents.challengeLoadFailed(challengeType, errorCode, retryable))
}

fun AnalyticsTracker.trackBattleSubmissionFailed(
    errorCode: String? = null,
    retryable: Boolean = false
) {
    track(CoreAnalyticsEvents.battleSubmissionFailed(errorCode, retryable))
}

fun AnalyticsTracker.trackFriendActionFailed(
    operation: String? = null,
    errorCode: String? = null,
    retryable: Boolean = false
) {
    track(CoreAnalyticsEvents.friendActionFailed(operation, errorCode, retryable))
}

fun AnalyticsTracker.trackDataLoadFailed(
    operation: String? = null,
    errorCode: String? = null,
    retryable: Boolean = false
) {
    track(CoreAnalyticsEvents.dataLoadFailed(operation, errorCode, retryable))
}

// 10. Network
fun AnalyticsTracker.trackNetworkUnavailable() {
    track(CoreAnalyticsEvents.networkUnavailable())
}

fun AnalyticsTracker.trackRequestTimeout(
    endpoint: String? = null
) {
    track(CoreAnalyticsEvents.requestTimeout(endpoint))
}

fun AnalyticsTracker.trackRequestFailed(
    endpoint: String? = null,
    errorCode: String? = null,
    retryable: Boolean = false
) {
    track(CoreAnalyticsEvents.requestFailed(endpoint, errorCode, retryable))
}

fun AnalyticsTracker.trackRequestRecovered(
    endpoint: String? = null
) {
    track(CoreAnalyticsEvents.requestRecovered(endpoint))
}

// 11. Performance
fun AnalyticsTracker.trackAppStartup(durationMs: Long) {
    track(CoreAnalyticsEvents.appStartup(durationMs))
}

fun AnalyticsTracker.trackScreenLoad(screenName: String, durationMs: Long) {
    track(CoreAnalyticsEvents.screenLoad(screenName, durationMs))
}

fun AnalyticsTracker.trackBattleLoad(durationMs: Long) {
    track(CoreAnalyticsEvents.battleLoad(durationMs))
}

fun AnalyticsTracker.trackChallengeTransition(fromIndex: Int, toIndex: Int, durationMs: Long) {
    track(CoreAnalyticsEvents.challengeTransition(fromIndex, toIndex, durationMs))
}

fun AnalyticsTracker.trackApiLatency(endpoint: String, latencyMs: Long) {
    track(CoreAnalyticsEvents.apiLatency(endpoint, latencyMs))
}

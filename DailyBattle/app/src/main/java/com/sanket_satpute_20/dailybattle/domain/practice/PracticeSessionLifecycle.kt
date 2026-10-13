package com.sanket_satpute_20.dailybattle.domain.practice

/**
 * Observable lifecycle representation of a Practice Session.
 */
sealed interface PracticeSessionLifecycle {
    data object Created : PracticeSessionLifecycle
    data class Ready(val challengeIndex: Int) : PracticeSessionLifecycle
    data class ChallengeActive(val challengeIndex: Int) : PracticeSessionLifecycle
    data class ChallengeComplete(val challengeIndex: Int) : PracticeSessionLifecycle
    data object PracticeComplete : PracticeSessionLifecycle
    data class Interrupted(val reason: String? = null) : PracticeSessionLifecycle
}

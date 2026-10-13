package com.sanket_satpute_20.dailybattle.domain.practice

/**
 * Domain events that can occur during a practice session.
 */
sealed class PracticeEvent {
    data class StartPractice(val now: Long = System.currentTimeMillis()) : PracticeEvent()
    data class BeginChallenge(val challengeIndex: Int, val now: Long = System.currentTimeMillis()) : PracticeEvent()
    data class FinishChallenge(val challengeIndex: Int, val now: Long = System.currentTimeMillis()) : PracticeEvent()
    data class ContinueNext(val now: Long = System.currentTimeMillis()) : PracticeEvent()
    data class InterruptPractice(val reason: String? = null, val now: Long = System.currentTimeMillis()) : PracticeEvent()
    data class RepeatPractice(val now: Long = System.currentTimeMillis()) : PracticeEvent()
    data class CompletePractice(val now: Long = System.currentTimeMillis()) : PracticeEvent()
}

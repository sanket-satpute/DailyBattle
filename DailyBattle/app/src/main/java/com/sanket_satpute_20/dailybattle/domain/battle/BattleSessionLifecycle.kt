package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Represents the high-level lifecycle of an active BattleSession,
 * as defined in Sprint 7.2 (Battle Session Lifecycle).
 * 
 * States:
 * - Created: Session exists but is not yet ready for gameplay.
 * - Ready: The session is ready to begin a challenge.
 * - Started: The session has officially begun gameplay.
 * - Challenge Active: The user is currently playing a challenge.
 * - Challenge Complete: The current challenge is finished.
 * - Battle Complete: All challenges are finished, pending submission.
 * - Submitted: The result has been saved/submitted.
 */
sealed interface BattleSessionLifecycle {
    data object Created : BattleSessionLifecycle
    data class Ready(val challengeIndex: Int) : BattleSessionLifecycle
    data class Started(val challengeIndex: Int) : BattleSessionLifecycle
    data class ChallengeActive(val challengeIndex: Int) : BattleSessionLifecycle
    data class ChallengeComplete(val challengeIndex: Int) : BattleSessionLifecycle
    data object BattleComplete : BattleSessionLifecycle
    data object Submitted : BattleSessionLifecycle
}

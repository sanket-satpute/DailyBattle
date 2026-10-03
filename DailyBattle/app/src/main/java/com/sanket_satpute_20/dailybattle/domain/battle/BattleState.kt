package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Domain representation of the Battle State Machine states.
 *
 * Per `ROADMAP.md` Sprint 5.3, the states are:
 * - NOT_STARTED
 * - READY
 * - ACTIVE
 * - CHALLENGE_COMPLETE
 * - NEXT_CHALLENGE
 * - BATTLE_COMPLETE
 * - RESULTS
 *
 * The states that occur during a specific challenge include a `challengeIndex` (1, 2, or 3)
 * to clearly associate the state with the ongoing challenge.
 */
sealed interface BattleState {
    data object NotStarted : BattleState
    data class Ready(val challengeIndex: Int) : BattleState
    data class Active(val challengeIndex: Int) : BattleState
    data class ChallengeComplete(val challengeIndex: Int) : BattleState
    data class NextChallenge(val challengeIndex: Int) : BattleState
    data object BattleComplete : BattleState
    data object Results : BattleState
}

package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Domain representation of the Challenge State Machine states.
 *
 * Per `ROADMAP.md` Sprint 5.4 and `02_GAMEPLAY.md` §16, the states are:
 * - READY
 * - ACTIVE
 * - CORRECT
 * - INCORRECT
 * - COMPLETE
 */
sealed interface ChallengeState {
    data object Ready : ChallengeState
    data object Active : ChallengeState
    data object Correct : ChallengeState
    data object Incorrect : ChallengeState
    data object Complete : ChallengeState
}

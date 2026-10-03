package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * State machine managing the official Battle flow.
 *
 * Enforces the progression defined in `ROADMAP.md` Sprint 5.3:
 * NOT_STARTED -> READY -> ACTIVE -> CHALLENGE_COMPLETE -> NEXT_CHALLENGE -> BATTLE_COMPLETE -> RESULTS
 *
 * Provides a deterministic transition function `transition(event)` that returns
 * either a `Success` with the new state, or a `Failure` with `AppError.BattleState`
 * if the transition is invalid for the current state.
 */
class BattleStateMachine(initialState: BattleState = BattleState.NotStarted) {

    var currentState: BattleState = initialState
        private set

    /**
     * Attempts to transition to the next state based on the provided [event].
     * If successful, updates [currentState] and returns it.
     * If invalid, returns a domain failure.
     */
    fun transition(event: BattleEvent): DomainResult<BattleState> {
        val nextState = when (currentState) {
            is BattleState.NotStarted -> {
                if (event is BattleEvent.StartBattle) BattleState.Ready(1) else null
            }
            is BattleState.Ready -> {
                val state = currentState as BattleState.Ready
                if (event is BattleEvent.BeginChallenge) BattleState.Active(state.challengeIndex) else null
            }
            is BattleState.Active -> {
                val state = currentState as BattleState.Active
                if (event is BattleEvent.FinishChallenge) BattleState.ChallengeComplete(state.challengeIndex) else null
            }
            is BattleState.ChallengeComplete -> {
                val state = currentState as BattleState.ChallengeComplete
                if (event is BattleEvent.Continue) {
                    if (state.challengeIndex < 3) {
                        BattleState.NextChallenge(state.challengeIndex + 1)
                    } else {
                        BattleState.BattleComplete
                    }
                } else null
            }
            is BattleState.NextChallenge -> {
                val state = currentState as BattleState.NextChallenge
                if (event is BattleEvent.PrepareNext) BattleState.Ready(state.challengeIndex) else null
            }
            is BattleState.BattleComplete -> {
                if (event is BattleEvent.ShowResults) BattleState.Results else null
            }
            is BattleState.Results -> {
                // Terminal state, no further transitions defined in MVP flow
                null
            }
        }

        return if (nextState != null) {
            currentState = nextState
            DomainResult.Success(nextState)
        } else {
            DomainResult.Failure(AppError.BattleState)
        }
    }
}

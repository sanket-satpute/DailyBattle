package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * State machine managing an individual challenge's gameplay flow.
 *
 * Enforces the progression defined in `ROADMAP.md` Sprint 5.4 and `02_GAMEPLAY.md` §16:
 * READY -> ACTIVE -> CORRECT/INCORRECT -> COMPLETE
 *
 * Provides a deterministic transition function `transition(event)` that returns
 * either a `Success` with the new state, or a `Failure` with `AppError.Domain`
 * if the transition is invalid for the current state.
 */
class ChallengeStateMachine(initialState: ChallengeState = ChallengeState.Ready) {

    var currentState: ChallengeState = initialState
        private set

    /**
     * Attempts to transition to the next state based on the provided [event].
     * If successful, updates [currentState] and returns it.
     * If invalid, returns a domain failure.
     */
    fun transition(event: ChallengeEvent): DomainResult<ChallengeState> {
        val nextState = when (currentState) {
            is ChallengeState.Ready -> {
                if (event is ChallengeEvent.Start) ChallengeState.Active else null
            }
            is ChallengeState.Active -> {
                when (event) {
                    is ChallengeEvent.SubmitCorrect -> ChallengeState.Correct
                    is ChallengeEvent.SubmitIncorrect -> ChallengeState.Incorrect
                    is ChallengeEvent.Finish -> ChallengeState.Complete
                    else -> null
                }
            }
            is ChallengeState.Correct, is ChallengeState.Incorrect -> {
                when (event) {
                    is ChallengeEvent.Continue -> ChallengeState.Active
                    is ChallengeEvent.Finish -> ChallengeState.Complete
                    else -> null
                }
            }
            is ChallengeState.Complete -> {
                // Terminal state, no further transitions defined
                null
            }
        }

        return if (nextState != null) {
            currentState = nextState
            DomainResult.Success(nextState)
        } else {
            DomainResult.Failure(AppError.Domain)
        }
    }
}

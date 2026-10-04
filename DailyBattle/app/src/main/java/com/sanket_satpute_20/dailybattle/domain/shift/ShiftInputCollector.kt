package com.sanket_satpute_20.dailybattle.domain.shift

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Collects raw inputs for a Shift challenge.
 *
 * Since DEC-GAME-002 is PENDING, we stub the actual grid sizes and rules,
 * but enforce the conceptual state flow:
 * Observation -> Shifting -> Answer -> ReactionFeedback
 */
class ShiftInputCollector(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) : ShiftEngine {

    private var _currentState: ShiftState = ShiftState.NotStarted
    override val currentState: ShiftState
        get() = _currentState

    private var challengeStartTime: Long? = null
    private val selections = mutableListOf<ShiftSelectionRecord>()
    private var roundsCompleted = 0
    private var challengeDurationMs: Long = 0L
    private val currentGrid = ShiftGrid(emptyList()) // Stub grid

    override fun prepare(): DomainResult<ShiftState> {
        if (_currentState !is ShiftState.NotStarted) {
            return DomainResult.Failure(AppError.BattleState)
        }
        // Normally we'd generate the grid here.
        _currentState = ShiftState.Observation(currentGrid)
        return DomainResult.Success(_currentState)
    }

    override fun start(): DomainResult<ShiftState> {
        if (_currentState !is ShiftState.Observation) {
            return DomainResult.Failure(AppError.BattleState)
        }
        challengeStartTime = timeProvider()
        return DomainResult.Success(_currentState)
    }

    override fun handleInput(event: ShiftEvent): DomainResult<ShiftState> {
        when (event) {
            ShiftEvent.Start -> {
                return start()
            }
            ShiftEvent.BeginShift -> {
                if (_currentState !is ShiftState.Observation) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                _currentState = ShiftState.Shifting(currentGrid)
            }
            ShiftEvent.PresentAnswer -> {
                if (_currentState !is ShiftState.Shifting) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                _currentState = ShiftState.Answer(currentGrid)
            }
            is ShiftEvent.TapCell -> {
                if (_currentState !is ShiftState.Answer) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                
                selections.add(ShiftSelectionRecord(event.cellId, event.timestampMs))
                
                // Transition to feedback. We assume one guess per round for the input collector's basic flow.
                // Normally scoring decides correctness.
                _currentState = ShiftState.ReactionFeedback(isCorrect = true) // Stubbed as true
            }
            ShiftEvent.Timeout -> {
                if (_currentState !is ShiftState.Answer) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                _currentState = ShiftState.ReactionFeedback(isCorrect = false)
            }
            ShiftEvent.Continue -> {
                if (_currentState !is ShiftState.ReactionFeedback) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                roundsCompleted++
                // Normally we check if total rounds reached, but algorithm is pending.
                // We'll just transition back to Observation for the next round.
                _currentState = ShiftState.Observation(currentGrid)
            }
        }
        return DomainResult.Success(_currentState)
    }

    override fun evaluate(): RawShiftResult? {
        if (challengeStartTime == null) return null
        return RawShiftResult(
            roundsCompleted = roundsCompleted,
            selections = selections.toList(),
            challengeDurationMs = challengeDurationMs
        )
    }

    override fun complete(): DomainResult<ShiftState> {
        if (_currentState is ShiftState.Completed) {
            return DomainResult.Failure(AppError.BattleState)
        }
        
        val end = timeProvider()
        challengeDurationMs = if (challengeStartTime != null) end - challengeStartTime!! else 0L
        
        val result = evaluate() ?: return DomainResult.Failure(AppError.BattleState)
        _currentState = ShiftState.Completed(result)
        return DomainResult.Success(_currentState)
    }
}

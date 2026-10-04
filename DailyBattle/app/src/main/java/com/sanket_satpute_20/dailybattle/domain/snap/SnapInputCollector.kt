package com.sanket_satpute_20.dailybattle.domain.snap

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Collects raw inputs for a Snap challenge round.
 * 
 * Since the authoritative scoring algorithm (DEC-GAME-001) is PENDING,
 * this engine does not evaluate correctness or points. It solely records
 * the timestamps of events and validates state transitions to produce a RawSnapResult.
 */
class SnapInputCollector(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) : SnapEngine {

    private var _currentState: SnapState = SnapState.NotStarted
    override val currentState: SnapState
        get() = _currentState

    private var roundStartTime: Long? = null
    private var targetAppearanceTime: Long? = null
    private val taps = mutableListOf<TapRecord>()
    private var targetAppeared = false
    private var roundDurationMs: Long = 0L

    override fun prepare(): DomainResult<SnapState> {
        if (_currentState !is SnapState.NotStarted) {
            return DomainResult.Failure(AppError.BattleState)
        }
        // Normally elements are generated here. Blocked by DEC-GAME-001.
        _currentState = SnapState.Active(emptyList())
        return DomainResult.Success(_currentState)
    }

    override fun start(): DomainResult<SnapState> {
        if (_currentState !is SnapState.Active) {
             return DomainResult.Failure(AppError.BattleState)
        }
        roundStartTime = timeProvider()
        return DomainResult.Success(_currentState)
    }
    
    /**
     * For testing/mocking target appearance since algorithm is pending.
     */
    fun markTargetAppeared(timestamp: Long) {
        targetAppeared = true
        targetAppearanceTime = timestamp
    }

    override fun handleInput(event: SnapEvent): DomainResult<SnapState> {
        when (event) {
            is SnapEvent.Tap -> {
                if (_currentState is SnapState.Completed) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                
                // Record the tap
                taps.add(TapRecord(event.timestampMs, event.elementId))
                
                // We transition to ReactionFeedback.
                // We do NOT score the tap (Blocked by DEC-GAME-001). 
                // We just output generic feedback state to satisfy the lifecycle.
                _currentState = SnapState.ReactionFeedback(isCorrect = targetAppeared)
            }
            is SnapEvent.Timeout -> {
                 if (_currentState is SnapState.Completed) {
                    return DomainResult.Failure(AppError.BattleState)
                 }
                 _currentState = SnapState.ReactionFeedback(isCorrect = false)
            }
            SnapEvent.Continue -> {
                 // Conceptually moving to next round or finishing
                 // Will be fully implemented when rules are defined
            }
            SnapEvent.Start -> {
                 return start()
            }
        }
        return DomainResult.Success(_currentState)
    }

    override fun evaluate(): RawSnapResult? {
        if (roundStartTime == null) return null
        return RawSnapResult(
            targetAppeared = targetAppeared,
            targetAppearanceTimeMs = targetAppearanceTime,
            taps = taps.toList(),
            roundDurationMs = roundDurationMs
        )
    }

    override fun complete(): DomainResult<SnapState> {
        if (_currentState is SnapState.Completed) {
            return DomainResult.Failure(AppError.BattleState)
        }
        
        val end = timeProvider()
        roundDurationMs = if (roundStartTime != null) end - roundStartTime!! else 0L
        
        val result = evaluate() ?: return DomainResult.Failure(AppError.BattleState)
        
        _currentState = SnapState.Completed(result)
        return DomainResult.Success(_currentState)
    }
}

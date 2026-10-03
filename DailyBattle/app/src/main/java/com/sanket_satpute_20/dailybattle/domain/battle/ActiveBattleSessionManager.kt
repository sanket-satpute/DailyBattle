package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveBattleSessionManager @Inject constructor(
    private val repository: BattleSessionRepository
) {
    private var engine: BattleEngine? = null

    private val _lifecycle = MutableStateFlow<BattleSessionLifecycle?>(null)
    val lifecycle: StateFlow<BattleSessionLifecycle?> = _lifecycle.asStateFlow()

    fun bindSession(session: BattleSession) {
        engine = BattleEngine(session)
        updateLifecycleFromEngine()
    }

    fun processBattleEvent(event: BattleEvent): DomainResult<BattleSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processBattleEvent(event)
        
        if (result is DomainResult.Success) {
            updateLifecycleFromEngine()
            // Persist the updated session
            repository.saveSession(result.value)
        }
        
        return result
    }

    fun processChallengeEvent(event: ChallengeEvent): DomainResult<BattleSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processChallengeEvent(event)
        
        if (result is DomainResult.Success) {
            updateLifecycleFromEngine()
            // Persist the updated session
            repository.saveSession(result.value)
        }
        
        return result
    }
    
    fun getActiveSession(): BattleSession? {
        return engine?.session
    }

    private fun updateLifecycleFromEngine() {
        val currentEngine = engine ?: return
        val state = currentEngine.currentBattleState
        val lifecycleState = mapToLifecycle(state, currentEngine.session)
        _lifecycle.value = lifecycleState
    }

    private fun mapToLifecycle(state: BattleState, session: BattleSession): BattleSessionLifecycle {
        return when (state) {
            is BattleState.NotStarted -> BattleSessionLifecycle.Created
            is BattleState.Ready -> BattleSessionLifecycle.Ready(state.challengeIndex)
            is BattleState.Active -> {
                // If it's active but startedAt is just set, we might consider it Started then ChallengeActive.
                // The requirements say "Started" then "Challenge Active". 
                // We'll map Active to ChallengeActive.
                BattleSessionLifecycle.ChallengeActive(state.challengeIndex)
            }
            is BattleState.ChallengeComplete -> BattleSessionLifecycle.ChallengeComplete(state.challengeIndex)
            is BattleState.NextChallenge -> BattleSessionLifecycle.Ready(state.challengeIndex)
            is BattleState.BattleComplete -> BattleSessionLifecycle.BattleComplete
            is BattleState.Results -> BattleSessionLifecycle.Submitted
        }
    }
}

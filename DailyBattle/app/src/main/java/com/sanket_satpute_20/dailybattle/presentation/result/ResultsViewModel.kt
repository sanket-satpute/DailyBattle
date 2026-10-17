package com.sanket_satpute_20.dailybattle.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResultsUiState {
    data object Loading : ResultsUiState()
    data class Success(val summary: BattleResultSummary) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val battleResultRepository: BattleResultRepository,
    private val activeBattleSessionManager: ActiveBattleSessionManager? = null,
    private val completeBattleUseCase: CompleteBattleUseCase? = null,
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadResult(summary: BattleResultSummary) {
        _uiState.value = ResultsUiState.Success(summary)
    }

    fun loadInitialResult(
        defaultUserId: UserId = UserId("mock_user_id"),
        defaultBattleId: BattleId = BattleId("battle_today")
    ) {
        viewModelScope.launch {
            _uiState.value = ResultsUiState.Loading
            val session = activeBattleSessionManager?.getActiveSession()
            val effectiveUserId = session?.userId ?: defaultUserId
            val effectiveBattleId = session?.battleId ?: defaultBattleId

            if (session != null && completeBattleUseCase != null && session.status != BattleSessionStatus.COMPLETED) {
                when (val completeRes = completeBattleUseCase.execute(session)) {
                    is DomainResult.Success -> {
                        _uiState.value = ResultsUiState.Success(
                            BattleResultSummary(
                                result = completeRes.value,
                                previousDayScore = null,
                                scoreImprovement = null,
                                isNewPersonalBest = false,
                                personalBestScore = null,
                                averageScore = null,
                                rivalComparison = null
                            )
                        )
                        return@launch
                    }
                    is DomainResult.Failure -> {
                        // Fall back to querying existing result below
                    }
                }
            }

            loadResult(effectiveUserId, effectiveBattleId)
        }
    }

    fun loadResult(userId: UserId, battleId: BattleId) {
        viewModelScope.launch {
            _uiState.value = ResultsUiState.Loading
            when (val res = battleResultRepository.getBattleResult(userId, battleId)) {
                is DomainResult.Success -> {
                    _uiState.value = ResultsUiState.Success(
                        BattleResultSummary(
                            result = res.value,
                            previousDayScore = null,
                            scoreImprovement = null,
                            isNewPersonalBest = false,
                            personalBestScore = null,
                            averageScore = null,
                            rivalComparison = null
                        )
                    )
                }
                is DomainResult.Failure -> {
                    _uiState.value = ResultsUiState.Error("Could not load battle result.")
                }
            }
        }
    }
}

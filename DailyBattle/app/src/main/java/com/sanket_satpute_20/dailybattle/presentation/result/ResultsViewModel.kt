package com.sanket_satpute_20.dailybattle.presentation.result

import androidx.lifecycle.ViewModel
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.launch

sealed class ResultsUiState {
    data object Loading : ResultsUiState()
    data class Success(val summary: BattleResultSummary) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val battleResultRepository: BattleResultRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadResult(summary: BattleResultSummary) {
        _uiState.value = ResultsUiState.Success(summary)
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

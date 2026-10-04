package com.sanket_satpute_20.dailybattle.presentation.result

import androidx.lifecycle.ViewModel
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class ResultsUiState {
    data object Loading : ResultsUiState()
    data class Success(val summary: BattleResultSummary) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}

@HiltViewModel
class ResultsViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadResult(summary: BattleResultSummary) {
        _uiState.value = ResultsUiState.Success(summary)
    }
}

package com.sanket_satpute_20.dailybattle.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.history.GetHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
data class HistoryUiStateData(
    val best: Int = 0,
    val average: Int = 0,
    val momentum: Int = 0,
    val recentBattles: List<Any> = emptyList(),
    val graphData: List<Any> = emptyList()
)

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data class Success(val data: HistoryUiStateData) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            
            val userName = userRepository.getBattleName()
            val userId = UserId(userName ?: "current-user")
            
            val result = getHistoryUseCase(userId)
            
            if (result is DomainResult.Success) {
                val history = result.value
                if (history.recentBattles.isEmpty()) {
                    _uiState.value = HistoryUiState.Empty
                } else {
                    _uiState.value = HistoryUiState.Success(
                        HistoryUiStateData(
                            best = history.best,
                            average = history.average,
                            momentum = history.momentum,
                            recentBattles = history.recentBattles,
                            graphData = history.graphData
                        )
                    )
                }
            } else {
                _uiState.value = HistoryUiState.Error("Failed to load history.")
            }
        }
    }
}

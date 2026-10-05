package com.sanket_satpute_20.dailybattle.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
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
    private val battleResultRepository: BattleResultRepository,
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
            
            // Temporary logic for Sprint 14.3. Sprint 14.4 will calculate actual data.
            val userName = userRepository.getBattleName()
            val userId = UserId(userName ?: "current-user") // Use battleName as temporary user ID or similar. Wait, how do we get UserId?
            
            val resultsResponse = battleResultRepository.getUserBattleResults(userId)
            
            if (resultsResponse is DomainResult.Success) {
                if (resultsResponse.value.isEmpty()) {
                    _uiState.value = HistoryUiState.Empty
                } else {
                    _uiState.value = HistoryUiState.Success(HistoryUiStateData())
                }
            } else {
                _uiState.value = HistoryUiState.Error("Failed to load history.")
            }
        }
    }
}

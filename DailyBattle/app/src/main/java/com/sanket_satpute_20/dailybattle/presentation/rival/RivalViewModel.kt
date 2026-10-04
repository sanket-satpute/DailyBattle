package com.sanket_satpute_20.dailybattle.presentation.rival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.GetCurrentRivalUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetRivalComparisonUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetRivalMatchHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RivalUiState {
    data object Loading : RivalUiState
    
    data class Success(
        val rivalName: String,
        val rivalScore: Int,
        val userName: String,
        val userScore: Int,
        val gap: Int,
        val direction: ScoreDirection,
        val history: List<RivalMatchHistoryItem>
    ) : RivalUiState
    
    data object NoRival : RivalUiState
    
    data object Error : RivalUiState
}

@HiltViewModel
class RivalViewModel @Inject constructor(
    private val getCurrentRivalUseCase: GetCurrentRivalUseCase,
    private val getRivalComparisonUseCase: GetRivalComparisonUseCase,
    private val getRivalMatchHistoryUseCase: GetRivalMatchHistoryUseCase,
    private val userRepository: UserRepository,
    private val activeBattleSessionManager: ActiveBattleSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<RivalUiState>(RivalUiState.Loading)
    val state: StateFlow<RivalUiState> = _state.asStateFlow()

    init {
        loadRivalData()
    }

    fun loadRivalData() {
        viewModelScope.launch {
            _state.value = RivalUiState.Loading
            
            val userId = UserId("current-user")
            val userName = userRepository.getBattleName() ?: "Player"
            val battleId = activeBattleSessionManager.getActiveSession()?.battleId ?: BattleId("current-battle")

            val rivalResult = getCurrentRivalUseCase.execute(userId)
            
            if (rivalResult is DomainResult.Failure) {
                _state.value = RivalUiState.Error
                return@launch
            }
            
            val rival = (rivalResult as DomainResult.Success).value
            
            if (rival == null) {
                _state.value = RivalUiState.NoRival
                return@launch
            }

            val comparisonResult = getRivalComparisonUseCase.execute(userId, rival.rivalUserId, battleId)
            val historyResult = getRivalMatchHistoryUseCase.execute(userId, rival.rivalUserId)

            if (comparisonResult is DomainResult.Success && historyResult is DomainResult.Success) {
                val comparison = comparisonResult.value
                val history = historyResult.value

                _state.value = RivalUiState.Success(
                    rivalName = rival.rivalName,
                    rivalScore = comparison.rivalScore,
                    userName = userName,
                    userScore = comparison.userScore,
                    gap = comparison.scoreGap,
                    direction = comparison.direction,
                    history = history
                )
            } else {
                _state.value = RivalUiState.Error
            }
        }
    }
}

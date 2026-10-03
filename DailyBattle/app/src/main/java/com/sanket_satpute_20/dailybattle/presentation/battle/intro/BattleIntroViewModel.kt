package com.sanket_satpute_20.dailybattle.presentation.battle.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BattleIntroState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val challengesCount: Int = 3,
    val estimatedMinutes: Int = 3
)

sealed interface BattleIntroEvent {
    object NavigateToBattle : BattleIntroEvent
    object NavigateBack : BattleIntroEvent
}

@HiltViewModel
class BattleIntroViewModel @Inject constructor(
    private val getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BattleIntroState())
    val state: StateFlow<BattleIntroState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BattleIntroEvent>()
    val uiEvent: SharedFlow<BattleIntroEvent> = _uiEvent.asSharedFlow()

    fun onStartBattle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isError = false) }
            
            // In a real scenario, UserId and BattleId come from authenticated user / current day logic.
            val userId = UserId("mock_user_id")
            val battleId = BattleId("battle_today")
            
            val result = getOrStartOfficialBattleUseCase.execute(userId, battleId)
            
            when (result) {
                is DomainResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.emit(BattleIntroEvent.NavigateToBattle)
                }
                is DomainResult.Failure -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "You have already completed today's official attempt."
                        )
                    }
                }
            }
        }
    }

    fun onBack() {
        viewModelScope.launch {
            _uiEvent.emit(BattleIntroEvent.NavigateBack)
        }
    }
}

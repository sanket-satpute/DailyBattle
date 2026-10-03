package com.sanket_satpute_20.dailybattle.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val userName: String = "",
    val momentumDays: Int = 0,
    val battleState: DBBattleCardState = DBBattleCardState.Loading,
    val challengesCount: Int = 0,
    val estimatedMinutes: Int = 0,
    val hasRival: Boolean = false,
    val rivalName: String = "",
    val rivalScore: Int = 0,
    val userScore: Int = 0,
    val pointsToCatch: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isError = false, battleState = DBBattleCardState.Loading) }
            
            // Simulate network delay
            delay(800)
            
            val name = userRepository.getBattleName() ?: "Player"
            
            _state.update {
                it.copy(
                    isLoading = false,
                    userName = name,
                    momentumDays = 7, // Placeholder per SCR-003
                    battleState = DBBattleCardState.Ready,
                    challengesCount = 3,
                    estimatedMinutes = 3,
                    hasRival = true, // Placeholder per SCR-003
                    rivalName = "Rahul",
                    rivalScore = 914,
                    userScore = 901,
                    pointsToCatch = 13
                )
            }
        }
    }
}

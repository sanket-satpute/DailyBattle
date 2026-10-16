package com.sanket_satpute_20.dailybattle.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.BuildTypeEnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val userName: String = "",
    val momentumDays: Int = 0,
    val battleState: DBBattleCardState = DBBattleCardState.Loading,
    val challengesCount: Int = 0,
    val estimatedMinutes: Int = 0,
    val score: Int? = null,
    val percentileText: String? = null,
    val nextBattleTime: String? = null,
    val hasRival: Boolean = false,
    val rivalName: String = "",
    val rivalScore: Int = 0,
    val userScore: Int = 0,
    val pointsToCatch: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val environmentProvider: EnvironmentProvider = BuildTypeEnvironmentProvider(isDebugBuild = true),
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isError = false, battleState = DBBattleCardState.Loading) }
            
            val name = userRepository.getBattleName() ?: "Player"
            val isProduction = environmentProvider.current == AppEnvironment.Production
            
            _state.update {
                it.copy(
                    isLoading = false,
                    userName = name,
                    momentumDays = if (isProduction) 0 else 7, // Demo placeholder isolated from production per §65
                    battleState = DBBattleCardState.Ready,
                    challengesCount = 3,
                    estimatedMinutes = 3,
                    hasRival = !isProduction, // Demo rival isolated from production per §65
                    rivalName = if (isProduction) "" else "Rahul",
                    rivalScore = if (isProduction) 0 else 914,
                    userScore = if (isProduction) 0 else 901,
                    pointsToCatch = if (isProduction) 0 else 13
                )
            }
        }
    }

    fun setOffline(isOffline: Boolean) {
        _state.update { it.copy(isOffline = isOffline) }
    }

    fun setError(isError: Boolean, message: String? = null) {
        _state.update { it.copy(isError = isError, errorMessage = message) }
    }
}

package com.sanket_satpute_20.dailybattle.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.GetProfileUseCase
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: Profile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val userRepository: UserRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val battleName = userRepository?.getBattleName()
            val userId = UserId("current-user")
            
            when (val result = getProfileUseCase.execute(userId)) {
                is DomainResult.Success -> {
                    _uiState.value = ProfileUiState.Success(result.value)
                }
                is DomainResult.Failure -> {
                    val localResult = getProfileUseCase.execute(UserId(RoomUserRepository.LOCAL_USER_ID))
                    if (localResult is DomainResult.Success) {
                        _uiState.value = ProfileUiState.Success(localResult.value)
                    } else if (battleName != null) {
                        _uiState.value = ProfileUiState.Success(
                            Profile(
                                userId = UserId(RoomUserRepository.LOCAL_USER_ID),
                                battleName = battleName,
                                momentum = 0,
                                bestScore = 0,
                                averageScore = 0,
                                battleDNA = BattleDNA(speed = null, memory = null, people = null),
                                records = PersonalRecords(bestScore = 0, bestMomentum = 0, battlesPlayed = 0)
                            )
                        )
                    } else {
                        _uiState.value = ProfileUiState.Error("Failed to load profile.")
                    }
                }
            }
        }
    }
}

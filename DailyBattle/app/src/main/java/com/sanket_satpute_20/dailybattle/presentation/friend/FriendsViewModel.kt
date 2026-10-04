package com.sanket_satpute_20.dailybattle.presentation.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.GetBattleResultUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.friend.GetFriendsUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.GetCurrentRivalUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendUiModel(
    val id: String,
    val name: String,
    val score: Int?,
    val isUser: Boolean,
    val isRival: Boolean,
    val status: FriendStatus
)

sealed interface FriendsUiState {
    data object Loading : FriendsUiState
    data class Success(val friends: List<FriendUiModel>) : FriendsUiState
    data object Error : FriendsUiState
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getBattleResultUseCase: GetBattleResultUseCase,
    private val getCurrentRivalUseCase: GetCurrentRivalUseCase,
    private val userRepository: UserRepository,
    private val activeBattleSessionManager: ActiveBattleSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<FriendsUiState>(FriendsUiState.Loading)
    val state: StateFlow<FriendsUiState> = _state.asStateFlow()

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _state.value = FriendsUiState.Loading

            val userId = UserId("current-user")
            val userName = userRepository.getBattleName() ?: "Player"
            val battleId = activeBattleSessionManager.getActiveSession()?.battleId ?: BattleId("current-battle")

            // 1. Get user score
            val userScoreResult = getBattleResultUseCase.execute(userId, battleId)
            val userScore = if (userScoreResult is DomainResult.Success) {
                userScoreResult.value.totalScore
            } else {
                null
            }

            // 2. Get friends
            val friendsResult = getFriendsUseCase.execute(userId)
            if (friendsResult is DomainResult.Failure) {
                _state.value = FriendsUiState.Error
                return@launch
            }
            val domainFriends = (friendsResult as DomainResult.Success).value

            // 3. Get current rival
            val rivalResult = getCurrentRivalUseCase.execute(userId)
            val currentRivalId = if (rivalResult is DomainResult.Success) {
                rivalResult.value?.rivalUserId
            } else {
                null
            }

            // 4. Map to UI model
            val uiModels = mutableListOf<FriendUiModel>()

            // Add user
            uiModels.add(
                FriendUiModel(
                    id = userId.value,
                    name = userName,
                    score = userScore,
                    isUser = true,
                    isRival = false,
                    status = FriendStatus.ACCEPTED
                )
            )

            // Add friends
            domainFriends.forEach { f ->
                val otherUserId = if (f.userId == userId) f.friendUserId else f.userId
                uiModels.add(
                    FriendUiModel(
                        id = otherUserId.value,
                        name = f.friendName,
                        score = f.todayScore,
                        isUser = false,
                        isRival = otherUserId == currentRivalId,
                        status = f.status
                    )
                )
            }

            // 5. Sort: Highest today's score first. Null scores go last.
            uiModels.sortWith(compareByDescending<FriendUiModel> { it.score ?: -1 }
                .thenBy { it.name })

            _state.value = FriendsUiState.Success(uiModels)
        }
    }
}

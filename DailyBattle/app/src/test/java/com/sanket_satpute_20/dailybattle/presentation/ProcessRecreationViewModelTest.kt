package com.sanket_satpute_20.dailybattle.presentation

import androidx.lifecycle.SavedStateHandle
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.friend.AddFriendByBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.presentation.addfriend.AddFriendViewModel
import com.sanket_satpute_20.dailybattle.presentation.startup.BattleNameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessRecreationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: FakeUserRepository
    private lateinit var friendRepository: FakeFriendRepository
    private lateinit var addFriendUseCase: AddFriendByBattleCodeUseCase
    private lateinit var getUserBattleCodeUseCase: GetUserBattleCodeUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = FakeUserRepository()
        friendRepository = FakeFriendRepository()
        addFriendUseCase = AddFriendByBattleCodeUseCase(friendRepository)
        getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun battleNameViewModel_restoresTypedNameAcrossProcessRecreation() {
        // Arrange: SavedStateHandle contains state from before process death
        val savedStateHandle = SavedStateHandle(
            mapOf(BattleNameViewModel.KEY_BATTLE_NAME to "ThunderStrike")
        )

        // Act: ViewModel recreated by Android OS after process kill
        val viewModel = BattleNameViewModel(userRepository, savedStateHandle)

        // Assert: Restored state matches without data loss
        assertEquals("ThunderStrike", viewModel.state.value.name)
        assertTrue(viewModel.state.value.isValid)
    }

    @Test
    fun battleNameViewModel_updatesSavedStateHandleOnInput() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = BattleNameViewModel(userRepository, savedStateHandle)

        viewModel.onNameChanged("ViperKing")

        assertEquals("ViperKing", savedStateHandle.get<String>(BattleNameViewModel.KEY_BATTLE_NAME))
    }

    @Test
    fun addFriendViewModel_restoresEnteredCodeAcrossProcessRecreation() {
        // Arrange: SavedStateHandle contains code entered before process death
        val savedStateHandle = SavedStateHandle(
            mapOf(AddFriendViewModel.KEY_ENTERED_CODE to "BTL-9999")
        )

        // Act: Recreated ViewModel
        val viewModel = AddFriendViewModel(addFriendUseCase, getUserBattleCodeUseCase, savedStateHandle)

        // Assert: Input preserved
        assertEquals("BTL-9999", viewModel.enteredCode.value)
    }

    @Test
    fun addFriendViewModel_updatesSavedStateHandleOnCodeChange() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddFriendViewModel(addFriendUseCase, getUserBattleCodeUseCase, savedStateHandle)

        viewModel.onCodeChanged("TEST-1234")

        assertEquals("TEST-1234", savedStateHandle.get<String>(AddFriendViewModel.KEY_ENTERED_CODE))
    }

    private class FakeUserRepository : UserRepository {
        var storedName: String? = null
        override suspend fun saveBattleName(name: String) { storedName = name }
        override suspend fun getBattleName(): String? = storedName
    }

    private class FakeFriendRepository : FriendRepository {
        override suspend fun getFriends(userId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun getFriend(userId: UserId, friendUserId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String): DomainResult<Friend> {
            return DomainResult.Success(
                Friend(
                    friendshipId = FriendshipId("1"),
                    userId = userId,
                    friendUserId = UserId("f1"),
                    friendName = "NewFriend",
                    status = FriendStatus.PENDING,
                    todayScore = null,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
        override suspend fun acceptFriendRequest(friendshipId: FriendshipId) = DomainResult.Failure(AppError.Domain)
        override suspend fun rejectFriendRequest(friendshipId: FriendshipId) = DomainResult.Failure(AppError.Domain)
        override suspend fun blockFriend(friendshipId: FriendshipId) = DomainResult.Failure(AppError.Domain)
        override suspend fun getBattleCode(userId: UserId): DomainResult<String> {
            return DomainResult.Success("K4X8M9")
        }
    }
}

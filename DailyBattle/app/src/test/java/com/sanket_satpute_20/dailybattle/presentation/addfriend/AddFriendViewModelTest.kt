package com.sanket_satpute_20.dailybattle.presentation.addfriend

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.design.components.DBInputState
import com.sanket_satpute_20.dailybattle.domain.friend.AddFriendByBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddFriendViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeFriendRepository : FriendRepository {
        var shouldFail = false
        var lastAddedCode: String? = null

        override suspend fun getFriends(userId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun getFriend(userId: UserId, friendUserId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId) = DomainResult.Failure(AppError.Domain)
        
        override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String): DomainResult<Friend> {
            lastAddedCode = battleCode
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
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
            return if (shouldFail) DomainResult.Failure(AppError.Domain) else DomainResult.Success("K4X8M9")
        }
    }

    private lateinit var friendRepo: FakeFriendRepository
    private lateinit var addFriendUseCase: AddFriendByBattleCodeUseCase
    private lateinit var getUserBattleCodeUseCase: GetUserBattleCodeUseCase
    private lateinit var viewModel: AddFriendViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        friendRepo = FakeFriendRepository()
        addFriendUseCase = AddFriendByBattleCodeUseCase(friendRepo)
        getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepo)
        viewModel = AddFriendViewModel(addFriendUseCase, getUserBattleCodeUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        advanceUntilIdle()
        assertEquals("", viewModel.enteredCode.value)
        assertEquals("K4X8M9", viewModel.userCode.value)
        assertEquals(DBInputState.Default, viewModel.inputState.value)
        assertEquals(AddFriendSubmitState.Idle, viewModel.submitState.value)
    }

    @Test
    fun `onCodeChanged validates code length`() = runTest {
        // Less than 6
        viewModel.onCodeChanged("AB")
        assertEquals("AB", viewModel.enteredCode.value)
        assertEquals(DBInputState.Default, viewModel.inputState.value)

        // Exactly 6
        viewModel.onCodeChanged("ABCDEF")
        assertEquals("ABCDEF", viewModel.enteredCode.value)
        assertEquals(DBInputState.Valid, viewModel.inputState.value)

        // More than 6
        viewModel.onCodeChanged("ABCDEFG")
        assertEquals(DBInputState.Default, viewModel.inputState.value)
    }

    @Test
    fun `submitCode with invalid length shows error`() = runTest {
        viewModel.onCodeChanged("ABC")
        viewModel.submitCode()

        assertEquals(DBInputState.Invalid, viewModel.inputState.value)
        assertEquals("Battle Code must be exactly 6 uppercase letters and numbers.", viewModel.feedbackMessage.value)
    }

    @Test
    fun `submitCode success updates state and clears input`() = runTest {
        viewModel.onCodeChanged("ABCDEF")
        viewModel.submitCode()
        
        advanceUntilIdle()

        assertEquals("ABCDEF", friendRepo.lastAddedCode)
        assertTrue(viewModel.submitState.value is AddFriendSubmitState.Success)
        assertEquals("", viewModel.enteredCode.value)
        assertEquals(DBInputState.Default, viewModel.inputState.value)
    }

    @Test
    fun `submitCode failure updates state to error`() = runTest {
        friendRepo.shouldFail = true
        viewModel.onCodeChanged("ABCDEF")
        viewModel.submitCode()
        
        advanceUntilIdle()

        assertTrue(viewModel.submitState.value is AddFriendSubmitState.Error)
        assertEquals("ABCDEF", viewModel.enteredCode.value) // does not clear
        assertEquals(DBInputState.Invalid, viewModel.inputState.value)
    }
}

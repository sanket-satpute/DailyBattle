package com.sanket_satpute_20.dailybattle.presentation.friend

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.GetBattleResultUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.friend.GetFriendsUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.GetCurrentRivalUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalRepository
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.user.User
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
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
class FriendsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserRepository : UserRepository {
        override suspend fun saveBattleName(name: String) {}
        override suspend fun getBattleName() = "MyName"
    }

    private class FakeBattleSessionRepository : com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository {
        override fun getOfficialSession(userId: UserId, battleId: BattleId) = null
        override fun saveSession(session: BattleSession) {}
        override fun getUnacknowledgedSessions() = emptyList<BattleSession>()
    }

    private class FakeFriendRepository : FriendRepository {
        var friends: List<Friend> = emptyList()
        var shouldFail = false

        override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(friends)
        }

        override suspend fun getFriend(userId: UserId, friendUserId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId) = DomainResult.Failure(AppError.Domain)
        override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String) = DomainResult.Failure(AppError.Domain)
        override suspend fun acceptFriendRequest(friendshipId: FriendshipId) = DomainResult.Failure(AppError.Domain)
        override suspend fun rejectFriendRequest(friendshipId: FriendshipId) = DomainResult.Failure(AppError.Domain)
        override suspend fun blockFriend(friendshipId: FriendshipId) = DomainResult.Failure(AppError.Domain)
        override suspend fun getBattleCode(userId: UserId) = DomainResult.Success("K4X8M9")
    }

    private class FakeRivalRepository : RivalRepository {
        var currentRival: Rival? = null
        override suspend fun getCurrentRival(userId: UserId) = DomainResult.Success(currentRival)
        override suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId) = DomainResult.Failure(AppError.Domain)
        override suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId) = DomainResult.Failure(AppError.Domain)
    }

    private class FakeBattleResultRepository : BattleResultRepository {
        var result: BattleResult? = null
        override suspend fun completeBattle(sessionId: com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId, clientRequestId: String) = DomainResult.Failure(AppError.Domain)
        override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> {
            return if (result != null) DomainResult.Success(result!!) else DomainResult.Failure(AppError.Domain)
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFriends sets success state with sorted friends`() = runTest {
        val friendRepo = FakeFriendRepository()
        val rivalRepo = FakeRivalRepository()
        val resultRepo = FakeBattleResultRepository()
        
        friendRepo.friends = listOf(
            Friend(FriendshipId("1"), UserId("current-user"), UserId("u1"), "A", FriendStatus.ACCEPTED, 100, 0, 0),
            Friend(FriendshipId("2"), UserId("current-user"), UserId("u2"), "B", FriendStatus.ACCEPTED, 300, 0, 0),
            Friend(FriendshipId("3"), UserId("current-user"), UserId("u3"), "C", FriendStatus.PENDING, null, 0, 0)
        )
        
        rivalRepo.currentRival = Rival(UserId("current-user"), UserId("u2"), "B", 0L, com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus.ACTIVE)
        
        resultRepo.result = BattleResult(
            resultId = com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId("r1"),
            battleId = BattleId("b1"),
            userId = UserId("current-user"),
            sessionId = com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId("s1"),
            snapScore = 50,
            shiftScore = 50,
            crowdCallScore = 50,
            consistencyScore = 50,
            totalScore = 200,
            percentile = 50.0,
            completedAt = 0L
        )

        val viewModel = FriendsViewModel(
            GetFriendsUseCase(friendRepo),
            GetBattleResultUseCase(resultRepo),
            GetCurrentRivalUseCase(rivalRepo),
            FakeUserRepository(),
            ActiveBattleSessionManager(FakeBattleSessionRepository())
        )

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is FriendsUiState.Success)
        
        val friendsList = (state as FriendsUiState.Success).friends
        assertEquals(4, friendsList.size) // 3 friends + 1 user
        
        // Highest score first, null last
        assertEquals("B", friendsList[0].name)
        assertEquals(300, friendsList[0].score)
        assertTrue(friendsList[0].isRival)
        
        assertEquals("MyName", friendsList[1].name)
        assertEquals(200, friendsList[1].score)
        assertTrue(friendsList[1].isUser)
        
        assertEquals("A", friendsList[2].name)
        assertEquals(100, friendsList[2].score)
        
        assertEquals("C", friendsList[3].name)
        assertEquals(null, friendsList[3].score)
        assertEquals(FriendStatus.PENDING, friendsList[3].status)
    }

    @Test
    fun `loadFriends sets error state on friend repo failure`() = runTest {
        val friendRepo = FakeFriendRepository().apply { shouldFail = true }
        
        val viewModel = FriendsViewModel(
            GetFriendsUseCase(friendRepo),
            GetBattleResultUseCase(FakeBattleResultRepository()),
            GetCurrentRivalUseCase(FakeRivalRepository()),
            FakeUserRepository(),
            ActiveBattleSessionManager(FakeBattleSessionRepository())
        )

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is FriendsUiState.Error)
    }
}

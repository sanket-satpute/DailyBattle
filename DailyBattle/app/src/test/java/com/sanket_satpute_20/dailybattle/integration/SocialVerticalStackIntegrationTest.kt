package com.sanket_satpute_20.dailybattle.integration

import androidx.lifecycle.SavedStateHandle
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.data.rival.RoomRivalRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.design.components.DBInputState
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.GetBattleResultUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.AddFriendByBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.friend.GetFriendsUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetCurrentRivalUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleResultDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryChallengeSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryFriendDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryRivalDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryRivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryUserDao
import com.sanket_satpute_20.dailybattle.presentation.addfriend.AddFriendSubmitState
import com.sanket_satpute_20.dailybattle.presentation.addfriend.AddFriendViewModel
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendsUiState
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendsViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> FriendsViewModel / AddFriendViewModel <-> Use Cases <-> Repositories <-> Room DAOs
 *
 * Verifies Sprint 23.3:
 * - Aggregation of friends, current user score, and rival from multiple Room DAOs into FriendsUiState.Success
 * - Add Friend by battle code validation and persistence into FriendDao
 * - Rejection of invalid codes and duplicate friend requests
 * - User's own battle code retrieval flow
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SocialVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var friendDao: InMemoryFriendDao
    private lateinit var rivalDao: InMemoryRivalDao
    private lateinit var rivalMatchHistoryDao: InMemoryRivalMatchHistoryDao
    private lateinit var userDao: InMemoryUserDao
    private lateinit var battleResultDao: InMemoryBattleResultDao
    private lateinit var battleHistoryDao: InMemoryBattleHistoryDao
    private lateinit var battleSessionDao: InMemoryBattleSessionDao

    private lateinit var friendRepository: RoomFriendRepository
    private lateinit var rivalRepository: RoomRivalRepository
    private lateinit var userRepository: RoomUserRepository
    private lateinit var battleResultRepository: RoomBattleResultRepository

    private lateinit var getFriendsUseCase: GetFriendsUseCase
    private lateinit var getBattleResultUseCase: GetBattleResultUseCase
    private lateinit var getCurrentRivalUseCase: GetCurrentRivalUseCase
    private lateinit var addFriendByBattleCodeUseCase: AddFriendByBattleCodeUseCase
    private lateinit var getUserBattleCodeUseCase: GetUserBattleCodeUseCase
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager

    @Before
    fun setup() {
        friendDao = InMemoryFriendDao()
        rivalDao = InMemoryRivalDao()
        rivalMatchHistoryDao = InMemoryRivalMatchHistoryDao()
        userDao = InMemoryUserDao()
        battleResultDao = InMemoryBattleResultDao()
        battleHistoryDao = InMemoryBattleHistoryDao()
        battleSessionDao = InMemoryBattleSessionDao()
        val challengeSessionDao = InMemoryChallengeSessionDao()
        val sessionRepository = RoomBattleSessionRepository(
            battleSessionDao = battleSessionDao,
            challengeSessionDao = challengeSessionDao,
            database = null
        )

        friendRepository = RoomFriendRepository(friendDao)
        rivalRepository = RoomRivalRepository(rivalDao, rivalMatchHistoryDao)
        userRepository = RoomUserRepository(userDao)
        battleResultRepository = RoomBattleResultRepository(
            battleResultDao = battleResultDao,
            battleHistoryDao = battleHistoryDao,
            battleSessionDao = battleSessionDao,
            database = null
        )

        getFriendsUseCase = GetFriendsUseCase(friendRepository)
        getBattleResultUseCase = GetBattleResultUseCase(battleResultRepository)
        getCurrentRivalUseCase = GetCurrentRivalUseCase(rivalRepository)
        addFriendByBattleCodeUseCase = AddFriendByBattleCodeUseCase(friendRepository)
        getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepository)
        activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
    }

    @Test
    fun loadFriendsAggregatesAcrossUserRivalAndFriendDaos() = runTest {
        val currentUserId = "current-user"
        val rivalUserId = "rival-user-1"
        val friendUserId = "friend-user-2"

        // 1. Populate UserDao with user name
        userDao.insertUser(
            UserEntity(
                userId = RoomUserRepository.LOCAL_USER_ID,
                battleName = "Phoenix",
                createdAt = 1000L,
                updatedAt = 1000L,
                accountStatus = "ACTIVE"
            )
        )

        // 2. Populate BattleResultDao with user's battle score
        battleResultDao.insertResult(
            BattleResultEntity(
                resultId = "res-1",
                sessionId = "sess-1",
                userId = currentUserId,
                battleId = "current-battle",
                snapScore = 280,
                shiftScore = 290,
                crowdCallScore = 270,
                consistencyScore = 95,
                totalScore = 935,
                percentile = 90.0,
                completedAt = 1000L
            )
        )

        // 3. Populate RivalDao with current rival
        rivalDao.insertRival(
            RivalEntity(
                userId = currentUserId,
                rivalUserId = rivalUserId,
                rivalName = "RivalViper",
                selectedAt = 1000L,
                status = RivalStatus.ACTIVE.name
            )
        )

        // 4. Populate FriendDao with a friend
        friendDao.insertFriend(
            FriendEntity(
                friendshipId = "friend-rel-1",
                userId = currentUserId,
                friendUserId = friendUserId,
                friendName = "AllyTitan",
                status = FriendStatus.ACCEPTED.name,
                todayScore = 880,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        // 5. Initialize FriendsViewModel and verify aggregated UI state
        val viewModel = FriendsViewModel(
            getFriendsUseCase = getFriendsUseCase,
            getBattleResultUseCase = getBattleResultUseCase,
            getCurrentRivalUseCase = getCurrentRivalUseCase,
            userRepository = userRepository,
            activeBattleSessionManager = activeBattleSessionManager
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue("State must be Success", state is FriendsUiState.Success)
        val friendsList = (state as FriendsUiState.Success).friends

        // First item is user row
        val userRow = friendsList.find { it.isUser }
        assertNotNull("User row must exist", userRow)
        assertEquals("Phoenix", userRow?.name)
        assertEquals(935, userRow?.score)

        // Friend row
        val friendRow = friendsList.find { it.id == friendUserId }
        assertNotNull("Friend row must exist", friendRow)
        assertEquals("AllyTitan", friendRow?.name)
        assertEquals(FriendStatus.ACCEPTED, friendRow?.status)
    }

    @Test
    fun addFriendByBattleCodeValidCodePersistsToFriendDao() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddFriendViewModel(
            addFriendByBattleCodeUseCase = addFriendByBattleCodeUseCase,
            getUserBattleCodeUseCase = getUserBattleCodeUseCase,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        // 1. User enters 6-character valid code "WAR999"
        viewModel.onCodeChanged("WAR999")
        assertEquals(DBInputState.Valid, viewModel.inputState.value)

        // 2. User submits
        viewModel.submitCode()
        advanceUntilIdle()

        // 3. Verify submit state becomes Success
        assertEquals(AddFriendSubmitState.Success, viewModel.submitState.value)

        // 4. Verify entity was persisted into FriendDao
        val persistedFriend = friendDao.friends.values.find { it.friendUserId == "u-WAR999" }
        assertNotNull("Friend must be inserted in FriendDao", persistedFriend)
        assertEquals(FriendStatus.PENDING.name, persistedFriend?.status)
    }

    @Test
    fun addFriendWithInvalidCodeRejectsSubmission() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddFriendViewModel(
            addFriendByBattleCodeUseCase = addFriendByBattleCodeUseCase,
            getUserBattleCodeUseCase = getUserBattleCodeUseCase,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        // User enters too short code
        viewModel.onCodeChanged("AB")
        viewModel.submitCode()
        advanceUntilIdle()

        // Verify input state marks Invalid and displays feedback message
        assertEquals(DBInputState.Invalid, viewModel.inputState.value)
        assertEquals("Battle Code must be exactly 6 uppercase letters and numbers.", viewModel.feedbackMessage.value)
        assertEquals(0, friendDao.friends.size)
    }

    @Test
    fun addFriendDuplicateFriendShowsSubmissionError() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddFriendViewModel(
            addFriendByBattleCodeUseCase = addFriendByBattleCodeUseCase,
            getUserBattleCodeUseCase = getUserBattleCodeUseCase,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        // Pre-insert existing friend in FriendDao with ID "u-DUP999"
        friendDao.insertFriend(
            FriendEntity(
                friendshipId = "existing-friend-rel",
                userId = "current-user",
                friendUserId = "u-DUP999",
                friendName = "DuplicateFriend",
                status = FriendStatus.ACCEPTED.name,
                todayScore = null,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        viewModel.onCodeChanged("DUP999")
        viewModel.submitCode()
        advanceUntilIdle()

        // Verify repository conflict bubbles up to submit state Error
        assertTrue(viewModel.submitState.value is AddFriendSubmitState.Error)
    }

    @Test
    fun userBattleCodeRetrievalPopulatesStateFlow() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddFriendViewModel(
            addFriendByBattleCodeUseCase = addFriendByBattleCodeUseCase,
            getUserBattleCodeUseCase = getUserBattleCodeUseCase,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        // Repository returns default battle code "K4X8M9"
        assertEquals("K4X8M9", viewModel.userCode.value)
    }
}

package com.sanket_satpute_20.dailybattle.integration

import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.data.rival.RoomRivalRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.rival.GetCurrentRivalUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetRivalComparisonUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetRivalMatchHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryChallengeSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryRivalDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryRivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryUserDao
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalUiState
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> RivalViewModel <-> Rival Use Cases <-> RoomRivalRepository <-> RivalDao / RivalMatchHistoryDao
 *
 * Verifies Sprint 23.3:
 * - Active rival, match history, and score comparison flow from DAOs into RivalUiState.Success
 * - Missing rival returns null and propagates to RivalUiState.NoRival
 * - Database exceptions in rival query layer cleanly map to RivalUiState.Error
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RivalVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var rivalDao: InMemoryRivalDao
    private lateinit var rivalMatchHistoryDao: InMemoryRivalMatchHistoryDao
    private lateinit var userDao: InMemoryUserDao

    private lateinit var rivalRepository: RoomRivalRepository
    private lateinit var userRepository: RoomUserRepository

    private lateinit var getCurrentRivalUseCase: GetCurrentRivalUseCase
    private lateinit var getRivalComparisonUseCase: GetRivalComparisonUseCase
    private lateinit var getRivalMatchHistoryUseCase: GetRivalMatchHistoryUseCase
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager

    @Before
    fun setup() {
        rivalDao = InMemoryRivalDao()
        rivalMatchHistoryDao = InMemoryRivalMatchHistoryDao()
        userDao = InMemoryUserDao()
        val battleSessionDao = InMemoryBattleSessionDao()
        val challengeSessionDao = InMemoryChallengeSessionDao()
        val sessionRepository = com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository(
            battleSessionDao = battleSessionDao,
            challengeSessionDao = challengeSessionDao,
            database = null
        )

        rivalRepository = RoomRivalRepository(rivalDao, rivalMatchHistoryDao)
        userRepository = RoomUserRepository(userDao)

        getCurrentRivalUseCase = GetCurrentRivalUseCase(rivalRepository)
        getRivalComparisonUseCase = GetRivalComparisonUseCase(rivalRepository)
        getRivalMatchHistoryUseCase = GetRivalMatchHistoryUseCase(rivalRepository)
        activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
    }

    private fun createViewModel(): RivalViewModel {
        return RivalViewModel(
            getCurrentRivalUseCase = getCurrentRivalUseCase,
            getRivalComparisonUseCase = getRivalComparisonUseCase,
            getRivalMatchHistoryUseCase = getRivalMatchHistoryUseCase,
            userRepository = userRepository,
            activeBattleSessionManager = activeBattleSessionManager
        )
    }

    @Test
    fun loadRivalDataWithActiveRivalAndHistoryPopulatesUiSuccess() = runTest {
        val currentUserId = "current-user"
        val rivalUserId = "rival-42"
        val battleId = "current-battle"

        // 1. Populate UserDao with user name
        userDao.insertUser(
            UserEntity(
                userId = RoomUserRepository.LOCAL_USER_ID,
                battleName = "Spartan",
                createdAt = 1000L,
                updatedAt = 1000L,
                accountStatus = "ACTIVE"
            )
        )

        // 2. Populate RivalDao with active rival
        rivalDao.insertRival(
            RivalEntity(
                userId = currentUserId,
                rivalUserId = rivalUserId,
                rivalName = "Nemesis",
                selectedAt = 1000L,
                status = RivalStatus.ACTIVE.name
            )
        )

        // 3. Populate RivalMatchHistoryDao with match record for current-battle
        rivalMatchHistoryDao.insertMatchHistory(
            listOf(
                RivalMatchHistoryEntity(
                    battleId = battleId,
                    userId = currentUserId,
                    date = 1000L,
                    userScore = 950,
                    rivalScore = 910,
                    outcome = "WIN"
                )
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue("State must be Success", state is RivalUiState.Success)
        val success = state as RivalUiState.Success
        assertEquals("Nemesis", success.rivalName)
        assertEquals(910, success.rivalScore)
        assertEquals("Spartan", success.userName)
        assertEquals(950, success.userScore)
        assertEquals(40, success.gap)
        assertEquals(ScoreDirection.AHEAD, success.direction)
        assertEquals(1, success.history.size)
    }

    @Test
    fun loadRivalDataWhenNoRivalInDatabaseEmitsNoRivalState() = runTest {
        // RivalDao is empty
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RivalUiState.NoRival, viewModel.state.value)
    }

    @Test
    fun databaseExceptionInRivalDaoSafelyTransitionsToErrorState() = runTest {
        val failingDao = object : InMemoryRivalDao() {
            override suspend fun getRivalForUser(userId: String): RivalEntity? {
                throw IllegalStateException("Simulated SQLite Crash")
            }
        }
        val failingRepo = RoomRivalRepository(failingDao, rivalMatchHistoryDao)
        val failingGetRivalUseCase = GetCurrentRivalUseCase(failingRepo)

        val viewModel = RivalViewModel(
            getCurrentRivalUseCase = failingGetRivalUseCase,
            getRivalComparisonUseCase = getRivalComparisonUseCase,
            getRivalMatchHistoryUseCase = getRivalMatchHistoryUseCase,
            userRepository = userRepository,
            activeBattleSessionManager = activeBattleSessionManager
        )
        advanceUntilIdle()

        assertEquals(RivalUiState.Error, viewModel.state.value)
    }
}

package com.sanket_satpute_20.dailybattle.integration

import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleResultDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryChallengeSessionDao
import com.sanket_satpute_20.dailybattle.presentation.result.ResultsUiState
import com.sanket_satpute_20.dailybattle.presentation.result.ResultsViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> ResultsViewModel <-> CompleteBattleUseCase / GetBattleResultUseCase <-> RoomBattleResultRepository <-> BattleResultDao / BattleSessionDao / BattleHistoryDao
 *
 * Verifies Sprint 23.3:
 * - Battle completion writes atomically down to Room DAOs and updates session state
 * - ResultsViewModel queries repository -> Room DAO and propagates to ResultsUiState.Success
 * - Missing records query returns clean domain failure mapped to ResultsUiState.Error
 * - Persistence failures safely mapped without unhandled crashes
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BattleResultsVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var battleResultDao: InMemoryBattleResultDao
    private lateinit var battleHistoryDao: InMemoryBattleHistoryDao
    private lateinit var battleSessionDao: InMemoryBattleSessionDao
    private lateinit var challengeSessionDao: InMemoryChallengeSessionDao

    private lateinit var battleResultRepository: RoomBattleResultRepository
    private lateinit var battleSessionRepository: RoomBattleSessionRepository
    private lateinit var completeBattleUseCase: CompleteBattleUseCase
    private lateinit var viewModel: ResultsViewModel

    @Before
    fun setup() {
        battleResultDao = InMemoryBattleResultDao()
        battleHistoryDao = InMemoryBattleHistoryDao()
        battleSessionDao = InMemoryBattleSessionDao()
        challengeSessionDao = InMemoryChallengeSessionDao()

        battleResultRepository = RoomBattleResultRepository(
            battleResultDao = battleResultDao,
            battleHistoryDao = battleHistoryDao,
            battleSessionDao = battleSessionDao,
            database = null
        )

        battleSessionRepository = RoomBattleSessionRepository(
            battleSessionDao = battleSessionDao,
            challengeSessionDao = challengeSessionDao,
            database = null
        )

        completeBattleUseCase = CompleteBattleUseCase(
            resultRepository = battleResultRepository,
            sessionRepository = battleSessionRepository
        )

        viewModel = ResultsViewModel(battleResultRepository)
    }

    @Test
    fun completeBattleToResultsScreenVerticalFlow() = runTest {
        val userId = UserId("warrior-99")
        val battleId = BattleId("battle-oct-13")
        val sessionId = BattleSessionId("session-xyz")

        // 1. Pre-insert in-progress official battle in BattleSessionDao
        battleSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = sessionId.value,
                userId = userId.value,
                battleId = battleId.value,
                mode = BattleMode.OFFICIAL.name,
                status = BattleSessionStatus.IN_PROGRESS.name,
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null
            )
        )

        // 2. Complete battle via CompleteBattleUseCase
        val session = BattleSession(
            sessionId = sessionId,
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = emptyList()
        )
        val completeResult = completeBattleUseCase.execute(session)
        assertTrue("Battle completion must succeed", completeResult is DomainResult.Success)

        // 3. Verify DAO persistence: result and history written, session marked COMPLETED
        assertEquals(1, battleResultDao.results.size)
        assertEquals(1, battleHistoryDao.historyItems.size)
        val updatedSessionEntity = battleSessionDao.getSessionById(sessionId.value)
        assertNotNull(updatedSessionEntity)
        assertEquals(BattleSessionStatus.COMPLETED.name, updatedSessionEntity?.status)

        // 4. UI navigates to Results screen and requests loadResult(userId, battleId)
        viewModel.loadResult(userId, battleId)
        advanceUntilIdle()

        // 5. Verify ResultsUiState is Success with summary populated from DB
        val state = viewModel.uiState.value
        assertTrue("State must be Success", state is ResultsUiState.Success)
        val summary = (state as ResultsUiState.Success).summary
        assertEquals(userId, summary.result.userId)
        assertEquals(battleId, summary.result.battleId)
        assertEquals(1000, summary.result.totalScore)
        assertEquals(300, summary.result.snapScore)
        assertEquals(300, summary.result.shiftScore)
        assertEquals(300, summary.result.crowdCallScore)
        assertEquals(100, summary.result.consistencyScore)
    }

    @Test
    fun queryNonExistentResultInDatabaseEmitsErrorState() = runTest {
        viewModel.loadResult(UserId("unknown-user"), BattleId("unknown-battle"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State must be Error for missing result", state is ResultsUiState.Error)
        assertEquals("Could not load battle result.", (state as ResultsUiState.Error).message)
    }

    @Test
    fun databaseFailureSafelyEmitsErrorStateWithoutCrash() = runTest {
        val failingDao = object : InMemoryBattleResultDao() {
            override suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity? {
                throw IllegalStateException("Simulated SQLite Corrupt Disk")
            }
        }
        val repoWithFailingDao = RoomBattleResultRepository(
            battleResultDao = failingDao,
            battleHistoryDao = battleHistoryDao,
            battleSessionDao = battleSessionDao,
            database = null
        )
        val vmWithFailingDao = ResultsViewModel(repoWithFailingDao)

        vmWithFailingDao.loadResult(UserId("user-1"), BattleId("battle-1"))
        advanceUntilIdle()

        val state = vmWithFailingDao.uiState.value
        assertTrue("State must be Error when database fails", state is ResultsUiState.Error)
        assertEquals("Could not load battle result.", (state as ResultsUiState.Error).message)
    }
}

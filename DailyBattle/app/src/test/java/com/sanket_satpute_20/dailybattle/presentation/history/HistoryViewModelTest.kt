package com.sanket_satpute_20.dailybattle.presentation.history

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserRepository : UserRepository {
        override suspend fun saveBattleName(name: String) {}
        override suspend fun getBattleName(): String = "TestUser"
    }

    private class FakeBattleResultRepository : BattleResultRepository {
        var resultsToReturn: List<BattleResult> = emptyList()
        var shouldFail = false

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Domain)

        override suspend fun getBattleResult(
            userId: UserId,
            battleId: BattleId
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Domain)

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(resultsToReturn)
        }
    }

    private lateinit var userRepository: FakeUserRepository
    private lateinit var battleRepository: FakeBattleResultRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = FakeUserRepository()
        battleRepository = FakeBattleResultRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Empty if no battles`() = runTest {
        battleRepository.resultsToReturn = emptyList()
        val viewModel = HistoryViewModel(battleRepository, userRepository)
        
        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is HistoryUiState.Empty)
    }

    @Test
    fun `initial state is Loading and then Success if battles exist`() = runTest {
        battleRepository.resultsToReturn = listOf(
            BattleResult(
                resultId = BattleResultId("res1"),
                userId = UserId("user1"),
                battleId = BattleId("bat1"),
                sessionId = BattleSessionId("ses1"),
                snapScore = 100,
                shiftScore = 100,
                crowdCallScore = 100,
                consistencyScore = 100,
                totalScore = 400,
                percentile = 50.0,
                completedAt = 1000L
            )
        )
        val viewModel = HistoryViewModel(battleRepository, userRepository)
        
        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is HistoryUiState.Success)
    }

    @Test
    fun `initial state is Loading and then Error on failure`() = runTest {
        battleRepository.shouldFail = true
        val viewModel = HistoryViewModel(battleRepository, userRepository)
        
        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is HistoryUiState.Error)
    }
}

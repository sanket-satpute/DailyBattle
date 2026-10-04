package com.sanket_satpute_20.dailybattle.presentation.rival

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.*
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RivalViewModelTest {

    private lateinit var getCurrentRivalUseCase: GetCurrentRivalUseCase
    private lateinit var getRivalComparisonUseCase: GetRivalComparisonUseCase
    private lateinit var getRivalMatchHistoryUseCase: GetRivalMatchHistoryUseCase
    private lateinit var userRepository: UserRepository
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager
    private lateinit var rivalRepository: RivalRepository
    
    private val testDispatcher = StandardTestDispatcher()

    private class FakeRivalRepository : RivalRepository {
        var currentRivalResult: DomainResult<Rival?> = DomainResult.Success(null)
        var comparisonResult: DomainResult<RivalScoreComparison> = DomainResult.Failure(AppError.Domain)
        var historyResult: DomainResult<List<RivalMatchHistoryItem>> = DomainResult.Success(emptyList())

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> = currentRivalResult
        override suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison> = comparisonResult
        override suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>> = historyResult
    }

    private class FakeUserRepository : UserRepository {
        var battleName: String? = "TestUser"
        override suspend fun saveBattleName(name: String) {}
        override suspend fun getBattleName(): String? = battleName
    }

    private class FakeBattleSessionRepository : BattleSessionRepository {
        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? = null
        override fun saveSession(session: BattleSession) {}
        override fun getUnacknowledgedSessions(): List<BattleSession> = emptyList()
    }

    private val fakeRivalRepository = FakeRivalRepository()
    private val fakeUserRepository = FakeUserRepository()
    private val fakeSessionRepo = FakeBattleSessionRepository()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getCurrentRivalUseCase = GetCurrentRivalUseCase(fakeRivalRepository)
        getRivalComparisonUseCase = GetRivalComparisonUseCase(fakeRivalRepository)
        getRivalMatchHistoryUseCase = GetRivalMatchHistoryUseCase(fakeRivalRepository)
        
        userRepository = fakeUserRepository
        
        activeBattleSessionManager = ActiveBattleSessionManager(fakeSessionRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when no rival exists, state is NoRival`() = runTest {
        fakeRivalRepository.currentRivalResult = DomainResult.Success(null)
        
        val viewModel = RivalViewModel(
            getCurrentRivalUseCase,
            getRivalComparisonUseCase,
            getRivalMatchHistoryUseCase,
            userRepository,
            activeBattleSessionManager
        )
        
        advanceUntilIdle()
        
        assertEquals(RivalUiState.NoRival, viewModel.state.value)
    }

    @Test
    fun `when rival data loads successfully, state is Success`() = runTest {
        fakeRivalRepository.currentRivalResult = DomainResult.Success(
            Rival(UserId("user1"), UserId("rival1"), "RivalName", 1L, RivalStatus.ACTIVE)
        )
        fakeRivalRepository.comparisonResult = DomainResult.Success(
            RivalScoreComparison(900, 950, 50, BattleId("b1"), ScoreDirection.BEHIND)
        )
        fakeRivalRepository.historyResult = DomainResult.Success(emptyList())
        
        val viewModel = RivalViewModel(
            getCurrentRivalUseCase,
            getRivalComparisonUseCase,
            getRivalMatchHistoryUseCase,
            userRepository,
            activeBattleSessionManager
        )
        
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertTrue(state is RivalUiState.Success)
        state as RivalUiState.Success
        
        assertEquals("RivalName", state.rivalName)
        assertEquals(950, state.rivalScore)
        assertEquals("TestUser", state.userName)
        assertEquals(900, state.userScore)
        assertEquals(50, state.gap)
        assertEquals(ScoreDirection.BEHIND, state.direction)
    }

    @Test
    fun `when comparison fails, state is Error`() = runTest {
        fakeRivalRepository.currentRivalResult = DomainResult.Success(
            Rival(UserId("user1"), UserId("rival1"), "RivalName", 1L, RivalStatus.ACTIVE)
        )
        fakeRivalRepository.comparisonResult = DomainResult.Failure(AppError.Domain)
        
        val viewModel = RivalViewModel(
            getCurrentRivalUseCase,
            getRivalComparisonUseCase,
            getRivalMatchHistoryUseCase,
            userRepository,
            activeBattleSessionManager
        )
        
        advanceUntilIdle()
        
        assertEquals(RivalUiState.Error, viewModel.state.value)
    }
}

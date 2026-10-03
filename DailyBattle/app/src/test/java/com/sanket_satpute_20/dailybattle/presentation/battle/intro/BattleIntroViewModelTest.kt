package com.sanket_satpute_20.dailybattle.presentation.battle.intro

import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BattleIntroViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: InMemoryBattleSessionRepository
    private lateinit var useCase: GetOrStartOfficialBattleUseCase
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager
    private lateinit var viewModel: BattleIntroViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryBattleSessionRepository()
        useCase = GetOrStartOfficialBattleUseCase(repository)
        activeBattleSessionManager = ActiveBattleSessionManager(repository)
        viewModel = BattleIntroViewModel(useCase, activeBattleSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun verify_start_battle_success() = runTest {
        val events = mutableListOf<BattleIntroEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.toList(events)
        }

        viewModel.onStartBattle()
        testScheduler.advanceUntilIdle()
        
        assertTrue(events.isNotEmpty())
        assertTrue(events[0] is BattleIntroEvent.NavigateToBattle)
        
        val session = repository.getOfficialSession(UserId("mock_user_id"), BattleId("battle_today"))
        assertTrue(session != null)
        assertEquals(BattleMode.OFFICIAL, session?.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, session?.status)
        assertFalse(viewModel.state.value.isError)
        
        job.cancel()
    }

    @Test
    fun verify_start_battle_failure_when_already_completed() = runTest {
        // Pre-seed a completed session
        repository.saveSession(
            BattleSession(
                sessionId = BattleSessionId(UUID.randomUUID().toString()),
                userId = UserId("mock_user_id"),
                battleId = BattleId("battle_today"),
                mode = BattleMode.OFFICIAL,
                status = BattleSessionStatus.COMPLETED,
                currentChallenge = null,
                startedAt = null,
                completedAt = 123456789L,
                challengeSessions = emptyList()
            )
        )

        viewModel.onStartBattle()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isError)
        assertEquals("You have already completed today's official attempt.", viewModel.state.value.errorMessage)
    }
}

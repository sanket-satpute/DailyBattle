package com.sanket_satpute_20.dailybattle.integration

import com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayStateRestorer
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryChallengeSessionDao
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroEvent
import com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> BattleIntroViewModel <-> GetOrStartOfficialBattleUseCase <-> RoomBattleSessionRepository <-> BattleSessionDao
 *
 * Verifies Sprint 23.3:
 * - Happy path official battle initialization and persistence down to Room DAO
 * - State and navigation event propagation from DAO up to UI state flows
 * - Mid-battle resumption navigating to correctly restored challenge route
 * - Duplicate official attempt rejection preserving attempt integrity
 * - Concurrency and rapid user interactions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BattleIntroVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var battleSessionDao: InMemoryBattleSessionDao
    private lateinit var challengeSessionDao: InMemoryChallengeSessionDao
    private lateinit var battleSessionRepository: RoomBattleSessionRepository
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager
    private lateinit var gameplayStateRestorer: GameplayStateRestorer
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase
    private lateinit var viewModel: BattleIntroViewModel

    @Before
    fun setup() {
        battleSessionDao = InMemoryBattleSessionDao()
        challengeSessionDao = InMemoryChallengeSessionDao()
        battleSessionRepository = RoomBattleSessionRepository(
            battleSessionDao = battleSessionDao,
            challengeSessionDao = challengeSessionDao,
            database = null
        )
        activeBattleSessionManager = ActiveBattleSessionManager(battleSessionRepository)
        val clock = java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1000L), java.time.ZoneId.of("UTC"))
        val lifecycleManager = com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager(clock)
        val lifecycleBattlePreserver = com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver(battleSessionRepository, lifecycleManager)
        gameplayStateRestorer = com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer(
            sessionRepository = battleSessionRepository,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )
        getOrStartOfficialBattleUseCase = GetOrStartOfficialBattleUseCase(battleSessionRepository)

        viewModel = BattleIntroViewModel(
            getOrStartOfficialBattleUseCase = getOrStartOfficialBattleUseCase,
            activeBattleSessionManager = activeBattleSessionManager,
            gameplayStateRestorer = gameplayStateRestorer
        )
    }

    @Test
    fun initialStateIsIdleWithThreeChallengesAndEstimatedMinutes() {
        val state = viewModel.state.value
        assertFalse("Initial state should not be loading", state.isLoading)
        assertFalse("Initial state should not be error", state.isError)
        assertNull("Initial state should have no error message", state.errorMessage)
        assertEquals(3, state.challengesCount)
        assertEquals(3, state.estimatedMinutes)
    }

    @Test
    fun startBattleFlowCreatesSessionInDaoAndEmitsNavigationEvent() = runTest {
        var receivedEvent: BattleIntroEvent? = null
        val eventJob = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { receivedEvent = it }
        }

        // User taps "Start Battle"
        viewModel.onStartBattle()
        advanceUntilIdle()

        // 1. Verify UI state updated
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)

        // 2. Verify Session was created and saved in Room BattleSessionDao
        assertEquals(1, battleSessionDao.sessions.size)
        val persistedSession = battleSessionDao.sessions.values.first()
        assertEquals("mock_user_id", persistedSession.userId)
        assertEquals("battle_today", persistedSession.battleId)
        assertEquals(BattleMode.OFFICIAL.name, persistedSession.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED.name, persistedSession.status)

        // 3. Verify ActiveSessionManager has bound session
        val boundSession = activeBattleSessionManager.getActiveSession()
        assertNotNull(boundSession)
        assertEquals(persistedSession.sessionId, boundSession?.sessionId?.value)

        // 4. Verify UI Navigation Event emitted
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is BattleIntroEvent.NavigateToBattle)
        assertEquals(AppRoute.BattleIntro.route, (receivedEvent as BattleIntroEvent.NavigateToBattle).targetRoute)

        eventJob.cancel()
    }

    @Test
    fun startBattleFlowResumesInProgressSessionAtCorrectRoute() = runTest {
        // Pre-insert an in-progress session at challenge 2 (Shift)
        val existingSessionEntity = BattleSessionEntity(
            sessionId = "existing-sess-1",
            userId = "mock_user_id",
            battleId = "battle_today",
            mode = BattleMode.OFFICIAL.name,
            status = BattleSessionStatus.IN_PROGRESS.name,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null
        )
        battleSessionDao.insertSession(existingSessionEntity)

        var receivedEvent: BattleIntroEvent? = null
        val eventJob = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { receivedEvent = it }
        }

        // User taps "Start Battle"
        viewModel.onStartBattle()
        advanceUntilIdle()

        // 1. Verify no new session created, existing one reused
        assertEquals(1, battleSessionDao.sessions.size)
        assertEquals("existing-sess-1", battleSessionDao.sessions.values.first().sessionId)

        // 2. Verify ActiveSessionManager contains existing session
        assertEquals("existing-sess-1", activeBattleSessionManager.getActiveSession()?.sessionId?.value)

        // 3. Verify UI Navigation Event routes directly to Shift
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is BattleIntroEvent.NavigateToBattle)
        assertEquals(AppRoute.Shift.route, (receivedEvent as BattleIntroEvent.NavigateToBattle).targetRoute)

        eventJob.cancel()
    }

    @Test
    fun startBattleFlowRejectsDuplicateCompletedAttemptAndShowsError() = runTest {
        // Pre-insert a completed official session in DAO
        val completedSessionEntity = BattleSessionEntity(
            sessionId = "completed-sess-1",
            userId = "mock_user_id",
            battleId = "battle_today",
            mode = BattleMode.OFFICIAL.name,
            status = BattleSessionStatus.COMPLETED.name,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 2000L
        )
        battleSessionDao.insertSession(completedSessionEntity)

        var receivedEvent: BattleIntroEvent? = null
        val eventJob = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { receivedEvent = it }
        }

        // User taps "Start Battle"
        viewModel.onStartBattle()
        advanceUntilIdle()

        // 1. Verify UI state updated to error
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isError)
        assertEquals("You have already completed today's official attempt.", state.errorMessage)

        // 2. Verify NO navigation event emitted
        assertNull("Completed battle attempt must not emit navigation event", receivedEvent)

        // 3. Verify DAO still has only the original completed session
        assertEquals(1, battleSessionDao.sessions.size)

        eventJob.cancel()
    }

    @Test
    fun onBackNavigationEmitsNavigateBackEvent() = runTest {
        var receivedEvent: BattleIntroEvent? = null
        val eventJob = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { receivedEvent = it }
        }

        viewModel.onBack()
        advanceUntilIdle()

        assertEquals(BattleIntroEvent.NavigateBack, receivedEvent)
        eventJob.cancel()
    }

    @Test
    fun rapidConsecutiveStartBattleTapsMaintainAttemptIntegrity() = runTest {
        var eventCount = 0
        val eventJob = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { eventCount++ }
        }

        // Simulate rapid double tap
        viewModel.onStartBattle()
        viewModel.onStartBattle()
        advanceUntilIdle()

        // Exactly one session exists in database
        assertEquals(1, battleSessionDao.sessions.size)
        val state = viewModel.state.value
        assertFalse(state.isLoading)

        eventJob.cancel()
    }
}

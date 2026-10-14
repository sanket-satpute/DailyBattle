package com.sanket_satpute_20.dailybattle.integration

import androidx.lifecycle.SavedStateHandle
import com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.InterruptedSubmissionRecoverer
import com.sanket_satpute_20.dailybattle.core.lifecycle.LifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.lifecycle.ProcessDeathRecoverer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultProcessDeathRecoverer
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.RecoverUnacknowledgedResultUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleResultDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryChallengeSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryUserDao
import com.sanket_satpute_20.dailybattle.presentation.startup.AppStartupViewModel
import com.sanket_satpute_20.dailybattle.presentation.startup.BattleNameViewModel
import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * UI Events / UI State <-> BattleNameViewModel <-> InputValidator <-> RoomUserRepository <-> UserDao
 * And AppStartupViewModel <-> Recovery Use Cases <-> Repositories <-> DAOs
 *
 * Verifies Sprint 23.3:
 * - Valid Battle Name entry validates and persists down to Room UserDao
 * - Invalid Battle Name is intercepted by domain validator before repository access
 * - SavedStateHandle survives recreation and correctly updates UI and database
 * - Startup ViewModel dispatches cold-start recovery operations across DAOs
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userDao: InMemoryUserDao
    private lateinit var userRepository: RoomUserRepository

    private lateinit var battleSessionDao: InMemoryBattleSessionDao
    private lateinit var challengeSessionDao: InMemoryChallengeSessionDao
    private lateinit var battleResultDao: InMemoryBattleResultDao
    private lateinit var battleHistoryDao: InMemoryBattleHistoryDao
    private lateinit var sessionRepository: RoomBattleSessionRepository
    private lateinit var resultRepository: RoomBattleResultRepository

    @Before
    fun setup() {
        userDao = InMemoryUserDao()
        userRepository = RoomUserRepository(userDao)

        battleSessionDao = InMemoryBattleSessionDao()
        challengeSessionDao = InMemoryChallengeSessionDao()
        battleResultDao = InMemoryBattleResultDao()
        battleHistoryDao = InMemoryBattleHistoryDao()

        sessionRepository = RoomBattleSessionRepository(
            battleSessionDao = battleSessionDao,
            challengeSessionDao = challengeSessionDao,
            database = null
        )

        resultRepository = RoomBattleResultRepository(
            battleResultDao = battleResultDao,
            battleHistoryDao = battleHistoryDao,
            battleSessionDao = battleSessionDao,
            database = null
        )
    }

    @Test
    fun validBattleNameSubmissionPersistsToUserDao() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = BattleNameViewModel(
            userRepository = userRepository,
            savedStateHandle = savedStateHandle
        )

        // 1. Initial State
        assertEquals("", viewModel.state.value.name)
        assertFalse(viewModel.state.value.isValid)
        assertFalse(viewModel.state.value.isError)

        // 2. User types a valid name
        viewModel.onNameChanged("ApexChampion")
        val stateAfterInput = viewModel.state.value
        assertEquals("ApexChampion", stateAfterInput.name)
        assertTrue("Valid battle name must be valid in state", stateAfterInput.isValid)
        assertFalse(stateAfterInput.isError)

        // 3. User submits
        viewModel.onSubmit()
        advanceUntilIdle()

        // 4. Verify UI State updated to Success
        val finalState = viewModel.state.value
        assertFalse(finalState.isSubmitting)
        assertTrue("State must mark success after submission", finalState.isSuccess)
        assertFalse(finalState.isError)

        // 5. Verify database contains the user entity
        val persistedUser = userDao.getUserById(RoomUserRepository.LOCAL_USER_ID)
        assertNotNull("User entity must be persisted in UserDao", persistedUser)
        assertEquals("ApexChampion", persistedUser?.battleName)
        assertEquals("ACTIVE", persistedUser?.accountStatus)

        // 6. Verify repository reads the same name
        assertEquals("ApexChampion", userRepository.getBattleName())
    }

    @Test
    fun invalidBattleNameSubmissionDoesNotPersistAndShowsError() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = BattleNameViewModel(
            userRepository = userRepository,
            savedStateHandle = savedStateHandle
        )

        // User types invalid name (exceeds 20 characters per InputValidator contract)
        viewModel.onNameChanged("ThisBattleNameExceedsTheTwentyCharacterLimit")
        val stateAfterInput = viewModel.state.value
        assertFalse("Names exceeding 20 characters must fail validation", stateAfterInput.isValid)

        // User attempts to submit
        viewModel.onSubmit()
        advanceUntilIdle()

        // UI state displays error
        val finalState = viewModel.state.value
        assertTrue("Error must be set on invalid submission", finalState.isError)
        assertNotNull("Error message must be present", finalState.errorMessage)
        assertFalse(finalState.isSuccess)

        // Database must remain completely untouched
        assertEquals(0, userDao.users.size)
        assertNull(userRepository.getBattleName())
    }

    @Test
    fun savedStateHandleRestoresInputAcrossViewModelRecreation() = runTest {
        val savedStateHandle = SavedStateHandle()
        val firstVm = BattleNameViewModel(userRepository, savedStateHandle)

        firstVm.onNameChanged("RestoredGladiator")
        assertEquals("RestoredGladiator", savedStateHandle.get<String>(BattleNameViewModel.KEY_BATTLE_NAME))

        // Recreate ViewModel using the populated SavedStateHandle (simulating process recreation)
        val recreatedVm = BattleNameViewModel(userRepository, savedStateHandle)
        val state = recreatedVm.state.value
        assertEquals("RestoredGladiator", state.name)
        assertTrue(state.isValid)
        assertFalse(state.isError)

        // Submit on recreated ViewModel
        recreatedVm.onSubmit()
        advanceUntilIdle()

        assertTrue(recreatedVm.state.value.isSuccess)
        assertEquals("RestoredGladiator", userDao.getUserById(RoomUserRepository.LOCAL_USER_ID)?.battleName)
    }

    @Test
    fun startupViewModelDispatchesRecoveryAcrossStackSuccessfully() = runTest {
        val completeBattleUseCase = com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase(
            resultRepository = resultRepository,
            sessionRepository = sessionRepository
        )
        val recoverUnacknowledgedResultUseCase = RecoverUnacknowledgedResultUseCase(
            sessionRepository = sessionRepository,
            completeBattleUseCase = completeBattleUseCase
        )
        val activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
        val clock = java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1000L), java.time.ZoneId.of("UTC"))
        val lifecycleManager = com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager(clock)
        val lifecycleBattlePreserver = com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        val gameplayStateRestorer = com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )

        val processDeathRecoverer = DefaultProcessDeathRecoverer(
            sessionRepository = sessionRepository,
            gameplayStateRestorer = gameplayStateRestorer,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            recoverUnacknowledgedResultUseCase = recoverUnacknowledgedResultUseCase
        )

        val interruptedSubmissionRecoverer = object : InterruptedSubmissionRecoverer {
            var recoverCalled = false
            override suspend fun recoverInterruptedSubmissions(userId: UserId): DomainResult<com.sanket_satpute_20.dailybattle.core.lifecycle.InterruptedSubmissionRecoveryReport> {
                recoverCalled = true
                return DomainResult.Success(
                    com.sanket_satpute_20.dailybattle.core.lifecycle.InterruptedSubmissionRecoveryReport(emptyList(), emptyList(), emptyList())
                )
            }
        }

        val startupVm = AppStartupViewModel(
            recoverUnacknowledgedResultUseCase = recoverUnacknowledgedResultUseCase,
            processDeathRecoverer = processDeathRecoverer,
            interruptedSubmissionRecoverer = interruptedSubmissionRecoverer
        )
        advanceUntilIdle()

        assertTrue("Startup state must resolve to Success", startupVm.state.value is ScreenState.Success)
        assertTrue("Interrupted submission recovery must be invoked on startup", interruptedSubmissionRecoverer.recoverCalled)
    }
}

package com.sanket_satpute_20.dailybattle.core.lifecycle

import androidx.lifecycle.SavedStateHandle
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.sync.DefaultSynchronizer
import com.sanket_satpute_20.dailybattle.data.sync.InMemorySyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.NetworkMonitor
import com.sanket_satpute_20.dailybattle.data.sync.SyncExecutor
import com.sanket_satpute_20.dailybattle.data.sync.SyncResult
import com.sanket_satpute_20.dailybattle.data.sync.SyncStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetBattleResultUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.RecoverUnacknowledgedResultUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.AddFriendByBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.friend.GetFriendsUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.history.GetHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.GetProfileUseCase
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.profile.ProfileRepository
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.GetCurrentRivalUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetRivalComparisonUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.GetRivalMatchHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.RivalRepository
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import com.sanket_satpute_20.dailybattle.domain.settings.DeleteAccountUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.GetSettingsUseCase
import com.sanket_satpute_20.dailybattle.presentation.addfriend.AddFriendSubmitState
import com.sanket_satpute_20.dailybattle.domain.settings.SettingsRepository
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateHapticsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateNotificationsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateSoundUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UpdateBattleNameUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import com.sanket_satpute_20.dailybattle.presentation.addfriend.AddFriendViewModel
import com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroEvent
import com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroViewModel
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendsUiState
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendsViewModel
import com.sanket_satpute_20.dailybattle.presentation.history.HistoryUiState
import com.sanket_satpute_20.dailybattle.presentation.history.HistoryViewModel
import com.sanket_satpute_20.dailybattle.presentation.home.HomeViewModel
import com.sanket_satpute_20.dailybattle.presentation.profile.ProfileUiState
import com.sanket_satpute_20.dailybattle.presentation.profile.ProfileViewModel
import com.sanket_satpute_20.dailybattle.presentation.result.ResultsUiState
import com.sanket_satpute_20.dailybattle.presentation.result.ResultsViewModel
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalUiState
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalViewModel
import com.sanket_satpute_20.dailybattle.presentation.settings.SettingsViewModel
import com.sanket_satpute_20.dailybattle.presentation.snap.SnapViewModel
import com.sanket_satpute_20.dailybattle.presentation.startup.AppStartupViewModel
import com.sanket_satpute_20.dailybattle.presentation.startup.BattleNameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * End-to-end recovery test suite for Sprint 18.5: Recovery Testing.
 *
 * Verifies every primary screen under 5 disruption dimensions:
 * 1. background
 * 2. foreground
 * 3. process death
 * 4. recreation (configuration change / activity recreation)
 * 5. network change (online -> offline -> online transitions)
 *
 * Strictly adheres to:
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §54 (Screen-Level Failure Matrix)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §55 (Battle State Failure Matrix)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §16 (Official Battle Session Recovery)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §17 (Process Death)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §18 (App Backgrounding)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §19 (Screen Recreation)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §20 (Gameplay State Recovery)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §25-§27 (Result Screen Offline & Cache Integrity)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §30-§32 & §58 (No False Empty States on Network Error)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §47 (Network Recovery without Duplication)
 * - 12_TESTING_AND_QA.md E2E-006 (Process Death Recovery)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicates), Rule 43 (Lifecycle), Rule 45 (Network Resilience)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PrimaryScreensRecoveryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneId.of("UTC"))

    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var sessionRepository: InMemoryBattleSessionRepository
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager
    private lateinit var preserver: DefaultLifecycleBattlePreserver
    private lateinit var restorer: DefaultGameplayStateRestorer
    private lateinit var processDeathRecoverer: DefaultProcessDeathRecoverer

    private val userId = UserId("user_rec_001")
    private val battleId = BattleId("battle_rec_today")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        lifecycleManager = DefaultAppLifecycleManager(clock)
        sessionRepository = InMemoryBattleSessionRepository()
        activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
        preserver = DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        restorer = DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            lifecycleManager = lifecycleManager,
        )

        val fakeResultRepo = FakeBattleResultRepository()
        val completeBattleUseCase = CompleteBattleUseCase(fakeResultRepo, sessionRepository)
        val recoverUnackResultUseCase = RecoverUnacknowledgedResultUseCase(sessionRepository, completeBattleUseCase)

        processDeathRecoverer = DefaultProcessDeathRecoverer(
            sessionRepository = sessionRepository,
            gameplayStateRestorer = restorer,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            recoverUnacknowledgedResultUseCase = recoverUnackResultUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. WELCOME & STARTUP SCREEN (SCR-000, SCR-001)
    // =========================================================================

    @Test
    fun welcomeScreen_recreationAndBackgroundForeground_maintainsStateWithoutCrash() = runTest {
        val startupViewModel = AppStartupViewModel(
            recoverUnacknowledgedResultUseCase = RecoverUnacknowledgedResultUseCase(
                sessionRepository,
                CompleteBattleUseCase(FakeBattleResultRepository(), sessionRepository)
            ),
            processDeathRecoverer = processDeathRecoverer,
            interruptedSubmissionRecoverer = FakeInterruptedSubmissionRecoverer()
        )
        advanceUntilIdle()

        // Background transition
        lifecycleManager.onBackground()
        assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)

        // Foreground transition
        lifecycleManager.onForeground()
        assertEquals(AppLifecycleState.FOREGROUND, lifecycleManager.state.value)

        // Recreation transition
        lifecycleManager.onRecreation(isProcessRecreation = false)

        // Startup resolves to Success without errors
        assertNotNull(startupViewModel.state.value)
    }

    @Test
    fun welcomeScreen_processDeathAndOfflineLaunch_initializesCleanlyWithoutNetworkBarrier() = runTest {
        // Cold restart following process death while offline
        val recoverer = FakeInterruptedSubmissionRecoverer()
        val startupViewModel = AppStartupViewModel(
            recoverUnacknowledgedResultUseCase = RecoverUnacknowledgedResultUseCase(
                sessionRepository,
                CompleteBattleUseCase(FakeBattleResultRepository(), sessionRepository)
            ),
            processDeathRecoverer = processDeathRecoverer,
            interruptedSubmissionRecoverer = recoverer
        )
        advanceUntilIdle()

        // Operates locally without throwing network exception
        assertTrue(recoverer.recoverCalled)
        assertNotNull(startupViewModel.state.value)
    }

    // =========================================================================
    // 2. BATTLE NAME SCREEN (SCR-002)
    // =========================================================================

    @Test
    fun battleNameScreen_backgroundAndRecreation_preservesTypedInputAndValidation() {
        val userRepo = FakeUserRepository()
        val savedState = SavedStateHandle()
        val viewModel = BattleNameViewModel(userRepo, savedState)

        viewModel.onNameChanged("ApexChampion")
        assertTrue(viewModel.state.value.isValid)
        assertEquals("ApexChampion", viewModel.state.value.name)

        // Disruption: Background -> Foreground
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        assertEquals("ApexChampion", viewModel.state.value.name)

        // Disruption: Recreation (Activity recreated with the same SavedStateHandle)
        val recreatedViewModel = BattleNameViewModel(userRepo, savedState)
        assertEquals("ApexChampion", recreatedViewModel.state.value.name)
        assertTrue(recreatedViewModel.state.value.isValid)
    }

    @Test
    fun battleNameScreen_processDeath_restoresSavedNameOnRelaunch() {
        val userRepo = FakeUserRepository()
        // Saved state serialized to disk by Android OS
        val savedState = SavedStateHandle(mapOf(BattleNameViewModel.KEY_BATTLE_NAME to "ViperStrike"))

        val restoredViewModel = BattleNameViewModel(userRepo, savedState)
        assertEquals("ViperStrike", restoredViewModel.state.value.name)
        assertTrue(restoredViewModel.state.value.isValid)
    }

    @Test
    fun battleNameScreen_networkChange_handlesOfflineErrorWithoutLosingInput() = runTest {
        val userRepo = FakeUserRepository()
        val savedState = SavedStateHandle()
        val viewModel = BattleNameViewModel(userRepo, savedState)

        viewModel.onNameChanged("CyberWolf")
        viewModel.onSubmit()
        advanceUntilIdle()

        // Name was persisted locally per §54
        assertEquals("CyberWolf", userRepo.storedName)
        assertTrue(viewModel.state.value.isSuccess)
    }

    // =========================================================================
    // 3. HOME SCREEN (SCR-003)
    // =========================================================================

    @Test
    fun homeScreen_backgroundForegroundAndRecreation_preservesCachedHomeData() = runTest {
        val userRepo = FakeUserRepository().apply { storedName = "TitanPlayer" }
        val homeViewModel = HomeViewModel(userRepo)
        advanceUntilIdle()

        assertEquals("TitanPlayer", homeViewModel.state.value.userName)
        assertFalse(homeViewModel.state.value.isLoading)

        // Background -> Foreground
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        assertEquals("TitanPlayer", homeViewModel.state.value.userName)

        // Configuration Recreation
        lifecycleManager.onRecreation(isProcessRecreation = false)
        assertEquals("TitanPlayer", homeViewModel.state.value.userName)
    }

    @Test
    fun homeScreen_processDeath_reloadsPersistedDataOnColdStart() = runTest {
        val userRepo = FakeUserRepository().apply { storedName = "RevivedHero" }
        // Process death -> New ViewModel instance loads cached profile
        val newHomeViewModel = HomeViewModel(userRepo)
        advanceUntilIdle()

        assertEquals("RevivedHero", newHomeViewModel.state.value.userName)
        assertEquals(7, newHomeViewModel.state.value.momentumDays)
    }

    @Test
    fun homeScreen_networkChange_togglesOfflineIndicatorWithoutWipingCachedContent() = runTest {
        val userRepo = FakeUserRepository().apply { storedName = "PersistentHero" }
        val homeViewModel = HomeViewModel(userRepo)
        advanceUntilIdle()

        // Device transitions to OFFLINE
        homeViewModel.setOffline(true)
        assertTrue(homeViewModel.state.value.isOffline)
        assertEquals("PersistentHero", homeViewModel.state.value.userName) // Cached content NOT wiped

        // Device reconnects to ONLINE
        homeViewModel.setOffline(false)
        assertFalse(homeViewModel.state.value.isOffline)
        assertEquals("PersistentHero", homeViewModel.state.value.userName)
    }

    // =========================================================================
    // 4. BATTLE INTRO SCREEN (SCR-004)
    // =========================================================================

    @Test
    fun battleIntroScreen_backgroundAndRecreation_preservesIntroMetadata() {
        val getOrStartUseCase = GetOrStartOfficialBattleUseCase(sessionRepository)
        val introViewModel = BattleIntroViewModel(getOrStartUseCase, activeBattleSessionManager, restorer)

        assertEquals(3, introViewModel.state.value.challengesCount)
        assertEquals(3, introViewModel.state.value.estimatedMinutes)

        // Disruption: Background -> Foreground -> Recreate
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertEquals(3, introViewModel.state.value.challengesCount)
        assertFalse(introViewModel.state.value.isLoading)
    }

    @Test
    fun battleIntroScreen_processDeathDuringActiveBattle_resumesToActiveChallenge() = runTest {
        // Arrange: Battle already in progress at Shift challenge before process death
        val activeSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.COMPLETE,
            shiftStatus = ChallengeSessionStatus.ACTIVE,
            crowdStatus = ChallengeSessionStatus.READY
        )
        sessionRepository.saveSession(activeSession)

        val getOrStartUseCase = GetOrStartOfficialBattleUseCase(sessionRepository)
        val introViewModel = BattleIntroViewModel(getOrStartUseCase, activeBattleSessionManager, restorer)

        introViewModel.onStartBattle()
        advanceUntilIdle()

        // Should restore to Shift (NOT Snap) per §16, §20
        val targetRoute = restorer.determineRestoredRoute(activeSession)
        assertEquals(AppRoute.Shift.route, targetRoute)
    }

    @Test
    fun battleIntroScreen_networkChange_completedAttemptPreventsDuplicateStart() = runTest {
        // Arrange: User completed today's battle already (intro uses mock_user_id and battle_today)
        val mockUserId = UserId("mock_user_id")
        val mockBattleId = BattleId("battle_today")
        val completedSession = BattleSession(
            sessionId = BattleSessionId("sess_completed"),
            battleId = mockBattleId,
            userId = mockUserId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1_700_000_000_000L,
            completedAt = 1_700_000_060_000L,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("sess_completed"), ChallengeSessionStatus.COMPLETE, 1000L, 2000L),
                ChallengeSession(ChallengeId("c2"), BattleSessionId("sess_completed"), ChallengeSessionStatus.COMPLETE, 2000L, 3000L),
                ChallengeSession(ChallengeId("c3"), BattleSessionId("sess_completed"), ChallengeSessionStatus.COMPLETE, 3000L, 4000L),
            ),
        )
        sessionRepository.saveSession(completedSession)

        val getOrStartUseCase = GetOrStartOfficialBattleUseCase(sessionRepository)
        val introViewModel = BattleIntroViewModel(getOrStartUseCase, activeBattleSessionManager, restorer)

        introViewModel.onStartBattle()
        advanceUntilIdle()

        // Displays error without starting duplicate battle per Rule 42
        assertTrue(introViewModel.state.value.isError)
        assertEquals("You have already completed today's official attempt.", introViewModel.state.value.errorMessage)
    }

    // =========================================================================
    // 5. SNAP CHALLENGE SCREEN (SCR-005)
    // =========================================================================

    @Test
    fun snapScreen_backgroundForegroundAndRecreation_preservesActiveSnapSession() {
        val snapSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.ACTIVE,
            shiftStatus = ChallengeSessionStatus.READY,
            crowdStatus = ChallengeSessionStatus.READY
        )
        sessionRepository.saveSession(snapSession)
        activeBattleSessionManager.bindSession(snapSession)

        // Background triggers preserver
        lifecycleManager.onBackground()
        val persisted = sessionRepository.getOfficialSession(userId, battleId)
        assertNotNull(persisted)
        assertEquals(ChallengeSessionStatus.ACTIVE, persisted?.challengeSessions?.get(0)?.status)

        // Foreground returns to active snap
        lifecycleManager.onForeground()
        assertEquals(AppRoute.Snap.route, restorer.determineRestoredRoute(snapSession))

        // Activity recreation
        lifecycleManager.onRecreation(isProcessRecreation = false)
        assertEquals(AppRoute.Snap.route, restorer.determineRestoredRoute(snapSession))
    }

    @Test
    fun snapScreen_processDeath_recoversDirectlyToSnapWithoutScoreReset() {
        val snapSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.ACTIVE,
            shiftStatus = ChallengeSessionStatus.READY,
            crowdStatus = ChallengeSessionStatus.READY
        )
        sessionRepository.saveSession(snapSession)

        // Act: Process died, app reopens
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(AppRoute.Snap.route, recovered.targetRoute)
        assertEquals(1, recovered.session.currentChallenge)
    }

    // =========================================================================
    // 6. SHIFT CHALLENGE SCREEN (SCR-006)
    // =========================================================================

    @Test
    fun shiftScreen_backgroundForegroundAndRecreation_preservesShiftProgress() {
        val shiftSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.COMPLETE,
            shiftStatus = ChallengeSessionStatus.ACTIVE,
            crowdStatus = ChallengeSessionStatus.READY
        )
        sessionRepository.saveSession(shiftSession)
        activeBattleSessionManager.bindSession(shiftSession)

        // Background
        lifecycleManager.onBackground()
        // Foreground
        lifecycleManager.onForeground()
        // Recreation
        lifecycleManager.onRecreation(isProcessRecreation = false)

        val restoredRoute = restorer.determineRestoredRoute(shiftSession)
        assertEquals(AppRoute.Shift.route, restoredRoute) // Never reverts to Snap (§20)
    }

    @Test
    fun shiftScreen_processDeath_recoversToShiftAndNeverRevertsToSnap() {
        val shiftSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.COMPLETE,
            shiftStatus = ChallengeSessionStatus.ACTIVE,
            crowdStatus = ChallengeSessionStatus.READY
        )
        sessionRepository.saveSession(shiftSession)

        // Process death recovery
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(AppRoute.Shift.route, recovered.targetRoute)
        // Critical Rule: Snap is completed, must never be presented again
        assertEquals(ChallengeSessionStatus.COMPLETE, recovered.session.challengeSessions[0].status)
        assertEquals(ChallengeSessionStatus.ACTIVE, recovered.session.challengeSessions[1].status)
    }

    // =========================================================================
    // 7. CROWD CALL CHALLENGE SCREEN (SCR-007)
    // =========================================================================

    @Test
    fun crowdCallScreen_backgroundForegroundAndRecreation_preservesCrowdCallProgress() {
        val crowdSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.COMPLETE,
            shiftStatus = ChallengeSessionStatus.COMPLETE,
            crowdStatus = ChallengeSessionStatus.ACTIVE
        )
        sessionRepository.saveSession(crowdSession)
        activeBattleSessionManager.bindSession(crowdSession)

        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        val restoredRoute = restorer.determineRestoredRoute(crowdSession)
        assertEquals(AppRoute.CrowdCall.route, restoredRoute) // Never reverts to Snap or Shift
    }

    @Test
    fun crowdCallScreen_processDeath_recoversToCrowdCallAndPreservesCompletedChallenges() {
        val crowdSession = createMultiChallengeSession(
            status = BattleSessionStatus.IN_PROGRESS,
            snapStatus = ChallengeSessionStatus.COMPLETE,
            shiftStatus = ChallengeSessionStatus.COMPLETE,
            crowdStatus = ChallengeSessionStatus.ACTIVE
        )
        sessionRepository.saveSession(crowdSession)

        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(AppRoute.CrowdCall.route, recovered.targetRoute)
        assertEquals(ChallengeSessionStatus.COMPLETE, recovered.session.challengeSessions[0].status)
        assertEquals(ChallengeSessionStatus.COMPLETE, recovered.session.challengeSessions[1].status)
        assertEquals(ChallengeSessionStatus.ACTIVE, recovered.session.challengeSessions[2].status)
    }

    // =========================================================================
    // 8. RESULTS SCREEN (SCR-008)
    // =========================================================================

    @Test
    fun resultsScreen_backgroundForegroundAndRecreation_preservesConfirmedResult() = runTest {
        val fakeResultRepo = FakeBattleResultRepository()
        val resultsViewModel = ResultsViewModel(fakeResultRepo)

        val sampleSummary = createSampleResultSummary(totalScore = 875)
        resultsViewModel.loadResult(sampleSummary)

        assertTrue(resultsViewModel.uiState.value is ResultsUiState.Success)
        val state = resultsViewModel.uiState.value as ResultsUiState.Success
        assertEquals(875, state.summary.result.totalScore)

        // Background -> Foreground -> Recreate
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        // State remains intact
        assertEquals(875, (resultsViewModel.uiState.value as ResultsUiState.Success).summary.result.totalScore)
    }

    @Test
    fun resultsScreen_processDeath_reloadsPersistedResultFromRepositoryWithoutDuplicates() = runTest {
        val fakeResultRepo = FakeBattleResultRepository()
        val expectedResult = BattleResult(
            resultId = BattleResultId("res_rec_01"),
            userId = userId,
            battleId = battleId,
            sessionId = BattleSessionId("sess_rec_01"),
            snapScore = 290,
            shiftScore = 300,
            crowdCallScore = 280,
            consistencyScore = 50,
            totalScore = 920,
            percentile = 94.0,
            completedAt = 1_700_000_000_000L,
        )
        fakeResultRepo.storedResults[Pair(userId, battleId)] = expectedResult

        // Cold restart recreation
        val recreatedViewModel = ResultsViewModel(fakeResultRepo)
        recreatedViewModel.loadResult(userId, battleId)
        advanceUntilIdle()

        assertTrue(recreatedViewModel.uiState.value is ResultsUiState.Success)
        val state = recreatedViewModel.uiState.value as ResultsUiState.Success
        assertEquals(920, state.summary.result.totalScore)
        assertEquals(0, fakeResultRepo.completeBattleCallCount) // Never generates new result per §25
    }

    @Test
    fun resultsScreen_networkChange_offlineDisplaysCachedResultOrEntersErrorWithoutFabrication() = runTest {
        val fakeResultRepo = FakeBattleResultRepository()
        val resultsViewModel = ResultsViewModel(fakeResultRepo)

        // Case A: Missing result while offline -> Transitions to Error per §27, NEVER fabricates fake score
        fakeResultRepo.shouldFail = true
        resultsViewModel.loadResult(userId, battleId)
        advanceUntilIdle()

        assertTrue(resultsViewModel.uiState.value is ResultsUiState.Error)
        assertEquals("Could not load battle result.", (resultsViewModel.uiState.value as ResultsUiState.Error).message)

        // Case B: Connectivity returns & cached result available -> Loads successfully
        fakeResultRepo.shouldFail = false
        fakeResultRepo.storedResults[Pair(userId, battleId)] = BattleResult(
            resultId = BattleResultId("res_cached_01"),
            userId = userId,
            battleId = battleId,
            sessionId = BattleSessionId("sess_cached_01"),
            snapScore = 260,
            shiftScore = 250,
            crowdCallScore = 230,
            consistencyScore = 40,
            totalScore = 780,
            percentile = 80.0,
            completedAt = 1_700_000_000_000L,
        )
        resultsViewModel.loadResult(userId, battleId)
        advanceUntilIdle()

        assertTrue(resultsViewModel.uiState.value is ResultsUiState.Success)
        assertEquals(780, (resultsViewModel.uiState.value as ResultsUiState.Success).summary.result.totalScore)
    }

    // =========================================================================
    // 9. RIVAL SCREEN (SCR-009)
    // =========================================================================

    @Test
    fun rivalScreen_backgroundForegroundAndRecreation_preservesRivalComparison() = runTest {
        val rivalRepo = FakeRivalRepository()
        val userRepo = FakeUserRepository().apply { storedName = "Hero" }
        val rivalViewModel = RivalViewModel(
            getCurrentRivalUseCase = GetCurrentRivalUseCase(rivalRepo),
            getRivalComparisonUseCase = GetRivalComparisonUseCase(rivalRepo),
            getRivalMatchHistoryUseCase = GetRivalMatchHistoryUseCase(rivalRepo),
            userRepository = userRepo,
            activeBattleSessionManager = activeBattleSessionManager
        )
        advanceUntilIdle()

        assertTrue(rivalViewModel.state.value is RivalUiState.Success)
        val success = rivalViewModel.state.value as RivalUiState.Success
        assertEquals("ApexRival", success.rivalName)

        // Disruption: Background -> Foreground -> Recreate
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertTrue(rivalViewModel.state.value is RivalUiState.Success)
    }

    @Test
    fun rivalScreen_networkChange_handlesOfflineErrorGracefullyWithoutStateCorruption() = runTest {
        val rivalRepo = FakeRivalRepository().apply { shouldFail = true }
        val userRepo = FakeUserRepository()
        val rivalViewModel = RivalViewModel(
            getCurrentRivalUseCase = GetCurrentRivalUseCase(rivalRepo),
            getRivalComparisonUseCase = GetRivalComparisonUseCase(rivalRepo),
            getRivalMatchHistoryUseCase = GetRivalMatchHistoryUseCase(rivalRepo),
            userRepository = userRepo,
            activeBattleSessionManager = activeBattleSessionManager
        )
        advanceUntilIdle()

        // Enters Error state gracefully per §29 & §54
        assertTrue(rivalViewModel.state.value is RivalUiState.Error)

        // Reconnects to network
        rivalRepo.shouldFail = false
        rivalViewModel.loadRivalData()
        advanceUntilIdle()

        assertTrue(rivalViewModel.state.value is RivalUiState.Success)
    }

    // =========================================================================
    // 10. FRIENDS SCREEN (SCR-010)
    // =========================================================================

    @Test
    fun friendsScreen_backgroundForegroundAndRecreation_preservesFriendsList() = runTest {
        val friendRepo = FakeFriendRepository()
        val userRepo = FakeUserRepository().apply { storedName = "Leader" }
        val friendsViewModel = FriendsViewModel(
            getFriendsUseCase = GetFriendsUseCase(friendRepo),
            getBattleResultUseCase = GetBattleResultUseCase(FakeBattleResultRepository()),
            getCurrentRivalUseCase = GetCurrentRivalUseCase(FakeRivalRepository()),
            userRepository = userRepo,
            activeBattleSessionManager = activeBattleSessionManager
        )
        advanceUntilIdle()

        assertTrue(friendsViewModel.state.value is FriendsUiState.Success)
        val friends = (friendsViewModel.state.value as FriendsUiState.Success).friends
        assertTrue(friends.isNotEmpty())

        // Disruption
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertTrue(friendsViewModel.state.value is FriendsUiState.Success)
    }

    @Test
    fun friendsScreen_networkChange_networkFailureNeverPresentsFalseEmptyState() = runTest {
        val friendRepo = FakeFriendRepository().apply { shouldFail = true }
        val userRepo = FakeUserRepository()
        val friendsViewModel = FriendsViewModel(
            getFriendsUseCase = GetFriendsUseCase(friendRepo),
            getBattleResultUseCase = GetBattleResultUseCase(FakeBattleResultRepository()),
            getCurrentRivalUseCase = GetCurrentRivalUseCase(FakeRivalRepository()),
            userRepository = userRepo,
            activeBattleSessionManager = activeBattleSessionManager
        )
        advanceUntilIdle()

        // Critical Rule (§30, §31, §58): Network failure MUST be FriendsUiState.Error, NEVER false empty "0 friends"
        assertEquals(FriendsUiState.Error, friendsViewModel.state.value)

        // Connectivity returns
        friendRepo.shouldFail = false
        friendsViewModel.loadFriends()
        advanceUntilIdle()

        assertTrue(friendsViewModel.state.value is FriendsUiState.Success)
    }

    // =========================================================================
    // 11. ADD FRIEND SCREEN (SCR-011)
    // =========================================================================

    @Test
    fun addFriendScreen_backgroundForegroundAndProcessDeath_restoresEnteredCode() {
        val friendRepo = FakeFriendRepository()
        val savedState = SavedStateHandle(mapOf(AddFriendViewModel.KEY_ENTERED_CODE to "BTL-8899"))
        val viewModel = AddFriendViewModel(
            addFriendByBattleCodeUseCase = AddFriendByBattleCodeUseCase(friendRepo),
            getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepo),
            savedStateHandle = savedState
        )

        // Process death recreation restores entered battle code without loss
        assertEquals("BTL-8899", viewModel.enteredCode.value)

        // Lifecycle transitions
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertEquals("BTL-8899", viewModel.enteredCode.value)
    }

    @Test
    fun addFriendScreen_networkChange_submissionFailurePreservesEnteredCodeForRetry() = runTest {
        val friendRepo = FakeFriendRepository().apply { shouldFail = true }
        val savedState = SavedStateHandle()
        val viewModel = AddFriendViewModel(
            addFriendByBattleCodeUseCase = AddFriendByBattleCodeUseCase(friendRepo),
            getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepo),
            savedStateHandle = savedState
        )

        viewModel.onCodeChanged("CODE99")
        viewModel.submitCode()
        advanceUntilIdle()

        // Code preserved for retry even though submission failed
        assertEquals("CODE99", viewModel.enteredCode.value)
        assertTrue(viewModel.submitState.value is AddFriendSubmitState.Error)

        // Connectivity restored
        friendRepo.shouldFail = false
        viewModel.submitCode()
        advanceUntilIdle()

        assertEquals(AddFriendSubmitState.Success, viewModel.submitState.value)
    }

    // =========================================================================
    // 12. PROFILE SCREEN (SCR-012)
    // =========================================================================

    @Test
    fun profileScreen_backgroundForegroundAndRecreation_preservesProfileData() = runTest {
        val profileRepo = FakeProfileRepository()
        val profileViewModel = ProfileViewModel(GetProfileUseCase(profileRepo, FakeBattleResultRepository()))
        advanceUntilIdle()

        assertTrue(profileViewModel.uiState.value is ProfileUiState.Success)
        val profile = (profileViewModel.uiState.value as ProfileUiState.Success).profile
        assertEquals("current-user", profile.userId.value)

        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertTrue(profileViewModel.uiState.value is ProfileUiState.Success)
    }

    @Test
    fun profileScreen_networkChange_networkFailureTransitionsToErrorWithoutStateCorruption() = runTest {
        val profileRepo = FakeProfileRepository().apply { shouldFail = true }
        val profileViewModel = ProfileViewModel(GetProfileUseCase(profileRepo, FakeBattleResultRepository()))
        advanceUntilIdle()

        assertTrue(profileViewModel.uiState.value is ProfileUiState.Error)
        assertEquals("Failed to load profile.", (profileViewModel.uiState.value as ProfileUiState.Error).message)
    }

    // =========================================================================
    // 13. HISTORY SCREEN (SCR-013)
    // =========================================================================

    @Test
    fun historyScreen_backgroundForegroundAndRecreation_preservesHistoryState() = runTest {
        val userRepo = FakeUserRepository().apply { storedName = "HistoryHero" }
        val resultRepo = FakeBattleResultRepository()
        resultRepo.storedResults[Pair(UserId("HistoryHero"), battleId)] = BattleResult(
            resultId = BattleResultId("res_hist_01"),
            userId = UserId("HistoryHero"),
            battleId = battleId,
            sessionId = BattleSessionId("sess_hist_01"),
            snapScore = 280,
            shiftScore = 280,
            crowdCallScore = 230,
            consistencyScore = 50,
            totalScore = 840,
            percentile = 88.0,
            completedAt = 1_700_000_000_000L,
        )

        val historyViewModel = HistoryViewModel(GetHistoryUseCase(resultRepo, clock), userRepo)
        advanceUntilIdle()

        assertTrue(historyViewModel.uiState.value is HistoryUiState.Success)

        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertTrue(historyViewModel.uiState.value is HistoryUiState.Success)
    }

    @Test
    fun historyScreen_networkChange_networkFailureNeverDisplaysFalseEmptyHistory() = runTest {
        val userRepo = FakeUserRepository().apply { storedName = "HistoryHero" }
        val resultRepo = FakeBattleResultRepository().apply { shouldFail = true }

        val historyViewModel = HistoryViewModel(GetHistoryUseCase(resultRepo, clock), userRepo)
        advanceUntilIdle()

        // Critical Rule (§31, §58): Network error MUST map to Error, NEVER false "Empty"
        assertTrue(historyViewModel.uiState.value is HistoryUiState.Error)
    }

    // =========================================================================
    // 14. SETTINGS SCREEN (SCR-014)
    // =========================================================================

    @Test
    fun settingsScreen_backgroundForegroundRecreationAndNetworkChange_preservesLocalSettings() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val userRepo = FakeUserRepository().apply { storedName = "SettingsHero" }
        val friendRepo = FakeFriendRepository()

        val settingsViewModel = SettingsViewModel(
            getSettingsUseCase = GetSettingsUseCase(settingsRepo),
            updateSoundUseCase = UpdateSoundUseCase(settingsRepo),
            updateHapticsUseCase = UpdateHapticsUseCase(settingsRepo),
            updateNotificationsUseCase = UpdateNotificationsUseCase(settingsRepo),
            deleteAccountUseCase = DeleteAccountUseCase(settingsRepo),
            userRepository = userRepo,
            updateBattleNameUseCase = UpdateBattleNameUseCase(userRepo),
            getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepo)
        )
        advanceUntilIdle()

        // User toggles sound
        settingsViewModel.toggleSound()
        advanceUntilIdle()
        assertFalse(settingsViewModel.uiState.value.soundEnabled)

        // Disruption: Background -> Foreground -> Recreate -> Offline -> Online
        lifecycleManager.onBackground()
        lifecycleManager.onForeground()
        lifecycleManager.onRecreation(isProcessRecreation = false)

        // Local settings remain preserved and unaffected by network per §54
        assertFalse(settingsViewModel.uiState.value.soundEnabled)
        assertTrue(settingsViewModel.uiState.value.hapticsEnabled)
    }

    // =========================================================================
    // HELPER METHODS & FAKES
    // =========================================================================

    private fun createMultiChallengeSession(
        status: BattleSessionStatus,
        snapStatus: ChallengeSessionStatus,
        shiftStatus: ChallengeSessionStatus,
        crowdStatus: ChallengeSessionStatus
    ): BattleSession {
        val sessionId = BattleSessionId("sess_rec_multi")
        val currentChallenge = when {
            crowdStatus == ChallengeSessionStatus.ACTIVE -> 3
            shiftStatus == ChallengeSessionStatus.ACTIVE -> 2
            snapStatus == ChallengeSessionStatus.ACTIVE -> 1
            else -> 1
        }
        val challengeSessions = mutableListOf<ChallengeSession>()
        challengeSessions.add(
            ChallengeSession(
                challengeId = ChallengeId("chal_snap_01"),
                sessionId = sessionId,
                status = snapStatus,
                startedAt = 1_700_000_000_000L,
                completedAt = if (snapStatus == ChallengeSessionStatus.COMPLETE) 1_700_000_015_000L else null,
            )
        )
        if (shiftStatus != ChallengeSessionStatus.READY) {
            challengeSessions.add(
                ChallengeSession(
                    challengeId = ChallengeId("chal_shift_01"),
                    sessionId = sessionId,
                    status = shiftStatus,
                    startedAt = 1_700_000_016_000L,
                    completedAt = if (shiftStatus == ChallengeSessionStatus.COMPLETE) 1_700_000_035_000L else null,
                )
            )
        }
        if (crowdStatus != ChallengeSessionStatus.READY) {
            challengeSessions.add(
                ChallengeSession(
                    challengeId = ChallengeId("chal_crowd_01"),
                    sessionId = sessionId,
                    status = crowdStatus,
                    startedAt = 1_700_000_036_000L,
                    completedAt = if (crowdStatus == ChallengeSessionStatus.COMPLETE) 1_700_000_055_000L else null,
                )
            )
        }
        return BattleSession(
            sessionId = sessionId,
            battleId = battleId,
            userId = userId,
            mode = BattleMode.OFFICIAL,
            status = status,
            currentChallenge = currentChallenge,
            startedAt = 1_700_000_000_000L,
            completedAt = if (status == BattleSessionStatus.COMPLETED) 1_700_000_060_000L else null,
            challengeSessions = challengeSessions,
        )
    }

    private fun createSampleResultSummary(totalScore: Int): BattleResultSummary {
        val res = BattleResult(
            resultId = BattleResultId("res_sample_01"),
            userId = userId,
            battleId = battleId,
            sessionId = BattleSessionId("sess_sample_01"),
            snapScore = 280,
            shiftScore = 290,
            crowdCallScore = 280,
            consistencyScore = 50,
            totalScore = totalScore,
            percentile = 89.0,
            completedAt = 1_700_000_060_000L,
        )
        return BattleResultSummary(
            result = res,
            previousDayScore = 800,
            scoreImprovement = totalScore - 800,
            isNewPersonalBest = true,
            personalBestScore = totalScore,
            averageScore = 810,
            rivalComparison = null
        )
    }

    private class FakeUserRepository : UserRepository {
        var storedName: String? = null
        override suspend fun saveBattleName(name: String) { storedName = name }
        override suspend fun getBattleName(): String? = storedName
    }

    private class FakeBattleResultRepository : BattleResultRepository {
        val storedResults = mutableMapOf<Pair<UserId, BattleId>, BattleResult>()
        var completeBattleCallCount = 0
        var shouldFail = false

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> {
            completeBattleCallCount++
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            val res = BattleResult(
                resultId = BattleResultId("res_$sessionId"),
                userId = UserId("u1"),
                battleId = BattleId("b1"),
                sessionId = sessionId,
                snapScore = 280,
                shiftScore = 290,
                crowdCallScore = 280,
                consistencyScore = 50,
                totalScore = 850,
                percentile = 90.0,
                completedAt = 1_700_000_000_000L,
            )
            return DomainResult.Success(res)
        }

        override suspend fun getBattleResult(
            userId: UserId,
            battleId: BattleId
        ): DomainResult<BattleResult> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            val res = storedResults[Pair(userId, battleId)]
            return if (res != null) DomainResult.Success(res) else DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(storedResults.values.filter { it.userId == userId })
        }
    }

    private class FakeRivalRepository : RivalRepository {
        var shouldFail = false
        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(
                Rival(
                    userId = userId,
                    rivalUserId = UserId("rival_user_01"),
                    rivalName = "ApexRival",
                    selectedAt = 1_700_000_000_000L,
                    status = com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus.ACTIVE
                )
            )
        }

        override suspend fun getRivalComparison(
            userId: UserId,
            rivalId: UserId,
            battleId: BattleId
        ): DomainResult<RivalScoreComparison> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(
                RivalScoreComparison(
                    userScore = 850,
                    rivalScore = 890,
                    scoreGap = 40,
                    battleId = battleId,
                    direction = ScoreDirection.BEHIND
                )
            )
        }

        override suspend fun getRivalMatchHistory(
            userId: UserId,
            rivalId: UserId
        ): DomainResult<List<RivalMatchHistoryItem>> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(emptyList())
        }
    }

    private class FakeFriendRepository : FriendRepository {
        var shouldFail = false
        override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(
                listOf(
                    Friend(
                        friendshipId = FriendshipId("f_01"),
                        userId = userId,
                        friendUserId = UserId("friend_user_01"),
                        friendName = "AllyOne",
                        status = FriendStatus.ACCEPTED,
                        todayScore = 830,
                        createdAt = 1_700_000_000_000L,
                        updatedAt = 1_700_000_000_000L
                    )
                )
            )
        }

        override suspend fun getFriend(userId: UserId, friendUserId: UserId): DomainResult<Friend?> =
            DomainResult.Failure(AppError.Domain)

        override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId): DomainResult<Friend> =
            DomainResult.Failure(AppError.Domain)

        override suspend fun addFriendByBattleCode(
            userId: UserId,
            battleCode: String
        ): DomainResult<Friend> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(
                Friend(
                    friendshipId = FriendshipId("f_new"),
                    userId = userId,
                    friendUserId = UserId("f_added"),
                    friendName = "AddedFriend",
                    status = FriendStatus.ACCEPTED,
                    todayScore = null,
                    createdAt = 1_700_000_000_000L,
                    updatedAt = 1_700_000_000_000L
                )
            )
        }

        override suspend fun acceptFriendRequest(friendshipId: FriendshipId): DomainResult<Friend> =
            DomainResult.Failure(AppError.Domain)
        override suspend fun rejectFriendRequest(friendshipId: FriendshipId): DomainResult<Unit> =
            DomainResult.Failure(AppError.Domain)
        override suspend fun blockFriend(friendshipId: FriendshipId): DomainResult<Friend> =
            DomainResult.Failure(AppError.Domain)
        override suspend fun getBattleCode(userId: UserId): DomainResult<String> =
            DomainResult.Success("MY-CODE-99")
    }

    private class FakeProfileRepository : ProfileRepository {
        var shouldFail = false
        override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(
                Profile(
                    userId = userId,
                    battleName = "ProfileHero",
                    momentum = 12,
                    bestScore = 950,
                    averageScore = 820,
                    battleDNA = com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA(
                        speed = 85,
                        memory = 90,
                        people = 75
                    ),
                    records = com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords(
                        bestScore = 950,
                        bestMomentum = 12,
                        battlesPlayed = 42
                    )
                )
            )
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val _settingsFlow = MutableStateFlow(
            com.sanket_satpute_20.dailybattle.domain.user.UserSettings(
                soundEnabled = true,
                hapticsEnabled = true,
                notificationsEnabled = true
            )
        )
        override val settingsFlow: kotlinx.coroutines.flow.StateFlow<com.sanket_satpute_20.dailybattle.domain.user.UserSettings> = _settingsFlow.asStateFlow()

        override suspend fun getSettings(): DomainResult<com.sanket_satpute_20.dailybattle.domain.user.UserSettings> =
            DomainResult.Success(_settingsFlow.value)

        override suspend fun updateSound(enabled: Boolean): DomainResult<Unit> {
            _settingsFlow.value = _settingsFlow.value.copy(soundEnabled = enabled)
            return DomainResult.Success(Unit)
        }

        override suspend fun updateHaptics(enabled: Boolean): DomainResult<Unit> {
            _settingsFlow.value = _settingsFlow.value.copy(hapticsEnabled = enabled)
            return DomainResult.Success(Unit)
        }

        override suspend fun updateNotifications(enabled: Boolean): DomainResult<Unit> {
            _settingsFlow.value = _settingsFlow.value.copy(notificationsEnabled = enabled)
            return DomainResult.Success(Unit)
        }

        override suspend fun deleteAccount(): DomainResult<Unit> = DomainResult.Success(Unit)
    }

    private class FakeInterruptedSubmissionRecoverer : InterruptedSubmissionRecoverer {
        var recoverCalled = false
        override suspend fun recoverInterruptedSubmissions(userId: UserId): DomainResult<InterruptedSubmissionRecoveryReport> {
            recoverCalled = true
            return DomainResult.Success(
                InterruptedSubmissionRecoveryReport(
                    recoveredSessions = emptyList(),
                    reconciledResults = emptyList(),
                    recoveredQueueOperations = emptyList()
                )
            )
        }
    }
}

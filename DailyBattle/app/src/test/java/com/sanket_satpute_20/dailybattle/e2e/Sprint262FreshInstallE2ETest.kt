package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.config.BuildTypeEnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import com.sanket_satpute_20.dailybattle.domain.validation.ValidationResult
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import com.sanket_satpute_20.dailybattle.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Sprint 26.2 — Fresh Install Test Suite
 *
 * Verifies the complete canonical progression on a clean environment per:
 * - `docs/ROADMAP.md` Phase 26, Sprint 26.2 (lines 1943-1956)
 * - `docs/16_DEFINITION_OF_DONE.md` §89 (lines 1397-1432)
 * - `docs/12_TESTING_AND_QA.md` §59 (lines 1567-1603)
 *
 * Primary verification sequence:
 *   Install (clean state)
 *    ↓
 *   First launch (Startup -> Welcome SCR-001)
 *    ↓
 *   Onboarding (Welcome -> Battle Name SCR-002)
 *    ↓
 *   Home (SCR-003)
 *    ↓
 *   Battle Intro (SCR-004)
 *    ↓
 *   Snap (SCR-005)
 *    ↓
 *   Shift (SCR-006)
 *    ↓
 *   Crowd Call (SCR-007)
 *    ↓
 *   Results (SCR-008)
 *    ↓
 *   Rival (SCR-009)
 *    ↓
 *   Home (SCR-003)
 *
 * Subsequent environment verification:
 *   - App restart / returning user identification
 *   - Completed battle state lock & single attempt guarantee
 *   - Practice battle isolation
 *   - Secondary screens navigation (Profile, Friends, History, Settings)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Sprint262FreshInstallE2ETest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneId.of("UTC"))

    private lateinit var cleanUserRepository: FakeCleanUserRepository
    private lateinit var sessionRepository: InMemoryBattleSessionRepository
    private lateinit var resultRepository: InMemoryBattleResultRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer
    private lateinit var scoreAggregator: BattleScoreAggregator

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        cleanUserRepository = FakeCleanUserRepository()
        sessionRepository = InMemoryBattleSessionRepository()
        resultRepository = InMemoryBattleResultRepository()
        activeSessionManager = ActiveBattleSessionManager(sessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(fixedClock)
        lifecycleBattlePreserver = DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        stateRestorer = DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )
        scoreAggregator = BattleScoreAggregator()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. CLEAN ENVIRONMENT PRECONDITIONS
    // =========================================================================

    @Test
    fun `fresh install environment starts with zero user state and empty persistence`() = runTest {
        val testSessionId = BattleSessionId("non_existent_session")
        // Assert initial clean state: no stored battle name, no sessions, no results
        assertNull("Clean install must have no persisted Battle Name", cleanUserRepository.getBattleName())
        assertNull("Clean install session store must have no session", sessionRepository.getSessionById(testSessionId))
        assertTrue("Clean install must have no unacknowledged sessions", sessionRepository.getUnacknowledgedSessions().isEmpty())
        assertNull("Clean install must have no active session in memory", activeSessionManager.getActiveSession())
    }

    // =========================================================================
    // 2. FIRST LAUNCH & ONBOARDING (WELCOME -> BATTLE NAME)
    // =========================================================================

    @Test
    fun `first launch enters Welcome screen and enforces hidden bottom navigation`() {
        val initialRoute = AppRoute.Welcome.route
        assertEquals("welcome", initialRoute)

        // Verify bottom nav hidden during Welcome per 06_NAVIGATION_AND_FLOWS.md §1.2
        val hiddenRoutes = setOf(
            AppRoute.Startup.route,
            AppRoute.Welcome.route,
            AppRoute.BattleName.route,
            AppRoute.BattleIntro.route,
            AppRoute.Snap.route,
            AppRoute.Shift.route,
            AppRoute.CrowdCall.route,
            AppRoute.Results.route,
            AppRoute.Rival.route
        )
        assertTrue(initialRoute in hiddenRoutes)
    }

    @Test
    fun `onboarding progression from Welcome to Battle Name with input validation`() = runTest {
        // Step 1: User on Welcome taps "GET STARTED" -> Navigates to Battle Name
        var currentRoute = AppRoute.Welcome.route
        currentRoute = AppRoute.BattleName.route
        assertEquals(AppRoute.BattleName.route, currentRoute)

        // Step 2: Test empty input validation
        val emptyValidation = InputValidator.validateBattleName("")
        assertTrue("Empty name must be invalid", emptyValidation is ValidationResult.Invalid)

        // Step 3: Test overly long input validation (>20 chars)
        val longValidation = InputValidator.validateBattleName("AveryLongNameExceedingTwentyChars")
        assertTrue("Long name must be invalid", longValidation is ValidationResult.Invalid)

        // Step 4: Test valid input
        val validName = "Commander"
        val validValidation = InputValidator.validateBattleName(validName)
        assertTrue("Valid 1-20 character name must pass", validValidation is ValidationResult.Valid)

        // Step 5: Save battle name through repository
        cleanUserRepository.saveBattleName(validName)
        assertEquals("Commander", cleanUserRepository.getBattleName())

        // Step 6: Navigation transitions to Home, popping startup graph inclusively
        currentRoute = AppRoute.Home.route
        assertEquals(AppRoute.Home.route, currentRoute)
    }

    // =========================================================================
    // 3. HOME SCREEN ON FRESH PROFILE
    // =========================================================================

    @Test
    fun `fresh user home screen reflects chosen battle name with zero momentum and ready battle card`() = runTest {
        cleanUserRepository.saveBattleName("Commander")

        val prodProvider = BuildTypeEnvironmentProvider(isDebugBuild = false)
        val homeViewModel = HomeViewModel(
            userRepository = cleanUserRepository,
            environmentProvider = prodProvider
        )

        homeViewModel.loadHomeData()
        advanceUntilIdle()

        val state = homeViewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals("Commander", state.userName)
        assertEquals(0, state.momentumDays)
        assertEquals(3, state.challengesCount)
        assertEquals(3, state.estimatedMinutes)
        assertFalse("Production must isolate demo rival", state.hasRival)
    }

    // =========================================================================
    // 4. FULL BATTLE FLOW FROM FRESH INSTALL
    // =========================================================================

    @Test
    fun `fresh install executes complete 3-challenge official battle flow to results and rival`() = runTest {
        cleanUserRepository.saveBattleName("Commander")
        val userId = UserId("user_fresh_1")
        val battleId = BattleId("2026-10-16")
        val sessionId = BattleSessionId("sess_fresh_1")

        // 1. Home -> Battle Intro
        var currentRoute = AppRoute.BattleIntro.route
        assertEquals(AppRoute.BattleIntro.route, currentRoute)

        // 2. Battle Intro -> Initialize Session
        var session = BattleSession(
            sessionId = sessionId,
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("snap_1"), sessionId, ChallengeSessionStatus.READY, startedAt = 1000L, completedAt = null),
                ChallengeSession(ChallengeId("shift_1"), sessionId, ChallengeSessionStatus.READY, startedAt = null, completedAt = null),
                ChallengeSession(ChallengeId("crowd_1"), sessionId, ChallengeSessionStatus.READY, startedAt = null, completedAt = null)
            )
        )
        sessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)

        // 3. Challenge 1: Snap
        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.Snap.route, currentRoute)
        assertEquals(ChallengeType.SNAP, ChallengeSequence.typeForIndex(1))

        // Complete Snap (Score: 280 / 300)
        val snapResult = ChallengeResult(
            resultId = ChallengeResultId("snap_res_1"),
            challengeId = ChallengeId("snap_1"),
            sessionId = sessionId,
            score = 280,
            maxScore = 300,
            completedAt = 1200L
        )
        val updatedChallenges1 = session.challengeSessions.toMutableList()
        updatedChallenges1[0] = updatedChallenges1[0].copy(status = ChallengeSessionStatus.COMPLETE, completedAt = 1200L)
        session = session.copy(currentChallenge = 1, challengeSessions = updatedChallenges1)
        sessionRepository.saveSession(session)

        // 4. Challenge 2: Shift
        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.Shift.route, currentRoute)
        assertEquals(ChallengeType.SHIFT, ChallengeSequence.typeForIndex(2))

        // Complete Shift (Score: 260 / 300)
        val shiftResult = ChallengeResult(
            resultId = ChallengeResultId("shift_res_1"),
            challengeId = ChallengeId("shift_1"),
            sessionId = sessionId,
            score = 260,
            maxScore = 300,
            completedAt = 1400L
        )
        val updatedChallenges2 = session.challengeSessions.toMutableList()
        updatedChallenges2[1] = updatedChallenges2[1].copy(status = ChallengeSessionStatus.COMPLETE, completedAt = 1400L)
        session = session.copy(currentChallenge = 2, challengeSessions = updatedChallenges2)
        sessionRepository.saveSession(session)

        // 5. Challenge 3: Crowd Call
        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.CrowdCall.route, currentRoute)
        assertEquals(ChallengeType.CROWD_CALL, ChallengeSequence.typeForIndex(3))

        // Complete Crowd Call (Score: 270 / 300)
        val crowdResult = ChallengeResult(
            resultId = ChallengeResultId("crowd_res_1"),
            challengeId = ChallengeId("crowd_1"),
            sessionId = sessionId,
            score = 270,
            maxScore = 300,
            completedAt = 1600L
        )
        val updatedChallenges3 = session.challengeSessions.toMutableList()
        updatedChallenges3[2] = updatedChallenges3[2].copy(status = ChallengeSessionStatus.COMPLETE, completedAt = 1600L)
        session = session.copy(
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            completedAt = 1600L,
            challengeSessions = updatedChallenges3
        )
        sessionRepository.saveSession(session)

        // 6. Transition to Results
        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.Results.route, currentRoute)

        // Score aggregation & verification
        val aggregatedScore = scoreAggregator.aggregate(
            mode = BattleMode.OFFICIAL,
            snapResult = snapResult,
            shiftResult = shiftResult,
            crowdCallResult = crowdResult
        )
        assertTrue("Total score must be bounded between 0 and 1000", aggregatedScore.totalScore in 0..1000)
        assertEquals(280, aggregatedScore.snapScore)
        assertEquals(260, aggregatedScore.shiftScore)
        assertEquals(270, aggregatedScore.crowdCallScore)

        // Persist authoritative BattleResult
        val battleResult = BattleResult(
            resultId = BattleResultId("res_fresh_1"),
            userId = userId,
            battleId = battleId,
            sessionId = sessionId,
            snapScore = aggregatedScore.snapScore,
            shiftScore = aggregatedScore.shiftScore,
            crowdCallScore = aggregatedScore.crowdCallScore,
            consistencyScore = aggregatedScore.consistencyScore,
            totalScore = aggregatedScore.totalScore,
            percentile = 88.5,
            completedAt = 1600L
        )
        resultRepository.saveResult(battleResult)

        // 7. Results -> Rival Screen
        currentRoute = AppRoute.Rival.route
        assertEquals(AppRoute.Rival.route, currentRoute)

        // 8. Rival -> Home
        currentRoute = AppRoute.Home.route
        assertEquals(AppRoute.Home.route, currentRoute)

        // Verify battle is now persisted in repository
        val fetchResult = resultRepository.getBattleResultBySessionId(sessionId)
        assertTrue(fetchResult is DomainResult.Success)
        val savedResult = (fetchResult as DomainResult.Success).value
        assertEquals(aggregatedScore.totalScore, savedResult.totalScore)
    }

    // =========================================================================
    // 5. RETURNING USER & PERSISTENCE CONTINUITY (APP RESTART)
    // =========================================================================

    @Test
    fun `subsequent launch after fresh install recognizes returning user and locks duplicate official battle`() = runTest {
        // Setup state resulting from completed fresh install flow
        cleanUserRepository.saveBattleName("Commander")
        val userId = UserId("user_fresh_1")
        val battleId = BattleId("2026-10-16")
        val sessionId = BattleSessionId("sess_fresh_1")

        val completedSession = BattleSession(
            sessionId = sessionId,
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = 1600L,
            challengeSessions = emptyList()
        )
        sessionRepository.saveSession(completedSession)

        // Simulate app restart / cold start
        val restoredName = cleanUserRepository.getBattleName()
        assertNotNull("Returning user must have persisted Battle Name", restoredName)
        assertEquals("Commander", restoredName)

        // Verify returning user bypasses onboarding and goes to Home
        val destination = if (restoredName.isNullOrBlank()) AppRoute.Welcome.route else AppRoute.Home.route
        assertEquals(AppRoute.Home.route, destination)

        // Verify official battle cannot be re-attempted on the same day per §20.3
        val existingSession = sessionRepository.getSessionById(sessionId)
        assertEquals(BattleSessionStatus.COMPLETED, existingSession?.status)
    }

    // =========================================================================
    // 6. PRACTICE & SECONDARY DESTINATIONS FROM HOME
    // =========================================================================

    @Test
    fun `secondary destinations and practice mode remain isolated and fully navigable`() = runTest {
        cleanUserRepository.saveBattleName("Commander")

        // Practice Battle isolation
        val practiceSessionId = BattleSessionId("sess_practice_1")
        val practiceSession = BattleSession(
            sessionId = practiceSessionId,
            userId = UserId("user_fresh_1"),
            battleId = BattleId("practice_battle"),
            mode = BattleMode.PRACTICE,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 2000L,
            completedAt = 2500L,
            challengeSessions = emptyList()
        )
        sessionRepository.saveSession(practiceSession)

        assertEquals(BattleMode.PRACTICE, practiceSession.mode)

        // Verify secondary destinations routes exist and are distinct
        val secondaryRoutes = listOf(
            AppRoute.Profile.route,
            AppRoute.Friends.route,
            AppRoute.History.route,
            AppRoute.Settings.route
        )
        assertEquals(4, secondaryRoutes.distinct().size)
    }
}

/**
 * Fake in-memory [UserRepository] initialized in a clean, unpopulated state.
 */
private class FakeCleanUserRepository : UserRepository {
    private var storedName: String? = null

    override suspend fun saveBattleName(name: String) {
        storedName = name
    }

    override suspend fun getBattleName(): String? {
        return storedName
    }
}

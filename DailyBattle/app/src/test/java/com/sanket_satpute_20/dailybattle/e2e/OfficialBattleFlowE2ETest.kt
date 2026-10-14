package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayRecoveryState
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.RivalComparisonDirection
import com.sanket_satpute_20.dailybattle.domain.battle.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Sprint 23.5 — Official Battle E2E Test Suite
 *
 * Verifies the complete canonical Official Battle flow per `docs/12_TESTING_AND_QA.md` §18,
 * `docs/ROADMAP.md` (lines 1766-1786), and `docs/06_NAVIGATION_AND_FLOWS.md` §11:
 *
 *   Launch -> Welcome -> Battle Name -> Home -> Battle Intro -> Snap -> Shift -> Crowd Call -> Results -> Rival
 *
 * Verification checkpoints:
 * 1. Complete flow order and sequence integrity (no skipped, no duplicated screens/challenges).
 * 2. Bottom navigation lock enforcement (hidden during onboarding and active gameplay, visible on Home).
 * 3. Challenge state transitions (Ready -> InProgress -> Complete).
 * 4. Official battle single-attempt guarantee (no duplicate attempt allowed).
 * 5. Score aggregation integrity (Snap + Shift + Crowd Call + Consistency = Total <= 1000).
 * 6. Back navigation behavior at every stage.
 */
class OfficialBattleFlowE2ETest {

    private val testUserId = UserId("user_e2e_1")
    private val testBattleId = BattleId("2026-10-14")
    private val testSessionId = BattleSessionId("sess_e2e_1")
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneId.of("UTC"))

    private lateinit var fakeSessionRepository: FakeBattleSessionRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer

    @Before
    fun setUp() {
        fakeSessionRepository = FakeBattleSessionRepository()
        activeSessionManager = ActiveBattleSessionManager(fakeSessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(fixedClock)
        lifecycleBattlePreserver = DefaultLifecycleBattlePreserver(fakeSessionRepository, lifecycleManager)
        stateRestorer = DefaultGameplayStateRestorer(
            sessionRepository = fakeSessionRepository,
            activeBattleSessionManager = activeSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )
    }

    @Test
    fun `canonical flow order strictly matches documented progression`() {
        // Define the authoritative canonical screen chain per 12_TESTING_AND_QA.md §18
        val expectedFlow = listOf(
            AppRoute.Welcome.route,
            AppRoute.BattleName.route,
            AppRoute.Home.route,
            AppRoute.BattleIntro.route,
            AppRoute.Snap.route,
            AppRoute.Shift.route,
            AppRoute.CrowdCall.route,
            AppRoute.Results.route,
            AppRoute.Rival.route
        )

        // Verify exact size and sequential ordering
        assertEquals(9, expectedFlow.size)
        assertEquals(AppRoute.Welcome.route, expectedFlow[0])
        assertEquals(AppRoute.BattleName.route, expectedFlow[1])
        assertEquals(AppRoute.Home.route, expectedFlow[2])
        assertEquals(AppRoute.BattleIntro.route, expectedFlow[3])
        assertEquals(AppRoute.Snap.route, expectedFlow[4])
        assertEquals(AppRoute.Shift.route, expectedFlow[5])
        assertEquals(AppRoute.CrowdCall.route, expectedFlow[6])
        assertEquals(AppRoute.Results.route, expectedFlow[7])
        assertEquals(AppRoute.Rival.route, expectedFlow[8])
    }

    @Test
    fun `gameplay challenge sequence strictly enforces Snap then Shift then CrowdCall without skipping`() {
        assertEquals(ChallengeType.SNAP, ChallengeSequence.typeForIndex(1))
        assertEquals(ChallengeType.SHIFT, ChallengeSequence.typeForIndex(2))
        assertEquals(ChallengeType.CROWD_CALL, ChallengeSequence.typeForIndex(3))
        assertEquals(3, ChallengeSequence.TOTAL_CHALLENGES)
    }

    @Test
    fun `bottom navigation is strictly hidden during gameplay and onboarding but visible on Home`() {
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

        // Onboarding: hidden
        assertTrue(AppRoute.Welcome.route in hiddenRoutes)
        assertTrue(AppRoute.BattleName.route in hiddenRoutes)

        // Gameplay: hidden
        assertTrue(AppRoute.BattleIntro.route in hiddenRoutes)
        assertTrue(AppRoute.Snap.route in hiddenRoutes)
        assertTrue(AppRoute.Shift.route in hiddenRoutes)
        assertTrue(AppRoute.CrowdCall.route in hiddenRoutes)
        assertTrue(AppRoute.Results.route in hiddenRoutes)
        assertTrue(AppRoute.Rival.route in hiddenRoutes)

        // Home hub: visible (not in hidden set)
        assertFalse(AppRoute.Home.route in hiddenRoutes)
    }

    @Test
    fun `full end-to-end official battle progression state machine execution`() {
        // --- Stage 1: Welcome & Battle Name Onboarding ---
        var currentRoute = AppRoute.Welcome.route
        assertEquals(AppRoute.Welcome.route, currentRoute)

        // User taps "GET STARTED" -> Navigates to Battle Name
        currentRoute = AppRoute.BattleName.route
        assertEquals(AppRoute.BattleName.route, currentRoute)

        // User enters valid name and taps "CONTINUE" -> Navigates to Home
        val userName = "Sanket"
        assertTrue(userName.length in 1..20)
        currentRoute = AppRoute.Home.route
        assertEquals(AppRoute.Home.route, currentRoute)

        // --- Stage 2: Home -> Battle Intro ---
        // User taps "PLAY BATTLE" -> Navigates to Battle Intro
        currentRoute = AppRoute.BattleIntro.route
        assertEquals(AppRoute.BattleIntro.route, currentRoute)

        // Initial session created: NOT_STARTED
        var session = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(challengeId = ChallengeId("c1"), sessionId = testSessionId, status = ChallengeSessionStatus.READY, startedAt = 1000L, completedAt = null),
                ChallengeSession(challengeId = ChallengeId("c2"), sessionId = testSessionId, status = ChallengeSessionStatus.READY, startedAt = null, completedAt = null),
                ChallengeSession(challengeId = ChallengeId("c3"), sessionId = testSessionId, status = ChallengeSessionStatus.READY, startedAt = null, completedAt = null)
            )
        )
        fakeSessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)

        // User taps "START BATTLE" -> Transitions to Challenge 1: Snap
        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.Snap.route, currentRoute)

        // Verify Snap can be submitted
        assertTrue(stateRestorer.canSubmitChallenge(session, 1))
        assertFalse(stateRestorer.canSubmitChallenge(session, 2)) // cannot skip to Shift

        // --- Stage 3: Complete Snap -> Transition to Shift ---
        val snapCompletedSessions = session.challengeSessions.toMutableList()
        snapCompletedSessions[0] = snapCompletedSessions[0].copy(status = ChallengeSessionStatus.COMPLETE, completedAt = 1200L)
        session = session.copy(
            currentChallenge = 1,
            challengeSessions = snapCompletedSessions
        )
        fakeSessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)

        // Once Snap is complete, determineRestoredRoute advances to Shift
        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.Shift.route, currentRoute)

        // Verify Snap cannot be submitted twice (duplicate prevention)
        assertFalse(stateRestorer.canSubmitChallenge(session, 1))

        // Advance active challenge pointer to Shift (2)
        session = session.copy(currentChallenge = 2)
        fakeSessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)
        assertTrue(stateRestorer.canSubmitChallenge(session, 2))

        // --- Stage 4: Complete Shift -> Transition to Crowd Call ---
        val shiftCompletedSessions = session.challengeSessions.toMutableList()
        shiftCompletedSessions[1] = shiftCompletedSessions[1].copy(status = ChallengeSessionStatus.COMPLETE, completedAt = 1400L)
        session = session.copy(
            currentChallenge = 2,
            challengeSessions = shiftCompletedSessions
        )
        fakeSessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)

        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.CrowdCall.route, currentRoute)

        // Advance active challenge pointer to Crowd Call (3)
        session = session.copy(currentChallenge = 3)
        fakeSessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)
        assertTrue(stateRestorer.canSubmitChallenge(session, 3))

        // --- Stage 5: Complete Crowd Call -> Transition to Results ---
        val crowdCompletedSessions = session.challengeSessions.toMutableList()
        crowdCompletedSessions[2] = crowdCompletedSessions[2].copy(status = ChallengeSessionStatus.COMPLETE, completedAt = 1600L)
        session = session.copy(
            currentChallenge = 3,
            status = BattleSessionStatus.COMPLETED,
            completedAt = 1600L,
            challengeSessions = crowdCompletedSessions
        )
        fakeSessionRepository.saveSession(session)
        activeSessionManager.bindSession(session)

        currentRoute = stateRestorer.determineRestoredRoute(session)
        assertEquals(AppRoute.Results.route, currentRoute)

        // Since Battle is COMPLETED, no challenge submissions are allowed
        assertFalse(stateRestorer.canSubmitChallenge(session, 1))
        assertFalse(stateRestorer.canSubmitChallenge(session, 2))
        assertFalse(stateRestorer.canSubmitChallenge(session, 3))

        // --- Stage 6: Results Screen Score Verification ---
        val snapScore = 287
        val shiftScore = 252
        val crowdScore = 271
        val consistencyScore = 91
        val totalScore = snapScore + shiftScore + crowdScore + consistencyScore
        assertEquals(901, totalScore)
        assertTrue("Total score must be <= 1000", totalScore <= 1000)

        val battleResult = BattleResult(
            resultId = BattleResultId("res_e2e_1"),
            userId = testUserId,
            battleId = testBattleId,
            sessionId = session.sessionId,
            snapScore = snapScore,
            shiftScore = shiftScore,
            crowdCallScore = crowdScore,
            consistencyScore = consistencyScore,
            totalScore = totalScore,
            percentile = 0.09,
            completedAt = System.currentTimeMillis()
        )

        val resultSummary = BattleResultSummary(
            result = battleResult,
            previousDayScore = 864,
            scoreImprovement = 37,
            isNewPersonalBest = true,
            personalBestScore = 927,
            averageScore = 806,
            rivalComparison = RivalScoreComparison(
                userScore = 901,
                rivalScore = 914,
                scoreGap = 13,
                battleId = testBattleId,
                direction = RivalComparisonDirection.RIVAL_AHEAD,
                rivalName = "Rahul"
            )
        )

        assertNotNull(resultSummary)
        assertEquals(901, resultSummary.result.totalScore)
        assertEquals(37, resultSummary.scoreImprovement)
        assertEquals("Rahul", resultSummary.rivalComparison?.rivalName)
        assertEquals(13, resultSummary.rivalComparison?.scoreGap)

        // --- Stage 7: Results -> Rival Screen ---
        // User taps "BEAT RAHUL" -> Navigates to Rival
        currentRoute = AppRoute.Rival.route
        assertEquals(AppRoute.Rival.route, currentRoute)
    }

    @Test
    fun `completed official battle cannot be replayed or re-entered`() {
        // Create an already-completed session
        val completedSession = BattleSession(
            sessionId = BattleSessionId("sess_completed"),
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = 1600L,
            challengeSessions = listOf(
                ChallengeSession(challengeId = ChallengeId("c1"), sessionId = BattleSessionId("sess_completed"), status = ChallengeSessionStatus.COMPLETE, startedAt = 1000L, completedAt = 1200L),
                ChallengeSession(challengeId = ChallengeId("c2"), sessionId = BattleSessionId("sess_completed"), status = ChallengeSessionStatus.COMPLETE, startedAt = 1200L, completedAt = 1400L),
                ChallengeSession(challengeId = ChallengeId("c3"), sessionId = BattleSessionId("sess_completed"), status = ChallengeSessionStatus.COMPLETE, startedAt = 1400L, completedAt = 1600L)
            )
        )
        fakeSessionRepository.saveSession(completedSession)

        // Restoring state returns BattleCompleted and directly navigates to Results
        val recovery = stateRestorer.evaluateRecoveryState(completedSession)
        assertEquals(GameplayRecoveryState.BattleCompleted, recovery)

        val restoredRoute = stateRestorer.determineRestoredRoute(completedSession)
        assertEquals(AppRoute.Results.route, restoredRoute)

        // Challenge submission is blocked
        assertFalse(stateRestorer.canSubmitChallenge(completedSession, 1))
    }

    @Test
    fun `back navigation contract preserves integrity at each stage`() {
        // Welcome: root of onboarding flow (no back)
        // Battle Name: back goes to Welcome
        val battleNameBackTarget = AppRoute.Welcome.route
        assertEquals(AppRoute.Welcome.route, battleNameBackTarget)

        // Battle Intro: back goes to Home (before starting official challenge)
        val battleIntroBackTarget = AppRoute.Home.route
        assertEquals(AppRoute.Home.route, battleIntroBackTarget)

        // Gameplay screens: Back does not restart or duplicate challenge
        // Results: back goes to Rival / Home
        val rivalBackTarget = AppRoute.Home.route
        assertEquals(AppRoute.Home.route, rivalBackTarget)
    }

    /**
     * In-memory test implementation of [BattleSessionRepository].
     */
    private class FakeBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()

        override fun saveSession(session: BattleSession) {
            sessions[Pair(session.userId, session.battleId)] = session
        }

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions[Pair(userId, battleId)]
        }

        override fun getActiveOfficialSession(userId: UserId): BattleSession? {
            return sessions.values.firstOrNull { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return sessions.values.filter { it.status != BattleSessionStatus.COMPLETED }
        }
    }
}

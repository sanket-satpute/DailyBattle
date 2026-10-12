package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class GameplayStateRestorerTest {

    private lateinit var sessionRepository: FakeBattleSessionRepository
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var preserver: DefaultLifecycleBattlePreserver
    private lateinit var restorer: DefaultGameplayStateRestorer

    private val userId = UserId("user_42")
    private val battleId = BattleId("battle_today")
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneId.of("UTC"))

    @Before
    fun setUp() {
        sessionRepository = FakeBattleSessionRepository()
        activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(fixedClock)
        preserver = DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        restorer = DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            lifecycleManager = lifecycleManager,
        )
    }

    @Test
    fun restoreGameplayState_whenNoSessionExists_returnsNoSession() {
        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.NoSession)
    }

    @Test
    fun restoreGameplayState_whenSessionNotStarted_routesToBattleIntro() {
        val session = createSession(
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored
        assertEquals(GameplayRecoveryState.NotStarted, restored.recoveryState)
        assertEquals(AppRoute.BattleIntro.route, restored.targetRoute)
        assertEquals(session.sessionId, restored.session.sessionId)
    }

    @Test
    fun restoreGameplayState_whenSnapActive_routesToSnap_doesNotResetScoreOrRestart() {
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 1000L,
            completedAt = null,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            challengeSessions = listOf(snapSession),
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored

        assertEquals(
            GameplayRecoveryState.ChallengeActive(
                challengeIndex = 1,
                challengeType = ChallengeType.SNAP,
                isFirstChallenge = true,
            ),
            restored.recoveryState
        )
        assertEquals(AppRoute.Snap.route, restored.targetRoute)
        assertEquals(1, restored.session.challengeSessions.size)
        assertEquals(1000L, restored.session.challengeSessions[0].startedAt)
    }

    @Test
    fun restoreGameplayState_whenSnapCompleteAndShiftActive_routesToShift_notSnap() {
        // Conforms to 09_OFFLINE_AND_ERROR_HANDLING.md §20:
        // "SNAP = COMPLETE, SHIFT = ACTIVE, CROWD CALL = NOT_STARTED means the application must not return the user to Snap."
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 2500L,
            completedAt = null,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            challengeSessions = listOf(snapSession, shiftSession),
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored

        assertEquals(
            GameplayRecoveryState.ChallengeActive(
                challengeIndex = 2,
                challengeType = ChallengeType.SHIFT,
                isFirstChallenge = false,
            ),
            restored.recoveryState
        )
        assertEquals(AppRoute.Shift.route, restored.targetRoute)
        assertEquals(2, restored.session.challengeSessions.size)
        assertEquals(ChallengeSessionStatus.COMPLETE, restored.session.challengeSessions[0].status)
    }

    @Test
    fun restoreGameplayState_whenShiftCompleted_routesToCrowdCall() {
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 2500L,
            completedAt = 3500L,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            challengeSessions = listOf(snapSession, shiftSession),
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored

        assertEquals(
            GameplayRecoveryState.ChallengeCompleted(
                challengeIndex = 2,
                challengeType = ChallengeType.SHIFT,
                nextChallengeIndex = 3,
                nextChallengeType = ChallengeType.CROWD_CALL,
            ),
            restored.recoveryState
        )
        assertEquals(AppRoute.CrowdCall.route, restored.targetRoute)
    }

    @Test
    fun restoreGameplayState_whenCrowdCallActive_routesToCrowdCall_notSnapOrShift() {
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 2500L,
            completedAt = 3500L,
        )
        val crowdCallSession = ChallengeSession(
            challengeId = ChallengeId("c-3"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 4000L,
            completedAt = null,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            challengeSessions = listOf(snapSession, shiftSession, crowdCallSession),
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored

        assertEquals(
            GameplayRecoveryState.ChallengeActive(
                challengeIndex = 3,
                challengeType = ChallengeType.CROWD_CALL,
                isFirstChallenge = false,
            ),
            restored.recoveryState
        )
        assertEquals(AppRoute.CrowdCall.route, restored.targetRoute)
    }

    @Test
    fun restoreGameplayState_whenAllChallengesComplete_routesToResults() {
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 2500L,
            completedAt = 3500L,
        )
        val crowdCallSession = ChallengeSession(
            challengeId = ChallengeId("c-3"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 4000L,
            completedAt = 5000L,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            challengeSessions = listOf(snapSession, shiftSession, crowdCallSession),
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored

        assertEquals(GameplayRecoveryState.BattleCompleted, restored.recoveryState)
        assertEquals(AppRoute.Results.route, restored.targetRoute)
    }

    @Test
    fun restoreGameplayState_whenBattleCompleted_routesToResults() {
        val session = createSession(
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            completedAt = 6000L,
        )
        sessionRepository.saveSession(session)

        val result = restorer.restoreGameplayState(userId, battleId)
        assertTrue(result is GameplayRestorationResult.Restored)
        val restored = result as GameplayRestorationResult.Restored

        assertEquals(GameplayRecoveryState.BattleCompleted, restored.recoveryState)
        assertEquals(AppRoute.Results.route, restored.targetRoute)
    }

    @Test
    fun canSubmitChallenge_preventsDuplicateSubmissionOnRecreation() {
        // Conforms to 09_OFFLINE_AND_ERROR_HANDLING.md §19:
        // "Screen recreation must not submit an answer twice"
        val snapComplete = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftActive = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 2500L,
            completedAt = null,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            challengeSessions = listOf(snapComplete, shiftActive),
        )

        // Submitting challenge 1 again is strictly prohibited (already complete)
        assertFalse(restorer.canSubmitChallenge(session, 1))

        // Submitting challenge 2 is allowed (currently active)
        assertTrue(restorer.canSubmitChallenge(session, 2))

        // Submitting challenge 3 is prohibited (not current challenge)
        assertFalse(restorer.canSubmitChallenge(session, 3))
    }

    @Test
    fun canSubmitChallenge_whenBattleCompleted_strictlyRejectsSubmissions() {
        val session = createSession(
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
        )
        assertFalse(restorer.canSubmitChallenge(session, 1))
        assertFalse(restorer.canSubmitChallenge(session, 2))
        assertFalse(restorer.canSubmitChallenge(session, 3))
    }

    @Test
    fun onLifecycleEvent_recreatedEvent_synchronizesActiveSessionWithoutScoreReset() {
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_1"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val session = createSession(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            challengeSessions = listOf(snapSession),
        )
        sessionRepository.saveSession(session)
        preserver.attachActiveSession(session)

        // Simulate screen recreation event
        lifecycleManager.onRecreation(isProcessRecreation = false)

        val active = preserver.activeSession.value
        assertNotNull(active)
        assertEquals(session.sessionId, active?.sessionId)
        assertEquals(1, active?.challengeSessions?.size)
        assertEquals(ChallengeSessionStatus.COMPLETE, active?.challengeSessions?.get(0)?.status)
    }

    private fun createSession(
        status: BattleSessionStatus,
        currentChallenge: Int?,
        challengeSessions: List<ChallengeSession> = emptyList(),
        completedAt: Long? = null,
    ): BattleSession {
        return BattleSession(
            sessionId = BattleSessionId("session_1"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = status,
            currentChallenge = currentChallenge,
            startedAt = 1000L,
            completedAt = completedAt,
            challengeSessions = challengeSessions,
        )
    }

    private class FakeBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<String, BattleSession>()

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions.values.firstOrNull {
                it.userId == userId && it.battleId == battleId && it.mode == BattleMode.OFFICIAL
            }
        }

        override fun saveSession(session: BattleSession) {
            sessions[session.sessionId.value] = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return emptyList()
        }

        fun clearAll() {
            sessions.clear()
        }
    }
}

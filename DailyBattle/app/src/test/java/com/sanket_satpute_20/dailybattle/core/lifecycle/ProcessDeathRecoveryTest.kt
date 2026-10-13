package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.RecoverUnacknowledgedResultUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Verifies Sprint 18.3 Process Death requirements:
 *
 * Sequence:
 * Battle active
 *  ↓
 * Process killed
 *  ↓
 * App reopened
 *  ↓
 * Recover state
 *
 * Conforms to:
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §16 (Official Battle Session Recovery)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §17 (Process Death: must not create new official attempt)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §20 (Gameplay State Recovery: never return user to completed challenge)
 * - 06_NAVIGATION_AND_FLOWS.md §57 (App Re-entry: resume appropriate battle state)
 * - 12_TESTING_AND_QA.md E2E-006 (Process death recovery)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicates), Rule 43 (Lifecycle), Rule 44 (Persistence)
 */
class ProcessDeathRecoveryTest {

    private lateinit var sessionRepository: InMemoryBattleSessionRepository
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var preserver: DefaultLifecycleBattlePreserver
    private lateinit var restorer: DefaultGameplayStateRestorer
    private lateinit var recoverUnacknowledgedResultUseCase: RecoverUnacknowledgedResultUseCase
    private lateinit var processDeathRecoverer: DefaultProcessDeathRecoverer

    private val userId = UserId("user_hero_99")
    private val battleId = BattleId("battle_today_2026")
    private val clock = Clock.fixed(Instant.ofEpochMilli(5_000_000L), ZoneId.of("UTC"))

    @Before
    fun setUp() {
        sessionRepository = InMemoryBattleSessionRepository()
        activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(clock)
        preserver = DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        restorer = DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            lifecycleManager = lifecycleManager,
        )

        val fakeResultRepository = object : BattleResultRepository {
            override suspend fun completeBattle(sessionId: BattleSessionId, clientRequestId: String): DomainResult<BattleResult> =
                DomainResult.Failure(AppError.Domain)
            override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> =
                DomainResult.Failure(AppError.Domain)
            override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> =
                DomainResult.Success(emptyList())
        }
        val completeBattleUseCase = CompleteBattleUseCase(fakeResultRepository, sessionRepository)
        recoverUnacknowledgedResultUseCase = RecoverUnacknowledgedResultUseCase(sessionRepository, completeBattleUseCase)

        processDeathRecoverer = DefaultProcessDeathRecoverer(
            sessionRepository = sessionRepository,
            gameplayStateRestorer = restorer,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            recoverUnacknowledgedResultUseCase = recoverUnacknowledgedResultUseCase,
        )
    }

    @Test
    fun processDeath_duringSnap_recoversActiveSnapStateOnAppReopen() {
        // Arrange: Battle active in Snap, persisted before process killed
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_death_1"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 1000L,
            completedAt = null,
        )
        val initialSession = BattleSession(
            sessionId = BattleSessionId("session_death_1"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(snapSession),
        )
        sessionRepository.saveSession(initialSession)

        // Simulate Process Death: in-memory components are completely wiped and freshly created on app reopen
        simulateProcessDeath()

        // Act: App reopened -> recover battle after process death
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        // Assert: Correctly recovered Snap state
        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(
            GameplayRecoveryState.ChallengeActive(
                challengeIndex = 1,
                challengeType = ChallengeType.SNAP,
                isFirstChallenge = true,
            ),
            recovered.recoveryState
        )
        assertEquals(AppRoute.Snap.route, recovered.targetRoute)
        assertEquals(initialSession.sessionId, recovered.session.sessionId)
        assertEquals(1000L, recovered.session.challengeSessions[0].startedAt)
        // Memory state is re-bound
        assertNotNull(activeBattleSessionManager.getActiveSession())
        assertEquals(initialSession.sessionId, activeBattleSessionManager.getActiveSession()?.sessionId)
    }

    @Test
    fun processDeath_duringShift_recoversActiveShiftState_andNeverReturnsToSnap() {
        // Arrange: Snap is COMPLETE, Shift is ACTIVE
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_death_2"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_death_2"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 2500L,
            completedAt = null,
        )
        val session = BattleSession(
            sessionId = BattleSessionId("session_death_2"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(snapSession, shiftSession),
        )
        sessionRepository.saveSession(session)

        // Simulate Process Death
        simulateProcessDeath()

        // Act: App reopened
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        // Assert: Recovers to Shift, NEVER returns to Snap (§20)
        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(
            GameplayRecoveryState.ChallengeActive(
                challengeIndex = 2,
                challengeType = ChallengeType.SHIFT,
                isFirstChallenge = false,
            ),
            recovered.recoveryState
        )
        assertEquals(AppRoute.Shift.route, recovered.targetRoute)
        assertEquals(2, recovered.session.challengeSessions.size)
        assertEquals(ChallengeSessionStatus.COMPLETE, recovered.session.challengeSessions[0].status)
    }

    @Test
    fun processDeath_duringCrowdCall_recoversActiveCrowdCallState_andNeverReturnsToSnapOrShift() {
        // Arrange: Snap is COMPLETE, Shift is COMPLETE, Crowd Call is ACTIVE
        val snapSession = ChallengeSession(
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("session_death_3"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1000L,
            completedAt = 2000L,
        )
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_death_3"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 2500L,
            completedAt = 3500L,
        )
        val crowdCallSession = ChallengeSession(
            challengeId = ChallengeId("c-3"),
            sessionId = BattleSessionId("session_death_3"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 4000L,
            completedAt = null,
        )
        val session = BattleSession(
            sessionId = BattleSessionId("session_death_3"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(snapSession, shiftSession, crowdCallSession),
        )
        sessionRepository.saveSession(session)

        // Simulate Process Death
        simulateProcessDeath()

        // Act: App reopened
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        // Assert: Recovers directly to Crowd Call (§20)
        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(
            GameplayRecoveryState.ChallengeActive(
                challengeIndex = 3,
                challengeType = ChallengeType.CROWD_CALL,
                isFirstChallenge = false,
            ),
            recovered.recoveryState
        )
        assertEquals(AppRoute.CrowdCall.route, recovered.targetRoute)
    }

    @Test
    fun processDeath_afterBattleCompletion_recoversCompletedState_withoutCreatingNewAttempt() {
        // Arrange: Battle completed before process killed
        val session = BattleSession(
            sessionId = BattleSessionId("session_death_4"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 5000L,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c-1"), BattleSessionId("session_death_4"), ChallengeSessionStatus.COMPLETE, 1000L, 2000L),
                ChallengeSession(ChallengeId("c-2"), BattleSessionId("session_death_4"), ChallengeSessionStatus.COMPLETE, 2500L, 3500L),
                ChallengeSession(ChallengeId("c-3"), BattleSessionId("session_death_4"), ChallengeSessionStatus.COMPLETE, 4000L, 5000L),
            ),
        )
        sessionRepository.saveSession(session)

        // Simulate Process Death
        simulateProcessDeath()

        // Act: App reopened
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        // Assert: Identifies BattleCompleted, enforces single official attempt (§17, §18)
        assertTrue(result is ProcessDeathRecoveryResult.BattleCompleted)
        val completed = result as ProcessDeathRecoveryResult.BattleCompleted
        assertEquals(BattleSessionStatus.COMPLETED, completed.session.status)
        assertEquals(session.sessionId, completed.session.sessionId)
    }

    @Test
    fun processDeath_whenNoActiveSessionExists_returnsNoActiveBattle() {
        simulateProcessDeath()
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)
        assertTrue(result is ProcessDeathRecoveryResult.NoActiveBattle)
    }

    @Test
    fun processDeath_withoutBattleId_automaticallyDiscoversActiveOfficialSession() {
        // Arrange: Active session exists in repository
        val shiftSession = ChallengeSession(
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("session_death_auto"),
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 2500L,
            completedAt = null,
        )
        val session = BattleSession(
            sessionId = BattleSessionId("session_death_auto"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(shiftSession),
        )
        sessionRepository.saveSession(session)

        simulateProcessDeath()

        // Act: App reopened without knowing battleId beforehand (cold start / app launch)
        val result = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId = null)

        // Assert: Auto-discovers and recovers
        assertTrue(result is ProcessDeathRecoveryResult.Recovered)
        val recovered = result as ProcessDeathRecoveryResult.Recovered
        assertEquals(AppRoute.Shift.route, recovered.targetRoute)
        assertEquals(session.sessionId, recovered.session.sessionId)
    }

    @Test
    fun processDeath_idempotency_multipleCallsDoNotMutateOrDuplicateSession() {
        val session = BattleSession(
            sessionId = BattleSessionId("session_death_idempotent"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
        )
        sessionRepository.saveSession(session)

        simulateProcessDeath()

        // Repeated recovery calls
        val result1 = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)
        val result2 = processDeathRecoverer.recoverBattleAfterProcessDeath(userId, battleId)

        assertTrue(result1 is ProcessDeathRecoveryResult.Recovered)
        assertTrue(result2 is ProcessDeathRecoveryResult.Recovered)
        assertEquals(
            (result1 as ProcessDeathRecoveryResult.Recovered).session.sessionId,
            (result2 as ProcessDeathRecoveryResult.Recovered).session.sessionId
        )
    }

    /**
     * Recreates fresh in-memory components (simulating brand new process memory space after kill).
     */
    private fun simulateProcessDeath() {
        activeBattleSessionManager = ActiveBattleSessionManager(sessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(clock)
        preserver = DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        restorer = DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            lifecycleManager = lifecycleManager,
        )
        processDeathRecoverer = DefaultProcessDeathRecoverer(
            sessionRepository = sessionRepository,
            gameplayStateRestorer = restorer,
            activeBattleSessionManager = activeBattleSessionManager,
            lifecycleBattlePreserver = preserver,
            recoverUnacknowledgedResultUseCase = recoverUnacknowledgedResultUseCase,
        )
    }
}

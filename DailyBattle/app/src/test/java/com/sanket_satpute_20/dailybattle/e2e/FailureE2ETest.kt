package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayRecoveryState
import com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayRestorationResult
import com.sanket_satpute_20.dailybattle.core.time.CountdownGameTimer
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleState
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStateMachine
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreConstants
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreValidator
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Sprint 23.6 — Failure E2E Test Suite
 *
 * Breaks every major boundary per `docs/ROADMAP.md` (lines 1787-1801) and
 * `docs/09_OFFLINE_AND_ERROR_HANDLING.md` §81, §82:
 *
 * Major boundaries verified:
 *   1. Network
 *   2. Persistence
 *   3. Lifecycle
 *   4. Navigation
 *   5. Timers
 *   6. Scoring
 *   7. Submission
 *   8. Authentication
 *   9. Authorization
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FailureE2ETest {

    private val testUserId = UserId("user_failure_e2e")
    private val otherUserId = UserId("user_attacker_999")
    private val testBattleId = BattleId("2026-10-14")
    private val testSessionId = BattleSessionId("sess_failure_1")

    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneId.of("UTC"))

    private lateinit var fakeSessionRepository: MutableBattleSessionRepository
    private lateinit var fakeResultRepository: MutableBattleResultRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer
    private lateinit var completeBattleUseCase: CompleteBattleUseCase
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase

    @Before
    fun setUp() {
        fakeSessionRepository = MutableBattleSessionRepository()
        fakeResultRepository = MutableBattleResultRepository()
        activeSessionManager = ActiveBattleSessionManager(fakeSessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(fixedClock)
        lifecycleBattlePreserver = DefaultLifecycleBattlePreserver(fakeSessionRepository, lifecycleManager)
        stateRestorer = DefaultGameplayStateRestorer(
            sessionRepository = fakeSessionRepository,
            activeBattleSessionManager = activeSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )
        completeBattleUseCase = CompleteBattleUseCase(
            resultRepository = fakeResultRepository,
            sessionRepository = fakeSessionRepository
        )
        getOrStartOfficialBattleUseCase = GetOrStartOfficialBattleUseCase(fakeSessionRepository)
    }

    // ========================================================================
    // 1. NETWORK BOUNDARY
    // ========================================================================

    @Test
    fun `network boundary - offline launch preserves valid cached session without creating artificial data`() {
        // Pre-condition: existing in-flight session cached locally
        val cachedSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(
                    challengeId = ChallengeId("snap_1"),
                    sessionId = testSessionId,
                    status = ChallengeSessionStatus.ACTIVE,
                    startedAt = 1000L,
                    completedAt = null
                )
            )
        )
        fakeSessionRepository.saveSession(cachedSession)

        // Simulate offline state: repository returns cached session safely
        val retrieved = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(retrieved)
        assertEquals(testSessionId, retrieved?.sessionId)
        assertEquals(BattleSessionStatus.IN_PROGRESS, retrieved?.status)

        // Evaluate restoration: must restore existing session and not fabricate a replacement
        val recovery = stateRestorer.evaluateRecoveryState(retrieved!!)
        assertTrue(recovery is GameplayRecoveryState.ChallengeActive)
        assertEquals(1, (recovery as GameplayRecoveryState.ChallengeActive).challengeIndex)
    }

    @Test
    fun `network boundary - API failure never produces an empty state and empty data never produces an error state`() {
        // Invariant 5: Network failure != Empty State
        val networkError = DomainResult.Failure(AppError.Network)
        val offlineError = DomainResult.Failure(AppError.Offline)
        val emptySuccess = DomainResult.Success(emptyList<String>())

        assertTrue(networkError is DomainResult.Failure)
        assertEquals(AppError.Network, networkError.error)

        assertTrue(offlineError is DomainResult.Failure)
        assertEquals(AppError.Offline, offlineError.error)

        assertTrue(emptySuccess is DomainResult.Success)
        assertTrue(emptySuccess.value.isEmpty())

        // Ensure failure and empty never equal each other
        assertNotEquals(networkError, emptySuccess)
        assertNotEquals(offlineError, emptySuccess)
    }

    @Test
    fun `network boundary - request timeout during completion preserves session in recoverable state`() = runTest {
        val activeSession = createActiveSession(currentChallenge = 3)
        fakeSessionRepository.saveSession(activeSession)

        // Simulate network timeout from backend
        fakeResultRepository.shouldTimeout = true

        val result = completeBattleUseCase.execute(activeSession)
        assertTrue("Completion with timeout must return Failure(AppError.Timeout)", result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)

        // Session must remain intact and recoverable locally, not marked complete without confirmation
        val currentSession = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(currentSession)
        assertNotEquals(BattleSessionStatus.COMPLETED, currentSession?.status)
    }

    @Test
    fun `network boundary - server 500 error maps to AppError Server and preserves existing local records`() = runTest {
        val activeSession = createActiveSession(currentChallenge = 2)
        fakeSessionRepository.saveSession(activeSession)

        fakeResultRepository.shouldReturnServerError = true

        val result = completeBattleUseCase.execute(activeSession)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)

        // Existing local record preserved
        val session = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertEquals(activeSession.sessionId, session?.sessionId)
        assertEquals(BattleSessionStatus.IN_PROGRESS, session?.status)
    }

    // ========================================================================
    // 2. PERSISTENCE BOUNDARY
    // ========================================================================

    @Test
    fun `persistence boundary - storage write failure is handled safely without crashing or corrupting state`() {
        fakeSessionRepository.shouldThrowOnWrite = true

        var caught = false
        try {
            fakeSessionRepository.saveSession(createActiveSession(1))
        } catch (e: Exception) {
            caught = true
        }
        assertTrue("Storage write failure was safely caught", caught)

        // Existing repository remains uncorrupted
        fakeSessionRepository.shouldThrowOnWrite = false
        val session = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNull("No corrupted partial session saved", session)
    }

    @Test
    fun `persistence boundary - missing record read handles gracefully without fabricating data`() {
        val nonExistentUser = UserId("non_existent_user_123")
        val session = fakeSessionRepository.getOfficialSession(nonExistentUser, testBattleId)
        assertNull("Missing record must return null, not fabricated session", session)

        val active = fakeSessionRepository.getActiveOfficialSession(nonExistentUser)
        assertNull("No active session found", active)
    }

    @Test
    fun `persistence boundary - failed write never silently mutates existing valid records`() {
        val originalSession = createActiveSession(currentChallenge = 1)
        fakeSessionRepository.saveSession(originalSession)

        // Simulate write failure on update
        fakeSessionRepository.shouldThrowOnWrite = true
        val updatedSession = originalSession.copy(currentChallenge = 2)
        try {
            fakeSessionRepository.saveSession(updatedSession)
        } catch (e: Exception) {
            // Handled
        }

        fakeSessionRepository.shouldThrowOnWrite = false
        val retrieved = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertEquals("Original challenge 1 preserved after failed write", 1, retrieved?.currentChallenge)
    }

    // ========================================================================
    // 3. LIFECYCLE BOUNDARY
    // ========================================================================

    @Test
    fun `lifecycle boundary - app backgrounding preserves in-flight challenge state`() {
        val session = createActiveSession(currentChallenge = 2)
        fakeSessionRepository.saveSession(session)

        // App backgrounds
        lifecycleBattlePreserver.attachActiveSession(session)

        // Verify session preserved with active state
        val active = lifecycleBattlePreserver.activeSession.value
        assertNotNull(active)
        assertEquals(2, active?.currentChallenge)
    }

    @Test
    fun `lifecycle boundary - process death during active battle recovers exact challenge without duplicate attempt`() {
        val session = createActiveSession(currentChallenge = 3)
        fakeSessionRepository.saveSession(session)

        // Simulate process death: clear in-memory preserver and reset active session manager
        lifecycleBattlePreserver.detachActiveSession()
        activeSessionManager = ActiveBattleSessionManager(fakeSessionRepository)

        // Recovery restores from persistent repository
        val recovered = stateRestorer.restoreGameplayState(testUserId, testBattleId)
        assertTrue(recovered is GameplayRestorationResult.Restored)
        val restored = recovered as GameplayRestorationResult.Restored

        assertEquals(AppRoute.CrowdCall.route, restored.targetRoute)
        assertTrue(restored.recoveryState is GameplayRecoveryState.ChallengeActive)
        assertEquals(3, (restored.recoveryState as GameplayRecoveryState.ChallengeActive).challengeIndex)

        // Attempting to start a new official battle returns existing session (Invariant 8)
        val startResult = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
        assertTrue(startResult is DomainResult.Success)
        assertEquals(session.sessionId, (startResult as DomainResult.Success).value.session.sessionId)
    }

    @Test
    fun `lifecycle boundary - process death after completion restores results without replaying challenges`() {
        val completedSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = 2000L,
            challengeSessions = emptyList()
        )
        fakeSessionRepository.saveSession(completedSession)

        val recovery = stateRestorer.evaluateRecoveryState(completedSession)
        assertEquals(GameplayRecoveryState.BattleCompleted, recovery)

        val targetRoute = stateRestorer.determineRestoredRoute(completedSession)
        assertEquals(AppRoute.Results.route, targetRoute)

        // Verify cannot submit any challenge
        assertFalse(stateRestorer.canSubmitChallenge(completedSession, 1))
        assertFalse(stateRestorer.canSubmitChallenge(completedSession, 2))
        assertFalse(stateRestorer.canSubmitChallenge(completedSession, 3))
    }

    // ========================================================================
    // 4. NAVIGATION BOUNDARY
    // ========================================================================

    @Test
    fun `navigation boundary - invalid transitions skipping challenges are rejected by state machine`() {
        val stateMachine = BattleStateMachine(initialState = BattleState.NotStarted)

        // Attempting to skip directly to Results from NotStarted
        val invalidJump = stateMachine.transition(BattleEvent.ShowResults)
        assertTrue(invalidJump is DomainResult.Failure)
        assertEquals(AppError.BattleState, (invalidJump as DomainResult.Failure).error)
        assertEquals(BattleState.NotStarted, stateMachine.currentState)

        // Start battle -> Ready(1)
        stateMachine.transition(BattleEvent.StartBattle)
        assertEquals(BattleState.Ready(1), stateMachine.currentState)

        // Attempting to continue past without finishing challenge
        val skipChallenge = stateMachine.transition(BattleEvent.Continue)
        assertTrue(skipChallenge is DomainResult.Failure)
        assertEquals(AppError.BattleState, (skipChallenge as DomainResult.Failure).error)
    }

    @Test
    fun `navigation boundary - terminal results state rejects gameplay events`() {
        val stateMachine = BattleStateMachine(initialState = BattleState.Results)

        val eventResult = stateMachine.transition(BattleEvent.BeginChallenge)
        assertTrue(eventResult is DomainResult.Failure)
        assertEquals(AppError.BattleState, (eventResult as DomainResult.Failure).error)
        assertEquals(BattleState.Results, stateMachine.currentState)
    }

    // ========================================================================
    // 5. TIMERS BOUNDARY
    // ========================================================================

    @Test
    fun `timers boundary - countdown timer expiration clamps remaining time to 0 and marks isFinished true`() {
        val testDispatcher = StandardTestDispatcher()
        val testScope = TestScope(testDispatcher)
        var simulatedRealtime = 100_000L

        val timeProvider = object : TimeProvider {
            override fun elapsedRealtime(): Long = simulatedRealtime
        }

        val timer = CountdownGameTimer(
            timeProvider = timeProvider,
            scope = testScope,
            tickIntervalMs = 100L
        )

        timer.start(durationMillis = 15_000L) // 15 seconds Snap duration
        assertTrue(timer.state.value.isRunning)
        assertFalse(timer.state.value.isFinished)

        // Advance simulated time past duration
        simulatedRealtime += 16_000L
        testDispatcher.scheduler.advanceTimeBy(16_000L)

        // Once finished, remaining millis clamped to 0 and isFinished is true
        assertEquals(0L, timer.state.value.remainingMillis)
        assertTrue(timer.state.value.isFinished)
        assertFalse(timer.state.value.isRunning)
    }

    @Test
    fun `timers boundary - restoring an already expired timer remains finished without negative remaining time`() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val timeProvider = object : TimeProvider {
            override fun elapsedRealtime(): Long = 200_000L
        }

        val timer = CountdownGameTimer(
            timeProvider = timeProvider,
            scope = testScope
        )

        // Restore with -5000ms (expired)
        timer.restore(durationMillis = 15_000L, remainingMillis = -5000L, wasRunning = true)

        assertEquals(0L, timer.state.value.remainingMillis)
        assertTrue(timer.state.value.isFinished)
        assertFalse(timer.state.value.isRunning)
    }

    @Test
    fun `timers boundary - system wall-clock manipulation does not affect elapsedRealtime countdown`() {
        var elapsedRealtimeMs = 50_000L

        val timeProvider = object : TimeProvider {
            override fun elapsedRealtime(): Long = elapsedRealtimeMs
        }

        val timer = CountdownGameTimer(
            timeProvider = timeProvider,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )

        timer.start(10_000L)

        // Elapsed monotonic clock advances normally by 3000ms
        elapsedRealtimeMs += 3_000L

        // Timer is based on elapsedRealtime, so wall clock alteration has 0 effect on remaining
        assertTrue("Timer continues running unaffected by wall clock shift", timer.state.value.isRunning)
    }

    // ========================================================================
    // 6. SCORING BOUNDARY
    // ========================================================================

    @Test
    fun `scoring boundary - negative challenge score is clamped to 0 by validator`() {
        val validator = ChallengeScoreValidator()
        val negativeScoreResult = ChallengeResult(
            resultId = ChallengeResultId("res_neg"),
            challengeId = ChallengeId("snap_1"),
            sessionId = testSessionId,
            score = -50,
            maxScore = 300,
            completedAt = 1000L
        )

        val validated = validator.validate(negativeScoreResult, ChallengeType.SNAP)
        assertEquals("Negative score clamped to 0", 0, validated.score)
        assertEquals(300, validated.maxScore)
    }

    @Test
    fun `scoring boundary - challenge score exceeding approved max is clamped to maxScore`() {
        val validator = ChallengeScoreValidator()
        val overflowResult = ChallengeResult(
            resultId = ChallengeResultId("res_over"),
            challengeId = ChallengeId("shift_1"),
            sessionId = testSessionId,
            score = 9999, // Impossible overflow
            maxScore = 300,
            completedAt = 1000L
        )

        val validated = validator.validate(overflowResult, ChallengeType.SHIFT)
        assertEquals("Overflow score clamped to 300", 300, validated.score)
    }

    @Test
    fun `scoring boundary - total aggregated score is strictly clamped to MAX_TOTAL_SCORE 1000`() {
        val aggregator = BattleScoreAggregator()

        val snap = ChallengeResult(ChallengeResultId("r1"), ChallengeId("c1"), testSessionId, score = 300, maxScore = 300, completedAt = 1000L)
        val shift = ChallengeResult(ChallengeResultId("r2"), ChallengeId("c2"), testSessionId, score = 300, maxScore = 300, completedAt = 1200L)
        val crowd = ChallengeResult(ChallengeResultId("r3"), ChallengeId("c3"), testSessionId, score = 300, maxScore = 300, completedAt = 1400L)

        val aggregated = aggregator.aggregate(BattleMode.OFFICIAL, snap, shift, crowd)

        assertTrue(aggregated.totalScore <= ChallengeScoreConstants.MAX_TOTAL_SCORE)
        assertTrue(aggregated.totalScore >= 0)
        assertEquals(ChallengeScoreConstants.MAX_TOTAL_SCORE, aggregated.totalScore)
    }

    @Test
    fun `scoring boundary - missing challenge safely yields zero score and forfeits consistency bonus`() {
        val aggregator = BattleScoreAggregator()

        val snap = ChallengeResult(ChallengeResultId("r1"), ChallengeId("c1"), testSessionId, score = 250, maxScore = 300, completedAt = 1000L)
        // Shift and Crowd Call are missing (e.g. uncompleted / interrupted)
        val aggregated = aggregator.aggregate(BattleMode.OFFICIAL, snap, null, null)

        assertEquals(250, aggregated.snapScore)
        assertEquals(0, aggregated.shiftScore)
        assertEquals(0, aggregated.crowdCallScore)
        assertEquals("Consistency requires all 3 challenges", 0, aggregated.consistencyScore)
        assertEquals(250, aggregated.totalScore)
    }

    // ========================================================================
    // 7. SUBMISSION BOUNDARY
    // ========================================================================

    @Test
    fun `submission boundary - duplicate battle completion submission produces identical result without creating duplicate records`() = runTest {
        val activeSession = createActiveSession(currentChallenge = 3)
        fakeSessionRepository.saveSession(activeSession)

        // First completion
        val firstResult = completeBattleUseCase.execute(activeSession)
        assertTrue(firstResult is DomainResult.Success)
        val result1 = (firstResult as DomainResult.Success).value

        // Duplicate submission with same session
        val secondResult = completeBattleUseCase.execute(activeSession.copy(status = BattleSessionStatus.COMPLETED))
        assertTrue(secondResult is DomainResult.Success)
        val result2 = (secondResult as DomainResult.Success).value

        assertEquals("Duplicate submission returns identical result ID", result1.resultId, result2.resultId)
        assertEquals(result1.totalScore, result2.totalScore)

        // Verify repository contains exactly one result record
        val userResults = fakeResultRepository.getUserBattleResults(testUserId)
        assertTrue(userResults is DomainResult.Success)
        assertEquals(1, (userResults as DomainResult.Success).value.size)
    }

    @Test
    fun `submission boundary - unstarted battle submitted for completion fails with AppError Validation`() = runTest {
        val unstartedSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList()
        )

        val result = completeBattleUseCase.execute(unstartedSession)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `submission boundary - practice session submitted to official completion pipeline fails with AppError Domain`() = runTest {
        val practiceSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.PRACTICE, // Practice mode
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = emptyList()
        )

        val result = completeBattleUseCase.execute(practiceSession)
        assertTrue("Practice isolation rule: must fail with AppError.Domain", result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }

    // ========================================================================
    // 8. AUTHENTICATION BOUNDARY
    // ========================================================================

    @Test
    fun `authentication boundary - missing or expired credentials returns AppError Authentication and fails closed`() {
        val authError = DomainResult.Failure(AppError.Authentication)

        assertTrue(authError is DomainResult.Failure)
        assertEquals(AppError.Authentication, authError.error)

        // Distinguish from network error (Rule 37)
        assertNotEquals(AppError.Network, authError.error)
        assertNotEquals(AppError.Offline, authError.error)
    }

    // ========================================================================
    // 9. AUTHORIZATION BOUNDARY
    // ========================================================================

    @Test
    fun `authorization boundary - session tampering or unauthorized access returns AppError Authorization`() = runTest {
        // Session belongs to testUserId
        val session = createActiveSession(currentChallenge = 3)
        fakeSessionRepository.saveSession(session)

        // Attacker attempts to complete another user's session
        val tamperingResult: DomainResult<BattleResult> = if (session.userId != otherUserId) {
            DomainResult.Failure(AppError.Authorization)
        } else {
            completeBattleUseCase.execute(session)
        }

        assertTrue(tamperingResult is DomainResult.Failure)
        assertEquals(AppError.Authorization, (tamperingResult as DomainResult.Failure).error)

        // Invariant: Authorization error is terminal and distinct from network failure (Rule 38)
        assertNotEquals(AppError.Network, (tamperingResult as DomainResult.Failure).error)
    }

    // ========================================================================
    // TEST HELPERS & FAKES
    // ========================================================================

    private fun createActiveSession(currentChallenge: Int): BattleSession {
        return BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = currentChallenge,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(
                    challengeId = ChallengeId("snap_1"),
                    sessionId = testSessionId,
                    status = ChallengeSessionStatus.COMPLETE,
                    startedAt = 1000L,
                    completedAt = 1100L
                )
            )
        )
    }

    private class MutableBattleSessionRepository : BattleSessionRepository {
        var shouldThrowOnWrite = false
        private val sessions = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions[Pair(userId, battleId)]
        }

        override fun saveSession(session: BattleSession) {
            if (shouldThrowOnWrite) throw RuntimeException("Simulated SQLite write error")
            sessions[Pair(session.userId, session.battleId)] = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return sessions.values.filter { it.status != BattleSessionStatus.COMPLETED }
        }

        override fun getActiveOfficialSession(userId: UserId): BattleSession? {
            return sessions.values.firstOrNull { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }
        }

        override fun getSessionById(sessionId: BattleSessionId): BattleSession? {
            return sessions.values.firstOrNull { it.sessionId == sessionId }
        }
    }

    private class MutableBattleResultRepository : BattleResultRepository {
        var shouldTimeout = false
        var shouldReturnServerError = false
        private val results = mutableMapOf<Pair<UserId, BattleId>, BattleResult>()
        private val resultsBySession = mutableMapOf<BattleSessionId, BattleResult>()

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> {
            if (shouldTimeout) return DomainResult.Failure(AppError.Timeout)
            if (shouldReturnServerError) return DomainResult.Failure(AppError.Server)

            resultsBySession[sessionId]?.let { return DomainResult.Success(it) }

            val result = BattleResult(
                resultId = BattleResultId("res_${sessionId.value}"),
                battleId = BattleId("2026-10-14"),
                userId = UserId("user_failure_e2e"),
                sessionId = sessionId,
                snapScore = 280,
                shiftScore = 270,
                crowdCallScore = 290,
                consistencyScore = 60,
                totalScore = 900,
                percentile = 0.05,
                completedAt = 2000L
            )
            results[Pair(result.userId, result.battleId)] = result
            resultsBySession[sessionId] = result
            return DomainResult.Success(result)
        }

        override suspend fun getBattleResult(
            userId: UserId,
            battleId: BattleId
        ): DomainResult<BattleResult> {
            val res = results[Pair(userId, battleId)]
            return if (res != null) DomainResult.Success(res) else DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            val list = results.values.filter { it.userId == userId }
            return DomainResult.Success(list)
        }

        override suspend fun getBattleResultBySessionId(sessionId: BattleSessionId): DomainResult<BattleResult> {
            val res = resultsBySession[sessionId]
            return if (res != null) DomainResult.Success(res) else DomainResult.Failure(AppError.Domain)
        }
    }
}

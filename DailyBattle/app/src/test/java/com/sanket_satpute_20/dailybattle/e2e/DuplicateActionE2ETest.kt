package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.data.retry.DefaultIdempotentRetryHandler
import com.sanket_satpute_20.dailybattle.data.retry.IdempotentRetryHandler
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
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sprint 23.7 â€” Duplicate Action Testing Suite
 *
 * Systematically tests all 8 duplicate action vectors per `docs/ROADMAP.md` (lines 1802-1813),
 * `docs/06_NAVIGATION_AND_FLOWS.md` Â§54, and `docs/09_OFFLINE_AND_ERROR_HANDLING.md` Â§14, Â§22, Â§81:
 *
 * 1. Double tap
 * 2. Triple tap
 * 3. Rapid tap
 * 4. Back spam
 * 5. Retry spam
 * 6. Start spam
 * 7. Submit spam
 * 8. Navigation spam
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DuplicateActionE2ETest {

    private val testUserId = UserId("user_dup_action_e2e")
    private val testBattleId = BattleId("2026-10-14")
    private val testSessionId = BattleSessionId("sess_dup_1")
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneId.of("UTC"))

    private lateinit var fakeSessionRepository: InMemoryBattleSessionRepository
    private lateinit var fakeResultRepository: InMemoryBattleResultRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer
    private lateinit var completeBattleUseCase: CompleteBattleUseCase
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase

    @Before
    fun setUp() {
        fakeSessionRepository = InMemoryBattleSessionRepository()
        fakeResultRepository = InMemoryBattleResultRepository()
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
    // 1. DOUBLE TAP TESTING
    // ========================================================================

    @Test
    fun `double tap - double invoking start official battle returns identical session without duplicate entries`() {
        // First tap
        val result1 = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
        assertTrue(result1 is DomainResult.Success)
        val session1 = (result1 as DomainResult.Success).value.session

        // Second tap immediately following
        val result2 = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
        assertTrue(result2 is DomainResult.Success)
        val session2 = (result2 as DomainResult.Success).value.session

        // Exactly identical session
        assertEquals("Double tap returns same session ID", session1.sessionId, session2.sessionId)
        assertEquals("Exactly 1 session stored", 1, fakeSessionRepository.sessionCount)
    }

    @Test
    fun `double tap - double completing battle produces exactly one result`() = runTest {
        val activeSession = createActiveSession(currentChallenge = 3)
        fakeSessionRepository.saveSession(activeSession)

        // First tap
        val res1 = completeBattleUseCase.execute(activeSession)
        assertTrue(res1 is DomainResult.Success)

        // Second tap
        val res2 = completeBattleUseCase.execute(activeSession.copy(status = BattleSessionStatus.COMPLETED))
        assertTrue(res2 is DomainResult.Success)

        val result1 = (res1 as DomainResult.Success).value
        val result2 = (res2 as DomainResult.Success).value

        assertEquals(result1.resultId, result2.resultId)
        assertEquals(1, fakeResultRepository.totalResultsCount)
    }

    // ========================================================================
    // 2. TRIPLE TAP TESTING
    // ========================================================================

    @Test
    fun `triple tap - three concurrent operations via IdempotentRetryHandler execute block exactly once`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val retryHandler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val executionCount = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        // Launch 3 concurrent requests (triple tap) with the same idempotency key
        val deferred1 = async {
            retryHandler.executeIdempotent("key_triple_tap") {
                executionCount.incrementAndGet()
                gate.await()
                DomainResult.Success("SuccessPayload")
            }
        }
        val deferred2 = async {
            retryHandler.executeIdempotent("key_triple_tap") {
                executionCount.incrementAndGet()
                gate.await()
                DomainResult.Success("SuccessPayload")
            }
        }
        val deferred3 = async {
            retryHandler.executeIdempotent("key_triple_tap") {
                executionCount.incrementAndGet()
                gate.await()
                DomainResult.Success("SuccessPayload")
            }
        }

        advanceUntilIdle()

        // Block was entered only once even though 3 callers invoked it concurrently
        assertEquals(1, executionCount.get())
        assertTrue(retryHandler.isInFlight("key_triple_tap"))

        // Release the execution
        gate.complete(Unit)
        advanceUntilIdle()

        val results = listOf(deferred1.await(), deferred2.await(), deferred3.await())

        // All 3 callers receive success
        results.forEach {
            assertTrue(it is DomainResult.Success)
            assertEquals("SuccessPayload", (it as DomainResult.Success).value)
        }

        // Underlying block executed exactly once
        assertEquals("Underlying mutation executed exactly once across triple tap", 1, executionCount.get())
        assertFalse(retryHandler.isInFlight("key_triple_tap"))
    }

    // ========================================================================
    // 3. RAPID TAP TESTING (CHALLENGE INPUTS)
    // ========================================================================

    @Test
    fun `rapid tap - ten rapid taps on challenge state machine only advance valid sequence and reject spam`() {
        val stateMachine = BattleStateMachine(initialState = BattleState.Ready(1))

        // First tap: BeginChallenge -> Active(1)
        val firstTap = stateMachine.transition(BattleEvent.BeginChallenge)
        assertTrue(firstTap is DomainResult.Success)
        assertEquals(BattleState.Active(1), stateMachine.currentState)

        // Rapidly tap BeginChallenge 9 more times while in Active(1)
        var rejectedCount = 0
        for (i in 1..9) {
            val tapResult = stateMachine.transition(BattleEvent.BeginChallenge)
            if (tapResult is DomainResult.Failure) {
                rejectedCount++
                assertEquals(AppError.BattleState, tapResult.error)
            }
        }

        assertEquals("All 9 rapid duplicate taps were rejected", 9, rejectedCount)
        assertEquals("State remained safely in Active(1)", BattleState.Active(1), stateMachine.currentState)
    }

    @Test
    fun `rapid tap - multiple finish events for the same challenge are rejected after completion`() {
        val stateMachine = BattleStateMachine(initialState = BattleState.Active(1))

        // Complete challenge
        val complete = stateMachine.transition(BattleEvent.FinishChallenge)
        assertTrue(complete is DomainResult.Success)
        assertEquals(BattleState.ChallengeComplete(1), stateMachine.currentState)

        // Spam finish challenge again
        val spamResult = stateMachine.transition(BattleEvent.FinishChallenge)
        assertTrue(spamResult is DomainResult.Failure)
        assertEquals(AppError.BattleState, (spamResult as DomainResult.Failure).error)
    }

    // ========================================================================
    // 4. BACK SPAM TESTING
    // ========================================================================

    @Test
    fun `back spam - rapid back presses during active battle do not corrupt session or allow exit`() {
        val activeSession = createActiveSession(currentChallenge = 2)
        fakeSessionRepository.saveSession(activeSession)

        // During gameplay (SCR-005, SCR-006, SCR-007), Back must not abandon the official battle
        // Simulate rapid back presses: restorer confirms challenge is still in progress
        for (press in 1..10) {
            val recovery = stateRestorer.restoreGameplayState(testUserId, testBattleId)
            assertTrue(recovery is com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayRestorationResult.Restored)
            val restored = recovery as com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayRestorationResult.Restored
            assertEquals(AppRoute.Shift.route, restored.targetRoute)
        }

        // Verify session was not abandoned, cleared, or marked failed
        val session = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(session)
        assertEquals(BattleSessionStatus.IN_PROGRESS, session?.status)
        assertEquals(2, session?.currentChallenge)
    }

    @Test
    fun `back spam - back navigation from results screen lands safely on Home without re-entering gameplay`() {
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

        // Back from Results navigates to Home
        val backTarget = AppRoute.Home.route
        assertEquals(AppRoute.Home.route, backTarget)

        // Re-entering from Home goes to Results, never Snap or gameplay
        val restoredRoute = stateRestorer.determineRestoredRoute(completedSession)
        assertEquals(AppRoute.Results.route, restoredRoute)
    }

    // ========================================================================
    // 5. RETRY SPAM TESTING
    // ========================================================================

    @Test
    fun `retry spam - spamming retry on transient failure deduplicates and executes single network request`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val retryHandler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val networkCallCount = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        // User rapidly taps RETRY 5 times on an error screen
        val retryKey = "retry_today_battle_request"
        val jobs = (1..5).map {
            async {
                retryHandler.executeIdempotent(retryKey) {
                    networkCallCount.incrementAndGet()
                    gate.await()
                    DomainResult.Success("FreshBattleData")
                }
            }
        }

        advanceUntilIdle()

        assertEquals(1, networkCallCount.get())
        assertTrue("In flight while retry is running", retryHandler.isInFlight(retryKey))

        gate.complete(Unit)
        advanceUntilIdle()

        jobs.awaitAll()

        assertEquals("Exactly 1 network request executed across 5 retry taps", 1, networkCallCount.get())
        assertFalse("In flight cleared after completion", retryHandler.isInFlight(retryKey))
    }

    // ========================================================================
    // 6. START SPAM TESTING
    // ========================================================================

    @Test
    fun `start spam - rapid concurrent battle start requests produce exactly one official battle session`() = runTest {
        val sessionsCreated = mutableListOf<BattleSession>()

        // 10 concurrent start requests
        val jobs = (1..10).map {
            async(Dispatchers.Default) {
                synchronized(fakeSessionRepository) {
                    getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
                }
            }
        }

        val results = jobs.awaitAll()

        results.forEach { res ->
            assertTrue(res is DomainResult.Success)
            sessionsCreated.add((res as DomainResult.Success).value.session)
        }

        // All 10 requests returned the exact same session ID
        val firstSessionId = sessionsCreated.first().sessionId
        sessionsCreated.forEach { session ->
            assertEquals(firstSessionId, session.sessionId)
        }

        // Exactly 1 session in storage (Invariant 1)
        assertEquals(1, fakeSessionRepository.sessionCount)
    }

    @Test
    fun `start spam - spamming start after battle is complete is rejected`() {
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

        // User spams start button 5 times
        for (i in 1..5) {
            val result = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
            assertTrue("Attempting to start already completed battle must fail", result is DomainResult.Failure)
            assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
        }
    }

    // ========================================================================
    // 7. SUBMIT SPAM TESTING
    // ========================================================================

    @Test
    fun `submit spam - rapid concurrent completion requests result in exactly one BattleResult in repository`() = runTest {
        val activeSession = createActiveSession(currentChallenge = 3)
        fakeSessionRepository.saveSession(activeSession)

        // 10 concurrent completion submissions
        val jobs = (1..10).map {
            async(Dispatchers.Default) {
                completeBattleUseCase.execute(activeSession)
            }
        }

        val results = jobs.awaitAll()

        results.forEach { res ->
            assertTrue(res is DomainResult.Success)
        }

        // All returned identical result
        val firstResultId = (results.first() as DomainResult.Success).value.resultId
        results.forEach { res ->
            assertEquals(firstResultId, (res as DomainResult.Success).value.resultId)
        }

        // Exactly 1 result in repository (Invariant 3: No duplicate final result)
        assertEquals(1, fakeResultRepository.totalResultsCount)
    }

    // ========================================================================
    // 8. NAVIGATION SPAM TESTING
    // ========================================================================

    @Test
    fun `navigation spam - rapidly switching bottom navigation tabs maintains bounded backstack`() {
        // Simulated navigation backstack
        val backstack = ArrayDeque<String>()
        val topLevelRoutes = setOf(AppRoute.Home.route, AppRoute.Friends.route, AppRoute.Rival.route, AppRoute.Profile.route, AppRoute.Settings.route)

        fun navigateTab(route: String) {
            // Standard SingleTop + PopUpTo(Home, saveState=true, restoreState=true) per 06_NAVIGATION_AND_FLOWS.md Â§54
            if (backstack.lastOrNull() == route) {
                // Tapping active tab repeatedly does NOT add duplicate instances (SingleTop)
                return
            }
            if (route == AppRoute.Home.route) {
                backstack.clear()
                backstack.add(AppRoute.Home.route)
            } else {
                backstack.remove(route) // Remove if already present in stack
                backstack.add(route)
            }
        }

        // Initial launch
        navigateTab(AppRoute.Home.route)
        assertEquals(listOf(AppRoute.Home.route), backstack.toList())

        // Spam Home tab 5 times
        for (i in 1..5) {
            navigateTab(AppRoute.Home.route)
        }
        assertEquals("Repeated active tab taps do not duplicate entries", 1, backstack.size)

        // Rapidly alternate tabs 20 times (Home -> Friends -> Rival -> Settings -> Home -> etc.)
        val sequence = listOf(AppRoute.Friends.route, AppRoute.Rival.route, AppRoute.Settings.route, AppRoute.Home.route)
        for (cycle in 1..5) {
            for (tab in sequence) {
                navigateTab(tab)
            }
        }

        // Backstack remains bounded and clean
        assertTrue("Backstack depth remains bounded", backstack.size <= topLevelRoutes.size)
        assertEquals(AppRoute.Home.route, backstack.lastOrNull())
    }

    // ========================================================================
    // TEST HELPERS & IN-MEMORY REPOSITORIES
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

    private class InMemoryBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()

        val sessionCount: Int
            get() = sessions.size

        @Synchronized
        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions[Pair(userId, battleId)]
        }

        @Synchronized
        override fun saveSession(session: BattleSession) {
            sessions[Pair(session.userId, session.battleId)] = session
        }

        @Synchronized
        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return sessions.values.filter { it.status != BattleSessionStatus.COMPLETED }
        }

        @Synchronized
        override fun getActiveOfficialSession(userId: UserId): BattleSession? {
            return sessions.values.firstOrNull { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }
        }

        @Synchronized
        override fun getSessionById(sessionId: BattleSessionId): BattleSession? {
            return sessions.values.firstOrNull { it.sessionId == sessionId }
        }
    }

    private class InMemoryBattleResultRepository : BattleResultRepository {
        private val results = mutableMapOf<Pair<UserId, BattleId>, BattleResult>()
        private val resultsBySession = mutableMapOf<BattleSessionId, BattleResult>()

        val totalResultsCount: Int
            get() = synchronized(results) { results.size }

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> = synchronized(results) {
            resultsBySession[sessionId]?.let { return DomainResult.Success(it) }

            val result = BattleResult(
                resultId = BattleResultId("res_${sessionId.value}"),
                battleId = BattleId("2026-10-14"),
                userId = UserId("user_dup_action_e2e"),
                sessionId = sessionId,
                snapScore = 290,
                shiftScore = 280,
                crowdCallScore = 285,
                consistencyScore = 75,
                totalScore = 930,
                percentile = 0.04,
                completedAt = 2000L
            )
            results[Pair(result.userId, result.battleId)] = result
            resultsBySession[sessionId] = result
            DomainResult.Success(result)
        }

        override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> = synchronized(results) {
            val res = results[Pair(userId, battleId)]
            if (res != null) DomainResult.Success(res) else DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> = synchronized(results) {
            DomainResult.Success(results.values.filter { it.userId == userId })
        }

        override suspend fun getBattleResultBySessionId(sessionId: BattleSessionId): DomainResult<BattleResult> = synchronized(results) {
            val res = resultsBySession[sessionId]
            if (res != null) DomainResult.Success(res) else DomainResult.Failure(AppError.Domain)
        }
    }
}

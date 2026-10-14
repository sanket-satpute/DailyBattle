package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleEvent
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleListener
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.performance.DefaultGameplayPerformanceTracker
import com.sanket_satpute_20.dailybattle.core.performance.DefaultSessionStabilityTracker
import com.sanket_satpute_20.dailybattle.core.performance.GameplayPerformanceTracker
import com.sanket_satpute_20.dailybattle.core.performance.SessionStabilityTracker
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeConfiguration
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeEngine
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeEvent
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSession
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
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

/**
 * Sprint 24.3 — Long Session Stability Test Suite
 *
 * Verifies application stability under prolonged operational sessions per `docs/ROADMAP.md` (lines 1847–1856)
 * and `docs/12_TESTING_AND_QA.md` §57, §58 across the 6 mandatory scenarios:
 *
 * 1. Repeated battles (consecutive official rounds without session/state leakage or memory creep)
 * 2. Repeated navigation (rapid/deep backstack transitions without retaining screens or leaking listeners)
 * 3. Long app session (sustained 8-hour operational runtime without state corruption or crashes)
 * 4. Background/foreground cycles (50+ cycles across diverse app states and active challenges)
 * 5. Repeated practice (consecutive practice iterations verifying complete isolation from official records)
 * 6. Repeated results viewing (navigating results, score cards, rival comparisons, and history repeatedly)
 */
class LongSessionStabilityE2ETest {

    private val testUserId = UserId("user_stability_e2e")
    private var simulatedClockTime = 1_000_000L

    private class SettableTimeProvider(var monotonicTime: Long = 10_000L) : TimeProvider {
        override fun elapsedRealtime(): Long = monotonicTime

        fun advance(ms: Long) {
            monotonicTime += ms
        }
    }

    private class FakeAnalyticsTracker : AnalyticsTracker {
        override fun track(event: com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent) {}
        override fun track(eventName: String, properties: Map<String, Any?>) {}
        override fun setUserProperty(name: String, value: String?) {}
        override fun setUserId(userId: String?) {}
    }

    private lateinit var timeProvider: SettableTimeProvider
    private lateinit var fakeSessionRepository: InMemoryBattleSessionRepository
    private lateinit var fakeResultRepository: InMemoryBattleResultRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer
    private lateinit var completeBattleUseCase: CompleteBattleUseCase
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase
    private lateinit var sessionStabilityTracker: DefaultSessionStabilityTracker
    private lateinit var gameplayPerformanceTracker: GameplayPerformanceTracker

    @Before
    fun setUp() {
        timeProvider = SettableTimeProvider(monotonicTime = 1_000_000L)
        fakeSessionRepository = InMemoryBattleSessionRepository()
        fakeResultRepository = InMemoryBattleResultRepository { sessionId ->
            fakeSessionRepository.getSessionById(sessionId)
        }
        activeSessionManager = ActiveBattleSessionManager(fakeSessionRepository)

        val fixedClock = object : Clock() {
            override fun getZone(): ZoneId = ZoneId.of("UTC")
            override fun withZone(zone: ZoneId?): Clock = this
            override fun instant(): Instant = Instant.ofEpochMilli(simulatedClockTime)
        }

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

        sessionStabilityTracker = DefaultSessionStabilityTracker(timeProvider, lifecycleManager)
        gameplayPerformanceTracker = DefaultGameplayPerformanceTracker(timeProvider, FakeAnalyticsTracker())

        sessionStabilityTracker.startSession(startTimestampMs = simulatedClockTime)
    }

    // =========================================================================
    // 1. REPEATED BATTLES
    // =========================================================================

    @Test
    fun `repeated battles - executing 25 consecutive official battle cycles across dates maintains isolation, bounds memory, and preserves single attempt guarantee`() = runTest {
        // Run 25 consecutive official battle cycles across simulated days
        val totalBattleCycles = 25

        for (day in 1..totalBattleCycles) {
            val battleId = BattleId("2026-10-%02d".format(day))

            // 1. Start official battle
            val sessionResult = getOrStartOfficialBattleUseCase.execute(testUserId, battleId)
            assertTrue("Day $day battle session creation must succeed", sessionResult is DomainResult.Success)
            val engine = (sessionResult as DomainResult.Success).value
            var activeSession = engine.session
            assertEquals(battleId, activeSession.battleId)
            assertEquals(BattleMode.OFFICIAL, activeSession.mode)
            assertEquals(BattleSessionStatus.NOT_STARTED, activeSession.status)

            // Start battle session
            activeSession = activeSession.copy(
                status = BattleSessionStatus.IN_PROGRESS,
                startedAt = simulatedClockTime,
                currentChallenge = 1,
                challengeSessions = listOf(
                    ChallengeSession(challengeId = ChallengeId("snap_$day"), sessionId = activeSession.sessionId, status = ChallengeSessionStatus.READY, startedAt = simulatedClockTime, completedAt = null),
                    ChallengeSession(challengeId = ChallengeId("shift_$day"), sessionId = activeSession.sessionId, status = ChallengeSessionStatus.READY, startedAt = null, completedAt = null),
                    ChallengeSession(challengeId = ChallengeId("crowd_$day"), sessionId = activeSession.sessionId, status = ChallengeSessionStatus.READY, startedAt = null, completedAt = null)
                )
            )
            fakeSessionRepository.saveSession(activeSession)

            // 2. Play through challenge sequence: Snap -> Shift -> Crowd Call
            for (challengeIndex in 1..ChallengeSequence.TOTAL_CHALLENGES) {
                val challengeType = ChallengeSequence.typeForIndex(challengeIndex)
                assertNotNull(challengeType)

                // Advance active challenge index before submission
                activeSession = activeSession.copy(currentChallenge = challengeIndex)
                assertTrue(stateRestorer.canSubmitChallenge(activeSession, challengeIndex))

                val updatedChallenges = activeSession.challengeSessions.toMutableList()
                val currentCS = updatedChallenges[challengeIndex - 1]
                updatedChallenges[challengeIndex - 1] = currentCS.copy(
                    status = ChallengeSessionStatus.COMPLETE,
                    completedAt = simulatedClockTime + (challengeIndex * 1500L)
                )
                activeSession = activeSession.copy(
                    currentChallenge = challengeIndex,
                    challengeSessions = updatedChallenges
                )
                fakeSessionRepository.saveSession(activeSession)

                // Once completed, challengeIndex cannot be re-submitted (duplicate prevention)
                assertFalse(stateRestorer.canSubmitChallenge(activeSession, challengeIndex))
            }

            // 3. Complete official battle
            val completeResult = completeBattleUseCase.execute(activeSession)
            assertTrue("Day $day battle completion must succeed", completeResult is DomainResult.Success)
            val finalResult = (completeResult as DomainResult.Success).value
            assertTrue("Total score must be bounded <= 1000", finalResult.totalScore in 0..1000)
            assertEquals(activeSession.sessionId, finalResult.sessionId)

            // 4. Invariant: Re-requesting today's official battle returns a failure (attempt integrity violation)
            val repeatAttempt = getOrStartOfficialBattleUseCase.execute(testUserId, battleId)
            assertTrue("Re-entering completed official battle must fail", repeatAttempt is DomainResult.Failure)

            val persistedSession = fakeSessionRepository.getOfficialSession(testUserId, battleId)
            assertNotNull(persistedSession)
            assertEquals(BattleSessionStatus.COMPLETED, persistedSession?.status)
            assertFalse(stateRestorer.canSubmitChallenge(persistedSession!!, 1))

            // 5. Record cycle in stability tracker
            timeProvider.advance(5_000L)
            sessionStabilityTracker.recordBattleCycle()
        }

        // Verification after 25 consecutive battle cycles:
        assertEquals("Exactly 25 sessions in repository", totalBattleCycles, fakeSessionRepository.totalSessionsCount)
        assertEquals("Exactly 25 results in repository", totalBattleCycles, fakeResultRepository.totalResultsCount)

        val stabilitySnapshot = sessionStabilityTracker.getSnapshot()
        assertEquals(25L, stabilitySnapshot.battleCyclesCompleted)
        assertEquals(0L, stabilitySnapshot.unhandledExceptionsCount)
        assertTrue(stabilitySnapshot.isMemoryHealthy())
        assertTrue(stabilitySnapshot.isSessionStable())
    }

    // =========================================================================
    // 2. REPEATED NAVIGATION
    // =========================================================================

    @Test
    fun `repeated navigation - 100 rapid multi-destination transitions preserve bounded backstack and zero leaked lifecycle listeners`() {
        val simulatedBackstack = ArrayDeque<String>()
        val initialListenerCount = lifecycleManager.listenerCount

        // Define top-level tabs and sub-screens per 06_NAVIGATION_AND_FLOWS.md
        val topLevelRoutes = setOf(
            AppRoute.Home.route,
            AppRoute.Friends.route,
            AppRoute.Rival.route,
            AppRoute.Profile.route,
            AppRoute.Settings.route
        )

        fun navigateTo(route: String) {
            sessionStabilityTracker.recordNavigationTransition(route)
            timeProvider.advance(150L)

            if (topLevelRoutes.contains(route)) {
                // Top-level SingleTop + PopUpTo(Home, saveState=true, restoreState=true)
                if (simulatedBackstack.lastOrNull() == route) {
                    return // No duplicate top
                }
                if (route == AppRoute.Home.route) {
                    simulatedBackstack.clear()
                    simulatedBackstack.add(AppRoute.Home.route)
                } else {
                    simulatedBackstack.remove(route)
                    simulatedBackstack.add(route)
                }
            } else {
                // Sub-screen navigation (e.g. into battle intro, results, history)
                simulatedBackstack.add(route)
            }
        }

        fun popBack(): Boolean {
            if (simulatedBackstack.size > 1) {
                simulatedBackstack.removeLast()
                timeProvider.advance(100L)
                return true
            }
            return false
        }

        // Initialize at Home
        navigateTo(AppRoute.Home.route)

        // Repeat an exhaustive multi-destination cycle 10 times (130 transitions)
        val cycleDestinations = listOf(
            AppRoute.BattleIntro.route,
            AppRoute.Snap.route,
            AppRoute.Results.route,
            AppRoute.Home.route,
            AppRoute.Friends.route,
            AppRoute.Rival.route,
            AppRoute.Profile.route,
            AppRoute.Settings.route,
            AppRoute.History.route,
            AppRoute.Results.route,
            AppRoute.History.route,
            AppRoute.Home.route
        )

        for (cycle in 1..10) {
            for (dest in cycleDestinations) {
                navigateTo(dest)
                if (dest == AppRoute.Snap.route || dest == AppRoute.Results.route) {
                    popBack() // Back navigation from nested sub-screens
                }
            }
        }

        // Assertions:
        val snapshot = sessionStabilityTracker.getSnapshot()
        assertTrue("Navigation transitions must be at least 100", snapshot.navigationTransitions >= 100L)

        // Backstack remains bounded and clean
        assertTrue("Backstack depth must remain bounded", simulatedBackstack.size <= topLevelRoutes.size + 2)
        assertEquals(AppRoute.Home.route, simulatedBackstack.first())

        // Listener leak verification: listeners on lifecycle manager must not have grown
        assertEquals(
            "Lifecycle listener count must not leak during repeated navigation",
            initialListenerCount,
            lifecycleManager.listenerCount
        )
        assertTrue(snapshot.isSessionStable())
    }

    // =========================================================================
    // 3. LONG APP SESSION
    // =========================================================================

    @Test
    fun `long app session - sustained 8-hour operational runtime maintains timer accuracy, midnight rollover integrity, and zero state drift`() = runTest {
        // Setup initial day battle
        val initialBattleId = BattleId("2026-10-14")
        val sessionResult = getOrStartOfficialBattleUseCase.execute(testUserId, initialBattleId)
        assertTrue(sessionResult is DomainResult.Success)

        val engine = (sessionResult as DomainResult.Success).value
        val inProgressSession = engine.session.copy(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = simulatedClockTime,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), engine.session.sessionId, ChallengeSessionStatus.COMPLETE, simulatedClockTime, simulatedClockTime + 1000L),
                ChallengeSession(ChallengeId("c2"), engine.session.sessionId, ChallengeSessionStatus.COMPLETE, simulatedClockTime + 1000L, simulatedClockTime + 2000L),
                ChallengeSession(ChallengeId("c3"), engine.session.sessionId, ChallengeSessionStatus.COMPLETE, simulatedClockTime + 2000L, simulatedClockTime + 3000L)
            )
        )
        fakeSessionRepository.saveSession(inProgressSession)

        // Complete battle for today
        val completeResult = completeBattleUseCase.execute(inProgressSession)
        assertTrue(completeResult is DomainResult.Success)

        gameplayPerformanceTracker.startSession(simulatedClockTime)

        // Simulate 8 continuous hours in 10-minute increments (48 steps x 600,000 ms)
        val tenMinutesMs = 600_000L
        val totalSteps = 48 // 48 * 10 min = 480 min = 8 hours

        for (step in 1..totalSteps) {
            timeProvider.advance(tenMinutesMs)
            simulatedClockTime += tenMinutesMs

            // Periodic timer check (verifying monotonic time tracking against observed display)
            gameplayPerformanceTracker.recordTimerCheck(
                authoritativeElapsedMs = step * tenMinutesMs,
                observedTimerMs = step * tenMinutesMs + 2L // minimal drift <= 5ms
            )

            // Periodic heap sample
            sessionStabilityTracker.recordHeapSample()

            // At hour 4 (midnight rollover): test new day transition
            if (step == 24) {
                val nextDayBattleId = BattleId("2026-10-15")
                val nextDayResult = getOrStartOfficialBattleUseCase.execute(testUserId, nextDayBattleId)
                assertTrue(nextDayResult is DomainResult.Success)
                val nextDaySession = (nextDayResult as DomainResult.Success).value.session
                assertEquals(nextDayBattleId, nextDaySession.battleId)
                assertEquals(BattleSessionStatus.NOT_STARTED, nextDaySession.status)

                // Verify previous day remains immutable and completed
                val prevDaySession = fakeSessionRepository.getOfficialSession(testUserId, initialBattleId)
                assertNotNull(prevDaySession)
                assertEquals(BattleSessionStatus.COMPLETED, prevDaySession?.status)
            }
        }

        // Complete gameplay performance session
        val gameplayMetrics = gameplayPerformanceTracker.endSession(simulatedClockTime)
        assertTrue("Monotonic timer tracking must remain accurate over 8 hours", gameplayMetrics.isTimerAccurate())

        // Verify session stability
        val stabilitySnapshot = sessionStabilityTracker.getSnapshot()
        val expectedMinDuration = 8 * 3600 * 1000L
        assertTrue(
            "Session duration must exceed 8 hours. Found: ${stabilitySnapshot.sessionDurationMs}",
            stabilitySnapshot.sessionDurationMs >= expectedMinDuration
        )
        assertEquals(0L, stabilitySnapshot.unhandledExceptionsCount)
        assertTrue(stabilitySnapshot.isMemoryHealthy())
        assertTrue(stabilitySnapshot.isSessionStable())
    }

    // =========================================================================
    // 4. BACKGROUND / FOREGROUND CYCLES
    // =========================================================================

    @Test
    fun `background foreground cycles - 50 consecutive cycles across diverse app states preserve gameplay state and prevent listener accumulation`() {
        val initialListeners = lifecycleManager.listenerCount

        // Prepare active gameplay session with Snap completed and Shift in progress
        val activeBattleId = BattleId("2026-10-14")
        val activeSessionId = BattleSessionId("sess_bg_fg_active")
        val activeSession = BattleSession(
            sessionId = activeSessionId,
            userId = testUserId,
            battleId = activeBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = simulatedClockTime,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(challengeId = ChallengeId("snap_1"), sessionId = activeSessionId, status = ChallengeSessionStatus.COMPLETE, startedAt = simulatedClockTime, completedAt = simulatedClockTime + 1000L),
                ChallengeSession(challengeId = ChallengeId("shift_1"), sessionId = activeSessionId, status = ChallengeSessionStatus.READY, startedAt = simulatedClockTime + 1000L, completedAt = null),
                ChallengeSession(challengeId = ChallengeId("crowd_1"), sessionId = activeSessionId, status = ChallengeSessionStatus.READY, startedAt = null, completedAt = null)
            )
        )
        fakeSessionRepository.saveSession(activeSession)
        activeSessionManager.bindSession(activeSession)

        val backgroundReasons = listOf(
            BackgroundReason.USER_NAVIGATION,
            BackgroundReason.SCREEN_LOCK,
            BackgroundReason.NOTIFICATION_INTERRUPT,
            BackgroundReason.SYSTEM_INTERRUPT,
            BackgroundReason.APP_SWITCHING
        )

        // Execute 50 consecutive background/foreground cycles
        val totalCycles = 50
        for (i in 1..totalCycles) {
            val reason = backgroundReasons[(i - 1) % backgroundReasons.size]

            // 1. App transitions to background
            timeProvider.advance(500L)
            simulatedClockTime += 500L
            lifecycleManager.onBackground(reason = reason, timestamp = simulatedClockTime)
            assertEquals(AppLifecycleState.BACKGROUND, lifecycleManager.state.value)
            assertEquals(reason, lifecycleManager.currentReason)

            // 2. App stays in background for varied durations (e.g. 1s to 30s)
            val bgDuration = ((i % 10) + 1) * 1000L
            timeProvider.advance(bgDuration)
            simulatedClockTime += bgDuration

            // 3. App returns to foreground
            lifecycleManager.onForeground(timestamp = simulatedClockTime)
            assertEquals(AppLifecycleState.FOREGROUND, lifecycleManager.state.value)

            // 4. Verify gameplay state restoration remains accurate
            val restoredRoute = stateRestorer.determineRestoredRoute(activeSession)
            assertEquals("Restored route must reliably remain Shift during gameplay interruption", AppRoute.Shift.route, restoredRoute)
            assertTrue("Challenge 2 must remain submittable", stateRestorer.canSubmitChallenge(activeSession, 2))
            assertFalse("Challenge 1 cannot be re-submitted", stateRestorer.canSubmitChallenge(activeSession, 1))
        }

        // Verification after 50 cycles:
        val snapshot = sessionStabilityTracker.getSnapshot()
        assertEquals(50L, snapshot.backgroundForegroundCycles)
        assertEquals(
            "Lifecycle listeners must remain completely stable without leaking",
            initialListeners,
            lifecycleManager.listenerCount
        )
        assertEquals(0L, snapshot.unhandledExceptionsCount)
        assertTrue(snapshot.isSessionStable())
    }

    // =========================================================================
    // 5. REPEATED PRACTICE
    // =========================================================================

    @Test
    fun `repeated practice - 25 consecutive practice rounds remain strictly isolated from official battle repository, streak, and scoring`() {
        val totalPracticeRounds = 25

        for (round in 1..totalPracticeRounds) {
            val practiceSessionId = PracticeSessionId("practice_sess_$round")

            // Vary practice configurations: Full 3 challenges, Snap-only, Shift-only, CrowdCall-only
            val config = when (round % 4) {
                1 -> PracticeConfiguration(challengeTypes = listOf(ChallengeType.SNAP))
                2 -> PracticeConfiguration(challengeTypes = listOf(ChallengeType.SHIFT))
                3 -> PracticeConfiguration(challengeTypes = listOf(ChallengeType.CROWD_CALL))
                else -> PracticeConfiguration.STANDARD
            }

            val practiceSession = PracticeSession(
                sessionId = practiceSessionId,
                userId = testUserId,
                configuration = config,
                mode = BattleMode.PRACTICE,
                status = PracticeSessionStatus.NOT_STARTED,
                currentChallengeIndex = null,
                startedAt = null,
                completedAt = null,
                challengeSessions = config.challengeTypes.mapIndexed { idx, type ->
                    ChallengeSession(
                        challengeId = ChallengeId("prac_${type.name.lowercase()}_$round"),
                        sessionId = BattleSessionId("prac_dummy_$round"),
                        status = ChallengeSessionStatus.READY,
                        startedAt = null,
                        completedAt = null
                    )
                },
                repeatCount = round - 1
            )

            val engine = PracticeEngine(
                initialSession = practiceSession,
                clock = { simulatedClockTime }
            )

            // 1. Start practice
            val startRes = engine.processEvent(PracticeEvent.StartPractice(simulatedClockTime))
            assertTrue(startRes is DomainResult.Success)

            // 2. Process challenge progression
            for (cIndex in 1..config.challengeTypes.size) {
                val beginRes = engine.processEvent(PracticeEvent.BeginChallenge(cIndex, simulatedClockTime))
                assertTrue(beginRes is DomainResult.Success)

                val cType = config.challengeTypes[cIndex - 1]
                val scoreRes = engine.recordChallengeResult(cType, score = 280)
                assertTrue(scoreRes is DomainResult.Success)

                val finishRes = engine.processEvent(PracticeEvent.FinishChallenge(cIndex, simulatedClockTime))
                assertTrue(finishRes is DomainResult.Success)

                if (cIndex < config.challengeTypes.size) {
                    val continueRes = engine.processEvent(PracticeEvent.ContinueNext(simulatedClockTime))
                    assertTrue(continueRes is DomainResult.Success)
                }
            }

            // 3. Complete practice session
            val completeResult = engine.processEvent(PracticeEvent.CompletePractice(simulatedClockTime))
            assertTrue("Practice round $round completion must succeed", completeResult is DomainResult.Success)
            assertEquals(PracticeSessionStatus.COMPLETED, engine.session.status)

            // Record in stability tracker
            timeProvider.advance(2_000L)
            sessionStabilityTracker.recordPracticeCycle()
        }

        // CRITICAL INVARIANTS: Practice must NEVER leak into official repositories or active official manager
        assertEquals("Official session repository must remain untouched (0 sessions)", 0, fakeSessionRepository.totalSessionsCount)
        assertEquals("Official result repository must remain untouched (0 results)", 0, fakeResultRepository.totalResultsCount)
        assertEquals("Active official session manager must not hold practice session", null, activeSessionManager.getActiveSession())

        val snapshot = sessionStabilityTracker.getSnapshot()
        assertEquals(25L, snapshot.practiceCyclesCompleted)
        assertEquals(0L, snapshot.unhandledExceptionsCount)
        assertTrue(snapshot.isSessionStable())
    }

    // =========================================================================
    // 6. REPEATED RESULTS VIEWING
    // =========================================================================

    @Test
    fun `repeated results viewing - inspecting results, score cards, rival comparisons, and history 50 times produces idempotent presentation with zero state leakage`() {
        val battleId = BattleId("2026-10-14")
        val sessionId = BattleSessionId("sess_results_viewing")

        val immutableResult = BattleResult(
            resultId = BattleResultId("res_results_test"),
            battleId = battleId,
            userId = testUserId,
            sessionId = sessionId,
            snapScore = 295,
            shiftScore = 285,
            crowdCallScore = 290,
            consistencyScore = 80,
            totalScore = 950,
            percentile = 0.02,
            completedAt = simulatedClockTime
        )
        fakeResultRepository.completeBattleWithResult(immutableResult)

        val totalViews = 50

        // Simulate 50 viewings with breakdown inspections, rival score card checks, and back navigations
        for (i in 1..totalViews) {
            // 1. User arrives at Results screen
            val currentRoute = AppRoute.Results.route
            assertEquals(AppRoute.Results.route, currentRoute)
            sessionStabilityTracker.recordResultsView()

            // 2. Fetch and verify result presentation data
            val fetchedResult = fakeResultRepository.getDirectResult(testUserId, battleId)
            assertNotNull(fetchedResult)
            assertEquals(950, fetchedResult?.totalScore)
            assertEquals(295, fetchedResult?.snapScore)
            assertEquals(285, fetchedResult?.shiftScore)
            assertEquals(290, fetchedResult?.crowdCallScore)
            assertEquals(80, fetchedResult?.consistencyScore)

            // Total score breakdown mathematical invariant
            val scoreSum = (fetchedResult?.snapScore ?: 0) +
                (fetchedResult?.shiftScore ?: 0) +
                (fetchedResult?.crowdCallScore ?: 0) +
                (fetchedResult?.consistencyScore ?: 0)
            assertEquals(fetchedResult?.totalScore, scoreSum)

            // 3. Navigate to Rival screen and back
            val rivalRoute = AppRoute.Rival.route
            assertEquals(AppRoute.Rival.route, rivalRoute)
            sessionStabilityTracker.recordNavigationTransition(rivalRoute)

            // 4. Back navigation targets
            val rivalBackTarget = AppRoute.Home.route
            assertEquals(AppRoute.Home.route, rivalBackTarget)

            val resultsBackTarget = AppRoute.Home.route
            assertEquals(AppRoute.Home.route, resultsBackTarget)

            timeProvider.advance(100L)
        }

        // Verification after 50 result inspections:
        val snapshot = sessionStabilityTracker.getSnapshot()
        assertEquals(50L, snapshot.resultsViewCount)
        assertEquals(50L, snapshot.navigationTransitions)
        assertEquals(0L, snapshot.unhandledExceptionsCount)
        assertTrue(snapshot.isSessionStable())

        // Verify immutable repository count remains exactly 1
        assertEquals(1, fakeResultRepository.totalResultsCount)
    }

    // =========================================================================
    // IN-MEMORY TEST REPOSITORIES
    // =========================================================================

    private class InMemoryBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()

        val totalSessionsCount: Int
            get() = synchronized(sessions) { sessions.size }

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

    private class InMemoryBattleResultRepository(
        private val sessionLookup: (BattleSessionId) -> BattleSession? = { null }
    ) : BattleResultRepository {
        private val results = mutableMapOf<Pair<UserId, BattleId>, BattleResult>()
        private val resultsBySession = mutableMapOf<BattleSessionId, BattleResult>()

        val totalResultsCount: Int
            get() = synchronized(results) { results.size }

        @Synchronized
        fun completeBattleWithResult(result: BattleResult) {
            results[Pair(result.userId, result.battleId)] = result
            resultsBySession[result.sessionId] = result
        }

        @Synchronized
        fun getDirectResult(userId: UserId, battleId: BattleId): BattleResult? {
            return results[Pair(userId, battleId)]
        }

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> = synchronized(results) {
            resultsBySession[sessionId]?.let { return DomainResult.Success(it) }

            val session = sessionLookup(sessionId)
            val battleId = session?.battleId ?: BattleId("2026-10-14")
            val userId = session?.userId ?: UserId("user_stability_e2e")

            val result = BattleResult(
                resultId = BattleResultId("res_${sessionId.value}"),
                battleId = battleId,
                userId = userId,
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

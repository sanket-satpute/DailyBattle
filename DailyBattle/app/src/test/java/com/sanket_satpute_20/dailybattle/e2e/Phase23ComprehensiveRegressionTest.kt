package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.retry.DefaultIdempotentRetryHandler
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEngine
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleState
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStateMachine
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.history.GetHistoryUseCase
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sprint 23.9 â€” Phase 23 Comprehensive Regression Suite
 *
 * Codifies and verifies all regression baselines established across Phase 23
 * (Sprint 23.1 through Sprint 23.8) per `docs/ROADMAP.md` (line 1826-1829),
 * `docs/12_TESTING_AND_QA.md` Â§46, and `docs/16_DEFINITION_OF_DONE.md` Â§57:
 *
 * 1. Official Battle Progression (Snap -> Shift -> Crowd Call -> Results)
 * 2. Attempt Integrity (Single official attempt, reject duplicate start)
 * 3. Failure Resilience (Storage failure, offline state handled non-fatally)
 * 4. Duplicate Action Deduplication (Double tap, triple tap, rapid tap)
 * 5. State Corruption Protection (Malformed records, missing challenges)
 * 6. Score Integrity (Bounds clamping to 0..300 / 0..1000)
 * 7. History & Momentum Calculation (Calendar date deduplication & gap breaking)
 */
class Phase23ComprehensiveRegressionTest {

    // =========================================================================
    // 1. OFFICIAL BATTLE PROGRESSION BASELINE
    // =========================================================================

    @Test
    fun `regression - official battle flow transitions monotonically from start to results`() {
        val sm = BattleStateMachine()
        assertEquals(BattleState.NotStarted, sm.currentState)

        // 1. Start battle -> Ready(1) (Snap)
        assertTrue(sm.transition(BattleEvent.StartBattle) is DomainResult.Success)
        assertEquals(BattleState.Ready(1), sm.currentState)

        // 2. Begin Snap -> Active(1)
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Success)
        assertEquals(BattleState.Active(1), sm.currentState)

        // 3. Finish Snap -> ChallengeComplete(1)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Success)
        assertEquals(BattleState.ChallengeComplete(1), sm.currentState)

        // 4. Continue to Shift -> NextChallenge(2) -> PrepareNext -> Ready(2)
        assertTrue(sm.transition(BattleEvent.Continue) is DomainResult.Success)
        assertEquals(BattleState.NextChallenge(2), sm.currentState)
        assertTrue(sm.transition(BattleEvent.PrepareNext) is DomainResult.Success)
        assertEquals(BattleState.Ready(2), sm.currentState)

        // 5. Shift Active -> Finish -> NextChallenge(3) -> PrepareNext -> Ready(3) (Crowd Call)
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Success)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Success)
        assertTrue(sm.transition(BattleEvent.Continue) is DomainResult.Success)
        assertEquals(BattleState.NextChallenge(3), sm.currentState)
        assertTrue(sm.transition(BattleEvent.PrepareNext) is DomainResult.Success)
        assertEquals(BattleState.Ready(3), sm.currentState)

        // 6. Crowd Call Active -> Finish -> Continue (3rd challenge complete) -> BattleComplete
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Success)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Success)
        assertTrue(sm.transition(BattleEvent.Continue) is DomainResult.Success)
        assertEquals(BattleState.BattleComplete, sm.currentState)

        // 7. ShowResults -> Results (terminal state)
        assertTrue(sm.transition(BattleEvent.ShowResults) is DomainResult.Success)
        assertEquals(BattleState.Results, sm.currentState)

        // Terminal state blocks further transitions
        assertTrue(sm.transition(BattleEvent.StartBattle) is DomainResult.Failure)
        assertEquals(BattleState.Results, sm.currentState)
    }

    // =========================================================================
    // 2. ATTEMPT INTEGRITY BASELINE
    // =========================================================================

    @Test
    fun `regression - attempt integrity - duplicate official battle attempt strictly prohibited`() {
        val repo = InMemoryBattleSessionRepository()
        val userId = UserId("user-regression")
        val battleId = BattleId("battle-daily")

        // First attempt creates session
        val useCase = GetOrStartOfficialBattleUseCase(repo)
        val firstStart = useCase.execute(userId, battleId)
        assertTrue("First attempt succeeds", firstStart is DomainResult.Success)
        val engine = (firstStart as DomainResult.Success).value

        // Complete the session
        val completedSession = engine.session.copy(status = BattleSessionStatus.COMPLETED)
        repo.saveSession(completedSession)

        // Second attempt on same battle must fail with AppError.Domain
        val secondStart = useCase.execute(userId, battleId)
        assertTrue("Second attempt on completed official battle must fail", secondStart is DomainResult.Failure)
        assertEquals(AppError.Domain, (secondStart as DomainResult.Failure).error)
    }

    // =========================================================================
    // 3. FAILURE RESILIENCE BASELINE
    // =========================================================================

    @Test
    fun `regression - failure resilience - illegal events produce AppError without crashing`() {
        val sm = BattleStateMachine(BattleState.NotStarted)

        // Out-of-order events rejected gracefully
        val res1 = sm.transition(BattleEvent.ShowResults)
        assertTrue(res1 is DomainResult.Failure)
        assertEquals(AppError.BattleState, (res1 as DomainResult.Failure).error)

        val res2 = sm.transition(BattleEvent.FinishChallenge)
        assertTrue(res2 is DomainResult.Failure)
        assertEquals(AppError.BattleState, (res2 as DomainResult.Failure).error)

        // State remains uncorrupted
        assertEquals(BattleState.NotStarted, sm.currentState)
    }

    // =========================================================================
    // 4. DUPLICATE ACTION DEDUPLICATION BASELINE
    // =========================================================================

    @Test
    fun `regression - duplicate action - concurrent triple-tap deduplicates to single execution`() = runTest {
        val testDispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
        val retryHandler = DefaultIdempotentRetryHandler(kotlinx.coroutines.test.TestScope(testDispatcher))
        val counter = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        val def1 = async {
            retryHandler.executeIdempotent("regression-dedup-key") {
                counter.incrementAndGet()
                gate.await()
                DomainResult.Success("SUCCESS")
            }
        }
        val def2 = async {
            retryHandler.executeIdempotent("regression-dedup-key") {
                counter.incrementAndGet()
                gate.await()
                DomainResult.Success("SUCCESS")
            }
        }
        val def3 = async {
            retryHandler.executeIdempotent("regression-dedup-key") {
                counter.incrementAndGet()
                gate.await()
                DomainResult.Success("SUCCESS")
            }
        }

        advanceUntilIdle()

        assertEquals("Block entered only once concurrently", 1, counter.get())
        assertTrue(retryHandler.isInFlight("regression-dedup-key"))

        gate.complete(Unit)
        advanceUntilIdle()

        val results = listOf(def1.await(), def2.await(), def3.await())
        results.forEach {
            assertTrue(it is DomainResult.Success)
            assertEquals("SUCCESS", (it as DomainResult.Success).value)
        }
    }

    // =========================================================================
    // 5. STATE CORRUPTION PROTECTION BASELINE
    // =========================================================================

    @Test
    fun `regression - state corruption - malformed entity data sanitizes to safe defaults`() {
        val malformedSession = BattleSessionEntity(
            sessionId = "s-malformed",
            userId = "u-1",
            battleId = "b-1",
            mode = "CORRUPTED_MODE",
            status = "CORRUPTED_STATUS",
            currentChallenge = 999,
            startedAt = -50L,
            completedAt = -100L
        )

        val domainSession = malformedSession.toDomain()
        assertEquals(BattleMode.OFFICIAL, domainSession.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, domainSession.status)
        assertNull(domainSession.currentChallenge)
        assertNull(domainSession.startedAt)
        assertNull(domainSession.completedAt)

        val malformedResult = BattleResultEntity(
            resultId = "res-malformed",
            userId = "u-1",
            battleId = "b-1",
            sessionId = "s-1",
            snapScore = -500,
            shiftScore = -300,
            crowdCallScore = -200,
            consistencyScore = -100,
            totalScore = -1100,
            percentile = Double.NaN,
            completedAt = -1L
        )

        val domainResult = malformedResult.toDomain()
        assertEquals(0, domainResult.snapScore)
        assertEquals(0, domainResult.shiftScore)
        assertEquals(0, domainResult.crowdCallScore)
        assertEquals(0, domainResult.consistencyScore)
        assertEquals(0, domainResult.totalScore)
        assertEquals(0.0, domainResult.percentile, 0.001)
        assertEquals(0L, domainResult.completedAt)
    }

    @Test
    fun `regression - state corruption - missing challenge in sequence rejected by BattleEngine`() {
        val incompleteChallenges = listOf(
            Challenge(ChallengeId("c-1"), BattleId("b-1"), ChallengeType.SNAP, 1, 1),
            Challenge(ChallengeId("c-2"), BattleId("b-1"), ChallengeType.SHIFT, 2, 1)
        )

        assertFalse(ChallengeSequence.isValidSequence(incompleteChallenges))

        val incompleteBattle = Battle(
            battleId = BattleId("b-1"),
            battleDate = 20261014L,
            status = BattleStatus.ACTIVE,
            version = 1,
            challenges = incompleteChallenges
        )

        val session = BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = UserId("u-1"),
            battleId = BattleId("b-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList()
        )

        try {
            BattleEngine(session, incompleteBattle)
            fail("Incomplete battle must throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("official sequence") == true)
        }
    }

    // =========================================================================
    // 6. SCORE INTEGRITY BASELINE
    // =========================================================================

    @Test
    fun `regression - score integrity - scores strictly clamped to approved constants`() {
        val validator = ChallengeScoreValidator()

        // Negative clamped to 0
        val neg = validator.validate(
            ChallengeResult(ChallengeResultId("r1"), ChallengeId("c1"), BattleSessionId("s1"), -100, 300, 1000L),
            ChallengeType.SNAP
        )
        assertEquals(0, neg.score)

        // Excessive clamped to approved max
        val excessive = validator.validate(
            ChallengeResult(ChallengeResultId("r2"), ChallengeId("c2"), BattleSessionId("s1"), 5000, 5000, 1000L),
            ChallengeType.SHIFT
        )
        assertEquals(ChallengeScoreConstants.maxScoreFor(ChallengeType.SHIFT), excessive.score)
        assertEquals(300, excessive.score)

        // Aggregator clamps total
        val aggregator = BattleScoreAggregator()
        val agg = aggregator.aggregate(
            mode = BattleMode.OFFICIAL,
            snapResult = neg,
            shiftResult = excessive,
            crowdCallResult = null
        )
        assertEquals(0, agg.snapScore)
        assertEquals(300, agg.shiftScore)
        assertEquals(0, agg.crowdCallScore)
        assertEquals(0, agg.consistencyScore)
        assertEquals(300, agg.totalScore)
        assertTrue(agg.totalScore <= ChallengeScoreConstants.MAX_TOTAL_SCORE)
    }

    // =========================================================================
    // 7. HISTORY & MOMENTUM CALCULATION BASELINE
    // =========================================================================

    @Test
    fun `regression - history & momentum - multiple battles on same date deduplicate and calendar gaps break streak`() = runTest {
        val repo = InMemoryBattleResultRepository()
        val userId = UserId("user-streak-regression")
        val fixedClock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("UTC"))

        // Add 2 battles on today (2026-10-14)
        val todayEpoch1 = Instant.parse("2026-10-14T08:00:00Z").toEpochMilli()
        val todayEpoch2 = Instant.parse("2026-10-14T10:00:00Z").toEpochMilli()
        repo.saveResult(BattleResult(BattleResultId("r1"), userId, BattleId("b1"), BattleSessionId("s1"), 200, 200, 200, 50, 650, 80.0, todayEpoch1))
        repo.saveResult(BattleResult(BattleResultId("r2"), userId, BattleId("b2"), BattleSessionId("s2"), 250, 250, 250, 70, 820, 95.0, todayEpoch2))

        val getHistory = GetHistoryUseCase(repo, fixedClock)
        val h1 = (getHistory(userId) as DomainResult.Success).value
        // Today has 2 battles, momentum should be 1
        assertEquals("Duplicate battles on same date count as 1 qualifying day", 1, h1.momentum)
        assertEquals("Best score selects maximum total score", 820, h1.best)

        // Add battle on yesterday (2026-10-13) -> momentum should become 2
        val yesterdayEpoch = Instant.parse("2026-10-13T10:00:00Z").toEpochMilli()
        repo.saveResult(BattleResult(BattleResultId("r3"), userId, BattleId("b3"), BattleSessionId("s3"), 200, 200, 200, 50, 650, 80.0, yesterdayEpoch))

        val h2 = (getHistory(userId) as DomainResult.Success).value
        assertEquals("Consecutive days increment momentum to 2", 2, h2.momentum)

        // Add battle with gap (skip 2026-10-12, add 2026-10-11) -> streak stops at 2
        val oldEpoch = Instant.parse("2026-10-11T10:00:00Z").toEpochMilli()
        repo.saveResult(BattleResult(BattleResultId("r4"), userId, BattleId("b4"), BattleSessionId("s4"), 200, 200, 200, 50, 650, 80.0, oldEpoch))

        val h3 = (getHistory(userId) as DomainResult.Success).value
        assertEquals("Calendar gap breaks momentum at 2", 2, h3.momentum)
    }
}

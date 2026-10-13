package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 23.1 — Unit Tests (Scoring)
 * Exhaustive boundary and correctness testing for the scoring subsystem per `docs/12_TESTING_AND_QA.md` §12.1:
 * 1. Boundary values (min 0, max 300, max total 1000, consistency 0-100)
 * 2. Determinism (100 repetitions of identical inputs yield identical outputs)
 * 3. Invalid & negative inputs (clamped to 0)
 * 4. Overflow inputs (clamped to 300)
 * 5. Additive invariant: Snap + Shift + Crowd + Consistency == Total
 * 6. Practice mode isolation (consistency evaluates to null / 0)
 */
class ScoringComprehensiveUnitTest {

    private val aggregator = BattleScoreAggregator()
    private val validator = ChallengeScoreValidator()
    private val consistencyEvaluator = ConsistencyEvaluator()
    private val defaultSessionId = BattleSessionId("session_scoring_qa")

    private fun mockResult(score: Int, type: ChallengeType = ChallengeType.SNAP): ChallengeResult {
        return ChallengeResult(
            resultId = ChallengeResultId("res_${type.name}_$score"),
            challengeId = ChallengeId("ch_${type.name}"),
            sessionId = defaultSessionId,
            score = score,
            maxScore = 300,
            completedAt = 1000L
        )
    }

    @Test
    fun `boundary test - minimum valid inputs produce minimum score with perfect balance`() {
        // (0, 0, 0) yields mean 0, variance 0 -> perfect balance 100 consistency
        val aggregated = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(0, ChallengeType.SNAP),
            mockResult(0, ChallengeType.SHIFT),
            mockResult(0, ChallengeType.CROWD_CALL)
        )
        assertEquals(0, aggregated.snapScore)
        assertEquals(0, aggregated.shiftScore)
        assertEquals(0, aggregated.crowdCallScore)
        assertEquals(100, aggregated.consistencyScore)
        assertEquals(100, aggregated.totalScore)
        assertEquals(
            aggregated.snapScore + aggregated.shiftScore + aggregated.crowdCallScore + aggregated.consistencyScore,
            aggregated.totalScore
        )
    }

    @Test
    fun `boundary test - maximum valid inputs produce 1000 total score`() {
        // (300, 300, 300) yields 300 + 300 + 300 + 100 = 1000
        val aggregated = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(300, ChallengeType.SNAP),
            mockResult(300, ChallengeType.SHIFT),
            mockResult(300, ChallengeType.CROWD_CALL)
        )
        assertEquals(300, aggregated.snapScore)
        assertEquals(300, aggregated.shiftScore)
        assertEquals(300, aggregated.crowdCallScore)
        assertEquals(100, aggregated.consistencyScore)
        assertEquals(1000, aggregated.totalScore)
        assertEquals(
            aggregated.snapScore + aggregated.shiftScore + aggregated.crowdCallScore + aggregated.consistencyScore,
            aggregated.totalScore
        )
    }

    @Test
    fun `boundary test - maximum imbalance yields zero consistency`() {
        // Maximum imbalance: 300, 0, 0 yields consistency 0
        val consistency = consistencyEvaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(300),
            mockResult(0),
            mockResult(0)
        )
        assertEquals(0, consistency)

        val aggregated = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(300, ChallengeType.SNAP),
            mockResult(0, ChallengeType.SHIFT),
            mockResult(0, ChallengeType.CROWD_CALL)
        )
        assertEquals(300, aggregated.totalScore)
        assertEquals(0, aggregated.consistencyScore)
    }

    @Test
    fun `determinism test - identical scoring inputs produce exact same outputs across 100 iterations`() {
        val snap = mockResult(240, ChallengeType.SNAP)
        val shift = mockResult(180, ChallengeType.SHIFT)
        val crowd = mockResult(210, ChallengeType.CROWD_CALL)

        val initial = aggregator.aggregate(BattleMode.OFFICIAL, snap, shift, crowd)

        repeat(100) {
            val repeated = aggregator.aggregate(BattleMode.OFFICIAL, snap, shift, crowd)
            assertEquals("Determinism violation on snapScore", initial.snapScore, repeated.snapScore)
            assertEquals("Determinism violation on shiftScore", initial.shiftScore, repeated.shiftScore)
            assertEquals("Determinism violation on crowdCallScore", initial.crowdCallScore, repeated.crowdCallScore)
            assertEquals("Determinism violation on consistencyScore", initial.consistencyScore, repeated.consistencyScore)
            assertEquals("Determinism violation on totalScore", initial.totalScore, repeated.totalScore)
        }
    }

    @Test
    fun `negative scores are safely clamped to zero without corruption`() {
        val clampedSnap = validator.validate(mockResult(-100, ChallengeType.SNAP), ChallengeType.SNAP)
        val clampedShift = validator.validate(mockResult(-5, ChallengeType.SHIFT), ChallengeType.SHIFT)

        assertEquals(0, clampedSnap.score)
        assertEquals(0, clampedShift.score)

        val aggregated = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(-50, ChallengeType.SNAP),
            mockResult(-10, ChallengeType.SHIFT),
            mockResult(-999, ChallengeType.CROWD_CALL)
        )
        assertEquals(0, aggregated.snapScore)
        assertEquals(0, aggregated.shiftScore)
        assertEquals(0, aggregated.crowdCallScore)
        assertTrue(aggregated.totalScore in 0..1000)
    }

    @Test
    fun `score overflow exceeding 300 is clamped to 300`() {
        val overflowSnap = validator.validate(mockResult(450, ChallengeType.SNAP), ChallengeType.SNAP)
        val overflowShift = validator.validate(mockResult(1000, ChallengeType.SHIFT), ChallengeType.SHIFT)

        assertEquals(300, overflowSnap.score)
        assertEquals(300, overflowShift.score)

        val aggregated = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(500, ChallengeType.SNAP),
            mockResult(600, ChallengeType.SHIFT),
            mockResult(700, ChallengeType.CROWD_CALL)
        )
        assertEquals(300, aggregated.snapScore)
        assertEquals(300, aggregated.shiftScore)
        assertEquals(300, aggregated.crowdCallScore)
        assertEquals(1000, aggregated.totalScore)
    }

    @Test
    fun `practice mode suppresses consistency calculation`() {
        val consistency = consistencyEvaluator.evaluate(
            BattleMode.PRACTICE,
            mockResult(300),
            mockResult(300),
            mockResult(300)
        )
        assertNull("Practice mode must return null consistency", consistency)

        val aggregated = aggregator.aggregate(
            BattleMode.PRACTICE,
            mockResult(250),
            mockResult(250),
            mockResult(250)
        )
        assertEquals(0, aggregated.consistencyScore)
        assertEquals(750, aggregated.totalScore)
    }

    @Test
    fun `missing individual challenge scores default to zero and suppress consistency`() {
        val aggregated = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(200, ChallengeType.SNAP),
            null, // missing Shift
            mockResult(200, ChallengeType.CROWD_CALL)
        )
        assertEquals(200, aggregated.snapScore)
        assertEquals(0, aggregated.shiftScore)
        assertEquals(200, aggregated.crowdCallScore)
        assertEquals(0, aggregated.consistencyScore)
        assertEquals(400, aggregated.totalScore)
    }
}

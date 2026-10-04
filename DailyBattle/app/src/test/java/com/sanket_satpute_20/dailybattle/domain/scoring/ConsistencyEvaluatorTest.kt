package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConsistencyEvaluatorTest {

    private val evaluator = ConsistencyEvaluator()
    private val defaultSessionId = BattleSessionId("session_test")

    private fun mockResult(score: Int): ChallengeResult {
        return ChallengeResult(
            resultId = ChallengeResultId("res_${score}"),
            challengeId = ChallengeId("ch_test"),
            sessionId = defaultSessionId,
            score = score,
            maxScore = 300,
            completedAt = 1000L
        )
    }

    @Test
    fun `perfect scores return 100`() {
        val score = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(300),
            mockResult(300),
            mockResult(300)
        )
        assertEquals(100, score)
    }

    @Test
    fun `equal non-perfect scores return 100`() {
        val score = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(150),
            mockResult(150),
            mockResult(150)
        )
        assertEquals(100, score) // Balance is perfect
    }

    @Test
    fun `maximum imbalance returns 0`() {
        // e.g. 300, 0, 0 gives the max standard deviation
        val score1 = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(300),
            mockResult(0),
            mockResult(0)
        )
        assertEquals(0, score1)
        
        val score2 = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(0),
            mockResult(300),
            mockResult(0)
        )
        assertEquals(0, score2)
    }

    @Test
    fun `uneven intermediate scores calculate correctly`() {
        // Example: 300, 150, 0
        // s=1, h=0.5, c=0
        // mean = 0.5
        // var = ((1-0.5)^2 + (0.5-0.5)^2 + (0-0.5)^2)/3 = (0.25 + 0 + 0.25)/3 = 0.5/3 = 1/6
        // stdDev = sqrt(1/6) = 0.408248...
        // maxStdDev = sqrt(2)/3 = 0.471404...
        // Balance = 1 - (0.408248 / 0.471404) = 1 - 0.866025 = 0.133975
        // Raw = 13.3975 -> round to 13
        val score = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(300),
            mockResult(150),
            mockResult(0)
        )
        assertEquals(13, score)
    }

    @Test
    fun `boundary values calculate correctly`() {
        val scoreAllZero = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(0),
            mockResult(0),
            mockResult(0)
        )
        assertEquals(100, scoreAllZero) // Balanced at 0
    }

    @Test
    fun `integer rounding works as expected`() {
        // Let's create a scenario that yields a .5 or similar to test rounding
        // Scores: 200, 190, 180
        // mean of 200, 190, 180 is 190
        // s=0.666, h=0.633, c=0.6
        // mean = 0.6333
        // var = ((0.0333)^2 + 0 + (-0.0333)^2)/3 = (0.00111 + 0.00111)/3 = 0.00074
        // stdDev = sqrt(0.00074) = 0.0272
        // maxStdDev = 0.4714
        // balance = 1 - (0.0272 / 0.4714) = 0.9423
        // raw = 94.23 -> 94
        val score = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(200),
            mockResult(190),
            mockResult(180)
        )
        assertEquals(94, score)
    }
    
    @Test
    fun `practice mode returns null`() {
        val score = evaluator.evaluate(
            BattleMode.PRACTICE,
            mockResult(300),
            mockResult(300),
            mockResult(300)
        )
        assertNull(score)
    }

    @Test
    fun `missing challenge inputs return null`() {
        assertNull(
            evaluator.evaluate(
                BattleMode.OFFICIAL,
                null,
                mockResult(300),
                mockResult(300)
            )
        )
        assertNull(
            evaluator.evaluate(
                BattleMode.OFFICIAL,
                mockResult(300),
                null,
                mockResult(300)
            )
        )
        assertNull(
            evaluator.evaluate(
                BattleMode.OFFICIAL,
                mockResult(300),
                mockResult(300),
                null
            )
        )
    }

    @Test
    fun `deterministic repeated calculation`() {
        val run1 = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(100),
            mockResult(250),
            mockResult(50)
        )
        
        val run2 = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(100),
            mockResult(250),
            mockResult(50)
        )
        
        assertEquals(run1, run2)
    }
    
    @Test
    fun `clamping prevents negative score`() {
        // The formula logically bounds to 0-100, but float precision might theoretically dip below 0
        // on maximum standard deviation (e.g. 300, 0, 0).
        val score = evaluator.evaluate(
            BattleMode.OFFICIAL,
            mockResult(300),
            mockResult(0),
            mockResult(0)
        )
        // With exact floating point math, 1.0 - (max / max) = 0.0
        // If it drifted slightly negative it should be clamped to 0.
        assertEquals(0, score)
    }
}

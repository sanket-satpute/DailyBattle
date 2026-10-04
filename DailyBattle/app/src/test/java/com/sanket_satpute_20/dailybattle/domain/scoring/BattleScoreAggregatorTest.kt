package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import org.junit.Assert.assertEquals
import org.junit.Test

class BattleScoreAggregatorTest {

    private val aggregator = BattleScoreAggregator()
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
    fun `maximum scores result in 1000 total`() {
        val result = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(300),
            mockResult(300),
            mockResult(300)
        )
        assertEquals(300, result.snapScore)
        assertEquals(300, result.shiftScore)
        assertEquals(300, result.crowdCallScore)
        assertEquals(100, result.consistencyScore)
        assertEquals(1000, result.totalScore)
    }

    @Test
    fun `zero scores result in 100 consistency and 100 total`() {
        // Balanced at zero yields 100 consistency
        val result = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(0),
            mockResult(0),
            mockResult(0)
        )
        assertEquals(0, result.snapScore)
        assertEquals(0, result.shiftScore)
        assertEquals(0, result.crowdCallScore)
        assertEquals(100, result.consistencyScore)
        assertEquals(100, result.totalScore)
    }

    @Test
    fun `missing challenge score results in 0 for that challenge and 0 consistency`() {
        val result = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(300),
            null, // missing shift
            mockResult(300)
        )
        assertEquals(300, result.snapScore)
        assertEquals(0, result.shiftScore) // defaulted to 0
        assertEquals(300, result.crowdCallScore)
        assertEquals(0, result.consistencyScore) // Consistency evaluator returns null -> 0
        assertEquals(600, result.totalScore)
    }

    @Test
    fun `practice mode zeroes consistency`() {
        val result = aggregator.aggregate(
            BattleMode.PRACTICE,
            mockResult(300),
            mockResult(300),
            mockResult(300)
        )
        assertEquals(300, result.snapScore)
        assertEquals(300, result.shiftScore)
        assertEquals(300, result.crowdCallScore)
        assertEquals(0, result.consistencyScore) // Because it's practice
        assertEquals(900, result.totalScore)
    }

    @Test
    fun `negative scores are clamped to 0`() {
        val result = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(-50),
            mockResult(-100),
            mockResult(-10)
        )
        assertEquals(0, result.snapScore)
        assertEquals(0, result.shiftScore)
        assertEquals(0, result.crowdCallScore)
        assertEquals(100, result.consistencyScore) // balanced at 0
        assertEquals(100, result.totalScore)
    }

    @Test
    fun `overflow scores are clamped to max`() {
        val result = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(999),
            mockResult(400),
            mockResult(301)
        )
        assertEquals(300, result.snapScore)
        assertEquals(300, result.shiftScore)
        assertEquals(300, result.crowdCallScore)
        assertEquals(100, result.consistencyScore)
        assertEquals(1000, result.totalScore)
    }

    @Test
    fun `intermediate scores aggregate correctly`() {
        val result = aggregator.aggregate(
            BattleMode.OFFICIAL,
            mockResult(200),
            mockResult(190),
            mockResult(180)
        )
        assertEquals(200, result.snapScore)
        assertEquals(190, result.shiftScore)
        assertEquals(180, result.crowdCallScore)
        assertEquals(94, result.consistencyScore) // Derived from evaluator test
        assertEquals(200 + 190 + 180 + 94, result.totalScore)
    }
}

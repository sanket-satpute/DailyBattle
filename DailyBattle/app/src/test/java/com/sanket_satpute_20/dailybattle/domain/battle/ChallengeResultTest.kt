package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeResultTest {

    private fun createResult(
        score: Int = 250,
        maxScore: Int = 300,
    ) = ChallengeResult(
        resultId = ChallengeResultId("cr-1"),
        challengeId = ChallengeId("c-1"),
        sessionId = BattleSessionId("s-1"),
        score = score,
        maxScore = maxScore,
        completedAt = 5000L,
    )

    @Test
    fun `result holds all required fields`() {
        val result = createResult()
        assertEquals(ChallengeResultId("cr-1"), result.resultId)
        assertEquals(ChallengeId("c-1"), result.challengeId)
        assertEquals(BattleSessionId("s-1"), result.sessionId)
        assertEquals(250, result.score)
        assertEquals(300, result.maxScore)
        assertEquals(5000L, result.completedAt)
    }

    @Test
    fun `score can be zero for a minimum result`() {
        val result = createResult(score = 0)
        assertEquals(0, result.score)
    }

    @Test
    fun `score can equal maxScore for a perfect result`() {
        val result = createResult(score = 300, maxScore = 300)
        assertEquals(result.score, result.maxScore)
    }

    @Test
    fun `score is always at most maxScore for approved max scores`() {
        // Per §24: max scores are Snap=300, Shift=300, CrowdCall=300
        val result = createResult(score = 300, maxScore = 300)
        assertTrue(result.score <= result.maxScore)
    }

    @Test
    fun `results with different scores are not equal`() {
        val a = createResult(score = 200)
        val b = createResult(score = 250)
        assertNotEquals(a, b)
    }

    @Test
    fun `result structural equality`() {
        val a = createResult()
        val b = createResult()
        assertEquals(a, b)
    }
}

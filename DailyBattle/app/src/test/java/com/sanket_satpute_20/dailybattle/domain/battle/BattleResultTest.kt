package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleResultTest {

    private fun createResult(
        snapScore: Int = 250,
        shiftScore: Int = 220,
        crowdCallScore: Int = 180,
        consistencyScore: Int = 75,
        totalScore: Int = 725,
        percentile: Double = 82.5,
    ) = BattleResult(
        resultId = BattleResultId("r-1"),
        userId = UserId("u-1"),
        battleId = BattleId("b-1"),
        sessionId = BattleSessionId("s-1"),
        snapScore = snapScore,
        shiftScore = shiftScore,
        crowdCallScore = crowdCallScore,
        consistencyScore = consistencyScore,
        totalScore = totalScore,
        percentile = percentile,
        completedAt = 10000L,
    )

    @Test
    fun `result holds all required fields`() {
        val result = createResult()
        assertEquals(BattleResultId("r-1"), result.resultId)
        assertEquals(UserId("u-1"), result.userId)
        assertEquals(BattleId("b-1"), result.battleId)
        assertEquals(BattleSessionId("s-1"), result.sessionId)
        assertEquals(250, result.snapScore)
        assertEquals(220, result.shiftScore)
        assertEquals(180, result.crowdCallScore)
        assertEquals(75, result.consistencyScore)
        assertEquals(725, result.totalScore)
        assertEquals(82.5, result.percentile, 0.001)
        assertEquals(10000L, result.completedAt)
    }

    @Test
    fun `max score structure is correct per section 26`() {
        // Per §26: Snap/300 + Shift/300 + CrowdCall/300 + Consistency/100 = 1000
        val perfect = createResult(
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 100.0,
        )
        assertTrue(perfect.snapScore <= 300)
        assertTrue(perfect.shiftScore <= 300)
        assertTrue(perfect.crowdCallScore <= 300)
        assertTrue(perfect.consistencyScore <= 100)
        assertEquals(1000, perfect.totalScore)
    }

    @Test
    fun `zero score represents minimum result`() {
        val zero = createResult(
            snapScore = 0,
            shiftScore = 0,
            crowdCallScore = 0,
            consistencyScore = 0,
            totalScore = 0,
            percentile = 0.0,
        )
        assertEquals(0, zero.totalScore)
        assertEquals(0.0, zero.percentile, 0.001)
    }

    @Test
    fun `percentile is stored as received - not recomputed`() {
        // The client must not recalculate percentile; it is the backend's authority.
        val result = createResult(percentile = 67.3)
        assertEquals(67.3, result.percentile, 0.001)
    }

    @Test
    fun `results with different total scores are not equal`() {
        val a = createResult(totalScore = 500)
        val b = createResult(totalScore = 750)
        assertNotEquals(a, b)
    }

    @Test
    fun `result structural equality`() {
        val a = createResult()
        val b = createResult()
        assertEquals(a, b)
    }
}

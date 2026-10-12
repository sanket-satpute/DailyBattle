package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleHistoryItemEntityMapperTest {

    @Test
    fun `toHistoryItemEntity converts BattleResult accurately`() {
        val battleResult = BattleResult(
            resultId = BattleResultId("res_1"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_1"),
            sessionId = BattleSessionId("sess_1"),
            snapScore = 290,
            shiftScore = 270,
            crowdCallScore = 280,
            consistencyScore = 95,
            totalScore = 935,
            percentile = 5.5,
            completedAt = 1700000000000L,
        )

        val entity = battleResult.toHistoryItemEntity(personalBest = true)

        assertEquals("battle_1", entity.battleId)
        assertEquals("user_1", entity.userId)
        assertEquals(1700000000000L, entity.date)
        assertEquals(935, entity.score)
        assertEquals(5.5, entity.percentile, 0.001)
        assertTrue(entity.personalBest)
        assertEquals("COMPLETED", entity.status)
    }

    @Test
    fun `toBattleResult converts BattleHistoryItemEntity back accurately`() {
        val entity = BattleHistoryItemEntity(
            battleId = "battle_2",
            userId = "user_2",
            date = 1700000100000L,
            score = 880,
            percentile = 12.0,
            personalBest = false,
            status = "COMPLETED",
        )

        val result = entity.toBattleResult(sessionId = "sess_2")

        assertEquals(BattleResultId("res_battle_2"), result.resultId)
        assertEquals(UserId("user_2"), result.userId)
        assertEquals(BattleId("battle_2"), result.battleId)
        assertEquals(BattleSessionId("sess_2"), result.sessionId)
        assertEquals(880, result.totalScore)
        assertEquals(12.0, result.percentile, 0.001)
        assertEquals(1700000100000L, result.completedAt)
    }
}

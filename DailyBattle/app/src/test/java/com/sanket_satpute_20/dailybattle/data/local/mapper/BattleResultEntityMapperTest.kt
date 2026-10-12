package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Test

class BattleResultEntityMapperTest {

    @Test
    fun `toDomain maps all BattleResultEntity fields accurately`() {
        val entity = BattleResultEntity(
            resultId = "res_100",
            userId = "user_1",
            battleId = "battle_1",
            sessionId = "sess_1",
            snapScore = 287,
            shiftScore = 252,
            crowdCallScore = 271,
            consistencyScore = 91,
            totalScore = 901,
            percentile = 9.0,
            completedAt = 1700000180000L,
        )

        val domain = entity.toDomain()

        assertEquals(BattleResultId("res_100"), domain.resultId)
        assertEquals(UserId("user_1"), domain.userId)
        assertEquals(BattleId("battle_1"), domain.battleId)
        assertEquals(BattleSessionId("sess_1"), domain.sessionId)
        assertEquals(287, domain.snapScore)
        assertEquals(252, domain.shiftScore)
        assertEquals(271, domain.crowdCallScore)
        assertEquals(91, domain.consistencyScore)
        assertEquals(901, domain.totalScore)
        assertEquals(9.0, domain.percentile, 0.001)
        assertEquals(1700000180000L, domain.completedAt)
    }

    @Test
    fun `toEntity maps all BattleResult domain fields accurately`() {
        val domain = BattleResult(
            resultId = BattleResultId("res_200"),
            userId = UserId("user_2"),
            battleId = BattleId("battle_2"),
            sessionId = BattleSessionId("sess_2"),
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 1.0,
            completedAt = 1700000200000L,
        )

        val entity = domain.toEntity()

        assertEquals("res_200", entity.resultId)
        assertEquals("user_2", entity.userId)
        assertEquals("battle_2", entity.battleId)
        assertEquals("sess_2", entity.sessionId)
        assertEquals(300, entity.snapScore)
        assertEquals(300, entity.shiftScore)
        assertEquals(300, entity.crowdCallScore)
        assertEquals(100, entity.consistencyScore)
        assertEquals(1000, entity.totalScore)
        assertEquals(1.0, entity.percentile, 0.001)
        assertEquals(1700000200000L, entity.completedAt)
    }

    @Test
    fun `roundtrip preserves zero boundary and max boundary`() {
        val minResult = BattleResult(
            resultId = BattleResultId("res_min"),
            userId = UserId("user_min"),
            battleId = BattleId("battle_min"),
            sessionId = BattleSessionId("sess_min"),
            snapScore = 0,
            shiftScore = 0,
            crowdCallScore = 0,
            consistencyScore = 0,
            totalScore = 0,
            percentile = 100.0,
            completedAt = 1700000000000L,
        )
        val maxResult = BattleResult(
            resultId = BattleResultId("res_max"),
            userId = UserId("user_max"),
            battleId = BattleId("battle_max"),
            sessionId = BattleSessionId("sess_max"),
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 0.0,
            completedAt = 1700000000000L,
        )

        assertEquals(minResult, minResult.toEntity().toDomain())
        assertEquals(maxResult, maxResult.toEntity().toDomain())
    }
}

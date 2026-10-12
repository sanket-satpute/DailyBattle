package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class RivalEntityMapperTest {

    @Test
    fun `toDomain maps all RivalEntity fields accurately`() {
        val entity = RivalEntity(
            userId = "user_me",
            rivalUserId = "user_rahul",
            rivalName = "Rahul",
            selectedAt = 1700000000000L,
            status = "ACTIVE",
        )

        val domain = entity.toDomain()

        assertEquals(UserId("user_me"), domain.userId)
        assertEquals(UserId("user_rahul"), domain.rivalUserId)
        assertEquals("Rahul", domain.rivalName)
        assertEquals(1700000000000L, domain.selectedAt)
        assertEquals(RivalStatus.ACTIVE, domain.status)
    }

    @Test
    fun `toEntity maps all Rival domain fields accurately`() {
        val domain = Rival(
            userId = UserId("user_me"),
            rivalUserId = UserId("user_vikram"),
            rivalName = "Vikram",
            selectedAt = 1700000100000L,
            status = RivalStatus.REMOVED,
        )

        val entity = domain.toEntity()

        assertEquals("user_me", entity.userId)
        assertEquals("user_vikram", entity.rivalUserId)
        assertEquals("Vikram", entity.rivalName)
        assertEquals(1700000100000L, entity.selectedAt)
        assertEquals("REMOVED", entity.status)
    }

    @Test
    fun `Rival roundtrip preserves data`() {
        val original = Rival(
            userId = UserId("user_x"),
            rivalUserId = UserId("user_y"),
            rivalName = "TopRival",
            selectedAt = 1700000200000L,
            status = RivalStatus.ACTIVE,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `Rival toDomain falls back to ACTIVE on unknown status`() {
        val entity = RivalEntity(
            userId = "user_x",
            rivalUserId = "user_y",
            rivalName = "RivalUnknown",
            selectedAt = 1700000000000L,
            status = "UNKNOWN_RIVAL_STATUS",
        )

        assertEquals(RivalStatus.ACTIVE, entity.toDomain().status)
    }

    @Test
    fun `RivalMatchHistoryEntity roundtrip preserves data`() {
        val item = RivalMatchHistoryItem(
            battleId = BattleId("battle_1"),
            date = 1700000000000L,
            userScore = 905,
            rivalScore = 890,
            outcome = MatchOutcome.WIN,
        )

        val entity = item.toEntity(userId = "user_me")

        assertEquals("battle_1", entity.battleId)
        assertEquals("user_me", entity.userId)
        assertEquals(1700000000000L, entity.date)
        assertEquals(905, entity.userScore)
        assertEquals(890, entity.rivalScore)
        assertEquals("WIN", entity.outcome)

        val domain = entity.toDomain()
        assertEquals(item, domain)
    }

    @Test
    fun `RivalMatchHistoryEntity falls back to TIE on unknown outcome`() {
        val entity = RivalMatchHistoryEntity(
            battleId = "b1",
            userId = "u1",
            date = 1700000000000L,
            userScore = 900,
            rivalScore = 900,
            outcome = "CORRUPTED_OUTCOME",
        )

        assertEquals(MatchOutcome.TIE, entity.toDomain().outcome)
    }
}

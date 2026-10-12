package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleEntity
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Test

class BattleEntityMapperTest {

    @Test
    fun `toDomain maps all BattleEntity fields accurately`() {
        val entity = BattleEntity(
            battleId = "battle_2026_10_12",
            battleDate = 1700000000000L,
            status = "AVAILABLE",
            version = 1,
        )

        val domain = entity.toDomain()

        assertEquals(BattleId("battle_2026_10_12"), domain.battleId)
        assertEquals(1700000000000L, domain.battleDate)
        assertEquals(BattleStatus.AVAILABLE, domain.status)
        assertEquals(1, domain.version)
        assertEquals(emptyList<Challenge>(), domain.challenges)
    }

    @Test
    fun `toDomain attaches supplied challenges accurately`() {
        val entity = BattleEntity(
            battleId = "battle_today",
            battleDate = 1700000000000L,
            status = "ACTIVE",
            version = 2,
        )
        val challenges = listOf(
            Challenge(ChallengeId("c1"), BattleId("battle_today"), ChallengeType.SNAP, 1, 1),
            Challenge(ChallengeId("c2"), BattleId("battle_today"), ChallengeType.SHIFT, 2, 1),
            Challenge(ChallengeId("c3"), BattleId("battle_today"), ChallengeType.CROWD_CALL, 3, 1),
        )

        val domain = entity.toDomain(challenges)

        assertEquals(3, domain.challenges.size)
        assertEquals(challenges, domain.challenges)
    }

    @Test
    fun `toEntity maps all Battle domain fields accurately`() {
        val domain = Battle(
            battleId = BattleId("battle_456"),
            battleDate = 1700000100000L,
            status = BattleStatus.COMPLETED,
            version = 3,
            challenges = emptyList(),
        )

        val entity = domain.toEntity()

        assertEquals("battle_456", entity.battleId)
        assertEquals(1700000100000L, entity.battleDate)
        assertEquals("COMPLETED", entity.status)
        assertEquals(3, entity.version)
    }

    @Test
    fun `roundtrip preserves Battle properties`() {
        val original = Battle(
            battleId = BattleId("battle_roundtrip"),
            battleDate = 1700000200000L,
            status = BattleStatus.NOT_AVAILABLE,
            version = 1,
            challenges = emptyList(),
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toDomain gracefully falls back to NOT_AVAILABLE when status string is unknown`() {
        val entity = BattleEntity(
            battleId = "battle_corrupt",
            battleDate = 1700000000000L,
            status = "CORRUPTED_STATUS",
            version = 1,
        )

        val domain = entity.toDomain()

        assertEquals(BattleStatus.NOT_AVAILABLE, domain.status)
    }
}

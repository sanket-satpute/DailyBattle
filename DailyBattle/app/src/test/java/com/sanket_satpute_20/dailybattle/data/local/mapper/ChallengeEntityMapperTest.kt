package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeEntity
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeEntityMapperTest {

    @Test
    fun `toDomain maps all ChallengeEntity fields accurately`() {
        val entity = ChallengeEntity(
            challengeId = "c_shift_1",
            battleId = "battle_100",
            type = "SHIFT",
            order = 2,
            version = 1,
        )

        val domain = entity.toDomain()

        assertEquals(ChallengeId("c_shift_1"), domain.challengeId)
        assertEquals(BattleId("battle_100"), domain.battleId)
        assertEquals(ChallengeType.SHIFT, domain.type)
        assertEquals(2, domain.order)
        assertEquals(1, domain.version)
    }

    @Test
    fun `toEntity maps all Challenge domain fields accurately`() {
        val domain = Challenge(
            challengeId = ChallengeId("c_crowd_1"),
            battleId = BattleId("battle_200"),
            type = ChallengeType.CROWD_CALL,
            order = 3,
            version = 2,
        )

        val entity = domain.toEntity()

        assertEquals("c_crowd_1", entity.challengeId)
        assertEquals("battle_200", entity.battleId)
        assertEquals("CROWD_CALL", entity.type)
        assertEquals(3, entity.order)
        assertEquals(2, entity.version)
    }

    @Test
    fun `roundtrip preserves Challenge data`() {
        val original = Challenge(
            challengeId = ChallengeId("c_snap_1"),
            battleId = BattleId("battle_300"),
            type = ChallengeType.SNAP,
            order = 1,
            version = 1,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toDomain gracefully falls back to SNAP on unknown challenge type`() {
        val entity = ChallengeEntity(
            challengeId = "c_unknown",
            battleId = "battle_300",
            type = "UNKNOWN_TYPE",
            order = 1,
            version = 1,
        )

        val domain = entity.toDomain()

        assertEquals(ChallengeType.SNAP, domain.type)
    }
}

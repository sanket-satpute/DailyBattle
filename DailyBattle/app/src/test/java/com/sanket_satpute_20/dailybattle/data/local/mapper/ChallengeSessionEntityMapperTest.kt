package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeSessionEntityMapperTest {

    @Test
    fun `toDomain maps active challenge session accurately`() {
        val entity = ChallengeSessionEntity(
            challengeId = "c_snap_1",
            sessionId = "sess_1",
            status = "ACTIVE",
            startedAt = 1700000000000L,
            completedAt = null,
        )

        val domain = entity.toDomain()

        assertEquals(ChallengeId("c_snap_1"), domain.challengeId)
        assertEquals(BattleSessionId("sess_1"), domain.sessionId)
        assertEquals(ChallengeSessionStatus.ACTIVE, domain.status)
        assertEquals(1700000000000L, domain.startedAt)
        assertNull(domain.completedAt)
    }

    @Test
    fun `toEntity maps completed challenge session accurately`() {
        val domain = ChallengeSession(
            challengeId = ChallengeId("c_shift_1"),
            sessionId = BattleSessionId("sess_2"),
            status = ChallengeSessionStatus.COMPLETE,
            startedAt = 1700000000000L,
            completedAt = 1700000050000L,
        )

        val entity = domain.toEntity()

        assertEquals("c_shift_1", entity.challengeId)
        assertEquals("sess_2", entity.sessionId)
        assertEquals("COMPLETE", entity.status)
        assertEquals(1700000000000L, entity.startedAt)
        assertEquals(1700000050000L, entity.completedAt)
    }

    @Test
    fun `roundtrip preserves ChallengeSession`() {
        val original = ChallengeSession(
            challengeId = ChallengeId("c_crowd_1"),
            sessionId = BattleSessionId("sess_3"),
            status = ChallengeSessionStatus.CORRECT,
            startedAt = 1700000010000L,
            completedAt = 1700000040000L,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toDomain gracefully falls back to READY on unknown status`() {
        val entity = ChallengeSessionEntity(
            challengeId = "c_corrupt",
            sessionId = "sess_4",
            status = "UNKNOWN_STATUS",
            startedAt = null,
            completedAt = null,
        )

        val domain = entity.toDomain()

        assertEquals(ChallengeSessionStatus.READY, domain.status)
    }
}

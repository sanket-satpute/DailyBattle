package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BattleSessionEntityMapperTest {

    @Test
    fun `toDomain maps all fields accurately when session is in progress`() {
        val entity = BattleSessionEntity(
            sessionId = "sess_123",
            userId = "user_abc",
            battleId = "battle_xyz",
            mode = "OFFICIAL",
            status = "IN_PROGRESS",
            currentChallenge = 2,
            startedAt = 1700000000000L,
            completedAt = null,
        )

        val domain = entity.toDomain()

        assertEquals(BattleSessionId("sess_123"), domain.sessionId)
        assertEquals(UserId("user_abc"), domain.userId)
        assertEquals(BattleId("battle_xyz"), domain.battleId)
        assertEquals(BattleMode.OFFICIAL, domain.mode)
        assertEquals(BattleSessionStatus.IN_PROGRESS, domain.status)
        assertEquals(2, domain.currentChallenge)
        assertEquals(1700000000000L, domain.startedAt)
        assertNull(domain.completedAt)
        assertEquals(emptyList<ChallengeSession>(), domain.challengeSessions)
    }

    @Test
    fun `toDomain attaches challenge sessions accurately`() {
        val entity = BattleSessionEntity(
            sessionId = "sess_123",
            userId = "user_abc",
            battleId = "battle_xyz",
            mode = "PRACTICE",
            status = "COMPLETED",
            currentChallenge = null,
            startedAt = 1700000000000L,
            completedAt = 1700000180000L,
        )
        val challengeSessions = listOf(
            ChallengeSession(
                challengeId = ChallengeId("c1"),
                sessionId = BattleSessionId("sess_123"),
                status = ChallengeSessionStatus.COMPLETE,
                startedAt = 1700000000000L,
                completedAt = 1700000060000L,
            )
        )

        val domain = entity.toDomain(challengeSessions)

        assertEquals(1, domain.challengeSessions.size)
        assertEquals(challengeSessions, domain.challengeSessions)
    }

    @Test
    fun `toEntity maps all fields accurately`() {
        val domain = BattleSession(
            sessionId = BattleSessionId("sess_456"),
            userId = UserId("user_def"),
            battleId = BattleId("battle_uvw"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
        )

        val entity = domain.toEntity()

        assertEquals("sess_456", entity.sessionId)
        assertEquals("user_def", entity.userId)
        assertEquals("battle_uvw", entity.battleId)
        assertEquals("OFFICIAL", entity.mode)
        assertEquals("NOT_STARTED", entity.status)
        assertNull(entity.currentChallenge)
        assertNull(entity.startedAt)
        assertNull(entity.completedAt)
    }

    @Test
    fun `roundtrip preserves BattleSession data`() {
        val original = BattleSession(
            sessionId = BattleSessionId("sess_roundtrip"),
            userId = UserId("user_roundtrip"),
            battleId = BattleId("battle_roundtrip"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1700000000000L,
            completedAt = 1700000200000L,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toDomain gracefully falls back on unknown mode and status`() {
        val entity = BattleSessionEntity(
            sessionId = "sess_unknown",
            userId = "user_unknown",
            battleId = "battle_unknown",
            mode = "UNKNOWN_MODE",
            status = "UNKNOWN_STATUS",
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
        )

        val domain = entity.toDomain()

        assertEquals(BattleMode.OFFICIAL, domain.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, domain.status)
    }
}

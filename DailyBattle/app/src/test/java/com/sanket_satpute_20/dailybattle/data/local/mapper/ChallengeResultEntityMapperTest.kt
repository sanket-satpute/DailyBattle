package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeResultEntityMapperTest {

    @Test
    fun `toDomain maps all ChallengeResultEntity fields accurately`() {
        val entity = ChallengeResultEntity(
            resultId = "cr_1",
            challengeId = "c_snap_1",
            sessionId = "sess_1",
            score = 285,
            maxScore = 300,
            completedAt = 1700000030000L,
            metadataJson = null,
        )

        val domain = entity.toDomain()

        assertEquals(ChallengeResultId("cr_1"), domain.resultId)
        assertEquals(ChallengeId("c_snap_1"), domain.challengeId)
        assertEquals(BattleSessionId("sess_1"), domain.sessionId)
        assertEquals(285, domain.score)
        assertEquals(300, domain.maxScore)
        assertEquals(1700000030000L, domain.completedAt)
        assertNull(domain.metadata)
    }

    @Test
    fun `toEntity maps all ChallengeResult domain fields accurately`() {
        val domain = ChallengeResult(
            resultId = ChallengeResultId("cr_2"),
            challengeId = ChallengeId("c_shift_1"),
            sessionId = BattleSessionId("sess_2"),
            score = 300,
            maxScore = 300,
            completedAt = 1700000060000L,
            metadata = null,
        )

        val entity = domain.toEntity()

        assertEquals("cr_2", entity.resultId)
        assertEquals("c_shift_1", entity.challengeId)
        assertEquals("sess_2", entity.sessionId)
        assertEquals(300, entity.score)
        assertEquals(300, entity.maxScore)
        assertEquals(1700000060000L, entity.completedAt)
        assertNull(entity.metadataJson)
    }

    @Test
    fun `roundtrip preserves ChallengeResult score boundaries`() {
        val minScoreResult = ChallengeResult(
            resultId = ChallengeResultId("cr_min"),
            challengeId = ChallengeId("c_min"),
            sessionId = BattleSessionId("sess_min"),
            score = 0,
            maxScore = 300,
            completedAt = 1700000000000L,
        )
        val maxScoreResult = ChallengeResult(
            resultId = ChallengeResultId("cr_max"),
            challengeId = ChallengeId("c_max"),
            sessionId = BattleSessionId("sess_max"),
            score = 300,
            maxScore = 300,
            completedAt = 1700000000000L,
        )

        assertEquals(minScoreResult, minScoreResult.toEntity().toDomain())
        assertEquals(maxScoreResult, maxScoreResult.toEntity().toDomain())
    }
}

package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleSessionResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeResultDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.TodayBattleResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BattleDtoMapperTest {

    @Test
    fun mapsTodayBattleDtoToDomainCorrectly() {
        val dto = TodayBattleResponseDto(
            battleId = "b-2026-10-12",
            battleDate = 1700000000000L,
            status = "AVAILABLE",
            version = 1,
            challenges = listOf(
                ChallengeDto("c-1", "SNAP", 1, 1),
                ChallengeDto("c-2", "SHIFT", 2, 1),
                ChallengeDto("c-3", "CROWD_CALL", 3, 1),
            ),
        )

        val domain = dto.toDomain()

        assertEquals(BattleId("b-2026-10-12"), domain.battleId)
        assertEquals(1700000000000L, domain.battleDate)
        assertEquals(BattleStatus.AVAILABLE, domain.status)
        assertEquals(1, domain.version)
        assertEquals(3, domain.challenges.size)
        assertEquals(ChallengeType.SNAP, domain.challenges[0].type)
        assertEquals(ChallengeType.SHIFT, domain.challenges[1].type)
        assertEquals(ChallengeType.CROWD_CALL, domain.challenges[2].type)
    }

    @Test
    fun mapsBattleSessionDtoToDomainCorrectly() {
        val dto = BattleSessionResponseDto(
            sessionId = "sess-1",
            battleId = "b-1",
            mode = "OFFICIAL",
            status = "IN_PROGRESS",
            currentChallenge = 2,
            startedAt = 5000L,
        )

        val domain = dto.toDomain(UserId("user-1"))

        assertEquals("sess-1", domain.sessionId.value)
        assertEquals(UserId("user-1"), domain.userId)
        assertEquals(BattleId("b-1"), domain.battleId)
        assertEquals(BattleMode.OFFICIAL, domain.mode)
        assertEquals(BattleSessionStatus.IN_PROGRESS, domain.status)
        assertEquals(2, domain.currentChallenge)
        assertEquals(5000L, domain.startedAt)
        assertNull(domain.completedAt)
    }

    @Test
    fun mapsChallengeResultDtoToDomainCorrectly() {
        val dto = ChallengeResultDto(
            resultId = "cr-1",
            challengeId = "c-1",
            sessionId = "s-1",
            score = 295,
            maxScore = 300,
            completedAt = 6000L,
        )

        val domain = dto.toDomain()

        assertEquals("cr-1", domain.resultId.value)
        assertEquals("c-1", domain.challengeId.value)
        assertEquals("s-1", domain.sessionId.value)
        assertEquals(295, domain.score)
        assertEquals(300, domain.maxScore)
        assertEquals(6000L, domain.completedAt)
    }

    @Test
    fun mapsBattleResultDtoToDomainWithoutAlteringAuthoritativeMetrics() {
        val dto = BattleResultResponseDto(
            resultId = "res-1",
            userId = "u-1",
            battleId = "b-1",
            sessionId = "s-1",
            snapScore = 290,
            shiftScore = 280,
            crowdCallScore = 270,
            consistencyScore = 90,
            totalScore = 930,
            percentile = 96.5,
            completedAt = 7000L,
        )

        val domain = dto.toDomain()

        // Verifies client does not recalculate scores or percentile in transport layer
        assertEquals(290, domain.snapScore)
        assertEquals(280, domain.shiftScore)
        assertEquals(270, domain.crowdCallScore)
        assertEquals(90, domain.consistencyScore)
        assertEquals(930, domain.totalScore)
        assertEquals(96.5, domain.percentile, 0.0001)
        assertEquals(7000L, domain.completedAt)
    }
}

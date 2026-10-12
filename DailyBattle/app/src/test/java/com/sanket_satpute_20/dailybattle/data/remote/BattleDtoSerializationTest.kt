package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleSessionResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeResultDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.CompleteBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.StartBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.TodayBattleResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BattleDtoSerializationTest {

    @Test
    fun todayBattleResponseDtoRoundtrip() {
        val dto = TodayBattleResponseDto(
            battleId = "battle-2026-10-12",
            battleDate = 1700000000000L,
            status = "AVAILABLE",
            version = 1,
            challenges = listOf(
                ChallengeDto(challengeId = "c-1", type = "SNAP", order = 1, version = 1),
                ChallengeDto(challengeId = "c-2", type = "SHIFT", order = 2, version = 1),
                ChallengeDto(challengeId = "c-3", type = "CROWD_CALL", order = 3, version = 1),
            ),
        )

        val json = DailyBattleJson.encodeToString(TodayBattleResponseDto.serializer(), dto)
        val decoded = DailyBattleJson.decodeFromString(TodayBattleResponseDto.serializer(), json)

        assertEquals(dto, decoded)
    }

    @Test
    fun startBattleRequestAndResponseDtoRoundtrip() {
        val request = StartBattleRequestDto(clientRequestId = "req-idempotent-1")
        val reqJson = DailyBattleJson.encodeToString(StartBattleRequestDto.serializer(), request)
        val decodedReq = DailyBattleJson.decodeFromString(StartBattleRequestDto.serializer(), reqJson)
        assertEquals(request, decodedReq)

        val response = BattleSessionResponseDto(
            sessionId = "session-101",
            battleId = "battle-2026-10-12",
            mode = "OFFICIAL",
            status = "IN_PROGRESS",
            currentChallenge = 1,
            startedAt = 1700000050000L,
        )
        val respJson = DailyBattleJson.encodeToString(BattleSessionResponseDto.serializer(), response)
        val decodedResp = DailyBattleJson.decodeFromString(BattleSessionResponseDto.serializer(), respJson)
        assertEquals(response, decodedResp)
    }

    @Test
    fun submitChallengeResultRequestAndResponseDtoRoundtrip() {
        val request = SubmitChallengeResultRequestDto(
            clientRequestId = "req-ch-1",
            score = 280,
            maxScore = 300,
            completedAt = 1700000090000L,
        )
        val reqJson = DailyBattleJson.encodeToString(SubmitChallengeResultRequestDto.serializer(), request)
        val decodedReq = DailyBattleJson.decodeFromString(SubmitChallengeResultRequestDto.serializer(), reqJson)
        assertEquals(request, decodedReq)

        val response = SubmitChallengeResultResponseDto(
            challengeResult = ChallengeResultDto(
                resultId = "cr-1",
                challengeId = "c-1",
                sessionId = "session-101",
                score = 280,
                maxScore = 300,
                completedAt = 1700000090000L,
            ),
            nextChallenge = 2,
            sessionStatus = "IN_PROGRESS",
        )
        val respJson = DailyBattleJson.encodeToString(SubmitChallengeResultResponseDto.serializer(), response)
        val decodedResp = DailyBattleJson.decodeFromString(SubmitChallengeResultResponseDto.serializer(), respJson)
        assertEquals(response, decodedResp)
    }

    @Test
    fun completeBattleRequestAndResponseDtoRoundtrip() {
        val request = CompleteBattleRequestDto(clientRequestId = "req-complete-1")
        val reqJson = DailyBattleJson.encodeToString(CompleteBattleRequestDto.serializer(), request)
        val decodedReq = DailyBattleJson.decodeFromString(CompleteBattleRequestDto.serializer(), reqJson)
        assertEquals(request, decodedReq)

        val response = BattleResultResponseDto(
            resultId = "res-99",
            userId = "user-1",
            battleId = "battle-2026-10-12",
            sessionId = "session-101",
            snapScore = 280,
            shiftScore = 270,
            crowdCallScore = 290,
            consistencyScore = 85,
            totalScore = 925,
            percentile = 94.5,
            completedAt = 1700000200000L,
        )
        val respJson = DailyBattleJson.encodeToString(BattleResultResponseDto.serializer(), response)
        val decodedResp = DailyBattleJson.decodeFromString(BattleResultResponseDto.serializer(), respJson)
        assertEquals(response, decodedResp)
    }

    @Test
    fun handlesNullableFieldsSafely() {
        val json = """
            {
                "sessionId": "s-1",
                "battleId": "b-1",
                "mode": "OFFICIAL",
                "status": "COMPLETED",
                "currentChallenge": null,
                "startedAt": 1000
            }
        """.trimIndent()

        val decoded = DailyBattleJson.decodeFromString(BattleSessionResponseDto.serializer(), json)
        assertNull(decoded.currentChallenge)
        assertEquals("COMPLETED", decoded.status)
    }
}

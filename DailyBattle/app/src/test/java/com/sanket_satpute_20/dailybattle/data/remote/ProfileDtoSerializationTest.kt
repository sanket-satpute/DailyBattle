package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.history.HistoryResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.BattleDnaDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.PersonalRecordsDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileDtoSerializationTest {

    @Test
    fun profileResponseDtoRoundtripWithFullDna() {
        val dto = ProfileResponseDto(
            userId = "usr-1",
            battleName = "MasterWarrior",
            momentum = 7,
            bestScore = 945,
            averageScore = 880,
            battleDNA = BattleDnaDto(
                speed = 290,
                memory = 285,
                people = 295,
            ),
            records = PersonalRecordsDto(
                bestScore = 945,
                bestMomentum = 14,
                battlesPlayed = 50,
            ),
        )

        val json = DailyBattleJson.encodeToString(ProfileResponseDto.serializer(), dto)
        val decoded = DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), json)

        assertEquals(dto, decoded)
        assertEquals(290, decoded.battleDNA.speed)
        assertEquals(285, decoded.battleDNA.memory)
        assertEquals(295, decoded.battleDNA.people)
    }

    @Test
    fun profileResponseDtoRoundtripWithNullDnaDimensions() {
        val dto = ProfileResponseDto(
            userId = "usr-2",
            battleName = "NewRecruit",
            momentum = 0,
            bestScore = 0,
            averageScore = 0,
            battleDNA = BattleDnaDto(
                speed = null,
                memory = null,
                people = null,
            ),
            records = PersonalRecordsDto(
                bestScore = 0,
                bestMomentum = 0,
                battlesPlayed = 0,
            ),
        )

        val json = DailyBattleJson.encodeToString(ProfileResponseDto.serializer(), dto)
        val decoded = DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), json)

        assertEquals(dto, decoded)
        assertNull(decoded.battleDNA.speed)
        assertNull(decoded.battleDNA.memory)
        assertNull(decoded.battleDNA.people)
    }

    @Test
    fun battleDnaDtoHandlesMissingFieldsInJson() {
        val json = """{ "speed": 280 }"""
        val decoded = DailyBattleJson.decodeFromString(BattleDnaDto.serializer(), json)

        assertEquals(280, decoded.speed)
        assertNull(decoded.memory)
        assertNull(decoded.people)
    }

    @Test
    fun historyResponseDtoRoundtrip() {
        val battleResult = BattleResultResponseDto(
            resultId = "res-101",
            userId = "usr-1",
            battleId = "b-2026-10-12",
            sessionId = "sess-1",
            snapScore = 290,
            shiftScore = 280,
            crowdCallScore = 270,
            consistencyScore = 90,
            totalScore = 930,
            percentile = 95.5,
            completedAt = 1700000000000L,
        )
        val response = HistoryResponseDto(history = listOf(battleResult))

        val json = DailyBattleJson.encodeToString(HistoryResponseDto.serializer(), response)
        val decoded = DailyBattleJson.decodeFromString(HistoryResponseDto.serializer(), json)

        assertEquals(response, decoded)
        assertEquals(1, decoded.history.size)
        assertEquals("res-101", decoded.history[0].resultId)
    }

    @Test
    fun emptyHistoryResponseDtoRoundtrip() {
        val response = HistoryResponseDto(history = emptyList())
        val json = DailyBattleJson.encodeToString(HistoryResponseDto.serializer(), response)
        val decoded = DailyBattleJson.decodeFromString(HistoryResponseDto.serializer(), json)

        assertEquals(response, decoded)
        assertTrue(decoded.history.isEmpty())
    }

    @Test(expected = SerializationException::class)
    fun malformedJsonThrowsSerializationException() {
        val malformedJson = """{ "userId": 12345 }"""
        DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), malformedJson)
    }
}

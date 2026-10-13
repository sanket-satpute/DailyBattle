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
import com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.BattleDnaDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.PersonalRecordsDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendsResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive verification of Serialization fidelity and Schema evolution resilience (Sprint 23.2).
 *
 * Verifies:
 * - Round-trip serialization across all request and response DTO schemas.
 * - Schema resilience: unknown keys in payloads are ignored safely without crash.
 * - Lenient parsing and input coercion.
 * - Numeric precision preservation (floating point percentiles, 64-bit epoch timestamps, boundaries).
 */
class SerializationResilienceTest {

    // =========================================================================
    // 1. Battle DTO Roundtrip and Schema Evolution
    // =========================================================================

    @Test
    fun todayBattleResponseRoundtripAndUnknownKeysResilience() {
        val dto = TodayBattleResponseDto(
            battleId = "b-2026-10-13",
            battleDate = 1700000000000L,
            status = "AVAILABLE",
            version = 2,
            challenges = listOf(
                ChallengeDto("c-1", "SNAP", 1, 1),
                ChallengeDto("c-2", "SHIFT", 2, 1),
                ChallengeDto("c-3", "CROWD_CALL", 3, 1),
            ),
        )

        val json = DailyBattleJson.encodeToString(TodayBattleResponseDto.serializer(), dto)
        val decoded = DailyBattleJson.decodeFromString(TodayBattleResponseDto.serializer(), json)
        assertEquals(dto, decoded)

        // Inject unknown future server fields (08_DATA_AND_API.md schema evolution)
        val jsonWithUnknownFields = """
            {
                "battleId": "b-2026-10-13",
                "battleDate": 1700000000000,
                "status": "AVAILABLE",
                "version": 2,
                "challenges": [
                    {"challengeId": "c-1", "type": "SNAP", "order": 1, "version": 1, "experimentalConfig": {"difficulty": "hard"}}
                ],
                "futureTheme": "CYBER_NEON",
                "unannouncedFeatureFlag": true,
                "serverProcessingTimeMs": 14.2
            }
        """.trimIndent()

        val decodedWithUnknowns = DailyBattleJson.decodeFromString(TodayBattleResponseDto.serializer(), jsonWithUnknownFields)
        assertEquals("b-2026-10-13", decodedWithUnknowns.battleId)
        assertEquals(1, decodedWithUnknowns.challenges.size)
        assertEquals("c-1", decodedWithUnknowns.challenges.first().challengeId)
    }

    @Test
    fun battleResultSerializationPreservesFloatPrecisionAndScores() {
        val dto = BattleResultResponseDto(
            resultId = "res-precise",
            userId = "usr-1",
            battleId = "b-1",
            sessionId = "s-1",
            snapScore = 300,
            shiftScore = 295,
            crowdCallScore = 290,
            consistencyScore = 100,
            totalScore = 985,
            percentile = 99.95,
            completedAt = 1700000099999L,
        )

        val json = DailyBattleJson.encodeToString(BattleResultResponseDto.serializer(), dto)
        val decoded = DailyBattleJson.decodeFromString(BattleResultResponseDto.serializer(), json)

        assertEquals(dto, decoded)
        assertEquals(99.95, decoded.percentile, 0.0001)
        assertEquals(1700000099999L, decoded.completedAt)
    }

    @Test
    fun submitChallengeResultRoundtrip() {
        val request = SubmitChallengeResultRequestDto(
            clientRequestId = "req-idemp-101",
            score = 290,
            maxScore = 300,
            completedAt = 1700000012345L,
        )
        val reqJson = DailyBattleJson.encodeToString(SubmitChallengeResultRequestDto.serializer(), request)
        val decodedReq = DailyBattleJson.decodeFromString(SubmitChallengeResultRequestDto.serializer(), reqJson)
        assertEquals(request, decodedReq)

        val response = SubmitChallengeResultResponseDto(
            challengeResult = ChallengeResultDto(
                resultId = "cr-99",
                challengeId = "c-1",
                sessionId = "s-101",
                score = 290,
                maxScore = 300,
                completedAt = 1700000012345L,
            ),
            nextChallenge = 2,
            sessionStatus = "IN_PROGRESS",
        )
        val respJson = DailyBattleJson.encodeToString(SubmitChallengeResultResponseDto.serializer(), response)
        val decodedResp = DailyBattleJson.decodeFromString(SubmitChallengeResultResponseDto.serializer(), respJson)
        assertEquals(response, decodedResp)
    }

    // =========================================================================
    // 2. Profile DTO Roundtrip and Null Handling
    // =========================================================================

    @Test
    fun profileResponseRoundtripWithNullableRadarFields() {
        val dto = ProfileResponseDto(
            userId = "usr-dna",
            battleName = "RadarRunner",
            momentum = 5,
            bestScore = 890,
            averageScore = 840,
            battleDNA = BattleDnaDto(
                speed = 280,
                memory = null, // Missing radar metric
                people = 290,
            ),
            records = PersonalRecordsDto(
                bestScore = 890,
                bestMomentum = 7,
                battlesPlayed = 15,
            ),
        )

        val json = DailyBattleJson.encodeToString(ProfileResponseDto.serializer(), dto)
        val decoded = DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), json)

        assertEquals(dto, decoded)
        assertNull(decoded.battleDNA.memory)
        assertEquals(280, decoded.battleDNA.speed)
    }

    // =========================================================================
    // 3. Social DTO Roundtrip
    // =========================================================================

    @Test
    fun friendsAndRivalDtoRoundtrip() {
        val friendsDto = FriendsResponseDto(
            friends = listOf(
                FriendDto(
                    friendshipId = "fs-10",
                    userId = "usr-me",
                    friendUserId = "usr-f1",
                    friendName = "Falcon",
                    status = "ACCEPTED",
                    todayScore = 920,
                    createdAt = 1000L,
                    updatedAt = 2000L,
                ),
                FriendDto(
                    friendshipId = "fs-11",
                    userId = "usr-me",
                    friendUserId = "usr-f2",
                    friendName = "Eagle",
                    status = "PENDING",
                    todayScore = null,
                    createdAt = 1500L,
                    updatedAt = 1500L,
                ),
            ),
        )

        val friendsJson = DailyBattleJson.encodeToString(FriendsResponseDto.serializer(), friendsDto)
        val decodedFriends = DailyBattleJson.decodeFromString(FriendsResponseDto.serializer(), friendsJson)
        assertEquals(friendsDto, decodedFriends)

        val rivalDto = RivalResponseDto(
            userId = "usr-me",
            rivalUserId = "usr-rival",
            rivalName = "Thunder",
            selectedAt = 5000L,
            status = "ACTIVE",
        )
        val rivalJson = DailyBattleJson.encodeToString(RivalResponseDto.serializer(), rivalDto)
        val decodedRival = DailyBattleJson.decodeFromString(RivalResponseDto.serializer(), rivalJson)
        assertEquals(rivalDto, decodedRival)

        val comparisonDto = RivalScoreComparisonResponseDto(
            userScore = 920,
            rivalScore = 880,
            scoreGap = 40,
            battleId = "battle-2026-10-13",
            direction = "AHEAD",
        )
        val compJson = DailyBattleJson.encodeToString(RivalScoreComparisonResponseDto.serializer(), comparisonDto)
        val decodedComp = DailyBattleJson.decodeFromString(RivalScoreComparisonResponseDto.serializer(), compJson)
        assertEquals(comparisonDto, decodedComp)
    }

    @Test
    fun addFriendRequestAndResponseRoundtrip() {
        val req = AddFriendRequestDto(
            userId = "usr-me",
            battleCode = "BATTLE-4242",
            clientRequestId = "req-friend-1",
        )
        val reqJson = DailyBattleJson.encodeToString(AddFriendRequestDto.serializer(), req)
        val decodedReq = DailyBattleJson.decodeFromString(AddFriendRequestDto.serializer(), reqJson)
        assertEquals(req, decodedReq)

        val resp = AddFriendResponseDto(
            friendship = FriendDto(
                friendshipId = "fs-new",
                userId = "usr-me",
                friendUserId = "usr-added",
                friendName = "NewPal",
                status = "PENDING",
                todayScore = null,
                createdAt = 3000L,
                updatedAt = 3000L,
            ),
            status = "REQUEST_SENT",
        )
        val respJson = DailyBattleJson.encodeToString(AddFriendResponseDto.serializer(), resp)
        val decodedResp = DailyBattleJson.decodeFromString(AddFriendResponseDto.serializer(), respJson)
        assertEquals(resp, decodedResp)
    }

    // =========================================================================
    // 4. Structured Error Response Serialization
    // =========================================================================

    @Test
    fun apiErrorResponseDtoRoundtripWithOptionalFields() {
        val fullError = ApiErrorResponseDto(
            code = "BATTLE_NOT_AVAILABLE",
            message = "The daily battle has expired.",
            details = "Battle window closed at 23:59:59",
            requestId = "req-err-999",
        )
        val fullJson = DailyBattleJson.encodeToString(ApiErrorResponseDto.serializer(), fullError)
        val decodedFull = DailyBattleJson.decodeFromString(ApiErrorResponseDto.serializer(), fullJson)
        assertEquals(fullError, decodedFull)

        // Minimal error with null optional fields
        val minErrorJson = """{"code":"UNAUTHORIZED","message":"Invalid token"}"""
        val decodedMin = DailyBattleJson.decodeFromString(ApiErrorResponseDto.serializer(), minErrorJson)
        assertEquals("UNAUTHORIZED", decodedMin.code)
        assertEquals("Invalid token", decodedMin.message)
        assertNull(decodedMin.details)
        assertNull(decodedMin.requestId)
    }

    @Test
    fun lenientJsonToleratesLenientNuancesAndExtraWhitespaces() {
        // DailyBattleJson isLenient = true allows unquoted keys
        val lenientJson = """{code: "SERVER_ERROR", message: "Internal failure", requestId: "req-unquoted"}"""
        val decoded = DailyBattleJson.decodeFromString(ApiErrorResponseDto.serializer(), lenientJson)
        assertEquals("SERVER_ERROR", decoded.code)
        assertEquals("req-unquoted", decoded.requestId)
    }
}

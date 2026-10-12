package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleSessionResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeResultDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.TodayBattleResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.history.HistoryResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.BattleDnaDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.PersonalRecordsDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendsResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.error.HttpErrorMapper
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 16.5 — Schema Validation Test Suite.
 *
 * Validates:
 * - Response schema conformance across Battle, Social, and Profile endpoints.
 * - Missing fields (required vs optional with defaults).
 * - Unexpected/unknown fields resilience.
 * - Invalid enum fallback safety.
 * - Malformed payload rejection.
 * - Unexpected schema structure rejection.
 */
class ApiSchemaValidationTest {

    @Test
    fun validBattleResponseSchemasRoundtrip() {
        val todayBattle = TodayBattleResponseDto(
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
        val json = DailyBattleJson.encodeToString(TodayBattleResponseDto.serializer(), todayBattle)
        val decoded = DailyBattleJson.decodeFromString(TodayBattleResponseDto.serializer(), json)
        assertEquals(todayBattle, decoded)
    }

    @Test
    fun unexpectedFieldsAreSafelyIgnoredAcrossAllSchemas() {
        // Battle payload with unknown fields
        val battleJsonWithExtra = """
            {
                "battleId": "b-extra-1",
                "battleDate": 1700000000000,
                "status": "AVAILABLE",
                "version": 1,
                "server_signature": "sig_xyz_123",
                "challenges": [
                    { "challengeId": "c-1", "type": "SNAP", "order": 1, "version": 1, "extra_metric": 42 }
                ],
                "deprecated_metadata": { "region": "us-east" }
            }
        """.trimIndent()
        val decodedBattle = DailyBattleJson.decodeFromString(TodayBattleResponseDto.serializer(), battleJsonWithExtra)
        assertEquals("b-extra-1", decodedBattle.battleId)
        assertEquals(1, decodedBattle.challenges.size)

        // Social payload with unknown fields
        val friendJsonWithExtra = """
            {
                "friends": [
                    {
                        "friendshipId": "fs-1",
                        "userId": "usr-1",
                        "friendUserId": "usr-2",
                        "friendName": "Alex",
                        "status": "ACCEPTED",
                        "todayScore": 880,
                        "createdAt": 1700000000000,
                        "updatedAt": 1700000010000,
                        "client_platform": "ANDROID",
                        "avatar_url": "https://example.com/pic.png"
                    }
                ],
                "cursor": "cur_next_99"
            }
        """.trimIndent()
        val decodedFriends = DailyBattleJson.decodeFromString(FriendsResponseDto.serializer(), friendJsonWithExtra)
        assertEquals(1, decodedFriends.friends.size)
        assertEquals("Alex", decodedFriends.friends[0].friendName)

        // Profile payload with unknown fields
        val profileJsonWithExtra = """
            {
                "userId": "usr-1",
                "battleName": "Player",
                "momentum": 3,
                "bestScore": 900,
                "averageScore": 850,
                "tier_name": "DIAMOND",
                "battleDNA": {
                    "speed": 290,
                    "memory": 280,
                    "people": 270,
                    "endurance": 300
                },
                "records": {
                    "bestScore": 900,
                    "bestMomentum": 5,
                    "battlesPlayed": 20,
                    "allTimeRank": 12
                }
            }
        """.trimIndent()
        val decodedProfile = DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), profileJsonWithExtra)
        assertEquals("usr-1", decodedProfile.userId)
        assertEquals(290, decodedProfile.battleDNA.speed)
    }

    @Test(expected = SerializationException::class)
    fun missingRequiredBattleIdThrowsSerializationException() {
        val missingIdJson = """
            {
                "battleDate": 1700000000000,
                "status": "AVAILABLE",
                "version": 1,
                "challenges": []
            }
        """.trimIndent()
        DailyBattleJson.decodeFromString(TodayBattleResponseDto.serializer(), missingIdJson)
    }

    @Test(expected = SerializationException::class)
    fun missingRequiredUserIdInProfileThrowsSerializationException() {
        val missingUserIdJson = """
            {
                "battleName": "Player",
                "momentum": 3,
                "bestScore": 900,
                "averageScore": 850,
                "battleDNA": { "speed": 290, "memory": 280, "people": 270 },
                "records": { "bestScore": 900, "bestMomentum": 5, "battlesPlayed": 20 }
            }
        """.trimIndent()
        DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), missingUserIdJson)
    }

    @Test
    fun missingOptionalFieldsFallbackToDefaults() {
        // FriendDto omitting todayScore defaults to null
        val friendJson = """
            {
                "friendshipId": "fs-99",
                "userId": "usr-1",
                "friendUserId": "usr-2",
                "friendName": "Sam",
                "status": "ACCEPTED",
                "createdAt": 1700000000000,
                "updatedAt": 1700000000000
            }
        """.trimIndent()
        val decodedFriend = DailyBattleJson.decodeFromString(FriendDto.serializer(), friendJson)
        assertNull(decodedFriend.todayScore)

        // BattleDnaDto omitting all dimensions defaults to null
        val dnaJson = """{}"""
        val decodedDna = DailyBattleJson.decodeFromString(BattleDnaDto.serializer(), dnaJson)
        assertNull(decodedDna.speed)
        assertNull(decodedDna.memory)
        assertNull(decodedDna.people)

        // FriendsResponseDto omitting friends array defaults to empty list
        val emptyFriendsJson = """{}"""
        val decodedFriends = DailyBattleJson.decodeFromString(FriendsResponseDto.serializer(), emptyFriendsJson)
        assertTrue(decodedFriends.friends.isEmpty())

        // HistoryResponseDto omitting history array defaults to empty list
        val emptyHistoryJson = """{}"""
        val decodedHistory = DailyBattleJson.decodeFromString(HistoryResponseDto.serializer(), emptyHistoryJson)
        assertTrue(decodedHistory.history.isEmpty())
    }

    @Test
    fun invalidEnumValuesHandledGracefullyByMappers() {
        // Unknown ChallengeType falls back to SNAP
        val unknownChallengeDto = ChallengeDto("c-x", "UNKNOWN_CHALLENGE_TYPE", 1, 1)
        val domainChallenge = unknownChallengeDto.toDomain(BattleId("b-1"))
        assertEquals(ChallengeType.SNAP, domainChallenge.type)

        // Unknown BattleStatus falls back to AVAILABLE
        val unknownStatusBattleDto = TodayBattleResponseDto("b-1", 1700000000000L, "UNKNOWN_STATUS", 1, emptyList())
        val domainBattle = unknownStatusBattleDto.toDomain()
        assertEquals(BattleStatus.AVAILABLE, domainBattle.status)

        // Unknown BattleMode and BattleSessionStatus
        val unknownSessionDto = BattleSessionResponseDto("s-1", "b-1", "SUPER_MODE", "CORRUPTED_STATUS", 1, 1000L)
        val domainSession = unknownSessionDto.toDomain(UserId("u-1"))
        assertEquals(BattleMode.OFFICIAL, domainSession.mode)
        assertEquals(BattleSessionStatus.IN_PROGRESS, domainSession.status)

        // Unknown FriendStatus falls back to PENDING
        val unknownFriendDto = FriendDto("fs-1", "u-1", "u-2", "Bob", "ARCHIVED", null, 1000L, 1000L)
        val domainFriend = unknownFriendDto.toDomain()
        assertEquals(FriendStatus.PENDING, domainFriend.status)

        // Unknown RivalStatus falls back to ACTIVE
        val unknownRivalDto = RivalResponseDto("u-1", "u-3", "Rival", 1000L, "BANNED")
        val domainRival = unknownRivalDto.toDomain()
        assertEquals(RivalStatus.ACTIVE, domainRival.status)

        // Unknown ScoreDirection falls back to TIED
        val unknownComparisonDto = RivalScoreComparisonResponseDto(900, 900, 0, "b-1", "EVEN")
        val domainComparison = unknownComparisonDto.toDomain()
        assertEquals(ScoreDirection.TIED, domainComparison.direction)
    }

    @Test(expected = SerializationException::class)
    fun malformedJsonSyntaxThrowsSerializationException() {
        val brokenJson = """{ "userId": "usr-1", "battleName": """
        DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), brokenJson)
    }

    @Test(expected = SerializationException::class)
    fun wrongDataTypeThrowsSerializationException() {
        // momentum is expected as Int, received string
        val wrongTypeJson = """
            {
                "userId": "usr-1",
                "battleName": "Player",
                "momentum": "FIVE_NOT_INT",
                "bestScore": 900,
                "averageScore": 850,
                "battleDNA": {},
                "records": { "bestScore": 900, "bestMomentum": 5, "battlesPlayed": 20 }
            }
        """.trimIndent()
        DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), wrongTypeJson)
    }

    @Test(expected = SerializationException::class)
    fun unexpectedArrayStructureInsteadOfObjectThrowsSerializationException() {
        val arrayJson = """[ { "userId": "usr-1" } ]"""
        DailyBattleJson.decodeFromString(ProfileResponseDto.serializer(), arrayJson)
    }

    @Test
    fun validApiErrorResponsePayloadRoundtrip() {
        val errorDto = com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto(
            code = "BATTLE_NOT_AVAILABLE",
            message = "The requested battle is not currently available.",
            details = "Battle window closed at 23:59:59 UTC",
            requestId = "req-999888",
        )
        val json = DailyBattleJson.encodeToString(com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto.serializer(), errorDto)
        val decoded = DailyBattleJson.decodeFromString(com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto.serializer(), json)
        assertEquals(errorDto, decoded)
    }

    @Test
    fun apiErrorResponseWithMinimalFieldsOmitsOptionalDetailsAndRequestId() {
        val minimalJson = """
            {
                "code": "BATTLE_ALREADY_COMPLETED",
                "message": "User has already completed the daily battle."
            }
        """.trimIndent()
        val decoded = DailyBattleJson.decodeFromString(com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto.serializer(), minimalJson)
        assertEquals("BATTLE_ALREADY_COMPLETED", decoded.code)
        assertEquals("User has already completed the daily battle.", decoded.message)
        assertNull(decoded.details)
        assertNull(decoded.requestId)
    }

    @Test
    fun apiErrorResponseIgnoresUnknownServerFields() {
        val unknownFieldsJson = """
            {
                "code": "RATE_LIMITED",
                "message": "Too many requests",
                "retry_after_seconds": 60,
                "server_tier": "prod-east"
            }
        """.trimIndent()
        val decoded = DailyBattleJson.decodeFromString(com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto.serializer(), unknownFieldsJson)
        assertEquals("RATE_LIMITED", decoded.code)
        assertEquals("Too many requests", decoded.message)
    }

    @Test
    fun httpErrorMapperParsesErrorPayloadSafely() {
        val json = """{ "code": "FRIEND_NOT_FOUND", "message": "Friend code invalid", "requestId": "req-1" }"""
        val parsed = HttpErrorMapper.parseErrorResponse(json)
        assertNotNull(parsed)
        assertEquals("FRIEND_NOT_FOUND", parsed?.code)
        assertEquals("Friend code invalid", parsed?.message)
        assertEquals("req-1", parsed?.requestId)

        // Null, blank, and malformed inputs return null safely
        assertNull(HttpErrorMapper.parseErrorResponse(null))
        assertNull(HttpErrorMapper.parseErrorResponse(""))
        assertNull(HttpErrorMapper.parseErrorResponse("   "))
        assertNull(HttpErrorMapper.parseErrorResponse("not a json"))
        assertNull(HttpErrorMapper.parseErrorResponse("""{ "missing_code": 123 }"""))
    }
}


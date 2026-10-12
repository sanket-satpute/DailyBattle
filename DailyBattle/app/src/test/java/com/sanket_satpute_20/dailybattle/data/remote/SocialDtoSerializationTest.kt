package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendsResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialDtoSerializationTest {

    @Test
    fun friendDtoAndFriendsResponseDtoRoundtrip() {
        val friend1 = FriendDto(
            friendshipId = "fs-1",
            userId = "usr-1",
            friendUserId = "usr-2",
            friendName = "Alex",
            status = "ACCEPTED",
            todayScore = 850,
            createdAt = 1700000000000L,
            updatedAt = 1700000050000L,
        )
        val friend2 = FriendDto(
            friendshipId = "fs-2",
            userId = "usr-1",
            friendUserId = "usr-3",
            friendName = "Jordan",
            status = "PENDING",
            todayScore = null,
            createdAt = 1700000010000L,
            updatedAt = 1700000020000L,
        )
        val response = FriendsResponseDto(friends = listOf(friend1, friend2))

        val json = DailyBattleJson.encodeToString(FriendsResponseDto.serializer(), response)
        val decoded = DailyBattleJson.decodeFromString(FriendsResponseDto.serializer(), json)

        assertEquals(response, decoded)
        assertEquals(2, decoded.friends.size)
        assertEquals(850, decoded.friends[0].todayScore)
        assertNull(decoded.friends[1].todayScore)
    }

    @Test
    fun emptyFriendsResponseDtoRoundtrip() {
        val emptyResponse = FriendsResponseDto(friends = emptyList())
        val json = DailyBattleJson.encodeToString(FriendsResponseDto.serializer(), emptyResponse)
        val decoded = DailyBattleJson.decodeFromString(FriendsResponseDto.serializer(), json)

        assertEquals(emptyResponse, decoded)
        assertTrue(decoded.friends.isEmpty())
    }

    @Test
    fun friendDtoHandlesMissingTodayScoreInJson() {
        val json = """
            {
                "friendshipId": "fs-3",
                "userId": "usr-1",
                "friendUserId": "usr-4",
                "friendName": "Sam",
                "status": "ACCEPTED",
                "createdAt": 1700000000000,
                "updatedAt": 1700000000000
            }
        """.trimIndent()

        val decoded = DailyBattleJson.decodeFromString(FriendDto.serializer(), json)
        assertEquals("fs-3", decoded.friendshipId)
        assertNull(decoded.todayScore)
    }

    @Test
    fun addFriendRequestAndResponseDtoRoundtrip() {
        val request = AddFriendRequestDto(
            userId = "usr-1",
            battleCode = "BATTLE-ABC-123",
            clientRequestId = "req-idempotent-42",
        )
        val reqJson = DailyBattleJson.encodeToString(AddFriendRequestDto.serializer(), request)
        val decodedReq = DailyBattleJson.decodeFromString(AddFriendRequestDto.serializer(), reqJson)
        assertEquals(request, decodedReq)
        assertEquals("req-idempotent-42", decodedReq.clientRequestId)
        assertEquals("BATTLE-ABC-123", decodedReq.battleCode)

        val response = AddFriendResponseDto(
            friendship = FriendDto(
                friendshipId = "fs-new",
                userId = "usr-1",
                friendUserId = "usr-5",
                friendName = "Taylor",
                status = "PENDING",
                todayScore = null,
                createdAt = 1700000100000L,
                updatedAt = 1700000100000L,
            ),
            status = "PENDING",
        )
        val respJson = DailyBattleJson.encodeToString(AddFriendResponseDto.serializer(), response)
        val decodedResp = DailyBattleJson.decodeFromString(AddFriendResponseDto.serializer(), respJson)
        assertEquals(response, decodedResp)
    }

    @Test
    fun rivalResponseDtoRoundtrip() {
        val rival = RivalResponseDto(
            userId = "usr-1",
            rivalUserId = "usr-9",
            rivalName = "RivalPlayer",
            selectedAt = 1700000080000L,
            status = "ACTIVE",
        )
        val json = DailyBattleJson.encodeToString(RivalResponseDto.serializer(), rival)
        val decoded = DailyBattleJson.decodeFromString(RivalResponseDto.serializer(), json)

        assertEquals(rival, decoded)
    }

    @Test
    fun rivalScoreComparisonResponseDtoRoundtrip() {
        val comparison = RivalScoreComparisonResponseDto(
            userScore = 910,
            rivalScore = 895,
            scoreGap = 15,
            battleId = "battle-2026-10-12",
            direction = "AHEAD",
        )
        val json = DailyBattleJson.encodeToString(RivalScoreComparisonResponseDto.serializer(), comparison)
        val decoded = DailyBattleJson.decodeFromString(RivalScoreComparisonResponseDto.serializer(), json)

        assertEquals(comparison, decoded)
        assertEquals("AHEAD", decoded.direction)
        assertEquals(15, decoded.scoreGap)
    }

    @Test(expected = SerializationException::class)
    fun malformedJsonThrowsSerializationException() {
        val malformedJson = """{ "friendshipId": 12345 }"""
        DailyBattleJson.decodeFromString(FriendDto.serializer(), malformedJson)
    }
}

package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SocialDtoMapperTest {

    @Test
    fun mapsFriendDtoToDomainCorrectly() {
        val dto = FriendDto(
            friendshipId = "fs-1",
            userId = "usr-1",
            friendUserId = "usr-2",
            friendName = "Alex",
            status = "ACCEPTED",
            todayScore = 880,
            createdAt = 1700000000000L,
            updatedAt = 1700000010000L,
        )

        val domain = dto.toDomain()

        assertEquals(FriendshipId("fs-1"), domain.friendshipId)
        assertEquals(UserId("usr-1"), domain.userId)
        assertEquals(UserId("usr-2"), domain.friendUserId)
        assertEquals("Alex", domain.friendName)
        assertEquals(FriendStatus.ACCEPTED, domain.status)
        assertEquals(880, domain.todayScore)
        assertEquals(1700000000000L, domain.createdAt)
        assertEquals(1700000010000L, domain.updatedAt)
    }

    @Test
    fun mapsFriendDtoWithNullScoreSafely() {
        val dto = FriendDto(
            friendshipId = "fs-2",
            userId = "usr-1",
            friendUserId = "usr-3",
            friendName = "Sam",
            status = "PENDING",
            todayScore = null,
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
        )

        val domain = dto.toDomain()

        assertNull(domain.todayScore)
        assertEquals(FriendStatus.PENDING, domain.status)
    }

    @Test
    fun mapsUnknownFriendStatusToFallbackPending() {
        val dto = FriendDto(
            friendshipId = "fs-3",
            userId = "usr-1",
            friendUserId = "usr-4",
            friendName = "UnknownStatusUser",
            status = "SOME_UNEXPECTED_STATUS",
            todayScore = null,
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
        )

        val domain = dto.toDomain()
        assertEquals(FriendStatus.PENDING, domain.status)
    }

    @Test
    fun mapsRivalDtoToDomainCorrectly() {
        val dto = RivalResponseDto(
            userId = "usr-1",
            rivalUserId = "usr-9",
            rivalName = "RivalPlayer",
            selectedAt = 1700000050000L,
            status = "ACTIVE",
        )

        val domain = dto.toDomain()

        assertEquals(UserId("usr-1"), domain.userId)
        assertEquals(UserId("usr-9"), domain.rivalUserId)
        assertEquals("RivalPlayer", domain.rivalName)
        assertEquals(1700000050000L, domain.selectedAt)
        assertEquals(RivalStatus.ACTIVE, domain.status)
    }

    @Test
    fun mapsUnknownRivalStatusToFallbackActive() {
        val dto = RivalResponseDto(
            userId = "usr-1",
            rivalUserId = "usr-9",
            rivalName = "RivalPlayer",
            selectedAt = 1700000050000L,
            status = "UNKNOWN_RIVAL_STATUS",
        )

        val domain = dto.toDomain()
        assertEquals(RivalStatus.ACTIVE, domain.status)
    }

    @Test
    fun mapsRivalScoreComparisonDtoToDomainCorrectly() {
        val dto = RivalScoreComparisonResponseDto(
            userScore = 905,
            rivalScore = 890,
            scoreGap = 15,
            battleId = "battle-2026-10-12",
            direction = "AHEAD",
        )

        val domain = dto.toDomain()

        assertEquals(905, domain.userScore)
        assertEquals(890, domain.rivalScore)
        assertEquals(15, domain.scoreGap)
        assertEquals(BattleId("battle-2026-10-12"), domain.battleId)
        assertEquals(ScoreDirection.AHEAD, domain.direction)
    }

    @Test
    fun mapsAllScoreDirectionsCorrectly() {
        val directions = listOf(
            "AHEAD" to ScoreDirection.AHEAD,
            "BEHIND" to ScoreDirection.BEHIND,
            "TIED" to ScoreDirection.TIED,
            "UNKNOWN" to ScoreDirection.TIED,
        )

        for ((inputStr, expectedEnum) in directions) {
            val dto = RivalScoreComparisonResponseDto(
                userScore = 800,
                rivalScore = 800,
                scoreGap = 0,
                battleId = "b-1",
                direction = inputStr,
            )
            assertEquals(expectedEnum, dto.toDomain().direction)
        }
    }
}

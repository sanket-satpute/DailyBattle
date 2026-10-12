package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FriendEntityMapperTest {

    @Test
    fun `toDomain maps all FriendEntity fields with todayScore accurately`() {
        val entity = FriendEntity(
            friendshipId = "f_1",
            userId = "user_me",
            friendUserId = "user_rahul",
            friendName = "Rahul",
            status = "ACCEPTED",
            todayScore = 914,
            createdAt = 1700000000000L,
            updatedAt = 1700000050000L,
        )

        val domain = entity.toDomain()

        assertEquals(FriendshipId("f_1"), domain.friendshipId)
        assertEquals(UserId("user_me"), domain.userId)
        assertEquals(UserId("user_rahul"), domain.friendUserId)
        assertEquals("Rahul", domain.friendName)
        assertEquals(FriendStatus.ACCEPTED, domain.status)
        assertEquals(914, domain.todayScore)
        assertEquals(1700000000000L, domain.createdAt)
        assertEquals(1700000050000L, domain.updatedAt)
    }

    @Test
    fun `toDomain handles null todayScore accurately`() {
        val entity = FriendEntity(
            friendshipId = "f_2",
            userId = "user_me",
            friendUserId = "user_priya",
            friendName = "Priya",
            status = "PENDING",
            todayScore = null,
            createdAt = 1700000100000L,
            updatedAt = 1700000100000L,
        )

        val domain = entity.toDomain()

        assertNull(domain.todayScore)
        assertEquals(FriendStatus.PENDING, domain.status)
    }

    @Test
    fun `toEntity maps all Friend domain fields accurately`() {
        val domain = Friend(
            friendshipId = FriendshipId("f_3"),
            userId = UserId("user_me"),
            friendUserId = UserId("user_blocked"),
            friendName = "BlockedUser",
            status = FriendStatus.BLOCKED,
            todayScore = null,
            createdAt = 1700000200000L,
            updatedAt = 1700000300000L,
        )

        val entity = domain.toEntity()

        assertEquals("f_3", entity.friendshipId)
        assertEquals("user_me", entity.userId)
        assertEquals("user_blocked", entity.friendUserId)
        assertEquals("BlockedUser", entity.friendName)
        assertEquals("BLOCKED", entity.status)
        assertNull(entity.todayScore)
        assertEquals(1700000200000L, entity.createdAt)
        assertEquals(1700000300000L, entity.updatedAt)
    }

    @Test
    fun `roundtrip preserves Friend model`() {
        val original = Friend(
            friendshipId = FriendshipId("f_roundtrip"),
            userId = UserId("user_a"),
            friendUserId = UserId("user_b"),
            friendName = "BestFriend",
            status = FriendStatus.ACCEPTED,
            todayScore = 880,
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toDomain gracefully falls back to PENDING on unknown status`() {
        val entity = FriendEntity(
            friendshipId = "f_corrupt",
            userId = "user_a",
            friendUserId = "user_b",
            friendName = "CorruptFriend",
            status = "UNKNOWN_STATUS",
            todayScore = null,
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
        )

        val domain = entity.toDomain()

        assertEquals(FriendStatus.PENDING, domain.status)
    }
}

package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.user.AccountStatus
import com.sanket_satpute_20.dailybattle.domain.user.User
import org.junit.Assert.assertEquals
import org.junit.Test

class UserEntityMapperTest {

    @Test
    fun `toDomain maps all UserEntity fields accurately`() {
        val entity = UserEntity(
            userId = "user_123",
            battleName = "ShadowStriker",
            createdAt = 1700000000000L,
            updatedAt = 1700000050000L,
            accountStatus = "ACTIVE",
        )

        val domain = entity.toDomain()

        assertEquals(UserId("user_123"), domain.userId)
        assertEquals("ShadowStriker", domain.battleName)
        assertEquals(1700000000000L, domain.createdAt)
        assertEquals(1700000050000L, domain.updatedAt)
        assertEquals(AccountStatus.ACTIVE, domain.accountStatus)
    }

    @Test
    fun `toEntity maps all User domain fields accurately`() {
        val domain = User(
            userId = UserId("user_456"),
            battleName = "EchoMaster",
            createdAt = 1700000100000L,
            updatedAt = 1700000200000L,
            accountStatus = AccountStatus.ONBOARDING,
        )

        val entity = domain.toEntity()

        assertEquals("user_456", entity.userId)
        assertEquals("EchoMaster", entity.battleName)
        assertEquals(1700000100000L, entity.createdAt)
        assertEquals(1700000200000L, entity.updatedAt)
        assertEquals("ONBOARDING", entity.accountStatus)
    }

    @Test
    fun `roundtrip conversion preserves User integrity`() {
        val original = User(
            userId = UserId("user_789"),
            battleName = "ApexRival",
            createdAt = 1700000300000L,
            updatedAt = 1700000400000L,
            accountStatus = AccountStatus.SUSPENDED,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }

    @Test
    fun `toDomain gracefully falls back to ACTIVE when accountStatus is unknown or corrupted`() {
        val entity = UserEntity(
            userId = "user_corrupted",
            battleName = "CorruptUser",
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
            accountStatus = "NON_EXISTENT_STATUS",
        )

        val domain = entity.toDomain()

        assertEquals(AccountStatus.ACTIVE, domain.accountStatus)
    }
}

package com.sanket_satpute_20.dailybattle.domain.user

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserTest {

    private fun createUser(
        userId: String = "user-1",
        battleName: String = "TestPlayer",
        createdAt: Long = 1000L,
        updatedAt: Long = 2000L,
        accountStatus: AccountStatus = AccountStatus.ACTIVE,
    ) = User(
        userId = UserId(userId),
        battleName = battleName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        accountStatus = accountStatus,
    )

    @Test
    fun `user holds all required fields from 08_DATA_AND_API section 6`() {
        val user = createUser()
        assertEquals(UserId("user-1"), user.userId)
        assertEquals("TestPlayer", user.battleName)
        assertEquals(1000L, user.createdAt)
        assertEquals(2000L, user.updatedAt)
        assertEquals(AccountStatus.ACTIVE, user.accountStatus)
    }

    @Test
    fun `user with different battle names are not equal`() {
        val user1 = createUser(battleName = "Alpha")
        val user2 = createUser(battleName = "Bravo")
        assertNotEquals(user1, user2)
    }

    @Test
    fun `user with different account statuses are not equal`() {
        val active = createUser(accountStatus = AccountStatus.ACTIVE)
        val onboarding = createUser(accountStatus = AccountStatus.ONBOARDING)
        assertNotEquals(active, onboarding)
    }

    @Test
    fun `user copy preserves unchanged fields`() {
        val original = createUser()
        val updated = original.copy(battleName = "NewName", updatedAt = 3000L)
        assertEquals(original.userId, updated.userId)
        assertEquals(original.createdAt, updated.createdAt)
        assertEquals(original.accountStatus, updated.accountStatus)
        assertEquals("NewName", updated.battleName)
        assertEquals(3000L, updated.updatedAt)
    }

    @Test
    fun `user equality is structural`() {
        val a = createUser()
        val b = createUser()
        assertEquals(a, b)
    }
}

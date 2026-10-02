package com.sanket_satpute_20.dailybattle.domain.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AccountStatusTest {

    @Test
    fun `account status contains exactly 4 MVP lifecycle values`() {
        val values = AccountStatus.entries
        assertEquals(4, values.size)
    }

    @Test
    fun `account status values match expected names`() {
        val names = AccountStatus.entries.map { it.name }
        assertEquals(
            listOf("ONBOARDING", "ACTIVE", "SUSPENDED", "DELETED"),
            names,
        )
    }

    @Test
    fun `different account statuses are not equal`() {
        assertNotEquals(AccountStatus.ACTIVE, AccountStatus.ONBOARDING)
        assertNotEquals(AccountStatus.ACTIVE, AccountStatus.SUSPENDED)
        assertNotEquals(AccountStatus.ACTIVE, AccountStatus.DELETED)
        assertNotEquals(AccountStatus.ONBOARDING, AccountStatus.SUSPENDED)
    }

    @Test
    fun `account status valueOf round trips`() {
        for (status in AccountStatus.entries) {
            assertEquals(status, AccountStatus.valueOf(status.name))
        }
    }
}

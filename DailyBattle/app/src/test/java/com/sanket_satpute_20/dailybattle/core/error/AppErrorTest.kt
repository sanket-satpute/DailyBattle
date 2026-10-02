package com.sanket_satpute_20.dailybattle.core.error

import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppErrorTest {
    @Test
    fun `offline and authorization stay distinct`() {
        assertNotEquals(AppError.Offline, AppError.Authorization)
    }

    @Test
    fun `timeout and unknown stay distinct`() {
        assertNotEquals(AppError.Timeout, AppError.Unknown)
    }

    @Test
    fun `domain and battle state stay distinct`() {
        assertNotEquals(AppError.Domain, AppError.BattleState)
    }
}

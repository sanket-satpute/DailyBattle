package com.sanket_satpute_20.dailybattle.presentation.state

import com.sanket_satpute_20.dailybattle.core.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ScreenStateTest {
    @Test
    fun `success preserves the supplied presentation value`() {
        val state: ScreenState<String> = ScreenState.Success("content")

        assertEquals("content", (state as ScreenState.Success).value)
    }

    @Test
    fun `terminal screen state variants are mutually exclusive`() {
        assertNotEquals(ScreenState.Empty, ScreenState.Error(AppError.Unknown))
    }

    @Test
    fun `error retains its structured category`() {
        val state: ScreenState<Nothing> = ScreenState.Error(AppError.Offline)

        assertEquals(AppError.Offline, (state as ScreenState.Error).error)
    }
}

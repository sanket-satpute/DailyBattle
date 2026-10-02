package com.sanket_satpute_20.dailybattle.design.radius

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pins every radius token to its locked value from `05_DESIGN_SYSTEM.md` sections 60/85/94. */
class DBRadiusTest {

    @Test
    fun `radius values match the approved system`() {
        assertEquals(8.dp, DBRadius.Small)
        assertEquals(12.dp, DBRadius.Medium)
        assertEquals(16.dp, DBRadius.Large)
        assertEquals(24.dp, DBRadius.XLarge)
        assertEquals(999.dp, DBRadius.Pill)
    }

    @Test
    fun `scale contains exactly 5 distinct values`() {
        val allValues = listOf(
            DBRadius.Small,
            DBRadius.Medium,
            DBRadius.Large,
            DBRadius.XLarge,
            DBRadius.Pill,
        )
        assertEquals(5, allValues.size)
        assertEquals(5, allValues.toSet().size)
    }
}

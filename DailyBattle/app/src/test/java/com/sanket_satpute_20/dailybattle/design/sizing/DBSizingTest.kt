package com.sanket_satpute_20.dailybattle.design.sizing

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pins sizing tokens to locked values from `05_DESIGN_SYSTEM.md` sections 18/22/63/86/94/95. */
class DBSizingTest {

    @Test
    fun `touch targets match section 63 and 94`() {
        assertEquals(44.dp, DBSizing.TouchMinimum)
        assertEquals(48.dp, DBSizing.TouchPreferred)
    }

    @Test
    fun `button dimensions match section 18 and 94`() {
        assertEquals(52.dp, DBSizing.ButtonHeight)
    }

    @Test
    fun `button radius matches section 95 decision strictly`() {
        // Section 95: button radius must be 14px. Not 12px, not 16px.
        assertEquals(14.dp, DBSizing.ButtonRadius)
    }

    @Test
    fun `input dimensions match section 22 and 94`() {
        assertEquals(52.dp, DBSizing.InputHeight)
    }

    @Test
    fun `screen margins match section 94`() {
        assertEquals(20.dp, DBSizing.ScreenMargin)
        assertEquals(24.dp, DBSizing.HeroMargin)
    }
}

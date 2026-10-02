package com.sanket_satpute_20.dailybattle.design.spacing

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pins every spacing token to its locked value from `05_DESIGN_SYSTEM.md` sections 5/84/94. */
class DBSpacingTest {

    @Test
    fun `full scale matches the approved 8px spacing system`() {
        assertEquals(4.dp, DBSpacing.XXS)
        assertEquals(8.dp, DBSpacing.XS)
        assertEquals(12.dp, DBSpacing.SM)
        assertEquals(16.dp, DBSpacing.MD)
        assertEquals(20.dp, DBSpacing.LG)
        assertEquals(24.dp, DBSpacing.XL)
        assertEquals(32.dp, DBSpacing.XXL)
        assertEquals(40.dp, DBSpacing.XXXL)
        assertEquals(48.dp, DBSpacing.Sp48)
        assertEquals(56.dp, DBSpacing.Sp56)
        assertEquals(64.dp, DBSpacing.Sp64)
    }

    @Test
    fun `ScreenMargin equals 20dp page margin`() {
        assertEquals(20.dp, DBSpacing.ScreenMargin)
        assertEquals(DBSpacing.LG, DBSpacing.ScreenMargin)
    }

    @Test
    fun `HeroMargin equals 24dp major hero margin`() {
        assertEquals(24.dp, DBSpacing.HeroMargin)
        assertEquals(DBSpacing.XL, DBSpacing.HeroMargin)
    }

    @Test
    fun `scale contains exactly 11 distinct values`() {
        val allValues = listOf(
            DBSpacing.XXS,
            DBSpacing.XS,
            DBSpacing.SM,
            DBSpacing.MD,
            DBSpacing.LG,
            DBSpacing.XL,
            DBSpacing.XXL,
            DBSpacing.XXXL,
            DBSpacing.Sp48,
            DBSpacing.Sp56,
            DBSpacing.Sp64,
        )
        assertEquals(11, allValues.size)
        assertEquals(11, allValues.toSet().size)
    }
}

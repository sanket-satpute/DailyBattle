package com.sanket_satpute_20.dailybattle.design.typography

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins every typography token to its locked value from `05_DESIGN_SYSTEM.md` section 93
 * (Typography — Source of Truth).
 *
 * Format per SOT entry: fontSize / lineHeight / weight.
 */
class DBTypographyTest {

    // ── Font family ─────────────────────────────────────────────────────

    @Test
    fun `InterFamily is non-null`() {
        assertNotNull(DBTypography.InterFamily)
    }

    // ── Display: 56 / 60 / 700 ─────────────────────────────────────────

    @Test
    fun `Display matches SOT 56 - 60 - 700`() {
        val style = DBTypography.Display
        assertEquals(56.sp, style.fontSize)
        assertEquals(60.sp, style.lineHeight)
        assertEquals(FontWeight.Bold, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── H1: 30 / 36 / 700 ──────────────────────────────────────────────

    @Test
    fun `H1 matches SOT 30 - 36 - 700`() {
        val style = DBTypography.H1
        assertEquals(30.sp, style.fontSize)
        assertEquals(36.sp, style.lineHeight)
        assertEquals(FontWeight.Bold, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── H2: 24 / 30 / 700 ──────────────────────────────────────────────

    @Test
    fun `H2 matches SOT 24 - 30 - 700`() {
        val style = DBTypography.H2
        assertEquals(24.sp, style.fontSize)
        assertEquals(30.sp, style.lineHeight)
        assertEquals(FontWeight.Bold, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── H3: 18 / 24 / 600 ──────────────────────────────────────────────

    @Test
    fun `H3 matches SOT 18 - 24 - 600`() {
        val style = DBTypography.H3
        assertEquals(18.sp, style.fontSize)
        assertEquals(24.sp, style.lineHeight)
        assertEquals(FontWeight.SemiBold, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── Body Large: 16 / 24 / 500 ──────────────────────────────────────

    @Test
    fun `BodyLarge matches SOT 16 - 24 - 500`() {
        val style = DBTypography.BodyLarge
        assertEquals(16.sp, style.fontSize)
        assertEquals(24.sp, style.lineHeight)
        assertEquals(FontWeight.Medium, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── Body: 14 / 20 / 500 ────────────────────────────────────────────

    @Test
    fun `Body matches SOT 14 - 20 - 500`() {
        val style = DBTypography.Body
        assertEquals(14.sp, style.fontSize)
        assertEquals(20.sp, style.lineHeight)
        assertEquals(FontWeight.Medium, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── Caption: 12 / 16 / 500 ─────────────────────────────────────────

    @Test
    fun `Caption matches SOT 12 - 16 - 500`() {
        val style = DBTypography.Caption
        assertEquals(12.sp, style.fontSize)
        assertEquals(16.sp, style.lineHeight)
        assertEquals(FontWeight.Medium, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── Label: 12 / 16 / 600 (lower bound of 11–12 range, per section 16 minimum) ──

    @Test
    fun `Label matches SOT 12 - 16 - 600`() {
        val style = DBTypography.Label
        assertEquals(12.sp, style.fontSize)
        assertEquals(16.sp, style.lineHeight)
        assertEquals(FontWeight.SemiBold, style.fontWeight)
        assertEquals(DBTypography.InterFamily, style.fontFamily)
    }

    // ── Minimum text size rule (section 16) ─────────────────────────────

    @Test
    fun `no scale token uses a font size below 12sp`() {
        val allStyles = listOf(
            DBTypography.Display,
            DBTypography.H1,
            DBTypography.H2,
            DBTypography.H3,
            DBTypography.BodyLarge,
            DBTypography.Body,
            DBTypography.Caption,
            DBTypography.Label,
        )
        allStyles.forEach { style ->
            assertTrue(
                "Font size ${style.fontSize} is below 12sp minimum",
                style.fontSize >= 12.sp,
            )
        }
    }

    // ── Tabular numerals ────────────────────────────────────────────────

    @Test
    fun `TabularNumerals enables tnum OpenType feature`() {
        assertEquals("tnum", DBTypography.TabularNumerals.fontFeatureSettings)
    }

    // ── All tokens use InterFamily ──────────────────────────────────────

    @Test
    fun `all scale tokens use InterFamily`() {
        val allStyles = listOf(
            DBTypography.Display,
            DBTypography.H1,
            DBTypography.H2,
            DBTypography.H3,
            DBTypography.BodyLarge,
            DBTypography.Body,
            DBTypography.Caption,
            DBTypography.Label,
        )
        allStyles.forEach { style ->
            assertEquals(
                "Expected InterFamily for ${style.fontSize}",
                DBTypography.InterFamily,
                style.fontFamily,
            )
        }
    }
}

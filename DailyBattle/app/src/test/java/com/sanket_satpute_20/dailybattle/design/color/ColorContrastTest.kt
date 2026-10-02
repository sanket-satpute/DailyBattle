package com.sanket_satpute_20.dailybattle.design.color

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Validates the text/surface combinations `11_ACCESSIBILITY.md` section 53 requires to be checked
 * for contrast, using the standard WCAG relative-luminance contrast formula.
 */
class ColorContrastTest {
    private fun channelLuminance(channel: Float): Double =
        if (channel <= 0.03928) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)

    private fun relativeLuminance(color: Color): Double =
        0.2126 * channelLuminance(color.red) +
            0.7152 * channelLuminance(color.green) +
            0.0722 * channelLuminance(color.blue)

    private fun contrastRatio(a: Color, b: Color): Double {
        val lighter = maxOf(relativeLuminance(a), relativeLuminance(b))
        val darker = minOf(relativeLuminance(a), relativeLuminance(b))
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun `primary and secondary text meet the normal-text AA threshold on every approved surface`() {
        val surfaces = listOf(
            DBColor.BackgroundPrimary,
            DBColor.Surface1,
            DBColor.Surface2,
            DBColor.SurfaceElevated,
        )

        for (surface in surfaces) {
            assertTrue(
                "TextPrimary on $surface",
                contrastRatio(DBColor.TextPrimary, surface) >= 4.5,
            )
            assertTrue(
                "TextSecondary on $surface",
                contrastRatio(DBColor.TextSecondary, surface) >= 4.5,
            )
        }
    }

    @Test
    fun `muted text meets only the large-text AA threshold against background`() {
        val ratio = contrastRatio(DBColor.TextMuted, DBColor.BackgroundPrimary)

        // Documented as "low-priority metadata" only; must not be used for important/normal text.
        assertTrue("TextMuted vs Background was $ratio", ratio >= 3.0)
    }
}

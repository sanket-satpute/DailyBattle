package com.sanket_satpute_20.dailybattle.design.typography

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.dailybattle.R

/**
 * Locked typography tokens from `05_DESIGN_SYSTEM.md` section 93 (Typography — Source of Truth),
 * sections 14-17 (Type Scale, Minimum Text Size, Numeric Typography), and
 * `00_MASTER_SPEC.md` section 19. Values must not be changed without a recorded design-system
 * change-control decision.
 *
 * Font/lineHeight/weight triplet format from the SOT:
 *   Display:    56 / 60 / 700
 *   H1:         30 / 36 / 700
 *   H2:         24 / 30 / 700
 *   H3:         18 / 24 / 600
 *   Body Large: 16 / 24 / 500
 *   Body:       14 / 20 / 500
 *   Caption:    12 / 16 / 500
 *   Label:      11–12 / 16 / 600  (implemented as 12 / 16 / 600 per section 16 minimum)
 *
 * Numeric typography (section 17): tabular numerals are enabled via [fontFeatureSettings].
 */
object DBTypography {

    /**
     * The Inter font family, loaded from bundled static TTF files for the three weights required
     * by the approved type scale: Medium (500), SemiBold (600), Bold (700).
     */
    val InterFamily = FontFamily(
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
    )

    // ── Scale tokens ────────────────────────────────────────────────────

    /** 56 / 60 / 700 — very large score / hero moments, major visual emphasis. */
    val Display = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 60.sp,
    )

    /** 30 / 36 / 700 — major screen headings, important titles. */
    val H1 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    )

    /** 24 / 30 / 700 — section headings, major content blocks. */
    val H2 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    )

    /** 18 / 24 / 600 — card headings, subsections, important row titles. */
    val H3 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )

    /** 16 / 24 / 500 — important supporting copy, large instructions. */
    val BodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

    /** 14 / 20 / 500 — standard supporting text, list content, secondary information. */
    val Body = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    /** 12 / 16 / 500 — metadata, supporting labels. */
    val Caption = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    /**
     * 12 / 16 / 600 — small UI labels.
     *
     * The SOT defines labels as "11–12 / 16 / 600". Section 16 (Minimum Text Size) mandates
     * that important UI text should not be below 12 px. Implemented at 12 sp, the safe lower
     * bound.
     */
    val Label = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    // ── Numeric variant ─────────────────────────────────────────────────

    /**
     * OpenType feature `tnum` (tabular numerals) ensures digits occupy equal widths, so columns
     * of numbers (scores, percentiles, personal bests) align cleanly. Apply by merging with any
     * scale token: `DBTypography.Display.merge(DBTypography.TabularNumerals)`.
     *
     * See `05_DESIGN_SYSTEM.md` section 17.
     */
    val TabularNumerals = TextStyle(
        fontFeatureSettings = "tnum",
    )
}

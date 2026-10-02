package com.sanket_satpute_20.dailybattle.design.spacing

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Locked spacing tokens from `05_DESIGN_SYSTEM.md` section 5 (Grid / Spacing System),
 * section 84 (Spacing Tokens), and section 94 (Core Dimensions — Source of Truth).
 * Values must not be changed without a recorded design-system change-control decision.
 *
 * Daily Battle uses an 8 px spacing system with the following approved values:
 * 4, 8, 12, 16, 20, 24, 32, 40, 48, 56, 64.
 *
 * Section 108 (Token Traceability) recommends semantic aliases such as `DBSpacing.XS`,
 * `DBSpacing.SM`, etc. Both the full numeric scale and the semantic aliases are provided
 * so that consumers may choose whichever best communicates intent.
 */
object DBSpacing {

    // ── Full approved scale ─────────────────────────────────────────────

    /** 4 dp — minimal internal spacing, icon gaps. */
    val XXS: Dp = 4.dp

    /** 8 dp — tight internal spacing, small gaps. */
    val XS: Dp = 8.dp

    /** 12 dp — compact spacing. */
    val SM: Dp = 12.dp

    /** 16 dp — standard content spacing (primary). */
    val MD: Dp = 16.dp

    /** 20 dp — page margin, button horizontal padding lower bound. */
    val LG: Dp = 20.dp

    /** 24 dp — major hero margin, section gaps. */
    val XL: Dp = 24.dp

    /** 32 dp — large section spacing. */
    val XXL: Dp = 32.dp

    /** 40 dp — major section divider. */
    val XXXL: Dp = 40.dp

    /** 48 dp. */
    val Sp48: Dp = 48.dp

    /** 56 dp. */
    val Sp56: Dp = 56.dp

    /** 64 dp — largest approved spacing. */
    val Sp64: Dp = 64.dp

    // ── Semantic aliases (section 94 Core Dimensions SOT) ───────────────

    /** Primary horizontal screen margin — 20 dp. Same as [LG]. */
    val ScreenMargin: Dp = LG

    /** Major hero section margin — 24 dp. Same as [XL]. */
    val HeroMargin: Dp = XL
}

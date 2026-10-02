package com.sanket_satpute_20.dailybattle.design.sizing

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Locked sizing tokens from `05_DESIGN_SYSTEM.md` section 86 (Sizing Tokens),
 * section 94 (Core Dimensions — Source of Truth), section 95 (Button Radius Decision),
 * section 63 (Touch Targets), and sections 18/22 (Button/Input Systems).
 * Values must not be changed without a recorded design-system change-control decision.
 *
 * These tokens define fixed component dimensions and interaction constraints,
 * as distinct from the variable [DBSpacing] scale and the [DBRadius] corner system.
 */
object DBSizing {

    // ── Touch targets (section 63, section 94) ──────────────────────────

    /** Minimum interactive touch target — 44 dp. */
    val TouchMinimum: Dp = 44.dp

    /** Preferred interactive touch target — 48 dp. */
    val TouchPreferred: Dp = 48.dp

    // ── Button (sections 18, 94) ────────────────────────────────────────

    /** Primary button height — 52 dp. */
    val ButtonHeight: Dp = 52.dp

    /**
     * Button corner radius — 14 dp.
     *
     * Per section 95 (Button Radius Decision): the Master UI/UX specification defines buttons
     * at 14 px radius. This is an explicit implementation token and must NOT be silently
     * replaced with 12 or 16.
     */
    val ButtonRadius: Dp = 14.dp

    // ── Input (sections 22, 94) ─────────────────────────────────────────

    /** Standard input height — 52 dp. */
    val InputHeight: Dp = 52.dp

    // ── Screen (section 94) ─────────────────────────────────────────────

    /** Primary horizontal screen margin — 20 dp. Mirrors [DBSpacing.ScreenMargin]. */
    val ScreenMargin: Dp = 20.dp

    /** Major hero section margin — 24 dp. Mirrors [DBSpacing.HeroMargin]. */
    val HeroMargin: Dp = 24.dp
}

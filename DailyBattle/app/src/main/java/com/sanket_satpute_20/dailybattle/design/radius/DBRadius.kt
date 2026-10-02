package com.sanket_satpute_20.dailybattle.design.radius

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Locked radius tokens from `05_DESIGN_SYSTEM.md` section 60 (Radius System),
 * section 85 (Radius Tokens), and section 94 (Core Dimensions — Source of Truth).
 * Values must not be changed without a recorded design-system change-control decision.
 *
 * Approved radius values: 8, 12, 16, 24, 999.
 *
 * Naming follows section 108 (Token Traceability): `DBRadius.Small`, `Medium`, `Large`,
 * `XLarge`, `Pill`.
 */
object DBRadius {

    /** 8 dp — small controls. */
    val Small: Dp = 8.dp

    /** 12 dp — inputs, small cards. */
    val Medium: Dp = 12.dp

    /** 16 dp — major cards. */
    val Large: Dp = 16.dp

    /** 24 dp — major visual containers. */
    val XLarge: Dp = 24.dp

    /** 999 dp — pills / chips (fully rounded). */
    val Pill: Dp = 999.dp
}

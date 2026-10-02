# Sprint 3.3 — Spacing / Radius / Sizing scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md` (Phase 3, Sprint 3.3), `docs/05_DESIGN_SYSTEM.md` sections
5/84 (Spacing), 60/85 (Radius), 18/22/63/86 (Sizing), 94 (Core Dimensions SOT), and 95 (Button Radius).

## Token Separation Strategy

Instead of bundling spacing, radius, and sizing into a single massive `DBDimensions` object,
they were split into semantic files following the token structure defined in section 84-86:
- `design/spacing/DBSpacing.kt` (Scale values + page margins)
- `design/radius/DBRadius.kt` (Corner constraints)
- `design/sizing/DBSizing.kt` (Fixed component heights/dimensions)

This provides better autocomplete context and follows the pattern established by `DBColor` and
`DBTypography`.

## Button Radius Decision

Section 94 states "See approved component implementation token" for Button radius.
Section 95 (Button Radius Decision) explicitly locks the button radius to 14px as defined
in the Master UX spec, prohibiting developers from silently replacing it with the 12px or
16px radius tokens.

This is implemented strictly as `DBSizing.ButtonRadius = 14.dp` and a unit test was
written specifically to pin this deviation from the standard radius scale.

## Scale Completeness

All 11 approved 8px spacing values (from 4dp to 64dp) were implemented.
All 5 approved radius values (from 8dp to 999dp) were implemented.

## Semantic Aliases

Per Section 108 (Token Traceability), semantic names were provided for the standard
spacing values (XXS through XXXL). Explicit aliases were also created for `ScreenMargin`
and `HeroMargin` mirroring the layout section.

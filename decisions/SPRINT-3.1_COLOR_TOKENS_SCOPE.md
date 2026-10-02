# Sprint 3.1 — Color tokens scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md` (Phase 3, Sprint 3.1), `docs/05_DESIGN_SYSTEM.md` sections
6-11 (Color System) and 91/92 (locked source of truth), `docs/11_ACCESSIBILITY.md` section 53
(Contrast), `docs/15_ANTIGRAVITY_RULES.md`/`05_DESIGN_SYSTEM.md` section 113 (no light theme in
MVP, no invented colors).

## Token values

`design/color/DBColor.kt` reproduces every locked hex value from sections 91/92 exactly, using the
`DBColor.*` naming recommended in section 108 (Token Traceability). No color was invented, adjusted,
or approximated.

## Required supporting fix: theme wiring

The pre-existing `ui/theme/Theme.kt` (Android Studio Compose template) had a `lightColorScheme`, a
`dynamicColor` branch (Material You, Android 12+), and unused placeholder swatches
(`Purple80`/`PurpleGrey80`/`Pink80`/`Purple40`/`PurpleGrey40`/`Pink40` in `ui/theme/Color.kt`). All
three directly contradict section 113's explicit MVP constraints ("do not introduce a light theme
in MVP") and the "Dark + restrained + premium" color philosophy (section 8.1): dynamic color lets the
OS replace the locked brand palette with a wallpaper-derived color, and the light scheme is an
unapproved, unused second theme. Leaving them in place would mean the app does not actually render
with the approved color system — directly contradicting this sprint's objective ("turn the approved
design system into reusable Android components"). They were removed; `DailyBattleTheme` now always
applies one dark `ColorScheme` built from `DBColor`, and the placeholder swatch file was deleted
(no code referenced it outside the theme files, confirmed by a full-source search).

## Material3 `ColorScheme` role mapping — explicitly limited

Only the roles with an unambiguous 1:1 locked-token correspondence were overridden:
`background`, `onBackground`, `surface`, `onSurface`, `surfaceVariant`, `primary`, `error`. Material3
has many more roles (`onPrimary`, `onError`, `secondary`, `tertiary`, `outline`, container variants,
etc.) that `05_DESIGN_SYSTEM.md` does not define a token for yet. These keep Material3's baseline
dark-theme defaults until a future design-system/component sprint defines them; this is a known,
documented gap, not an invented color.

## Accessibility verification

`ColorContrastTest` computes the standard WCAG relative-luminance contrast ratio (no custom formula
invented) for the exact combinations `11_ACCESSIBILITY.md` section 53 calls out:
`TextPrimary`/`TextSecondary` against `BackgroundPrimary`/`Surface1`/`Surface2`/`SurfaceElevated` (all
≥ 4.5:1, the standard AA normal-text threshold) and `TextMuted` against `BackgroundPrimary` (≥ 3:1,
AA large-text threshold only, consistent with its "low-priority metadata" usage restriction).
`TextDisabled` was not asserted against a threshold: WCAG 1.4.3 exempts inactive/disabled controls
from contrast minimums, matching this project's own restriction that the disabled token is "only for
genuinely disabled content."

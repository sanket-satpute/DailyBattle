# Sprint 3.2 — Typography scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md` (Phase 3, Sprint 3.2), `docs/05_DESIGN_SYSTEM.md` sections
14-17 (Typography, Type Scale, Minimum Text Size, Numeric Typography) and section 93 (locked source
of truth), `docs/00_MASTER_SPEC.md` section 19, `docs/15_ANTIGRAVITY_RULES.md` rule 33,
`docs/11_ACCESSIBILITY.md` section 52 (Typography Accessibility).

## Token values

`design/typography/DBTypography.kt` reproduces every locked typography triplet
(fontSize/lineHeight/weight) from section 93 exactly, using the `DBTypography.*` naming
consistent with the `DBColor.*` pattern established in Sprint 3.1 and recommended in section 108
(Token Traceability). No scale level was invented, adjusted, or approximated.

## Label font size resolution

The SOT defines Label as "11–12 / 16 / 600". Section 16 (Minimum Text Size) mandates "minimum
important UI text: 12px." Implemented at 12 sp — the safe lower bound that satisfies both the
SOT range and the minimum size rule.

## Inter font bundling

Inter is bundled as three static TTF files (`inter_medium.ttf`, `inter_semibold.ttf`,
`inter_bold.ttf`) in `res/font/`, covering the three weights required by the approved type scale:
Medium (500), SemiBold (600), Bold (700). Downloaded from the official Google Fonts GitHub
repository. No additional typefaces were introduced (rule 33, section 14).

## Tabular numerals

Section 17 requires tabular numerals for score values. Provided as a composable `TextStyle`
modifier (`DBTypography.TabularNumerals`) with the OpenType `tnum` feature flag, which can be
merged with any scale token via `TextStyle.merge()`.

## Material3 Typography wiring

`ui/theme/Type.kt` maps each DB design-system token to the closest-matching Material3 `Typography`
role. All 15 M3 slots are populated with Inter-based styles so no M3 component falls back to the
default Roboto. This is a supporting change required to make the theme actually render the approved
typography. The Theme.kt file was not modified; it already references the `Typography` val.

## No new dependencies

No new library dependencies were added. The font files are bundled as Android resources and
referenced via `R.font.*` and `Font()` / `FontFamily()` from the Compose UI text API already
present in the project.

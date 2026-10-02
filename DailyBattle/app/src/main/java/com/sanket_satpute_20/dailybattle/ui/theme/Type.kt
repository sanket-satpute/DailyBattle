package com.sanket_satpute_20.dailybattle.ui.theme

import androidx.compose.material3.Typography
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Material3 [Typography] wired to [DBTypography] tokens.
 *
 * The mapping follows the closest semantic match between the Daily Battle design system and
 * Material3 type roles:
 *
 * | Design system | M3 role         |
 * |---------------|-----------------|
 * | Display       | displayLarge    |
 * | H1            | headlineLarge   |
 * | H2            | headlineMedium  |
 * | H3            | titleLarge      |
 * | Body Large    | bodyLarge       |
 * | Body          | bodyMedium      |
 * | Caption       | bodySmall       |
 * | Label         | labelSmall      |
 *
 * Additional M3 roles that don't have a 1:1 Daily Battle counterpart are set to the nearest
 * sensible default using Inter to prevent the font from falling back to Roboto in M3 components.
 */
val Typography = Typography(
    displayLarge = DBTypography.Display,
    displayMedium = DBTypography.H1,
    displaySmall = DBTypography.H2,
    headlineLarge = DBTypography.H1,
    headlineMedium = DBTypography.H2,
    headlineSmall = DBTypography.H3,
    titleLarge = DBTypography.H3,
    titleMedium = DBTypography.BodyLarge,
    titleSmall = DBTypography.Body,
    bodyLarge = DBTypography.BodyLarge,
    bodyMedium = DBTypography.Body,
    bodySmall = DBTypography.Caption,
    labelLarge = DBTypography.BodyLarge,
    labelMedium = DBTypography.Caption,
    labelSmall = DBTypography.Label,
)
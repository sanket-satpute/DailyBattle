package com.sanket_satpute_20.dailybattle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.sanket_satpute_20.dailybattle.design.color.DBColor

/**
 * The approved design system has no light theme or dynamic (wallpaper-derived) color in the MVP;
 * only the locked dark palette is used. Color roles not yet covered by a design-system token keep
 * Material3's baseline dark defaults until a future design-system sprint defines them.
 */
private val DarkColorScheme = darkColorScheme(
    background = DBColor.BackgroundPrimary,
    onBackground = DBColor.TextPrimary,
    surface = DBColor.Surface1,
    onSurface = DBColor.TextPrimary,
    surfaceVariant = DBColor.Surface2,
    primary = DBColor.BrandPrimary,
    error = DBColor.Error,
)

@Composable
fun DailyBattleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
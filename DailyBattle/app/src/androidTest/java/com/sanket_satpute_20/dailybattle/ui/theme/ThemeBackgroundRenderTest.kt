package com.sanket_satpute_20.dailybattle.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders the real theme and reads back an actual pixel, proving `colorScheme.background`
 * resolves to the locked token at runtime rather than only asserting the constant in isolation.
 */
@RunWith(AndroidJUnit4::class)
class ThemeBackgroundRenderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun colorSchemeBackgroundRendersTheLockedBackgroundToken() {
        composeRule.setContent {
            DailyBattleTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                )
            }
        }

        val pixel = composeRule.onRoot()
            .captureToImage()
            .asAndroidBitmap()
            .getPixel(0, 0)

        assertEquals(DBColor.BackgroundPrimary.toArgb(), pixel)
    }
}

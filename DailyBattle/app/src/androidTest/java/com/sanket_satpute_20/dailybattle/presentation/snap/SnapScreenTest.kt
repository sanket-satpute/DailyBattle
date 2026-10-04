package com.sanket_satpute_20.dailybattle.presentation.snap

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SnapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun snapScreen_rendersScaffolding() {
        composeTestRule.setContent {
            SnapScreen(
                uiState = SnapUiState(
                    progressText = "1 / ?",
                    timerText = "--:--",
                    scoreText = "Score --"
                ),
                onElementTap = {}
            )
        }

        // Verify the title
        composeTestRule.onNodeWithText("SNAP").assertIsDisplayed()
        
        // Verify the progress
        composeTestRule.onNodeWithText("1 / ?").assertIsDisplayed()
        
        // Verify the timer
        composeTestRule.onNodeWithText("--:--").assertIsDisplayed()
        
        // Verify the placeholder for game area exists
        composeTestRule.onNodeWithText("GAME AREA\n(Pending DEC-GAME-001)").assertIsDisplayed()
        
        // Verify the bottom instructions and score
        composeTestRule.onNodeWithText("TAP TARGET").assertIsDisplayed()
        composeTestRule.onNodeWithText("Score --").assertIsDisplayed()
    }
}

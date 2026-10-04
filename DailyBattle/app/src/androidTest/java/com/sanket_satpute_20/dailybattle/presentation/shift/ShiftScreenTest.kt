package com.sanket_satpute_20.dailybattle.presentation.shift

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class ShiftScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_shift_screen_renders_all_required_elements() {
        composeTestRule.setContent {
            ShiftScreen(
                uiState = ShiftUiState(
                    progressText = "2 / 3",
                    timerText = "00:15",
                    instructionText = "WHAT MOVED?",
                    answerOptions = listOf("Card A", "Card B")
                ),
                onAnswerSelected = {},
                onComplete = {}
            )
        }

        // Title and Progress
        composeTestRule.onNodeWithText("SHIFT").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 / 3").assertIsDisplayed()

        // Timer
        composeTestRule.onNodeWithText("00:15").assertIsDisplayed()

        // Instruction
        composeTestRule.onNodeWithText("WHAT MOVED?").assertIsDisplayed()

        // Answer options
        composeTestRule.onNodeWithText("Card A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Card B").assertIsDisplayed()
    }

    @Test
    fun verify_answer_selection_triggers_callback() {
        var selectedAnswer: String? = null
        
        composeTestRule.setContent {
            ShiftScreen(
                uiState = ShiftUiState(
                    answerOptions = listOf("Card A", "Card B")
                ),
                onAnswerSelected = { selectedAnswer = it },
                onComplete = {}
            )
        }

        composeTestRule.onNodeWithText("Card B").performClick()
        
        assertTrue(selectedAnswer == "Card B")
    }
}

package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-002 (Battle Name Screen)
 * Verifies title, explanatory copy, input validation errors, and continue CTA per `docs/04_SCREEN_BLUEPRINTS.md` §10.
 */
@RunWith(AndroidJUnit4::class)
class BattleNameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_battle_name_screen_interactions() {
        var submitClicked = false

        composeTestRule.setContent {
            BattleNameScreen(
                state = BattleNameState(
                    name = "Sanket",
                    isValid = true,
                    isError = false,
                    errorMessage = null,
                    isSubmitting = false,
                    isSuccess = false
                ),
                onNameChanged = {},
                onSubmit = { submitClicked = true }
            )
        }

        // Verify title & copy
        composeTestRule.onNodeWithText("YOUR BATTLE\nNAME").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose the name your\nfriends will see.").assertIsDisplayed()

        // Verify Continue button exists and works
        val button = composeTestRule.onNodeWithText("CONTINUE")
        button.assertIsDisplayed()
        button.performClick()
        assertTrue("Continue button click callback was not invoked", submitClicked)
    }

    @Test
    fun verify_battle_name_screen_validation_error() {
        composeTestRule.setContent {
            BattleNameScreen(
                state = BattleNameState(
                    name = "",
                    isValid = false,
                    isError = true,
                    errorMessage = "Battle Name cannot be empty.",
                    isSubmitting = false,
                    isSuccess = false
                ),
                onNameChanged = {},
                onSubmit = {}
            )
        }

        composeTestRule.onNodeWithText("Battle Name cannot be empty.").assertIsDisplayed()
    }
}

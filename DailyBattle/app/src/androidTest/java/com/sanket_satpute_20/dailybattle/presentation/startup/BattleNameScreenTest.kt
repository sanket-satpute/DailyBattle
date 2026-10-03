package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class BattleNameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_battle_name_screen_interactions() {
        var typedName = ""
        var submitClicked = false

        composeTestRule.setContent {
            BattleNameScreen(
                state = BattleNameState(
                    name = typedName,
                    isValid = typedName.length in 1..20,
                    isError = typedName.isEmpty(),
                    errorMessage = if (typedName.isEmpty()) "Battle Name cannot be empty." else null,
                    isSubmitting = false,
                    isSuccess = false
                ),
                onNameChanged = { typedName = it },
                onSubmit = { submitClicked = true }
            )
        }

        // Verify title exists
        composeTestRule.onNodeWithText("YOUR BATTLE\nNAME").assertExists()

        // Verify Continue button exists
        val button = composeTestRule.onNodeWithText("CONTINUE")
        button.assertExists()
        
        button.performClick()
        
        assertTrue("Continue button click callback was not invoked", submitClicked)
    }
}

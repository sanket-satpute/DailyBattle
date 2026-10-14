package com.sanket_satpute_20.dailybattle.presentation.addfriend

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.design.components.DBInputState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-011 (Add Friend Screen)
 * Verifies code entry, copy user code, submission, and validation states per `docs/04_SCREEN_BLUEPRINTS.md` §19.
 */
@RunWith(AndroidJUnit4::class)
class AddFriendScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_add_friend_screen_renders_initial_state() {
        composeTestRule.setContent {
            AddFriendScreen(
                userCode = "K4X8M9",
                enteredCode = "",
                inputState = DBInputState.Default,
                feedbackMessage = null,
                submitState = AddFriendSubmitState.Idle,
                onCodeChanged = {},
                onSubmit = {},
                onSuccessAcknowledged = {},
                onNavigateBack = {}
            )
        }

        // Top Bar
        composeTestRule.onNodeWithText("ADD A FRIEND").assertIsDisplayed()

        // Own code display
        composeTestRule.onNodeWithText("YOUR BATTLE CODE").assertIsDisplayed()
        composeTestRule.onNodeWithText("K4X8M9").assertIsDisplayed()
        composeTestRule.onNodeWithText("COPY").assertIsDisplayed()

        // Submit button is disabled when empty
        composeTestRule.onNodeWithText("ADD FRIEND").assertIsNotEnabled()
    }

    @Test
    fun verify_add_friend_screen_code_entry_and_submit() {
        var submitClicked = false

        composeTestRule.setContent {
            AddFriendScreen(
                userCode = "K4X8M9",
                enteredCode = "B7K9P2",
                inputState = DBInputState.Valid,
                feedbackMessage = null,
                submitState = AddFriendSubmitState.Idle,
                onCodeChanged = {},
                onSubmit = { submitClicked = true },
                onSuccessAcknowledged = {},
                onNavigateBack = {}
            )
        }

        // Button should be enabled for valid 6-char code
        val button = composeTestRule.onNodeWithText("ADD FRIEND")
        button.assertIsEnabled()
        button.performClick()
        assertTrue("Submit callback was not invoked", submitClicked)
    }

    @Test
    fun verify_add_friend_screen_error_and_invalid_state() {
        composeTestRule.setContent {
            AddFriendScreen(
                userCode = "K4X8M9",
                enteredCode = "INV",
                inputState = DBInputState.Invalid,
                feedbackMessage = "Battle Code must be 6 alphanumeric characters.",
                submitState = AddFriendSubmitState.Error("Battle Code must be 6 alphanumeric characters."),
                onCodeChanged = {},
                onSubmit = {},
                onSuccessAcknowledged = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Battle Code must be 6 alphanumeric characters.").assertIsDisplayed()
        composeTestRule.onNodeWithText("ADD FRIEND").assertIsNotEnabled()
    }

    @Test
    fun verify_add_friend_screen_success_state() {
        composeTestRule.setContent {
            AddFriendScreen(
                userCode = "K4X8M9",
                enteredCode = "B7K9P2",
                inputState = DBInputState.Valid,
                feedbackMessage = null,
                submitState = AddFriendSubmitState.Success,
                onCodeChanged = {},
                onSubmit = {},
                onSuccessAcknowledged = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Friend request sent successfully!").assertIsDisplayed()
    }

    @Test
    fun verify_add_friend_screen_back_click() {
        var backClicked = false

        composeTestRule.setContent {
            AddFriendScreen(
                userCode = "K4X8M9",
                enteredCode = "",
                inputState = DBInputState.Default,
                feedbackMessage = null,
                submitState = AddFriendSubmitState.Idle,
                onCodeChanged = {},
                onSubmit = {},
                onSuccessAcknowledged = {},
                onNavigateBack = { backClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        assertTrue("Back callback was not invoked", backClicked)
    }
}

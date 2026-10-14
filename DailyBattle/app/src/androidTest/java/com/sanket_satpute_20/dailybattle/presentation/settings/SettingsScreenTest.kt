package com.sanket_satpute_20.dailybattle.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-014 (Settings Screen)
 * Verifies preference toggles, account management, destructive delete flow, and dialogs per `docs/04_SCREEN_BLUEPRINTS.md` §22.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_settings_screen_renders_all_groups_and_toggles() {
        var soundToggled = false
        var hapticsToggled = false
        var notificationsToggled = false

        val state = SettingsUiState(
            soundEnabled = true,
            hapticsEnabled = false,
            notificationsEnabled = true,
            battleName = "Sanket",
            battleCode = "K4X8M9"
        )

        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onNavigateBack = {},
                onToggleSound = { soundToggled = true },
                onToggleHaptics = { hapticsToggled = true },
                onToggleNotifications = { notificationsToggled = true },
                onOpenDetailDialog = {},
                onCloseDetailDialog = {},
                onEditBattleNameChange = {},
                onSaveBattleName = {},
                onShowDeleteConfirmation = {},
                onDismissDeleteConfirmation = {},
                onConfirmDeleteAccount = {},
                onClearError = {}
            )
        }

        // Top bar
        composeTestRule.onNodeWithText("SETTINGS").assertIsDisplayed()

        // GAME Group
        composeTestRule.onNodeWithText("GAME").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sound").assertIsDisplayed()
        composeTestRule.onNodeWithText("Haptics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()

        // ACCOUNT Group
        composeTestRule.onNodeWithText("ACCOUNT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battle Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sanket").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battle Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("K4X8M9").assertIsDisplayed()

        // PRIVACY Group
        composeTestRule.onNodeWithText("PRIVACY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").assertIsDisplayed()

        // ABOUT Group
        composeTestRule.onNodeWithText("ABOUT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Terms").assertIsDisplayed()
        composeTestRule.onNodeWithText("About Daily Battle").assertIsDisplayed()

        // Test toggle clicks
        composeTestRule.onNodeWithText("Sound").performClick()
        assertTrue("Sound toggle callback was not invoked", soundToggled)

        composeTestRule.onNodeWithText("Haptics").performClick()
        assertTrue("Haptics toggle callback was not invoked", hapticsToggled)

        composeTestRule.onNodeWithText("Notifications").performClick()
        assertTrue("Notifications toggle callback was not invoked", notificationsToggled)
    }

    @Test
    fun verify_settings_screen_error_feedback_and_dismiss() {
        var errorCleared = false

        val state = SettingsUiState(
            error = "Failed to update preference."
        )

        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onNavigateBack = {},
                onToggleSound = {},
                onToggleHaptics = {},
                onToggleNotifications = {},
                onOpenDetailDialog = {},
                onCloseDetailDialog = {},
                onEditBattleNameChange = {},
                onSaveBattleName = {},
                onShowDeleteConfirmation = {},
                onDismissDeleteConfirmation = {},
                onConfirmDeleteAccount = {},
                onClearError = { errorCleared = true }
            )
        }

        composeTestRule.onNodeWithText("Failed to update preference.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to update preference.").performClick()
        assertTrue("Error clear callback was not invoked", errorCleared)
    }

    @Test
    fun verify_settings_screen_delete_account_dialog_flow() {
        var deleteConfirmed = false
        var deleteDismissed = false

        val state = SettingsUiState(
            showDeleteConfirmDialog = true
        )

        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onNavigateBack = {},
                onToggleSound = {},
                onToggleHaptics = {},
                onToggleNotifications = {},
                onOpenDetailDialog = {},
                onCloseDetailDialog = {},
                onEditBattleNameChange = {},
                onSaveBattleName = {},
                onShowDeleteConfirmation = {},
                onDismissDeleteConfirmation = { deleteDismissed = true },
                onConfirmDeleteAccount = { deleteConfirmed = true },
                onClearError = {}
            )
        }

        // Dialog title & warning
        composeTestRule.onNodeWithText("DELETE ACCOUNT").assertIsDisplayed()
        composeTestRule.onNodeWithText("This will permanently delete your account, your Battle Name, and all gameplay history. This action cannot be undone.").assertIsDisplayed()

        // Cancel
        composeTestRule.onNodeWithText("CANCEL").performClick()
        assertTrue("Dismiss delete callback was not invoked", deleteDismissed)

        // Confirm
        composeTestRule.onNodeWithText("CONFIRM DELETE").performClick()
        assertTrue("Confirm delete callback was not invoked", deleteConfirmed)
    }

    @Test
    fun verify_settings_screen_edit_battle_name_dialog_flow() {
        var saveClicked = false
        var closeClicked = false

        val state = SettingsUiState(
            activeDetailDialog = SettingsDetailDialog.EDIT_BATTLE_NAME,
            editBattleNameInput = "NewName"
        )

        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onNavigateBack = {},
                onToggleSound = {},
                onToggleHaptics = {},
                onToggleNotifications = {},
                onOpenDetailDialog = {},
                onCloseDetailDialog = { closeClicked = true },
                onEditBattleNameChange = {},
                onSaveBattleName = { saveClicked = true },
                onShowDeleteConfirmation = {},
                onDismissDeleteConfirmation = {},
                onConfirmDeleteAccount = {},
                onClearError = {}
            )
        }

        composeTestRule.onNodeWithText("EDIT BATTLE NAME").assertIsDisplayed()
        composeTestRule.onNodeWithText("1–20 characters. This identity is visible to rivals and friends.").assertIsDisplayed()
        composeTestRule.onNodeWithText("NewName").assertIsDisplayed()

        composeTestRule.onNodeWithText("SAVE").performClick()
        assertTrue("Save battle name callback was not invoked", saveClicked)

        composeTestRule.onNodeWithText("CANCEL").performClick()
        assertTrue("Close detail dialog callback was not invoked", closeClicked)
    }

    @Test
    fun verify_settings_screen_navigate_back() {
        var backClicked = false

        composeTestRule.setContent {
            SettingsScreen(
                uiState = SettingsUiState(),
                onNavigateBack = { backClicked = true },
                onToggleSound = {},
                onToggleHaptics = {},
                onToggleNotifications = {},
                onOpenDetailDialog = {},
                onCloseDetailDialog = {},
                onEditBattleNameChange = {},
                onSaveBattleName = {},
                onShowDeleteConfirmation = {},
                onDismissDeleteConfirmation = {},
                onConfirmDeleteAccount = {},
                onClearError = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        assertTrue("Back callback was not invoked", backClicked)
    }
}

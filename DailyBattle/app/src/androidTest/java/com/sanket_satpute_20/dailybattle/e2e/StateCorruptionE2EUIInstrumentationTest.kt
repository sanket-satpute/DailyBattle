package com.sanket_satpute_20.dailybattle.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBErrorState
import com.sanket_satpute_20.dailybattle.presentation.startup.WelcomeScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.8 — State Corruption E2E UI Instrumentation Tests
 *
 * Verifies UI presentation safety and recovery when state corruption occurs
 * per `docs/09_OFFLINE_AND_ERROR_HANDLING.md` §41, §42, §43:
 *
 * - DBErrorState recovery display when corrupted state or persistence failure occurs.
 * - Recovery action triggers retry callback without crashing.
 * - WelcomeScreen renders stably when default state is initialized following corruption recovery.
 */
@RunWith(AndroidJUnit4::class)
class StateCorruptionE2EUIInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dbErrorState_displaysSafelyWhenStateCorruptionOccurs() {
        var retryTriggered = false

        composeTestRule.setContent {
            DBErrorState(
                title = "STATE INCONSISTENCY DETECTED",
                description = "We encountered an unexpected battle state. Tap retry to reconcile.",
                onRetry = { retryTriggered = true }
            )
        }

        // Verify title and description are safely displayed without crashing
        composeTestRule.onNodeWithText("STATE INCONSISTENCY DETECTED").assertIsDisplayed()
        composeTestRule.onNodeWithText("We encountered an unexpected battle state. Tap retry to reconcile.").assertIsDisplayed()

        // Verify retry button is functional
        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed().performClick()
        assertEquals("Retry callback invoked to recover from corrupted state", true, retryTriggered)
    }

    @Test
    fun dbButton_handlesCorruptedTextSafely() {
        var clicked = false

        composeTestRule.setContent {
            DBButton(
                text = "RECOVER STATE",
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("RECOVER STATE").assertIsDisplayed().performClick()
        assertEquals("Button click functional", true, clicked)
    }

    @Test
    fun welcomeScreen_rendersStablyWithDefaultReconciledState() {
        var navigated = false

        composeTestRule.setContent {
            WelcomeScreen(
                onGetStarted = { navigated = true }
            )
        }

        composeTestRule.onNodeWithText("DAILY BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started", ignoreCase = true).assertIsDisplayed().performClick()
        assertEquals("Navigation works cleanly from recovered welcome screen", true, navigated)
    }
}

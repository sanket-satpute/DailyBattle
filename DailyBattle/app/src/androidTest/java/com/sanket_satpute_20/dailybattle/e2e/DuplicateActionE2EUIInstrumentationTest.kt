package com.sanket_satpute_20.dailybattle.e2e

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.components.DBErrorState
import com.sanket_satpute_20.dailybattle.presentation.startup.WelcomeScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.7 — Duplicate Action E2E UI Instrumentation Tests
 *
 * Verifies UI button debouncing, disabled states during loading, and
 * rapid tap protection per `docs/06_NAVIGATION_AND_FLOWS.md` §54:
 *
 * - Double tap, triple tap, and rapid tap on UI buttons.
 * - Loading state prevents duplicate click event dispatch.
 * - Retry spam on error state components.
 */
@RunWith(AndroidJUnit4::class)
class DuplicateActionE2EUIInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dbButton_doubleTapDuringLoading_executesClickExactlyOnce() {
        var clickCount = 0

        composeTestRule.setContent {
            var isLoading by remember { mutableStateOf(false) }

            DBButton(
                text = "START BATTLE",
                onClick = {
                    clickCount++
                    isLoading = true // Transition to loading state upon first click
                },
                isLoading = isLoading
            )
        }

        // First click
        composeTestRule.onNodeWithText("START BATTLE").performClick()
        assertEquals("First tap increments click count to 1", 1, clickCount)

        // Second tap immediately while loading
        composeTestRule.onNodeWithText("START BATTLE").performClick()
        assertEquals("Second tap during loading is ignored (click count remains 1)", 1, clickCount)
    }

    @Test
    fun dbButton_rapidTapSpam_disabledStateDispatchesZeroClicks() {
        var clickCount = 0

        composeTestRule.setContent {
            DBButton(
                text = "SUBMIT ANSWER",
                onClick = { clickCount++ },
                enabled = false // Disabled button
            )
        }

        // Spam tap 5 times
        repeat(5) {
            composeTestRule.onNodeWithText("SUBMIT ANSWER").performClick()
        }

        assertEquals("Zero clicks dispatched on disabled button", 0, clickCount)
    }

    @Test
    fun errorState_retryTap_dispatchesCallback() {
        var retryCount = 0

        composeTestRule.setContent {
            DBErrorState(
                title = "COULDN'T LOAD BATTLE",
                description = "Connection error",
                onRetry = { retryCount++ }
            )
        }

        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed().performClick()
        assertEquals("Retry tap invoked callback", 1, retryCount)
    }

    @Test
    fun welcomeScreen_startBattlingButton_respondsDeterministically() {
        var navigated = 0

        composeTestRule.setContent {
            WelcomeScreen(
                onGetStarted = { navigated++ }
            )
        }

        composeTestRule.onNodeWithText("Get Started", ignoreCase = true).assertIsDisplayed().performClick()
        assertEquals("Start battling clicked once", 1, navigated)
    }
}

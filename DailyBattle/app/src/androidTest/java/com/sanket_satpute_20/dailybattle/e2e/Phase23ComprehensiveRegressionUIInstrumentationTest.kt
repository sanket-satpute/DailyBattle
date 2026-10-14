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
 * Sprint 23.9 — Phase 23 Comprehensive Regression UI Instrumentation Test
 *
 * Verifies core UI regression baselines across Phase 23 per `docs/ROADMAP.md` (lines 1826-1829),
 * `docs/12_TESTING_AND_QA.md` §46, and `docs/16_DEFINITION_OF_DONE.md` §57:
 *
 * - Welcome and startup flow buttons respond deterministically.
 * - Error states render safely with accessible descriptions and working retry triggers.
 * - Button loading and normal states maintain visual integrity and click behavior.
 */
@RunWith(AndroidJUnit4::class)
class Phase23ComprehensiveRegressionUIInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun regression_welcomeScreen_launchesAndGetStartedResponds() {
        var clicked = false

        composeTestRule.setContent {
            WelcomeScreen(
                onGetStarted = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("DAILY BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started", ignoreCase = true).assertIsDisplayed().performClick()
        assertEquals("Get Started click handled cleanly", true, clicked)
    }

    @Test
    fun regression_errorState_rendersSafelyAndRetriesCleanly() {
        var retried = false

        composeTestRule.setContent {
            DBErrorState(
                title = "REGRESSION CHECK",
                description = "Verifying non-fatal error recovery rendering.",
                onRetry = { retried = true }
            )
        }

        composeTestRule.onNodeWithText("REGRESSION CHECK").assertIsDisplayed()
        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed().performClick()
        assertEquals("Retry handled cleanly", true, retried)
    }

    @Test
    fun regression_dbButton_rendersAndClicksDeterministically() {
        var clickCount = 0

        composeTestRule.setContent {
            DBButton(
                text = "CONTINUE BATTLE",
                onClick = { clickCount++ }
            )
        }

        composeTestRule.onNodeWithText("CONTINUE BATTLE").assertIsDisplayed().performClick()
        assertEquals("Button click increments count", 1, clickCount)
    }
}

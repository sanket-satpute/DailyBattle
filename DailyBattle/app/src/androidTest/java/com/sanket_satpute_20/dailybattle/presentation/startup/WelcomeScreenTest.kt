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
 * Sprint 23.4 — UI Tests: SCR-001 (Welcome Screen)
 * Verifies Daily Battle identity, core promise, and Get Started CTA per `docs/04_SCREEN_BLUEPRINTS.md` §9.
 */
@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_welcome_screen_renders_and_responds_to_clicks() {
        var buttonClicked = false

        composeTestRule.setContent {
            WelcomeScreen(
                onGetStarted = { buttonClicked = true }
            )
        }

        // Verify Daily Battle identity exists (Level 1 hierarchy)
        composeTestRule.onNodeWithText("DAILY BATTLE").assertIsDisplayed()

        // Verify core promise exists (Level 2 hierarchy)
        composeTestRule.onNodeWithText("3 challenges.\n~3 minutes.\nOne score.").assertIsDisplayed()

        // Verify Get Started CTA exists and works (Level 3 hierarchy)
        val button = composeTestRule.onNodeWithText("GET STARTED")
        button.assertIsDisplayed()
        
        button.performClick()
        
        assertTrue("Get Started button click callback was not invoked", buttonClicked)
    }
}

package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

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
        composeTestRule.onNodeWithText("DAILY BATTLE").assertExists()

        // Verify core promise exists (Level 2 hierarchy)
        composeTestRule.onNodeWithText("3 challenges.\n~3 minutes.\nOne score.").assertExists()

        // Verify Get Started CTA exists and works (Level 3 hierarchy)
        val button = composeTestRule.onNodeWithText("GET STARTED")
        button.assertExists()
        
        button.performClick()
        
        assertTrue("Get Started button click callback was not invoked", buttonClicked)
    }
}

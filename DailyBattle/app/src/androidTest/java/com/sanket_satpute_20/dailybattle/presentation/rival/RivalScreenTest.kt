package com.sanket_satpute_20.dailybattle.presentation.rival

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-009 (Rival Screen)
 * Verifies loading, error, no-rival, and active rival comparison states per `docs/04_SCREEN_BLUEPRINTS.md` §17.
 */
@RunWith(AndroidJUnit4::class)
class RivalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_rival_screen_loading_state() {
        composeTestRule.setContent {
            RivalScreen(
                state = RivalUiState.Loading,
                onNavigateBack = {},
                onBeatRival = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("YOUR RIVAL").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Loading rival data").assertIsDisplayed()
    }

    @Test
    fun verify_rival_screen_error_state() {
        var retryClicked = false

        composeTestRule.setContent {
            RivalScreen(
                state = RivalUiState.Error,
                onNavigateBack = {},
                onBeatRival = {},
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule.onNodeWithText("YOUR RIVAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("RETRY").performClick()
        assertTrue("Retry callback was not invoked", retryClicked)
    }

    @Test
    fun verify_rival_screen_no_rival_state() {
        composeTestRule.setContent {
            RivalScreen(
                state = RivalUiState.NoRival,
                onNavigateBack = {},
                onBeatRival = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("YOUR RIVAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("No active rival").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play battles to find a rival.").assertIsDisplayed()
    }

    @Test
    fun verify_rival_screen_success_state_and_beat_rival_click() {
        var beatRivalClicked = false

        val historyItems = listOf(
            RivalMatchHistoryItem(
                battleId = com.sanket_satpute_20.dailybattle.domain.identifier.BattleId("2026-10-13"),
                date = 1000L,
                userScore = 901,
                rivalScore = 914,
                outcome = MatchOutcome.LOSS
            ),
            RivalMatchHistoryItem(
                battleId = com.sanket_satpute_20.dailybattle.domain.identifier.BattleId("2026-10-12"),
                date = 2000L,
                userScore = 945,
                rivalScore = 912,
                outcome = MatchOutcome.WIN
            )
        )

        val successState = RivalUiState.Success(
            rivalName = "Rahul",
            rivalScore = 914,
            userName = "Sanket",
            userScore = 901,
            gap = 13,
            direction = ScoreDirection.BEHIND,
            history = historyItems
        )

        composeTestRule.setContent {
            RivalScreen(
                state = successState,
                onNavigateBack = {},
                onBeatRival = { beatRivalClicked = true },
                onRetry = {}
            )
        }

        // Top bar
        composeTestRule.onNodeWithText("YOUR RIVAL").assertIsDisplayed()

        // Competitors
        composeTestRule.onNodeWithText("Rahul").assertIsDisplayed()
        composeTestRule.onNodeWithText("914").assertIsDisplayed()
        composeTestRule.onNodeWithText("VS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sanket").assertIsDisplayed()
        composeTestRule.onNodeWithText("901").assertIsDisplayed()

        // Gap
        composeTestRule.onNodeWithText("13 POINTS TO CATCH").assertIsDisplayed()

        // CTA
        composeTestRule.onNodeWithText("BEAT RAHUL").performClick()
        assertTrue("Beat rival callback was not invoked", beatRivalClicked)

        // Match history
        composeTestRule.onNodeWithText("MATCH HISTORY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sanket 901 - 914 Rahul").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOSS").assertIsDisplayed()
    }
}

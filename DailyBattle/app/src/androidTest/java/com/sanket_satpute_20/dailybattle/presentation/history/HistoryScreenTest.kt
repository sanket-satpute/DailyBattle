package com.sanket_satpute_20.dailybattle.presentation.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-013 (History Screen)
 * Verifies loading, empty, error, and populated score progress history states per `docs/04_SCREEN_BLUEPRINTS.md` §21.
 */
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_history_screen_loading_state() {
        composeTestRule.setContent {
            HistoryScreen(
                uiState = HistoryUiState.Loading
            )
        }

        composeTestRule.onNodeWithText("Loading history...").assertIsDisplayed()
    }

    @Test
    fun verify_history_screen_empty_state() {
        composeTestRule.setContent {
            HistoryScreen(
                uiState = HistoryUiState.Empty
            )
        }

        composeTestRule.onNodeWithText("YOUR HISTORY STARTS TODAY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Complete your first Battle\nto see your progress.").assertIsDisplayed()
    }

    @Test
    fun verify_history_screen_error_state() {
        composeTestRule.setContent {
            HistoryScreen(
                uiState = HistoryUiState.Error("History service unavailable.")
            )
        }

        composeTestRule.onNodeWithText("COULDN'T LOAD HISTORY").assertIsDisplayed()
        composeTestRule.onNodeWithText("History service unavailable.").assertIsDisplayed()
    }

    @Test
    fun verify_history_screen_success_state() {
        val sampleData = HistoryUiStateData(
            best = 927,
            average = 806,
            momentum = 7,
            recentBattles = listOf("Battle 1", "Battle 2"),
            graphData = listOf(800, 850, 900, 927)
        )

        composeTestRule.setContent {
            HistoryScreen(
                uiState = HistoryUiState.Success(sampleData)
            )
        }

        // Header
        composeTestRule.onNodeWithText("HISTORY").assertIsDisplayed()
        composeTestRule.onNodeWithText("YOUR SCORE").assertIsDisplayed()

        // Stats
        composeTestRule.onNodeWithText("BEST").assertIsDisplayed()
        composeTestRule.onNodeWithText("927").assertIsDisplayed()
        composeTestRule.onNodeWithText("AVERAGE").assertIsDisplayed()
        composeTestRule.onNodeWithText("806").assertIsDisplayed()
        composeTestRule.onNodeWithText("MOMENTUM").assertIsDisplayed()
        composeTestRule.onNodeWithText("7 DAYS").assertIsDisplayed()

        // Recent Battles Section
        composeTestRule.onNodeWithText("RECENT BATTLES").assertIsDisplayed()
    }
}

package com.sanket_satpute_20.dailybattle.presentation.result

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import com.sanket_satpute_20.dailybattle.domain.battle.RivalComparisonDirection
import com.sanket_satpute_20.dailybattle.domain.battle.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-008 (Results Screen)
 * Verifies loading, error, and completed result presentation with animations, breakdown card,
 * rival comparison, and CTAs per `docs/04_SCREEN_BLUEPRINTS.md` §16 and `docs/12_TESTING_AND_QA.md` §22.
 */
@RunWith(AndroidJUnit4::class)
class ResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_results_screen_loading_state() {
        composeTestRule.setContent {
            ResultsScreen(
                uiState = ResultsUiState.Loading,
                onBeatRivalClick = {},
                onShareClick = {}
            )
        }

        // Top bar should be visible
        composeTestRule.onNodeWithText("RESULTS").assertIsDisplayed()
    }

    @Test
    fun verify_results_screen_error_state() {
        composeTestRule.setContent {
            ResultsScreen(
                uiState = ResultsUiState.Error("Could not load battle result."),
                onBeatRivalClick = {},
                onShareClick = {}
            )
        }

        composeTestRule.onNodeWithText("COULDN'T LOAD RESULTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Could not load battle result.").assertIsDisplayed()
    }

    @Test
    fun verify_results_screen_success_state_and_interactions() {
        var beatRivalClicked = false
        var shareClicked = false

        val sampleResult = BattleResult(
            resultId = BattleResultId("res-1"),
            userId = UserId("user-1"),
            battleId = BattleId("2026-10-14"),
            sessionId = BattleSessionId("sess-1"),
            snapScore = 287,
            shiftScore = 252,
            crowdCallScore = 271,
            consistencyScore = 91,
            totalScore = 901,
            percentile = 0.09,
            completedAt = 1000L
        )

        val sampleSummary = BattleResultSummary(
            result = sampleResult,
            previousDayScore = 864,
            scoreImprovement = 37,
            isNewPersonalBest = true,
            personalBestScore = 927,
            averageScore = 806,
            rivalComparison = RivalScoreComparison(
                userScore = 901,
                rivalScore = 914,
                scoreGap = 13,
                battleId = BattleId("2026-10-14"),
                direction = RivalComparisonDirection.RIVAL_AHEAD,
                rivalName = "Rahul"
            )
        )

        composeTestRule.setContent {
            ResultsScreen(
                uiState = ResultsUiState.Success(sampleSummary),
                onBeatRivalClick = { beatRivalClicked = true },
                onShareClick = { shareClicked = true }
            )
        }

        // Top Bar
        composeTestRule.onNodeWithText("RESULTS").assertIsDisplayed()

        // Title and Score context
        composeTestRule.onNodeWithText("TODAY'S RESULT").assertIsDisplayed()
        composeTestRule.onNodeWithText("/ 1000").assertIsDisplayed()

        // Wait for idle to allow reveal animations
        composeTestRule.waitForIdle()

        // Breakdown Card challenge scores
        composeTestRule.onNodeWithText("SNAP").assertIsDisplayed()
        composeTestRule.onNodeWithText("SHIFT").assertIsDisplayed()
        composeTestRule.onNodeWithText("CROWD CALL").assertIsDisplayed()

        // Personal best and average
        composeTestRule.onNodeWithText("NEW PERSONAL BEST").assertIsDisplayed()
        composeTestRule.onNodeWithText("PERSONAL BEST").assertIsDisplayed()
        composeTestRule.onNodeWithText("927").assertIsDisplayed()
        composeTestRule.onNodeWithText("AVERAGE").assertIsDisplayed()
        composeTestRule.onNodeWithText("806").assertIsDisplayed()

        // Rival comparison and actions
        composeTestRule.onNodeWithText("BEAT RAHUL").performClick()
        assertTrue("Beat rival callback was not invoked", beatRivalClicked)

        composeTestRule.onNodeWithText("SHARE RESULT").performClick()
        assertTrue("Share result callback was not invoked", shareClicked)

        // Tomorrow message
        composeTestRule.onNodeWithText("Tomorrow's Battle awaits.").assertIsDisplayed()
    }
}

package com.sanket_satpute_20.dailybattle.presentation.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_home_screen_ready_state_interactions() {
        var playClicked = false
        var beatRivalClicked = false

        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(
                    isLoading = false,
                    isError = false,
                    userName = "Sanket",
                    momentumDays = 7,
                    battleState = DBBattleCardState.Ready,
                    challengesCount = 3,
                    estimatedMinutes = 3,
                    hasRival = true,
                    rivalName = "Rahul",
                    rivalScore = 914,
                    userScore = 901,
                    pointsToCatch = 13
                ),
                onPlayBattle = { playClicked = true },
                onBeatRival = { beatRivalClicked = true },
                onAddFriend = {},
                onRetry = {}
            )
        }

        // Verify elements
        composeTestRule.onNodeWithText("Good evening, Sanket.").assertExists()
        composeTestRule.onNodeWithText("TODAY'S BATTLE").assertExists()
        composeTestRule.onNodeWithText("YOUR RIVAL").assertExists()
        
        // Verify actions
        composeTestRule.onNodeWithText("PLAY BATTLE").performClick()
        assertTrue(playClicked)

        composeTestRule.onNodeWithText("BEAT RAHUL").performClick()
        assertTrue(beatRivalClicked)
    }

    @Test
    fun verify_home_screen_no_rival_state() {
        var addFriendClicked = false

        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(
                    isLoading = false,
                    isError = false,
                    userName = "Sanket",
                    momentumDays = 7,
                    battleState = DBBattleCardState.Ready,
                    challengesCount = 3,
                    estimatedMinutes = 3,
                    hasRival = false
                ),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = { addFriendClicked = true },
                onRetry = {}
            )
        }

        // Verify elements
        composeTestRule.onNodeWithText("NO RIVAL YET").assertExists()
        composeTestRule.onNodeWithText("ADD FRIEND").performClick()
        assertTrue(addFriendClicked)
    }

    @Test
    fun verify_home_screen_error_state() {
        var retryClicked = false

        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(isError = true, errorMessage = "NO BATTLE AVAILABLE"),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = {},
                onRetry = { retryClicked = true }
            )
        }

        // Verify error element
        composeTestRule.onNodeWithText("NO BATTLE AVAILABLE").assertExists()
        composeTestRule.onNodeWithText("RETRY").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun verify_home_screen_completed_state() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(
                    isLoading = false,
                    isError = false,
                    battleState = DBBattleCardState.Completed,
                    score = 901,
                    percentileText = "TOP 9%",
                    nextBattleTime = "TOMORROW"
                ),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = {},
                onRetry = {}
            )
        }

        // Verify completed elements
        composeTestRule.onNodeWithText("TODAY'S BATTLE ✓").assertExists()
        composeTestRule.onNodeWithText("901").assertExists()
        composeTestRule.onNodeWithText("TOP 9%").assertExists()
        composeTestRule.onNodeWithText("NEXT BATTLE").assertExists()
        composeTestRule.onNodeWithText("TOMORROW").assertExists()
        
        // Verify action is hidden
        composeTestRule.onNodeWithText("PLAY BATTLE").assertDoesNotExist()
    }

    @Test
    fun verify_home_screen_offline_state() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(isError = true, isOffline = true),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = {},
                onRetry = {}
            )
        }

        // Verify offline description
        composeTestRule.onNodeWithText("You are currently offline.").assertExists()
    }
}

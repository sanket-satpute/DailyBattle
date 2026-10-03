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
                state = HomeState(isError = true),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = {},
                onRetry = { retryClicked = true }
            )
        }

        // Verify error element
        composeTestRule.onNodeWithText("RETRY").performClick()
        assertTrue(retryClicked)
    }
}

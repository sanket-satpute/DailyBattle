package com.sanket_satpute_20.dailybattle.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendUiModel
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendsScreen
import com.sanket_satpute_20.dailybattle.presentation.friend.FriendsUiState
import com.sanket_satpute_20.dailybattle.presentation.home.HomeScreen
import com.sanket_satpute_20.dailybattle.presentation.home.HomeState
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalScreen
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.6 — Failure E2E UI Instrumentation Tests
 *
 * Verifies UI error states, offline messaging, retry interactions, and
 * empty vs error invariants per `docs/09_OFFLINE_AND_ERROR_HANDLING.md` §54, §58, §82:
 *
 * - Invariant 5: Network failure never becomes an empty state.
 * - Invariant 6: Empty data never becomes an error state.
 * - Error states display clear recovery actions (RETRY).
 */
@RunWith(AndroidJUnit4::class)
class FailureE2EUIInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_errorState_displaysErrorMessageAndRetryButton() {
        var retryClicked = false

        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(
                    isLoading = false,
                    isError = true,
                    errorMessage = "COULDN'T LOAD TODAY'S BATTLE",
                    isOffline = false
                ),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = {},
                onRetry = { retryClicked = true }
            )
        }

        // Verify error title and prompt
        composeTestRule.onNodeWithText("COULDN'T LOAD TODAY'S BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check your connection and try again.").assertIsDisplayed()

        // Verify Retry action works
        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed().performClick()
        assertTrue("Retry callback must be triggered on button tap", retryClicked)
    }

    @Test
    fun homeScreen_offlineState_displaysOfflineExplanation() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeState(
                    isLoading = false,
                    isError = true,
                    errorMessage = "COULDN'T LOAD TODAY'S BATTLE",
                    isOffline = true
                ),
                onPlayBattle = {},
                onBeatRival = {},
                onAddFriend = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("COULDN'T LOAD TODAY'S BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("You are currently offline.").assertIsDisplayed()
        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed()
    }

    @Test
    fun friendsScreen_networkError_displaysErrorStateAndNeverEmptyState() {
        // Invariant 5: Network failure never becomes an empty state ("NO RIVALS YET")
        var retryTriggered = false

        composeTestRule.setContent {
            FriendsScreen(
                state = FriendsUiState.Error,
                onAddFriendClick = {},
                onRetry = { retryTriggered = true }
            )
        }

        // Must display error state
        composeTestRule.onNodeWithText("COULDN'T LOAD CONTENT").assertIsDisplayed()
        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed().performClick()
        assertTrue(retryTriggered)

        // Must NOT display "NO RIVALS YET" empty state
        composeTestRule.onNodeWithText("NO RIVALS YET").assertDoesNotExist()
    }

    @Test
    fun friendsScreen_emptyFriends_displaysEmptyStateAndNeverErrorState() {
        // Invariant 6: Empty data never becomes an error state
        val selfUser = FriendUiModel(
            id = "user_me",
            name = "Me",
            score = 850,
            isUser = true,
            isRival = false,
            status = com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus.ACCEPTED
        )

        composeTestRule.setContent {
            FriendsScreen(
                state = FriendsUiState.Success(listOf(selfUser)),
                onAddFriendClick = {},
                onRetry = {}
            )
        }

        // Must display empty state
        composeTestRule.onNodeWithText("NO RIVALS YET").assertIsDisplayed()
        composeTestRule.onNodeWithText("ADD FRIEND").assertIsDisplayed()

        // Must NOT display error state
        composeTestRule.onNodeWithText("COULDN'T LOAD CONTENT").assertDoesNotExist()
        composeTestRule.onNodeWithText("RETRY").assertDoesNotExist()
    }

    @Test
    fun rivalScreen_errorState_displaysErrorMessageAndRetryAction() {
        var retryCalled = false

        composeTestRule.setContent {
            RivalScreen(
                state = RivalUiState.Error,
                onNavigateBack = {},
                onBeatRival = {},
                onRetry = { retryCalled = true }
            )
        }

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().performClick()
        assertTrue(retryCalled)

        composeTestRule.onNodeWithText("No active rival").assertDoesNotExist()
    }

    @Test
    fun rivalScreen_noRivalState_displaysNoActiveRivalExplanation() {
        composeTestRule.setContent {
            RivalScreen(
                state = RivalUiState.NoRival,
                onNavigateBack = {},
                onBeatRival = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("No active rival").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play battles to find a rival.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()
    }
}

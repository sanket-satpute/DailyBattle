package com.sanket_satpute_20.dailybattle.presentation.friend

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-010 (Friends Screen)
 * Verifies loading, error, empty, and populated friend list states per `docs/04_SCREEN_BLUEPRINTS.md` §18.
 */
@RunWith(AndroidJUnit4::class)
class FriendsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_friends_screen_loading_state() {
        composeTestRule.setContent {
            FriendsScreen(
                state = FriendsUiState.Loading,
                onAddFriendClick = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("FRIENDS").assertIsDisplayed()
    }

    @Test
    fun verify_friends_screen_error_state() {
        var retryClicked = false

        composeTestRule.setContent {
            FriendsScreen(
                state = FriendsUiState.Error,
                onAddFriendClick = {},
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule.onNodeWithText("FRIENDS").assertIsDisplayed()
        composeTestRule.onNodeWithText("RETRY").performClick()
        assertTrue("Retry callback was not invoked", retryClicked)
    }

    @Test
    fun verify_friends_screen_empty_state() {
        var addFriendClicked = false

        composeTestRule.setContent {
            FriendsScreen(
                state = FriendsUiState.Success(
                    friends = listOf(
                        FriendUiModel(
                            id = "user-1",
                            name = "Sanket",
                            score = null,
                            isUser = true,
                            isRival = false,
                            status = FriendStatus.ACCEPTED
                        )
                    )
                ),
                onAddFriendClick = { addFriendClicked = true },
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("FRIENDS").assertIsDisplayed()
        composeTestRule.onNodeWithText("NO RIVALS YET").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add a friend and start\nyour first Battle rivalry.").assertIsDisplayed()

        composeTestRule.onNodeWithText("ADD FRIEND").performClick()
        assertTrue("Add friend callback was not invoked", addFriendClicked)
    }

    @Test
    fun verify_friends_screen_populated_list_state() {
        var addFriendClicked = false

        val friends = listOf(
            FriendUiModel(
                id = "rival-1",
                name = "Rahul",
                score = 914,
                isUser = false,
                isRival = true,
                status = FriendStatus.ACCEPTED
            ),
            FriendUiModel(
                id = "user-1",
                name = "Sanket",
                score = 901,
                isUser = true,
                isRival = false,
                status = FriendStatus.ACCEPTED
            ),
            FriendUiModel(
                id = "friend-2",
                name = "Neha",
                score = 817,
                isUser = false,
                isRival = false,
                status = FriendStatus.ACCEPTED
            ),
            FriendUiModel(
                id = "pending-1",
                name = "Aman",
                score = null,
                isUser = false,
                isRival = false,
                status = FriendStatus.PENDING
            )
        )

        composeTestRule.setContent {
            FriendsScreen(
                state = FriendsUiState.Success(friends = friends),
                onAddFriendClick = { addFriendClicked = true },
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("FRIENDS").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ ADD FRIEND").assertIsDisplayed()
        composeTestRule.onNodeWithText("TODAY").assertIsDisplayed()

        // Friend rows
        composeTestRule.onNodeWithText("Rahul").assertIsDisplayed()
        composeTestRule.onNodeWithText("914").assertIsDisplayed()
        composeTestRule.onNodeWithText("RIVAL").assertIsDisplayed()

        composeTestRule.onNodeWithText("Sanket").assertIsDisplayed()
        composeTestRule.onNodeWithText("901").assertIsDisplayed()
        composeTestRule.onNodeWithText("YOU").assertIsDisplayed()

        composeTestRule.onNodeWithText("Neha").assertIsDisplayed()
        composeTestRule.onNodeWithText("817").assertIsDisplayed()

        composeTestRule.onNodeWithText("Aman").assertIsDisplayed()
        composeTestRule.onNodeWithText("PENDING").assertIsDisplayed()

        // Click Add Friend
        composeTestRule.onNodeWithText("+ ADD FRIEND").performClick()
        assertTrue("Add friend button click was not invoked", addFriendClicked)
    }
}

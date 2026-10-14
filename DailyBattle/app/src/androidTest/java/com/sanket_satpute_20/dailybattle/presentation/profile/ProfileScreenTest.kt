package com.sanket_satpute_20.dailybattle.presentation.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.4 — UI Tests: SCR-012 (Profile / Battle DNA Screen)
 * Verifies loading, error, and profile statistics presentation per `docs/04_SCREEN_BLUEPRINTS.md` §20.
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_profile_screen_loading_state() {
        composeTestRule.setContent {
            ProfileScreen(
                uiState = ProfileUiState.Loading
            )
        }

        composeTestRule.onNodeWithText("PROFILE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loading profile...").assertIsDisplayed()
    }

    @Test
    fun verify_profile_screen_error_state() {
        composeTestRule.setContent {
            ProfileScreen(
                uiState = ProfileUiState.Error("Profile not found.")
            )
        }

        composeTestRule.onNodeWithText("PROFILE").assertIsDisplayed()
        composeTestRule.onNodeWithText("COULDN'T LOAD PROFILE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile not found.").assertIsDisplayed()
    }

    @Test
    fun verify_profile_screen_success_state() {
        val sampleProfile = Profile(
            userId = UserId("user-1"),
            battleName = "Sanket",
            momentum = 7,
            bestScore = 927,
            averageScore = 806,
            battleDNA = BattleDNA(
                speed = 91,
                memory = 78,
                people = 94
            ),
            records = PersonalRecords(
                bestScore = 927,
                bestMomentum = 14,
                battlesPlayed = 42
            )
        )

        composeTestRule.setContent {
            ProfileScreen(
                uiState = ProfileUiState.Success(sampleProfile)
            )
        }

        // Top Bar
        composeTestRule.onNodeWithText("PROFILE").assertIsDisplayed()

        // Name & Momentum
        composeTestRule.onNodeWithText("Sanket").assertIsDisplayed()
        composeTestRule.onNodeWithText("🔥 7 DAY MOMENTUM").assertIsDisplayed()

        // Best and Average Scores
        composeTestRule.onNodeWithText("BEST SCORE").assertIsDisplayed()
        composeTestRule.onNodeWithText("927").assertIsDisplayed()
        composeTestRule.onNodeWithText("AVERAGE").assertIsDisplayed()
        composeTestRule.onNodeWithText("806").assertIsDisplayed()

        // Battle DNA
        composeTestRule.onNodeWithText("BATTLE DNA").assertIsDisplayed()
        composeTestRule.onNodeWithText("SPEED").assertIsDisplayed()
        composeTestRule.onNodeWithText("91").assertIsDisplayed()
        composeTestRule.onNodeWithText("MEMORY").assertIsDisplayed()
        composeTestRule.onNodeWithText("78").assertIsDisplayed()
        composeTestRule.onNodeWithText("PEOPLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("94").assertIsDisplayed()

        // Personal Records
        composeTestRule.onNodeWithText("PERSONAL RECORDS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Best Score").assertIsDisplayed()
        composeTestRule.onNodeWithText("Best Momentum").assertIsDisplayed()
        composeTestRule.onNodeWithText("14").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battles Played").assertIsDisplayed()
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }
}

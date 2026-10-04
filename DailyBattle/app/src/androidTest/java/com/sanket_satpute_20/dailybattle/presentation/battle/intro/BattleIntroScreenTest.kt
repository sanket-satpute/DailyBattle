package com.sanket_satpute_20.dailybattle.presentation.battle.intro

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class BattleIntroScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verify_battle_intro_renders_correctly() {
        composeTestRule.setContent {
            BattleIntroScreen(
                state = BattleIntroState(challengesCount = 3, estimatedMinutes = 3),
                onStartBattle = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("TODAY'S BATTLE").assertExists()
        composeTestRule.onNodeWithText("3 challenges").assertExists()
        composeTestRule.onNodeWithText("~3 minutes").assertExists()
        composeTestRule.onNodeWithText("OFFICIAL ATTEMPT").assertExists()
        composeTestRule.onNodeWithText("START BATTLE").assertExists()
    }

    @Test
    fun verify_battle_intro_error_state() {
        composeTestRule.setContent {
            BattleIntroScreen(
                state = BattleIntroState(isError = true, errorMessage = "Duplicate attempt."),
                onStartBattle = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("COULDN'T START BATTLE").assertExists()
        composeTestRule.onNodeWithText("Duplicate attempt.").assertExists()
    }

    @Test
    fun verify_start_battle_click() {
        var startClicked = false

        composeTestRule.setContent {
            BattleIntroScreen(
                state = BattleIntroState(),
                onStartBattle = { startClicked = true },
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("START BATTLE").performClick()
        assertTrue(startClicked)
    }
}

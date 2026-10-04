package com.sanket_satpute_20.dailybattle.presentation.crowd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdChoice
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdDistribution
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrowdScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenRendersQuestionAndChoices() {
        val question = "What would most people choose?"
        val choices = listOf(
            CrowdChoice("1", "Movie"),
            CrowdChoice("2", "Food")
        )
        val uiState = CrowdUiState(
            questionText = question,
            choices = choices
        )

        composeTestRule.setContent {
            CrowdScreen(
                uiState = uiState,
                onAnswerSelected = {},
                onComplete = {}
            )
        }

        composeTestRule.onNodeWithText("CROWD CALL").assertIsDisplayed()
        composeTestRule.onNodeWithText(question).assertIsDisplayed()
        composeTestRule.onNodeWithText("Movie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun clickingChoiceInvokesCallback() {
        var selectedOption = ""
        val uiState = CrowdUiState(
            questionText = "Q",
            choices = listOf(CrowdChoice("1", "Movie"), CrowdChoice("2", "Food"))
        )

        composeTestRule.setContent {
            CrowdScreen(
                uiState = uiState,
                onAnswerSelected = { selectedOption = it },
                onComplete = {}
            )
        }

        composeTestRule.onNodeWithText("Food").performClick()
        assertEquals("2", selectedOption)
    }

    @Test
    fun screenRendersResultDistributionWhenRevealed() {
        val uiState = CrowdUiState(
            questionText = "Q",
            choices = listOf(CrowdChoice("1", "Movie"), CrowdChoice("2", "Food")),
            selectedChoiceId = "2",
            isResultRevealed = true,
            distribution = CrowdDistribution(mapOf("1" to 20, "2" to 80)),
            isCorrect = true
        )

        composeTestRule.setContent {
            CrowdScreen(
                uiState = uiState,
                onAnswerSelected = {},
                onComplete = {}
            )
        }

        composeTestRule.onNodeWithText("THE CROWD").assertIsDisplayed()
        composeTestRule.onNodeWithText("YOUR PICK").assertIsDisplayed()
        composeTestRule.onNodeWithText("CORRECT").assertIsDisplayed()
        composeTestRule.onNodeWithText("20%").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
    }
}

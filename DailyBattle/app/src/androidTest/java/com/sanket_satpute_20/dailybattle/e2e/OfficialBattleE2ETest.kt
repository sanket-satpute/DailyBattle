package com.sanket_satpute_20.dailybattle.e2e

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary
import com.sanket_satpute_20.dailybattle.domain.battle.RivalComparisonDirection
import com.sanket_satpute_20.dailybattle.domain.battle.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdChoice
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroScreen
import com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroState
import com.sanket_satpute_20.dailybattle.presentation.crowd.CrowdScreen
import com.sanket_satpute_20.dailybattle.presentation.crowd.CrowdUiState
import com.sanket_satpute_20.dailybattle.presentation.home.HomeScreen
import com.sanket_satpute_20.dailybattle.presentation.home.HomeState
import com.sanket_satpute_20.dailybattle.presentation.result.ResultsScreen
import com.sanket_satpute_20.dailybattle.presentation.result.ResultsUiState
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalScreen
import com.sanket_satpute_20.dailybattle.presentation.rival.RivalUiState
import com.sanket_satpute_20.dailybattle.presentation.shift.ShiftScreen
import com.sanket_satpute_20.dailybattle.presentation.shift.ShiftUiState
import com.sanket_satpute_20.dailybattle.presentation.snap.SnapScreen
import com.sanket_satpute_20.dailybattle.presentation.snap.SnapUiState
import com.sanket_satpute_20.dailybattle.presentation.startup.BattleNameScreen
import com.sanket_satpute_20.dailybattle.presentation.startup.BattleNameState
import com.sanket_satpute_20.dailybattle.presentation.startup.WelcomeScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sprint 23.5 — Official Battle E2E Compose UI Flow Test
 *
 * Runs the complete official sequence per `docs/ROADMAP.md` (lines 1766–1786) and `docs/12_TESTING_AND_QA.md` §18:
 *
 *   Welcome
 *    ↓
 *   Battle Name
 *    ↓
 *   Home
 *    ↓
 *   Battle Intro
 *    ↓
 *   Snap
 *    ↓
 *   Shift
 *    ↓
 *   Crowd Call
 *    ↓
 *   Results
 *    ↓
 *   Rival
 */
@RunWith(AndroidJUnit4::class)
class OfficialBattleE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun execute_complete_official_battle_e2e_flow() {
        val visitedScreens = mutableListOf<String>()
        var currentScreen by mutableStateOf(AppRoute.Welcome.route)
        var battleName by mutableStateOf("Sanket")

        composeTestRule.setContent {
            E2EBattleNavigator(
                currentRoute = currentScreen,
                battleName = battleName,
                onNavigate = { nextRoute ->
                    visitedScreens.add(nextRoute)
                    currentScreen = nextRoute
                }
            )
        }

        // --- Step 1: Welcome Screen ---
        composeTestRule.onNodeWithText("DAILY BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 challenges.\n~3 minutes.\nOne score.").assertIsDisplayed()
        composeTestRule.onNodeWithText("GET STARTED").performClick()
        assertEquals(AppRoute.BattleName.route, currentScreen)

        // --- Step 2: Battle Name Screen ---
        composeTestRule.onNodeWithText("YOUR BATTLE\nNAME").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose the name your\nfriends will see.").assertIsDisplayed()
        composeTestRule.onNodeWithText("CONTINUE").performClick()
        assertEquals(AppRoute.Home.route, currentScreen)

        // --- Step 3: Home Screen ---
        composeTestRule.onNodeWithText("TODAY'S BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 challenges").assertIsDisplayed()
        composeTestRule.onNodeWithText("PLAY BATTLE").performClick()
        assertEquals(AppRoute.BattleIntro.route, currentScreen)

        // --- Step 4: Battle Intro Screen ---
        composeTestRule.onNodeWithText("TODAY'S BATTLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("OFFICIAL ATTEMPT").assertIsDisplayed()
        composeTestRule.onNodeWithText("START BATTLE").performClick()
        assertEquals(AppRoute.Snap.route, currentScreen)

        // --- Step 5: Snap Screen (Challenge 1) ---
        composeTestRule.onNodeWithText("SNAP").assertIsDisplayed()
        composeTestRule.onNodeWithText("TAP TARGET").performClick()
        assertEquals(AppRoute.Shift.route, currentScreen)

        // --- Step 6: Shift Screen (Challenge 2) ---
        composeTestRule.onNodeWithText("SHIFT").assertIsDisplayed()
        composeTestRule.onNodeWithText("WHAT MOVED?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Card A").performClick()
        assertEquals(AppRoute.CrowdCall.route, currentScreen)

        // --- Step 7: Crowd Call Screen (Challenge 3) ---
        composeTestRule.onNodeWithText("CROWD CALL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Which is more popular?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coffee").performClick()
        assertEquals(AppRoute.Results.route, currentScreen)

        // --- Step 8: Results Screen ---
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("TODAY'S RESULT").assertIsDisplayed()
        composeTestRule.onNodeWithText("/ 1000").assertIsDisplayed()
        composeTestRule.onNodeWithText("SNAP").assertIsDisplayed()
        composeTestRule.onNodeWithText("SHIFT").assertIsDisplayed()
        composeTestRule.onNodeWithText("CROWD CALL").assertIsDisplayed()
        composeTestRule.onNodeWithText("BEAT RAHUL").performClick()
        assertEquals(AppRoute.Rival.route, currentScreen)

        // --- Step 9: Rival Screen ---
        composeTestRule.onNodeWithText("YOUR RIVAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rahul").assertIsDisplayed()
        composeTestRule.onNodeWithText("914").assertIsDisplayed()
        composeTestRule.onNodeWithText("VS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sanket").assertIsDisplayed()
        composeTestRule.onNodeWithText("901").assertIsDisplayed()
        composeTestRule.onNodeWithText("13 POINTS TO CATCH").assertIsDisplayed()

        // Verify the visited screens chain precisely matches the canonical E2E sequence
        val expectedChain = listOf(
            AppRoute.BattleName.route,
            AppRoute.Home.route,
            AppRoute.BattleIntro.route,
            AppRoute.Snap.route,
            AppRoute.Shift.route,
            AppRoute.CrowdCall.route,
            AppRoute.Results.route,
            AppRoute.Rival.route
        )
        assertEquals(expectedChain, visitedScreens)
    }

    /**
     * Test harness that renders each screen based on currentRoute, simulating NavHost transitions.
     */
    @Composable
    private fun E2EBattleNavigator(
        currentRoute: String,
        battleName: String,
        onNavigate: (String) -> Unit
    ) {
        when (currentRoute) {
            AppRoute.Welcome.route -> {
                WelcomeScreen(
                    onGetStarted = { onNavigate(AppRoute.BattleName.route) }
                )
            }
            AppRoute.BattleName.route -> {
                BattleNameScreen(
                    state = BattleNameState(
                        name = battleName,
                        isValid = true,
                        isError = false,
                        errorMessage = null,
                        isSubmitting = false,
                        isSuccess = false
                    ),
                    onNameChanged = {},
                    onSubmit = { onNavigate(AppRoute.Home.route) }
                )
            }
            AppRoute.Home.route -> {
                HomeScreen(
                    state = HomeState(
                        isLoading = false,
                        isError = false,
                        userName = battleName,
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
                    onPlayBattle = { onNavigate(AppRoute.BattleIntro.route) },
                    onBeatRival = { onNavigate(AppRoute.Rival.route) },
                    onAddFriend = {},
                    onRetry = {}
                )
            }
            AppRoute.BattleIntro.route -> {
                BattleIntroScreen(
                    state = BattleIntroState(
                        challengesCount = 3,
                        estimatedMinutes = 3,
                        isLoading = false,
                        isError = false
                    ),
                    onStartBattle = { onNavigate(AppRoute.Snap.route) },
                    onBack = { onNavigate(AppRoute.Home.route) }
                )
            }
            AppRoute.Snap.route -> {
                SnapScreen(
                    uiState = SnapUiState(
                        progressText = "1 / 3",
                        timerText = "00:10",
                        scoreText = "Score 287"
                    ),
                    onElementTap = {},
                    onComplete = { onNavigate(AppRoute.Shift.route) }
                )
            }
            AppRoute.Shift.route -> {
                ShiftScreen(
                    uiState = ShiftUiState(
                        progressText = "2 / 3",
                        timerText = "00:15",
                        instructionText = "WHAT MOVED?",
                        answerOptions = listOf("Card A", "Card B")
                    ),
                    onAnswerSelected = { onNavigate(AppRoute.CrowdCall.route) },
                    onComplete = { onNavigate(AppRoute.CrowdCall.route) }
                )
            }
            AppRoute.CrowdCall.route -> {
                CrowdScreen(
                    uiState = CrowdUiState(
                        progressText = "3 / 3",
                        questionText = "Which is more popular?",
                        choices = listOf(
                            CrowdChoice("1", "Coffee"),
                            CrowdChoice("2", "Tea")
                        )
                    ),
                    onAnswerSelected = { onNavigate(AppRoute.Results.route) },
                    onComplete = { onNavigate(AppRoute.Results.route) }
                )
            }
            AppRoute.Results.route -> {
                val sampleSummary = BattleResultSummary(
                    result = BattleResult(
                        resultId = BattleResultId("res-e2e"),
                        userId = UserId("user-e2e"),
                        battleId = BattleId("2026-10-14"),
                        sessionId = BattleSessionId("sess-e2e"),
                        snapScore = 287,
                        shiftScore = 252,
                        crowdCallScore = 271,
                        consistencyScore = 91,
                        totalScore = 901,
                        percentile = 0.09,
                        completedAt = 1000L
                    ),
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
                ResultsScreen(
                    uiState = ResultsUiState.Success(sampleSummary),
                    onBeatRivalClick = { onNavigate(AppRoute.Rival.route) },
                    onShareClick = {}
                )
            }
            AppRoute.Rival.route -> {
                val rivalState = RivalUiState.Success(
                    rivalName = "Rahul",
                    rivalScore = 914,
                    userName = battleName,
                    userScore = 901,
                    gap = 13,
                    direction = ScoreDirection.BEHIND,
                    history = listOf(
                        RivalMatchHistoryItem(
                            battleId = BattleId("2026-10-13"),
                            date = 1000L,
                            userScore = 901,
                            rivalScore = 914,
                            outcome = MatchOutcome.LOSS
                        )
                    )
                )
                RivalScreen(
                    state = rivalState,
                    onNavigateBack = { onNavigate(AppRoute.Home.route) },
                    onBeatRival = {},
                    onRetry = {}
                )
            }
        }
    }
}

package com.sanket_satpute_20.dailybattle

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies application startup, dependency initialization, navigation initialization, and the
 * initial state compose end to end without a crash.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityStartupTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = androidx.compose.ui.test.junit4.createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsAndReachesResumedState() {
        assertEquals(Lifecycle.State.RESUMED, composeTestRule.activityRule.scenario.state)
    }

    @Test
    fun appSurvivesRecreationAfterStartup() {
        composeTestRule.activityRule.scenario.recreate()
        assertEquals(Lifecycle.State.RESUMED, composeTestRule.activityRule.scenario.state)
    }

    @Test
    fun successfulStartupNavigatesToWelcomeScreen() {
        // AppStartupViewModel completes initialization via init {}, transitioning to Success state.
        // StartupRoute observes this and calls onStartupComplete.
        // DailyBattleNavHost navigates to WelcomeRoute.
        
        // Wait for the "DAILY BATTLE" text to appear on the Welcome screen.
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasText("DAILY BATTLE")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNode(androidx.compose.ui.test.hasText("DAILY BATTLE")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("GET STARTED")).assertExists()
    }
}

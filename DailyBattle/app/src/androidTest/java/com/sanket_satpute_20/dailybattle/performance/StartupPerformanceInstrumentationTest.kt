package com.sanket_satpute_20.dailybattle.performance

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.MainActivity
import com.sanket_satpute_20.dailybattle.core.performance.StartupPerformanceTracker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumentation test verifying runtime startup performance and screen rendering latency (Sprint 24.1).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StartupPerformanceInstrumentationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = androidx.compose.ui.test.junit4.createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var tracker: StartupPerformanceTracker

    @Test
    fun startupRendersFirstScreenWithinTarget() {
        hiltRule.inject()

        // Wait until first screen renders
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasText("DAILY BATTLE")
            ).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Activity reached resumed state
        assertEquals(Lifecycle.State.RESUMED, composeTestRule.activityRule.scenario.state)

        // Verify startup metrics captured
        val metrics = tracker.metrics.value
        assertTrue("Cold start must be completed on launch", metrics.isColdStartCompleted)
        assertTrue("First screen must be recorded on launch", metrics.isFirstScreenRendered)
    }

    @Test
    fun warmStartAfterRecreationCompletesQuickly() {
        hiltRule.inject()

        val startRecreate = System.currentTimeMillis()
        composeTestRule.activityRule.scenario.recreate()
        val recreateDuration = System.currentTimeMillis() - startRecreate

        assertEquals(Lifecycle.State.RESUMED, composeTestRule.activityRule.scenario.state)
        assertTrue("Warm recreation must complete within 2000ms", recreateDuration < 2000L)
    }
}

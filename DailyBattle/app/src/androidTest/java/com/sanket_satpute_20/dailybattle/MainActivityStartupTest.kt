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
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun appStartsAndReachesResumedState() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test
    fun appSurvivesRecreationAfterStartup() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()

            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}

package com.sanket_satpute_20.dailybattle

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltTestApplication

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.sanket_satpute_20.dailybattle", appContext.packageName)
    }

    @Test
    fun appUsesHiltTestApplicationCompositionRoot() {
        // Sprint 2.4's HiltTestRunner swaps the instrumentation application for
        // HiltTestApplication so @HiltAndroidTest classes can bind fakes; this is the standard
        // Hilt testing composition root, not DailyBattleApplication.
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        assertTrue(application is HiltTestApplication)
    }
}

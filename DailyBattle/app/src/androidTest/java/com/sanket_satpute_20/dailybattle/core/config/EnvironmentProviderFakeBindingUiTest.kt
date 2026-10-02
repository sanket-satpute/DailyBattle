package com.sanket_satpute_20.dailybattle.core.config

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.MainActivity
import com.sanket_satpute_20.dailybattle.core.config.di.ConfigModule
import com.sanket_satpute_20.dailybattle.testing.FakeEnvironmentProvider
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Proves the base test infrastructure can substitute a fake binding for a UI test. */
@HiltAndroidTest
@UninstallModules(ConfigModule::class)
@RunWith(AndroidJUnit4::class)
class EnvironmentProviderFakeBindingUiTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    @JvmField
    val environmentProvider: EnvironmentProvider = FakeEnvironmentProvider(AppEnvironment.Testing)

    @Test
    fun appLaunchesWithTheFakeEnvironmentBindingInstalled() {
        composeRule.onRoot().assertIsDisplayed()

        val entryPoint = EntryPointAccessors.fromApplication(
            composeRule.activity.applicationContext,
            EnvironmentProviderEntryPoint::class.java,
        )
        assertEquals(AppEnvironment.Testing, entryPoint.environmentProvider().current)
    }
}

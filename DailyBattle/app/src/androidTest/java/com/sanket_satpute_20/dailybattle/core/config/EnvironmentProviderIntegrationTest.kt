package com.sanket_satpute_20.dailybattle.core.config

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.EntryPointAccessors
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies the real Hilt graph resolves the debug instrumentation build to Development. */
@RunWith(AndroidJUnit4::class)
class EnvironmentProviderIntegrationTest {
    @Test
    fun debugBuildResolvesToDevelopmentEnvironment() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            EnvironmentProviderEntryPoint::class.java,
        )

        assertEquals(AppEnvironment.Development, entryPoint.environmentProvider().current)
    }
}

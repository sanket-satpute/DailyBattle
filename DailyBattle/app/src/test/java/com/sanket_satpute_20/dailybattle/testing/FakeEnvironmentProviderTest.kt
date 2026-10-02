package com.sanket_satpute_20.dailybattle.testing

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeEnvironmentProviderTest {
    @Test
    fun `defaults to the testing environment`() {
        assertEquals(AppEnvironment.Testing, FakeEnvironmentProvider().current)
    }

    @Test
    fun `can be configured to a specific environment`() {
        assertEquals(
            AppEnvironment.Production,
            FakeEnvironmentProvider(AppEnvironment.Production).current,
        )
    }
}

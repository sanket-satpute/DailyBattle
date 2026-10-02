package com.sanket_satpute_20.dailybattle.core.config

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildTypeEnvironmentProviderTest {
    @Test
    fun `debug build resolves to development`() {
        val provider = BuildTypeEnvironmentProvider(isDebugBuild = true)

        assertEquals(AppEnvironment.Development, provider.current)
    }

    @Test
    fun `non-debug build resolves to production`() {
        val provider = BuildTypeEnvironmentProvider(isDebugBuild = false)

        assertEquals(AppEnvironment.Production, provider.current)
    }
}

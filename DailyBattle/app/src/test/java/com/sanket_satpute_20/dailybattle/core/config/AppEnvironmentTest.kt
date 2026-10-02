package com.sanket_satpute_20.dailybattle.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppEnvironmentTest {
    @Test
    fun `environment values are distinct`() {
        val values = AppEnvironment.entries

        assertEquals(3, values.size)
        assertNotEquals(AppEnvironment.Development, AppEnvironment.Testing)
        assertNotEquals(AppEnvironment.Testing, AppEnvironment.Production)
        assertNotEquals(AppEnvironment.Development, AppEnvironment.Production)
    }
}

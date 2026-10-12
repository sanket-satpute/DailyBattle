package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkConfigTest {

    @Test
    fun defaultTimeoutsMatchApprovedSpecifications() {
        val config = NetworkConfig()

        assertEquals(15L, config.connectTimeoutSeconds)
        assertEquals(15L, config.readTimeoutSeconds)
        assertEquals(15L, config.writeTimeoutSeconds)
    }

    @Test
    fun baseUrlIsExplicitlyUnconfiguredByDefault() {
        val config = NetworkConfig()

        assertNull("Base URL must be unconfigured until backend decisions are resolved", config.baseUrl)
        assertEquals(AppEnvironment.Development, config.environment)
    }

    @Test
    fun headerConstantsMatchApprovedJsonSpecifications() {
        assertEquals("Accept", NetworkConfig.HEADER_ACCEPT)
        assertEquals("application/json", NetworkConfig.CONTENT_TYPE_JSON)
    }
}

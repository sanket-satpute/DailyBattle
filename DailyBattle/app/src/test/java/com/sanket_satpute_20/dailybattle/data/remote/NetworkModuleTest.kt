package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.data.di.NetworkModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkModuleTest {

    private val fakeEnvironmentProvider = object : EnvironmentProvider {
        override val current: AppEnvironment = AppEnvironment.Development
    }

    @Test
    fun providesJsonSingletonInstance() {
        val json = NetworkModule.provideJson()
        assertNotNull(json)
    }

    @Test
    fun providesNetworkConfigWithUnconfiguredBaseUrl() {
        val config = NetworkModule.provideNetworkConfig(fakeEnvironmentProvider)

        assertEquals(15L, config.connectTimeoutSeconds)
        assertEquals(15L, config.readTimeoutSeconds)
        assertEquals(15L, config.writeTimeoutSeconds)
        assertNull(config.baseUrl)
        assertEquals(AppEnvironment.Development, config.environment)
    }

    @Test
    fun providesAuthTokenProvider() {
        val provider = NetworkModule.provideAuthTokenProvider()
        assertNotNull(provider)
    }

    @Test
    fun providesOkHttpClientWithConfiguredTimeouts() {
        val config = NetworkModule.provideNetworkConfig(fakeEnvironmentProvider)
        val authProvider = NetworkModule.provideAuthTokenProvider()
        val client = NetworkModule.provideOkHttpClient(config, authProvider)

        assertNotNull(client)
        assertEquals(15_000, client.connectTimeoutMillis)
        assertEquals(15_000, client.readTimeoutMillis)
        assertEquals(15_000, client.writeTimeoutMillis)
    }

    @Test
    fun providesRetrofitBuilder() {
        val config = NetworkModule.provideNetworkConfig(fakeEnvironmentProvider)
        val authProvider = NetworkModule.provideAuthTokenProvider()
        val client = NetworkModule.provideOkHttpClient(config, authProvider)
        val json = NetworkModule.provideJson()

        val builder = NetworkModule.provideRetrofitBuilder(client, json, config)
        assertNotNull(builder)
    }
}

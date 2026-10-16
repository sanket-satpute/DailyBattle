package com.sanket_satpute_20.dailybattle.core.config

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Sprint 25.4 — Build Configuration Verification Tests:
 * - Release build environment resolution
 * - Production endpoint verification (HTTPS, no localhost, no debug paths)
 * - Production configuration (no debug flags, no debug headers)
 * - No test data / demo seeds in production paths
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Sprint254BuildConfigurationTest {

    private val testDispatcher = StandardTestDispatcher()
    private val debugGuard = DebugEndpointGuard()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `production environment provider resolves to Production for release build`() {
        val releaseProvider = BuildTypeEnvironmentProvider(isDebugBuild = false)
        assertEquals(AppEnvironment.Production, releaseProvider.current)
    }

    @Test
    fun `production endpoint is secure HTTPS and not localhost`() {
        val prodUrl = NetworkConfig.resolveBaseUrl(AppEnvironment.Production)

        assertTrue("Production URL must start with https://", prodUrl.startsWith("https://"))
        assertFalse("Production URL must not be localhost", prodUrl.contains("localhost"))
        assertFalse("Production URL must not be loopback", prodUrl.contains("127.0.0.1"))
        assertFalse("Production URL must not be emulator loopback", prodUrl.contains("10.0.2.2"))
        assertEquals("https://api.dailybattle.com/", prodUrl)
    }

    @Test
    fun `debug mode flag is strictly forbidden in production`() {
        val debugAttempt = debugGuard.validateDebugModeAllowed(
            isDebugRequested = true,
            environment = AppEnvironment.Production
        )
        assertTrue(debugAttempt is DomainResult.Failure)
        assertEquals(AppError.Authorization, (debugAttempt as DomainResult.Failure).error)

        val nonDebug = debugGuard.validateDebugModeAllowed(
            isDebugRequested = false,
            environment = AppEnvironment.Production
        )
        assertTrue(nonDebug is DomainResult.Success)
    }

    @Test
    fun `debug and mock endpoints are strictly blocked in production`() {
        val prohibitedEndpoints = listOf(
            "https://api.dailybattle.com/debug/reset",
            "https://api.dailybattle.com/test/users",
            "https://api.dailybattle.com/mock/battle",
            "https://api.dailybattle.com/admin/clear",
            "https://api.dailybattle.com/seed/data",
            "https://api.dailybattle.com/backdoor/login"
        )

        prohibitedEndpoints.forEach { endpoint ->
            val result = debugGuard.validateEndpointUrl(endpoint, AppEnvironment.Production)
            assertTrue("Expected prohibited endpoint $endpoint to fail", result is DomainResult.Failure)
        }

        val legitimateEndpoint = "https://api.dailybattle.com/v1/battle/daily"
        val validResult = debugGuard.validateEndpointUrl(legitimateEndpoint, AppEnvironment.Production)
        assertTrue("Legitimate endpoint must pass in production", validResult is DomainResult.Success)
    }

    @Test
    fun `debug headers are strictly blocked in production`() {
        val prohibitedHeaders = listOf(
            mapOf("x-debug-mode" to "true"),
            mapOf("x-mock-response" to "true"),
            mapOf("x-test-override" to "true"),
            mapOf("X-Bypass-Validation" to "1")
        )

        prohibitedHeaders.forEach { headers ->
            val result = debugGuard.validateHeaders(headers, AppEnvironment.Production)
            assertTrue("Expected debug header to be rejected in production", result is DomainResult.Failure)
        }

        val safeHeaders = mapOf(
            "Authorization" to "Bearer token123",
            "Content-Type" to "application/json"
        )
        val validResult = debugGuard.validateHeaders(safeHeaders, AppEnvironment.Production)
        assertTrue("Safe headers must pass in production", validResult is DomainResult.Success)
    }

    @Test
    fun `demo seed names and scores are rejected in production paths`() {
        val demoNames = listOf("rahul", "test_user", "demo_player")
        demoNames.forEach { name ->
            val result = debugGuard.validateSeedDataIsolation(name, 100, AppEnvironment.Production)
            assertTrue("Demo name $name must be rejected in production", result is DomainResult.Failure)
        }

        val demoScores = listOf(901, 914, 927)
        demoScores.forEach { score ->
            val result = debugGuard.validateSeedDataIsolation("real_player", score, AppEnvironment.Production)
            assertTrue("Demo score $score must be rejected in production", result is DomainResult.Failure)
        }

        val validUser = debugGuard.validateSeedDataIsolation("genuine_player", 850, AppEnvironment.Production)
        assertTrue("Genuine user and score must pass in production", validUser is DomainResult.Success)
    }

    @Test
    fun `home view model in production isolates placeholder data`() = runTest {
        val prodProvider = object : EnvironmentProvider {
            override val current: AppEnvironment = AppEnvironment.Production
        }
        val fakeRepo = object : UserRepository {
            override suspend fun saveBattleName(name: String) {}
            override suspend fun getBattleName(): String = "Commander"
        }

        val homeViewModel = HomeViewModel(
            userRepository = fakeRepo,
            environmentProvider = prodProvider
        )
        homeViewModel.loadHomeData()
        advanceUntilIdle()

        val state = homeViewModel.state.value
        assertEquals("Commander", state.userName)
        assertEquals(0, state.momentumDays)
        assertFalse(state.hasRival)
        assertEquals("", state.rivalName)
        assertEquals(0, state.rivalScore)
        assertEquals(0, state.userScore)
        assertEquals(0, state.pointsToCatch)
    }
}

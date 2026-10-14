package com.sanket_satpute_20.dailybattle.core.security.debug

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DebugEndpointGuardTest {

    private lateinit var guard: DebugEndpointGuard

    @Before
    fun setUp() {
        guard = DebugEndpointGuard()
    }

    @Test
    fun `validateEndpointUrl blocks debug, test, and mock routes in production`() {
        val debugUrl = "/api/v1/debug/reset_scores"
        val mockUrl = "/api/v1/mock/today_battle"
        val adminUrl = "/api/v1/admin/purge"

        assertTrue(guard.validateEndpointUrl(debugUrl, AppEnvironment.Production) is DomainResult.Failure)
        assertTrue(guard.validateEndpointUrl(mockUrl, AppEnvironment.Production) is DomainResult.Failure)
        assertTrue(guard.validateEndpointUrl(adminUrl, AppEnvironment.Production) is DomainResult.Failure)

        // Same routes allowed in development
        assertTrue(guard.validateEndpointUrl(debugUrl, AppEnvironment.Development) is DomainResult.Success)
        assertTrue(guard.validateEndpointUrl(mockUrl, AppEnvironment.Development) is DomainResult.Success)
    }

    @Test
    fun `validateEndpointUrl allows official api routes in production`() {
        val prodUrl = "/api/v1/battles/today"
        val result = guard.validateEndpointUrl(prodUrl, AppEnvironment.Production)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateHeaders blocks debug override headers in production`() {
        val debugHeaders = mapOf("X-Debug-Mode" to "true")
        val result = guard.validateHeaders(debugHeaders, AppEnvironment.Production)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)

        // Allowed in development
        assertTrue(guard.validateHeaders(debugHeaders, AppEnvironment.Development) is DomainResult.Success)
    }

    @Test
    fun `validateHeaders allows standard headers in production`() {
        val standardHeaders = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json"
        )
        val result = guard.validateHeaders(standardHeaders, AppEnvironment.Production)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateSeedDataIsolation blocks demo values Rahul and 901 in production`() {
        // Rahul is an illustrative UI demo user (§65)
        assertTrue(guard.validateSeedDataIsolation("Rahul", 800, AppEnvironment.Production) is DomainResult.Failure)
        // 901 is an illustrative demo score (§65)
        assertTrue(guard.validateSeedDataIsolation("PlayerOne", 901, AppEnvironment.Production) is DomainResult.Failure)
        assertTrue(guard.validateSeedDataIsolation("PlayerOne", 914, AppEnvironment.Production) is DomainResult.Failure)
        assertTrue(guard.validateSeedDataIsolation("PlayerOne", 927, AppEnvironment.Production) is DomainResult.Failure)

        // Genuine values allowed in production
        assertTrue(guard.validateSeedDataIsolation("PlayerOne", 850, AppEnvironment.Production) is DomainResult.Success)

        // Demo values permitted in development
        assertTrue(guard.validateSeedDataIsolation("Rahul", 901, AppEnvironment.Development) is DomainResult.Success)
    }

    @Test
    fun `validateDebugModeAllowed blocks debug mode requests in production`() {
        assertTrue(guard.validateDebugModeAllowed(isDebugRequested = true, environment = AppEnvironment.Production) is DomainResult.Failure)
        assertTrue(guard.validateDebugModeAllowed(isDebugRequested = false, environment = AppEnvironment.Production) is DomainResult.Success)
        assertTrue(guard.validateDebugModeAllowed(isDebugRequested = true, environment = AppEnvironment.Development) is DomainResult.Success)
    }
}

package com.sanket_satpute_20.dailybattle.release

import com.sanket_satpute_20.dailybattle.BuildConfig
import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.BuildTypeEnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Sprint 26.1 — Release Candidate Build Verification Tests
 *
 * Verifies:
 * - Release Candidate application configuration (package, version, SDK constraints)
 * - Release environment resolution (Production for release candidate builds)
 * - Production network endpoint and security guards
 * - Local persistence database schema readiness
 * - Release Candidate build artifacts existence (APK, AAB) and size constraints
 */
class Sprint261ReleaseCandidateBuildTest {

    private val debugGuard = DebugEndpointGuard()

    @Test
    fun `release candidate application configuration conforms to specifications`() {
        assertEquals("com.sanket_satpute_20.dailybattle", BuildConfig.APPLICATION_ID)
        assertEquals("1.0", BuildConfig.VERSION_NAME)
        assertEquals(1, BuildConfig.VERSION_CODE)
    }

    @Test
    fun `release candidate environment resolves to Production for release build`() {
        val releaseProvider = BuildTypeEnvironmentProvider(isDebugBuild = false)
        assertEquals(AppEnvironment.Production, releaseProvider.current)
    }

    @Test
    fun `release candidate production endpoint is strictly secure HTTPS`() {
        val prodUrl = NetworkConfig.resolveBaseUrl(AppEnvironment.Production)
        assertTrue("Production endpoint must use HTTPS", prodUrl.startsWith("https://"))
        assertFalse("Production endpoint must not be localhost", prodUrl.contains("localhost"))
        assertFalse("Production endpoint must not be loopback", prodUrl.contains("127.0.0.1"))
        assertEquals("https://api.dailybattle.com/", prodUrl)
    }

    @Test
    fun `release candidate prohibits debug flags, headers, and mock endpoints`() {
        val debugAttempt = debugGuard.validateDebugModeAllowed(
            isDebugRequested = true,
            environment = AppEnvironment.Production
        )
        assertTrue(debugAttempt is DomainResult.Failure)
        assertEquals(AppError.Authorization, (debugAttempt as DomainResult.Failure).error)

        val debugHeaderAttempt = debugGuard.validateHeaders(
            headers = mapOf("X-Debug-Mode" to "true"),
            environment = AppEnvironment.Production
        )
        assertTrue(debugHeaderAttempt is DomainResult.Failure)

        val debugEndpointAttempt = debugGuard.validateEndpointUrl(
            url = "https://api.dailybattle.com/mock/battle",
            environment = AppEnvironment.Production
        )
        assertTrue(debugEndpointAttempt is DomainResult.Failure)
    }

    @Test
    fun `release candidate database schema version is stable and synchronized`() {
        assertEquals(1, DailyBattleDatabase.DATABASE_VERSION)
    }

    @Test
    fun `release candidate APK artifact exists and satisfies size budget`() {
        val apkFile = File("build/outputs/apk/release/app-release.apk")
        assertTrue("Release Candidate APK must exist in build outputs", apkFile.exists())
        assertTrue("Release Candidate APK must be non-empty", apkFile.length() > 0)
        // Under 50MB budget as specified in performance requirements
        val sizeInMb = apkFile.length() / (1024 * 1024)
        assertTrue("Release APK size ($sizeInMb MB) must be within 50MB budget", sizeInMb < 50)
    }

    @Test
    fun `release candidate AAB bundle artifact exists and satisfies size budget`() {
        val aabFile = File("build/outputs/bundle/release/app-release.aab")
        assertTrue("Release Candidate AAB bundle must exist in build outputs", aabFile.exists())
        assertTrue("Release Candidate AAB bundle must be non-empty", aabFile.length() > 0)
        // Under 30MB budget for AAB bundle download size
        val sizeInMb = aabFile.length() / (1024 * 1024)
        assertTrue("Release AAB size ($sizeInMb MB) must be within 30MB budget", sizeInMb < 30)
    }
}

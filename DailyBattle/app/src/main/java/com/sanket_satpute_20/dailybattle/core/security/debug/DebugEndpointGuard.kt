package com.sanket_satpute_20.dailybattle.core.security.debug

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Guards against the leakage or execution of debug endpoints, demo seeds, and test backdoors in production per:
 * - `ROADMAP.md` Sprint 24.6 (Security — Debug Endpoints)
 * - `16_DEFINITION_OF_DONE.md` line 1021 ("No debug-only behavior in production path")
 * - `07_TECHNICAL_ARCHITECTURE.md` §64 & §65 (Seed / Demo Data Isolation)
 */
class DebugEndpointGuard {

    private val PROHIBITED_PROD_PATH_SEGMENTS = listOf(
        "/debug/",
        "/test/",
        "/mock/",
        "/admin/",
        "/internal/",
        "/seed/",
        "/backdoor/"
    )

    private val PROHIBITED_PROD_HEADERS = listOf(
        "x-debug-mode",
        "x-mock-response",
        "x-test-override",
        "x-bypass-validation"
    )

    // Seed / demo names and scores explicitly forbidden from shipping in production paths (§65)
    private val DEMO_NAMES = listOf("rahul", "test_user", "demo_player")
    private val DEMO_SCORES = setOf(901, 914, 927)

    /**
     * Validates whether a target endpoint URL is permitted for the active environment.
     */
    fun validateEndpointUrl(url: String, environment: AppEnvironment): DomainResult<Unit> {
        if (environment != AppEnvironment.Production) {
            return DomainResult.Success(Unit)
        }

        val lowerUrl = url.lowercase()
        val hasProhibitedSegment = PROHIBITED_PROD_PATH_SEGMENTS.any { segment ->
            lowerUrl.contains(segment)
        }

        return if (hasProhibitedSegment) {
            DomainResult.Failure(AppError.Authorization)
        } else {
            DomainResult.Success(Unit)
        }
    }

    /**
     * Validates that HTTP headers do not contain debug bypass flags in production.
     */
    fun validateHeaders(headers: Map<String, String>, environment: AppEnvironment): DomainResult<Unit> {
        if (environment != AppEnvironment.Production) {
            return DomainResult.Success(Unit)
        }

        val lowerKeys = headers.keys.map { it.lowercase() }
        val hasProhibitedHeader = PROHIBITED_PROD_HEADERS.any { header ->
            lowerKeys.contains(header)
        }

        return if (hasProhibitedHeader) {
            DomainResult.Failure(AppError.Authorization)
        } else {
            DomainResult.Success(Unit)
        }
    }

    /**
     * Validates that demo/seed values (Rahul, 901, 914, 927) are isolated from production builds (§65).
     */
    fun validateSeedDataIsolation(
        userName: String,
        score: Int,
        environment: AppEnvironment
    ): DomainResult<Unit> {
        if (environment != AppEnvironment.Production) {
            return DomainResult.Success(Unit)
        }

        val lowerName = userName.lowercase().trim()
        if (DEMO_NAMES.contains(lowerName) || DEMO_SCORES.contains(score)) {
            // Demo seed data must never silently ship or execute as production data
            return DomainResult.Failure(AppError.Validation)
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Validates that debug flags or test modes cannot be enabled in production.
     */
    fun validateDebugModeAllowed(isDebugRequested: Boolean, environment: AppEnvironment): DomainResult<Unit> {
        if (environment == AppEnvironment.Production && isDebugRequested) {
            return DomainResult.Failure(AppError.Authorization)
        }
        return DomainResult.Success(Unit)
    }
}

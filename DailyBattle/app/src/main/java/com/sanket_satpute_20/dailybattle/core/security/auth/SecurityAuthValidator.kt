package com.sanket_satpute_20.dailybattle.core.security.auth

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Validates authentication and authorization contracts per:
 * - `09_OFFLINE_AND_ERROR_HANDLING.md` §37 (Authentication Failure) & §38 (Authorization Failure)
 * - `15_ANTIGRAVITY_RULES.md` Rule 79 (Authentication Rule)
 * - `ROADMAP.md` Sprint 24.6 (Security — Authentication & Authorization)
 *
 * Adheres strictly to the PENDING status of DATA-PENDING-003 and ERROR-PENDING-016:
 * - Does NOT fabricate or assume a specific identity provider (Firebase, Google, OTP).
 * - Distinguishes 401 (Authentication) from 403 (Authorization) and network/offline errors.
 * - Protects against retry storms on expired credentials by enforcing a maximum retry limit.
 * - Enforces resource-level authorization boundaries.
 */
class SecurityAuthValidator(
    private val maxAuthRetries: Int = DEFAULT_MAX_AUTH_RETRIES
) {

    // Tracks consecutive authentication failure counts per user/session to prevent infinite loops
    private val authFailureCounters = ConcurrentHashMap<String, AtomicInteger>()

    /**
     * Validates that an authentication token is present and structurally non-blank
     * when an endpoint requires authentication.
     *
     * @param token The token provided by the auth layer.
     * @param endpoint Requires authentication if true.
     * @return [DomainResult.Success] if valid, or [DomainResult.Failure] with [AppError.Authentication].
     */
    fun validateTokenPresence(
        token: String?,
        endpointRequiresAuth: Boolean
    ): DomainResult<Unit> {
        if (!endpointRequiresAuth) {
            return DomainResult.Success(Unit)
        }

        if (token.isNullOrBlank()) {
            return DomainResult.Failure(AppError.Authentication)
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Evaluates an HTTP status code against authentication and authorization contracts.
     *
     * Distinguishes 401 Unauthorized from 403 Forbidden without confounding them with Network errors.
     */
    fun evaluateHttpStatus(statusCode: Int): DomainResult<Unit> {
        return when (statusCode) {
            401 -> DomainResult.Failure(AppError.Authentication)
            403 -> DomainResult.Failure(AppError.Authorization)
            in 200..299 -> DomainResult.Success(Unit)
            400, 422 -> DomainResult.Failure(AppError.Validation)
            in 500..599 -> DomainResult.Failure(AppError.Server)
            else -> DomainResult.Failure(AppError.Unknown)
        }
    }

    /**
     * Checks if a request may be retried after an authentication failure.
     *
     * Per `09_OFFLINE_AND_ERROR_HANDLING.md` §37:
     * "Do not repeatedly retry an expired credential forever."
     *
     * @param identityKey Unique key identifying the failing credential/session.
     * @return true if retry is permissible; false if retry threshold is exceeded.
     */
    fun recordAndCheckRetryEligibility(identityKey: String): Boolean {
        val counter = authFailureCounters.computeIfAbsent(identityKey) { AtomicInteger(0) }
        val currentCount = counter.incrementAndGet()
        return currentCount <= maxAuthRetries
    }

    /**
     * Resets failure counter upon successful authentication or recovery.
     */
    fun resetFailureCounter(identityKey: String) {
        authFailureCounters.remove(identityKey)
    }

    /**
     * Validates resource authorization: verifies that the authenticated user possesses
     * ownership or permission to access or modify a resource owned by [resourceOwnerId].
     *
     * @param authenticatedUserId The ID of the currently authenticated caller.
     * @param resourceOwnerId The ID of the user who owns the target resource.
     * @param isPublicResource Whether the target resource allows public read access.
     */
    fun validateResourceAuthorization(
        authenticatedUserId: UserId?,
        resourceOwnerId: UserId,
        isPublicResource: Boolean = false
    ): DomainResult<Unit> {
        if (isPublicResource) {
            return DomainResult.Success(Unit)
        }

        if (authenticatedUserId == null) {
            return DomainResult.Failure(AppError.Authentication)
        }

        if (authenticatedUserId != resourceOwnerId) {
            return DomainResult.Failure(AppError.Authorization)
        }

        return DomainResult.Success(Unit)
    }

    companion object {
        const val DEFAULT_MAX_AUTH_RETRIES = 1
    }
}

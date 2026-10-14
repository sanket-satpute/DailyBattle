package com.sanket_satpute_20.dailybattle.core.security.auth

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityAuthValidatorTest {

    private lateinit var validator: SecurityAuthValidator

    @Before
    fun setUp() {
        validator = SecurityAuthValidator(maxAuthRetries = 1)
    }

    @Test
    fun `validateTokenPresence returns failure when token missing on auth required endpoint`() {
        val result = validator.validateTokenPresence(token = null, endpointRequiresAuth = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateTokenPresence returns failure when token is blank on auth required endpoint`() {
        val result = validator.validateTokenPresence(token = "   ", endpointRequiresAuth = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateTokenPresence returns success when valid token present on auth required endpoint`() {
        val result = validator.validateTokenPresence(token = "valid_token_xyz", endpointRequiresAuth = true)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateTokenPresence returns success when endpoint does not require auth even if token null`() {
        val result = validator.validateTokenPresence(token = null, endpointRequiresAuth = false)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `evaluateHttpStatus maps 401 to Authentication error`() {
        val result = validator.evaluateHttpStatus(401)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun `evaluateHttpStatus maps 403 to Authorization error`() {
        val result = validator.evaluateHttpStatus(403)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun `evaluateHttpStatus maps 200 to Success`() {
        val result = validator.evaluateHttpStatus(200)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `evaluateHttpStatus maps 400 and 422 to Validation error`() {
        assertEquals(AppError.Validation, (validator.evaluateHttpStatus(400) as DomainResult.Failure).error)
        assertEquals(AppError.Validation, (validator.evaluateHttpStatus(422) as DomainResult.Failure).error)
    }

    @Test
    fun `evaluateHttpStatus maps 500 to Server error`() {
        assertEquals(AppError.Server, (validator.evaluateHttpStatus(500) as DomainResult.Failure).error)
    }

    @Test
    fun `recordAndCheckRetryEligibility bounds retries to prevent infinite auth loops`() {
        val sessionKey = "session_expired_creds"
        // 1st retry: permitted
        assertTrue(validator.recordAndCheckRetryEligibility(sessionKey))
        // 2nd retry: exceeded max limit
        assertFalse(validator.recordAndCheckRetryEligibility(sessionKey))
        // 3rd retry: still exceeded
        assertFalse(validator.recordAndCheckRetryEligibility(sessionKey))

        // Reset clears counter
        validator.resetFailureCounter(sessionKey)
        assertTrue(validator.recordAndCheckRetryEligibility(sessionKey))
    }

    @Test
    fun `validateResourceAuthorization allows resource owner access`() {
        val userId = UserId("user_100")
        val result = validator.validateResourceAuthorization(
            authenticatedUserId = userId,
            resourceOwnerId = userId,
            isPublicResource = false
        )
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateResourceAuthorization rejects cross-user access with Authorization error`() {
        val caller = UserId("user_caller")
        val owner = UserId("user_target")
        val result = validator.validateResourceAuthorization(
            authenticatedUserId = caller,
            resourceOwnerId = owner,
            isPublicResource = false
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateResourceAuthorization rejects unauthenticated access with Authentication error`() {
        val owner = UserId("user_target")
        val result = validator.validateResourceAuthorization(
            authenticatedUserId = null,
            resourceOwnerId = owner,
            isPublicResource = false
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateResourceAuthorization permits public resource even with null user`() {
        val owner = UserId("user_target")
        val result = validator.validateResourceAuthorization(
            authenticatedUserId = null,
            resourceOwnerId = owner,
            isPublicResource = true
        )
        assertTrue(result is DomainResult.Success)
    }
}

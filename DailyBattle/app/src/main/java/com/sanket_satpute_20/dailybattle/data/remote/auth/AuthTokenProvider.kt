package com.sanket_satpute_20.dailybattle.data.remote.auth

/**
 * Abstraction for retrieving an authentication token.
 *
 * Implements partial approval for Sprint 16.1 while keeping TECH-PENDING-011 and
 * DATA-PENDING-003 unresolved regarding the actual authentication model.
 */
interface AuthTokenProvider {
    suspend fun getAuthToken(): String?
}

/**
 * Default provider while authentication decisions remain unresolved.
 *
 * Never fabricates tokens or silently bypasses authentication requirements.
 */
class UnconfiguredAuthTokenProvider : AuthTokenProvider {
    override suspend fun getAuthToken(): String? = null
}

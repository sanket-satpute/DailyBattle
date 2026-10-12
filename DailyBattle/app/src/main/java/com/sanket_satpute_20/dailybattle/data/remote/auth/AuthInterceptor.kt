package com.sanket_satpute_20.dailybattle.data.remote.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that attaches authorization tokens to requests requiring authentication.
 *
 * Adheres to strict protocol:
 * 1. Does not invent or hardcode tokens.
 * 2. If authentication is required but no token is available, throws [AuthenticationRequiredException]
 *    rather than silently proceeding as anonymous.
 */
class AuthInterceptor(
    private val authTokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authRequired = originalRequest.header(HEADER_AUTH_REQUIRED)?.equals("true", ignoreCase = true) == true

        if (!authRequired) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            authTokenProvider.getAuthToken()
        }

        if (token.isNullOrBlank()) {
            throw AuthenticationRequiredException("Request requires authentication, but no valid token is provided.")
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .removeHeader(HEADER_AUTH_REQUIRED)
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    companion object {
        const val HEADER_AUTH_REQUIRED = "X-DailyBattle-Auth-Required"
    }
}

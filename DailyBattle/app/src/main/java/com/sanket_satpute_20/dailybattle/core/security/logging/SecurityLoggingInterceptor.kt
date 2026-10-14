package com.sanket_satpute_20.dailybattle.core.security.logging

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that ensures all network logging is strictly sanitized of auth tokens,
 * passwords, and sensitive headers per `09_OFFLINE_AND_ERROR_HANDLING.md` §62 and `ROADMAP.md` Sprint 24.6.
 */
class SecurityLoggingInterceptor(
    private val environment: AppEnvironment,
    private val logger: ((String) -> Unit)? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (logger != null) {
            val sanitizedUrl = SecurityLogSanitizer.sanitizeUrl(request.url.toString())
            val method = request.method
            logger.invoke("[Network Request] $method $sanitizedUrl")

            // Log sanitized headers
            for (headerName in request.headers.names()) {
                val rawValue = request.header(headerName) ?: ""
                val sanitizedValue = SecurityLogSanitizer.sanitizeHeader(headerName, rawValue)
                logger.invoke("  Header $headerName: $sanitizedValue")
            }
        }

        val response = chain.proceed(request)

        if (logger != null) {
            val sanitizedUrl = SecurityLogSanitizer.sanitizeUrl(response.request.url.toString())
            logger.invoke("[Network Response] ${response.code} $sanitizedUrl")

            for (headerName in response.headers.names()) {
                val rawValue = response.header(headerName) ?: ""
                val sanitizedValue = SecurityLogSanitizer.sanitizeHeader(headerName, rawValue)
                logger.invoke("  Header $headerName: $sanitizedValue")
            }
        }

        return response
    }
}

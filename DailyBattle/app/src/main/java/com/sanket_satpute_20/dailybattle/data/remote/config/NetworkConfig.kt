package com.sanket_satpute_20.dailybattle.data.remote.config

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment

/**
 * Centralized networking configuration for Daily Battle.
 *
 * Implements approved technical decision TECH-PENDING-009 timeouts and header constraints.
 * Base URL remains explicitly unconfigured per pending backend decision TECH-PENDING-008.
 */
data class NetworkConfig(
    val connectTimeoutSeconds: Long = DEFAULT_CONNECT_TIMEOUT_SECONDS,
    val readTimeoutSeconds: Long = DEFAULT_READ_TIMEOUT_SECONDS,
    val writeTimeoutSeconds: Long = DEFAULT_WRITE_TIMEOUT_SECONDS,
    val baseUrl: String? = null,
    val environment: AppEnvironment = AppEnvironment.Development,
) {
    companion object {
        const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 15L
        const val DEFAULT_READ_TIMEOUT_SECONDS = 15L
        const val DEFAULT_WRITE_TIMEOUT_SECONDS = 15L
        const val HEADER_ACCEPT = "Accept"
        const val CONTENT_TYPE_JSON = "application/json"
        const val PRODUCTION_BASE_URL = "https://api.dailybattle.com/"

        fun resolveBaseUrl(environment: AppEnvironment, overrideUrl: String? = null): String {
            if (!overrideUrl.isNullOrBlank()) return overrideUrl
            return when (environment) {
                AppEnvironment.Production -> PRODUCTION_BASE_URL
                AppEnvironment.Testing -> "https://test-api.dailybattle.com/"
                AppEnvironment.Development -> "https://dev-api.dailybattle.com/"
            }
        }
    }
}

package com.sanket_satpute_20.dailybattle.data.remote.interceptor

import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Standard header interceptor for JSON API requests.
 *
 * Implements approved technical decision:
 * Sets 'Accept: application/json' where appropriate.
 * Does not invent unapproved custom headers or request IDs.
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()

        if (original.header(NetworkConfig.HEADER_ACCEPT) == null) {
            requestBuilder.header(NetworkConfig.HEADER_ACCEPT, NetworkConfig.CONTENT_TYPE_JSON)
        }

        return chain.proceed(requestBuilder.build())
    }
}

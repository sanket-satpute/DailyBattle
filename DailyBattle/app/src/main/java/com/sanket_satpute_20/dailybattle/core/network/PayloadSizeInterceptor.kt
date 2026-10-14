package com.sanket_satpute_20.dailybattle.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor auditing network request and response payload sizes.
 *
 * Implements Sprint 24.5 (oversized payloads check) per `docs/ROADMAP.md`:
 * - Ensures gzip/deflate compression header 'Accept-Encoding' is requested.
 * - Inspects Content-Length on requests and responses.
 * - Records byte volume with [NetworkEfficiencyTracker].
 */
@Singleton
class PayloadSizeInterceptor @Inject constructor(
    private val tracker: NetworkEfficiencyTracker,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 1. Audit request payload
        val requestBody = original.body
        val requestBytes = requestBody?.contentLength() ?: 0L
        if (requestBytes > 0) {
            tracker.recordPayloadSize(requestBytes)
        }

        // 2. Ensure compression header
        val requestBuilder = original.newBuilder()
        if (original.header("Accept-Encoding") == null) {
            requestBuilder.header("Accept-Encoding", "gzip, deflate")
        }

        val request = requestBuilder.build()
        tracker.recordRequest(requestBytes)

        // 3. Execute call
        val response = chain.proceed(request)

        // 4. Audit response payload
        val responseBody = response.body
        val responseBytes = responseBody?.contentLength() ?: 0L
        if (responseBytes > 0) {
            tracker.recordPayloadSize(responseBytes)
        }

        return response
    }
}

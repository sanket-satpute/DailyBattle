package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthInterceptor
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthenticationRequiredException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun unauthenticatedRequestPassesThroughWithoutAuthHeader() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(object : AuthTokenProvider {
                override suspend fun getAuthToken(): String? = "unused_token"
            }))
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/public-endpoint"))
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)

        val recorded = server.takeRequest()
        assertNull("Unauthenticated request must not have Authorization header", recorded.getHeader("Authorization"))
    }

    @Test
    fun authenticatedRequestInjectsBearerTokenWhenProviderSuppliesToken() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(object : AuthTokenProvider {
                override suspend fun getAuthToken(): String = "valid_test_jwt_token"
            }))
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/secure-endpoint"))
            .header(AuthInterceptor.HEADER_AUTH_REQUIRED, "true")
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)

        val recorded = server.takeRequest()
        assertEquals("Bearer valid_test_jwt_token", recorded.getHeader("Authorization"))
        assertNull("Internal auth marker header must be removed", recorded.getHeader(AuthInterceptor.HEADER_AUTH_REQUIRED))
    }

    @Test(expected = AuthenticationRequiredException::class)
    fun authenticatedRequestThrowsExceptionWhenProviderReturnsNullToken() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(object : AuthTokenProvider {
                override suspend fun getAuthToken(): String? = null
            }))
            .build()

        val request = Request.Builder()
            .url(server.url("/secure-endpoint"))
            .header(AuthInterceptor.HEADER_AUTH_REQUIRED, "true")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = AuthenticationRequiredException::class)
    fun authenticatedRequestThrowsExceptionWhenProviderReturnsBlankToken() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(object : AuthTokenProvider {
                override suspend fun getAuthToken(): String = "   "
            }))
            .build()

        val request = Request.Builder()
            .url(server.url("/secure-endpoint"))
            .header(AuthInterceptor.HEADER_AUTH_REQUIRED, "true")
            .build()

        client.newCall(request).execute()
    }
}

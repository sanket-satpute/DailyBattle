package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.interceptor.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HeaderInterceptorTest {

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
    fun addsAcceptJsonHeaderWhenAbsent() {
        val client = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/api/data"))
            .build()

        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals("application/json", recorded.getHeader("Accept"))
    }

    @Test
    fun preservesExistingCustomAcceptHeader() {
        val client = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/api/text"))
            .header("Accept", "text/plain")
            .build()

        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals("text/plain", recorded.getHeader("Accept"))
    }
}

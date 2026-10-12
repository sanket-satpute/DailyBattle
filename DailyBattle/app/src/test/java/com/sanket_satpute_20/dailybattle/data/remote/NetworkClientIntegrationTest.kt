package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

@Serializable
private data class TestPayload(
    val title: String,
    val count: Int,
)

private interface TestApiService {
    @GET("/test")
    suspend fun getTestPayload(): TestPayload
}

class NetworkClientIntegrationTest {

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
    fun retrofitClientSuccessfullyExecutesRequestAndDeserializesJson() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"title":"Daily Battle Test","count":42}""")
        )

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(TestApiService::class.java)

        val result = SafeApiCall.execute {
            apiService.getTestPayload()
        }

        assertTrue(result is DomainResult.Success)
        val payload = (result as DomainResult.Success).value
        assertEquals("Daily Battle Test", payload.title)
        assertEquals(42, payload.count)

        val recorded = server.takeRequest()
        assertEquals("application/json", recorded.getHeader("Accept"))
    }

    @Test
    fun readTimeoutProducesAppErrorTimeout() = runBlocking {
        // Enqueue response with delay exceeding client read timeout
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"title":"Delayed","count":1}""")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )

        // Configure short timeout for test
        val shortTimeoutConfig = NetworkConfig(
            readTimeoutSeconds = 1L,
            baseUrl = server.url("/").toString(),
        )
        val okHttpClient = NetworkClientFactory.createOkHttpClient(shortTimeoutConfig, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, shortTimeoutConfig).build()
        val apiService = retrofit.create(TestApiService::class.java)

        val result = SafeApiCall.execute {
            apiService.getTestPayload()
        }

        assertTrue("Timed out call must produce DomainResult.Failure", result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }

    @Test
    fun serverError500ProducesAppErrorServer() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error":"internal_server_error"}""")
        )

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(TestApiService::class.java)

        val result = SafeApiCall.execute {
            apiService.getTestPayload()
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }

    @Test
    fun unauthorized401ProducesAppErrorAuthentication() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":"unauthorized"}""")
        )

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(TestApiService::class.java)

        val result = SafeApiCall.execute {
            apiService.getTestPayload()
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun malformedJsonProducesAppErrorValidation() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ malformed: json without quotes }""")
        )

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(TestApiService::class.java)

        val result = SafeApiCall.execute {
            apiService.getTestPayload()
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }
}

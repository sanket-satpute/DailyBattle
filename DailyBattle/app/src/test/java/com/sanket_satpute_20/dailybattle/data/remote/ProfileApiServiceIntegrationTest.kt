package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.api.ProfileApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class ProfileApiServiceIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ProfileApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        apiService = retrofit.create(ProfileApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getProfileVerifiesPathMethodAndHeaders() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userId": "usr-1",
                        "battleName": "ApexPlayer",
                        "momentum": 4,
                        "bestScore": 950,
                        "averageScore": 890,
                        "battleDNA": {
                            "speed": 295,
                            "memory": 285,
                            "people": 290
                        },
                        "records": {
                            "bestScore": 950,
                            "bestMomentum": 12,
                            "battlesPlayed": 40
                        }
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getProfile("usr-1")

        assertEquals("usr-1", response.userId)
        assertEquals("ApexPlayer", response.battleName)
        assertEquals(4, response.momentum)
        assertEquals(950, response.bestScore)
        assertEquals(295, response.battleDNA.speed)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/profile/usr-1", recorded.path)
        assertEquals("application/json", recorded.getHeader("Accept"))
    }

    @Test
    fun getBattleHistoryVerifiesPathAndAbsenceOfInventedPaginationParameters() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "history": [
                            {
                                "resultId": "res-1",
                                "userId": "usr-1",
                                "battleId": "b-2026-10-12",
                                "sessionId": "s-1",
                                "snapScore": 290,
                                "shiftScore": 280,
                                "crowdCallScore": 270,
                                "consistencyScore": 90,
                                "totalScore": 930,
                                "percentile": 96.0,
                                "completedAt": 1700000000000
                            }
                        ]
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getBattleHistory("usr-1")

        assertEquals(1, response.history.size)
        assertEquals("res-1", response.history[0].resultId)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/history/usr-1", recorded.path)

        // Strict verification: DATA-PENDING-020 pagination query params must not be invented
        assertFalse(recorded.path?.contains("page=") == true)
        assertFalse(recorded.path?.contains("limit=") == true)
        assertFalse(recorded.path?.contains("cursor=") == true)
    }

    @Test
    fun getBattleHistoryHandlesEmptyListSafely() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "history": [] }""")
        )

        val response = apiService.getBattleHistory("usr-1")
        assertTrue(response.history.isEmpty())

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/history/usr-1", recorded.path)
    }

    @Test
    fun getBattleHistoryItemVerifiesPathMethodAndPayload() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "resultId": "res-99",
                        "userId": "usr-1",
                        "battleId": "b-historical-1",
                        "sessionId": "s-historical-1",
                        "snapScore": 300,
                        "shiftScore": 290,
                        "crowdCallScore": 280,
                        "consistencyScore": 95,
                        "totalScore": 965,
                        "percentile": 99.0,
                        "completedAt": 1700000050000
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getBattleHistoryItem("usr-1", "b-historical-1")

        assertEquals("res-99", response.resultId)
        assertEquals("b-historical-1", response.battleId)
        assertEquals(965, response.totalScore)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/history/usr-1/battles/b-historical-1", recorded.path)
    }

    @Test
    fun propagatesHttpErrorsCorrectly() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{ "code": "PROFILE_NOT_FOUND", "message": "Profile not found" }""")
        )

        try {
            apiService.getProfile("unknown-user")
            fail("Expected HttpException to be thrown on 404")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }
}

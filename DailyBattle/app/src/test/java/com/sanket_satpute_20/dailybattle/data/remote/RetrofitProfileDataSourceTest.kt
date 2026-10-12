package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.api.ProfileApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RetrofitProfileDataSource
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RetrofitProfileDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitProfileDataSource

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(ProfileApiService::class.java)

        dataSource = RetrofitProfileDataSource(apiService)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getProfileReturnsSuccessDomainProfileWithAuthoritativeMetrics() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userId": "usr-1",
                        "battleName": "EliteWarrior",
                        "momentum": 8,
                        "bestScore": 960,
                        "averageScore": 905,
                        "battleDNA": {
                            "speed": 298,
                            "memory": 290,
                            "people": 295
                        },
                        "records": {
                            "bestScore": 960,
                            "bestMomentum": 15,
                            "battlesPlayed": 60
                        }
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getProfile(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val profile = (result as DomainResult.Success).value
        assertEquals(UserId("usr-1"), profile.userId)
        assertEquals("EliteWarrior", profile.battleName)
        assertEquals(8, profile.momentum)
        assertEquals(960, profile.bestScore)
        assertEquals(905, profile.averageScore)
        assertEquals(298, profile.battleDNA.speed)
        assertEquals(290, profile.battleDNA.memory)
        assertEquals(295, profile.battleDNA.people)
        assertEquals(960, profile.records.bestScore)
        assertEquals(15, profile.records.bestMomentum)
        assertEquals(60, profile.records.battlesPlayed)
    }

    @Test
    fun getProfileHandlesNullDnaDimensionsSafely() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userId": "usr-2",
                        "battleName": "NewPlayer",
                        "momentum": 0,
                        "bestScore": 0,
                        "averageScore": 0,
                        "battleDNA": {
                            "speed": null,
                            "memory": null,
                            "people": null
                        },
                        "records": {
                            "bestScore": 0,
                            "bestMomentum": 0,
                            "battlesPlayed": 0
                        }
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getProfile(UserId("usr-2"))

        assertTrue(result is DomainResult.Success)
        val profile = (result as DomainResult.Success).value
        assertNull(profile.battleDNA.speed)
        assertNull(profile.battleDNA.memory)
        assertNull(profile.battleDNA.people)
    }

    @Test
    fun getBattleHistoryReturnsSuccessDomainBattleResultList() = runBlocking {
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

        val result = dataSource.getBattleHistory(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val history = (result as DomainResult.Success).value
        assertEquals(1, history.size)
        assertEquals("res-1", history[0].resultId.value)
        assertEquals(930, history[0].totalScore)
        assertEquals(96.0, history[0].percentile, 0.001)
    }

    @Test
    fun getBattleHistoryHandlesEmptyHistory() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "history": [] }""")
        )

        val result = dataSource.getBattleHistory(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val history = (result as DomainResult.Success).value
        assertTrue(history.isEmpty())
    }

    @Test
    fun getBattleHistoryItemReturnsSuccessDomainBattleResult() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "resultId": "res-42",
                        "userId": "usr-1",
                        "battleId": "b-historical",
                        "sessionId": "s-historical",
                        "snapScore": 295,
                        "shiftScore": 285,
                        "crowdCallScore": 275,
                        "consistencyScore": 92,
                        "totalScore": 947,
                        "percentile": 97.5,
                        "completedAt": 1700000080000
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getBattleHistoryItem(
            userId = UserId("usr-1"),
            battleId = BattleId("b-historical"),
        )

        assertTrue(result is DomainResult.Success)
        val battleResult = (result as DomainResult.Success).value
        assertEquals("res-42", battleResult.resultId.value)
        assertEquals(947, battleResult.totalScore)
    }

    @Test
    fun validation400MapsToDomainFailureAppErrorValidation() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "code": "INVALID_USER_ID", "message": "Malformed user id" }""")
        )

        val result = dataSource.getProfile(UserId("invalid-id"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun unauthorized401MapsToDomainFailureAppErrorAuthentication() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))

        val result = dataSource.getProfile(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun forbidden403MapsToDomainFailureAppErrorAuthorization() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("{}"))

        val result = dataSource.getProfile(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun serverError500MapsToDomainFailureAppErrorServer() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val result = dataSource.getBattleHistory(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }

    @Test
    fun timeoutMapsToDomainFailureAppErrorTimeout() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )

        val shortConfig = NetworkConfig(readTimeoutSeconds = 1L, baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(shortConfig, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, shortConfig).build()
        val customDataSource = RetrofitProfileDataSource(retrofit.create(ProfileApiService::class.java))

        val result = customDataSource.getProfile(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }
}

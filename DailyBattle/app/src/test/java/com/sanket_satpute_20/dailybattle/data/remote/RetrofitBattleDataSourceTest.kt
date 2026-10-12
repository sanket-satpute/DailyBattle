package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.api.BattleApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RetrofitBattleDataSource
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RetrofitBattleDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitBattleDataSource

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(BattleApiService::class.java)

        dataSource = RetrofitBattleDataSource(apiService)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getTodayBattleReturnsSuccessDomainBattle() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "battleId": "b-today",
                        "battleDate": 1700000000000,
                        "status": "AVAILABLE",
                        "version": 1,
                        "challenges": [
                            { "challengeId": "c-1", "type": "SNAP", "order": 1, "version": 1 },
                            { "challengeId": "c-2", "type": "SHIFT", "order": 2, "version": 1 },
                            { "challengeId": "c-3", "type": "CROWD_CALL", "order": 3, "version": 1 }
                        ]
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getTodayBattle()

        assertTrue(result is DomainResult.Success)
        val battle = (result as DomainResult.Success).value
        assertEquals("b-today", battle.battleId.value)
        assertEquals(3, battle.challenges.size)
    }

    @Test
    fun startBattleReturnsSuccessDomainSession() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "sessionId": "s-1",
                        "battleId": "b-today",
                        "mode": "OFFICIAL",
                        "status": "IN_PROGRESS",
                        "currentChallenge": 1,
                        "startedAt": 1000
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.startBattle(
            battleId = BattleId("b-today"),
            clientRequestId = "req-1",
            userId = UserId("u-1"),
        )

        assertTrue(result is DomainResult.Success)
        val session = (result as DomainResult.Success).value
        assertEquals("s-1", session.sessionId.value)
        assertEquals(UserId("u-1"), session.userId)
        assertEquals(BattleMode.OFFICIAL, session.mode)
        assertEquals(BattleSessionStatus.IN_PROGRESS, session.status)
    }

    @Test
    fun submitChallengeResultReturnsSuccessDomainChallengeResult() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "challengeResult": {
                            "resultId": "cr-1",
                            "challengeId": "c-snap",
                            "sessionId": "s-1",
                            "score": 290,
                            "maxScore": 300,
                            "completedAt": 2000
                        },
                        "nextChallenge": 2,
                        "sessionStatus": "IN_PROGRESS"
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.submitChallengeResult(
            sessionId = BattleSessionId("s-1"),
            challengeId = ChallengeId("c-snap"),
            clientRequestId = "req-submit",
            score = 290,
            maxScore = 300,
            completedAt = 2000L,
        )

        assertTrue(result is DomainResult.Success)
        val challengeResult = (result as DomainResult.Success).value
        assertEquals("cr-1", challengeResult.resultId.value)
        assertEquals(290, challengeResult.score)
    }

    @Test
    fun completeBattleReturnsSuccessDomainBattleResult() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "resultId": "res-1",
                        "userId": "u-1",
                        "battleId": "b-today",
                        "sessionId": "s-1",
                        "snapScore": 290,
                        "shiftScore": 280,
                        "crowdCallScore": 270,
                        "consistencyScore": 90,
                        "totalScore": 930,
                        "percentile": 96.0,
                        "completedAt": 3000
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.completeBattle(
            sessionId = BattleSessionId("s-1"),
            clientRequestId = "req-comp",
        )

        assertTrue(result is DomainResult.Success)
        val battleResult = (result as DomainResult.Success).value
        assertEquals("res-1", battleResult.resultId.value)
        assertEquals(930, battleResult.totalScore)
        assertEquals(96.0, battleResult.percentile, 0.001)
    }

    @Test
    fun getBattleResultReturnsSuccessDomainBattleResult() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "resultId": "res-1",
                        "userId": "u-1",
                        "battleId": "b-today",
                        "sessionId": "s-1",
                        "snapScore": 290,
                        "shiftScore": 280,
                        "crowdCallScore": 270,
                        "consistencyScore": 90,
                        "totalScore": 930,
                        "percentile": 96.0,
                        "completedAt": 3000
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getBattleResult(
            battleId = BattleId("b-today"),
            userId = UserId("u-1"),
        )

        assertTrue(result is DomainResult.Success)
        val battleResult = (result as DomainResult.Success).value
        assertEquals("res-1", battleResult.resultId.value)
    }

    @Test
    fun serverError500MapsToDomainFailureAppErrorServer() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val result = dataSource.getTodayBattle()

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }

    @Test
    fun unauthorized401MapsToDomainFailureAppErrorAuthentication() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))

        val result = dataSource.completeBattle(BattleSessionId("s-1"), "req-1")

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
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
        val customDataSource = RetrofitBattleDataSource(retrofit.create(BattleApiService::class.java))

        val result = customDataSource.getTodayBattle()

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }
}

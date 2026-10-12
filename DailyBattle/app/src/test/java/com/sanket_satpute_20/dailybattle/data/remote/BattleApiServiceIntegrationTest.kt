package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.api.BattleApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.CompleteBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.StartBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class BattleApiServiceIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: BattleApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        apiService = retrofit.create(BattleApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getTodayBattleVerifiesPathMethodAndHeaders() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "battleId": "battle-today-1",
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

        val response = apiService.getTodayBattle()

        assertEquals("battle-today-1", response.battleId)
        assertEquals(3, response.challenges.size)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/battles/today", recorded.path)
        assertEquals("application/json", recorded.getHeader("Accept"))
    }

    @Test
    fun startBattleVerifiesPathPayloadAndIdempotencyKey() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "sessionId": "sess-new-1",
                        "battleId": "b-100",
                        "mode": "OFFICIAL",
                        "status": "IN_PROGRESS",
                        "currentChallenge": 1,
                        "startedAt": 1700000010000
                    }
                    """.trimIndent()
                )
        )

        val request = StartBattleRequestDto(clientRequestId = "idempotent-key-start")
        val response = apiService.startBattle("b-100", request)

        assertEquals("sess-new-1", response.sessionId)
        assertEquals(1, response.currentChallenge)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/battles/b-100/start", recorded.path)
        assertTrue(recorded.body.readUtf8().contains("idempotent-key-start"))
    }

    @Test
    fun submitChallengeResultVerifiesPathPayloadAndResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "challengeResult": {
                            "resultId": "cr-55",
                            "challengeId": "ch-snap",
                            "sessionId": "sess-1",
                            "score": 280,
                            "maxScore": 300,
                            "completedAt": 1700000030000
                        },
                        "nextChallenge": 2,
                        "sessionStatus": "IN_PROGRESS"
                    }
                    """.trimIndent()
                )
        )

        val request = SubmitChallengeResultRequestDto(
            clientRequestId = "idempotent-key-submit",
            score = 280,
            maxScore = 300,
            completedAt = 1700000030000L,
        )
        val response = apiService.submitChallengeResult("sess-1", "ch-snap", request)

        assertEquals("cr-55", response.challengeResult.resultId)
        assertEquals(2, response.nextChallenge)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/battles/sessions/sess-1/challenges/ch-snap/result", recorded.path)
        assertTrue(recorded.body.readUtf8().contains("idempotent-key-submit"))
    }

    @Test
    fun completeBattleVerifiesPathPayloadAndResultResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "resultId": "res-final",
                        "userId": "user-42",
                        "battleId": "b-100",
                        "sessionId": "sess-1",
                        "snapScore": 280,
                        "shiftScore": 270,
                        "crowdCallScore": 290,
                        "consistencyScore": 80,
                        "totalScore": 920,
                        "percentile": 95.0,
                        "completedAt": 1700000060000
                    }
                    """.trimIndent()
                )
        )

        val request = CompleteBattleRequestDto(clientRequestId = "idempotent-key-complete")
        val response = apiService.completeBattle("sess-1", request)

        assertEquals("res-final", response.resultId)
        assertEquals(920, response.totalScore)
        assertEquals(95.0, response.percentile, 0.001)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/battles/sessions/sess-1/complete", recorded.path)
        assertTrue(recorded.body.readUtf8().contains("idempotent-key-complete"))
    }

    @Test
    fun getBattleResultVerifiesPathAndRetrieval() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "resultId": "res-final",
                        "userId": "user-42",
                        "battleId": "b-100",
                        "sessionId": "sess-1",
                        "snapScore": 280,
                        "shiftScore": 270,
                        "crowdCallScore": 290,
                        "consistencyScore": 80,
                        "totalScore": 920,
                        "percentile": 95.0,
                        "completedAt": 1700000060000
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getBattleResult("b-100", "user-42")

        assertEquals("res-final", response.resultId)
        assertEquals("user-42", response.userId)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/battles/b-100/results/user-42", recorded.path)
    }

    @Test
    fun apiServiceThrowsHttpExceptionOn404() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))

        try {
            apiService.getTodayBattle()
            org.junit.Assert.fail("Expected HttpException on 404")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }
}

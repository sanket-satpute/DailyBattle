package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.api.BattleApiService
import com.sanket_satpute_20.dailybattle.data.remote.api.SocialApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.CompleteBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.StartBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Sprint 16.5 — Network Transport Edge Cases & Idempotency Validation Test Suite.
 *
 * Verifies network transport failure recovery and request idempotency contracts:
 * - Socket/Read timeout -> [AppError.Timeout].
 * - DNS resolution failure -> [AppError.Network].
 * - Connection reset -> [AppError.Network].
 * - Slow connection handling -> succeeds within configured timeout tolerances.
 * - Partial / truncated response (EOF) -> [AppError.Network].
 * - Malformed JSON payload -> [AppError.Validation].
 * - Unexpected schema structure -> [AppError.Validation].
 * - Request Idempotency (08_DATA_AND_API.md §57): Repeated mutation requests preserve identical
 *   clientRequestId across StartBattle, SubmitChallengeResult, CompleteBattle, and AddFriend.
 */
class ApiNetworkTransportValidationTest {

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

    private fun createBattleApi(): BattleApiService {
        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        return retrofit.create(BattleApiService::class.java)
    }

    private fun createFastTimeoutBattleApi(timeoutMs: Long): BattleApiService {
        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val baseClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val fastClient = baseClient.newBuilder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        val retrofit = NetworkClientFactory.createRetrofitBuilder(fastClient, DailyBattleJson, config).build()
        return retrofit.create(BattleApiService::class.java)
    }

    private fun createSocialApi(): SocialApiService {
        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        return retrofit.create(SocialApiService::class.java)
    }

    @Test
    fun socketReadTimeoutMapsToTimeoutError() = runBlocking {
        val battleApi = createFastTimeoutBattleApi(timeoutMs = 150)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "battleId": "b-slow", "battleDate": 1700000000000, "status": "AVAILABLE", "version": 1, "challenges": [] }""")
                .setBodyDelay(500, TimeUnit.MILLISECONDS),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }

    @Test
    fun dnsResolutionFailureMapsToNetworkError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw UnknownHostException("Unable to resolve host: api.dailybattle.com")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun connectionResetMapsToNetworkError() = runBlocking {
        val battleApi = createBattleApi()

        server.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun slowConnectionWithinTimeoutSucceedsNormally() = runBlocking {
        val battleApi = createBattleApi()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "battleId": "b-slow-ok", "battleDate": 1700000000000, "status": "AVAILABLE", "version": 1, "challenges": [] }""")
                .setBodyDelay(50, TimeUnit.MILLISECONDS),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Success)
        assertEquals("b-slow-ok", (result as DomainResult.Success).value.battleId)
    }

    @Test
    fun partialOrTruncatedResponseMapsToNetworkError() = runBlocking {
        val battleApi = createBattleApi()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "battleId": "b-truncated", "battleDate": 1700000000000, "status": "AVAILABLE" """)
                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun malformedJsonPayloadMapsToValidationError() = runBlocking {
        val battleApi = createBattleApi()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "battleId": "b-broken", "status": { "invalid": true """),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun unexpectedSchemaStructureMapsToValidationError() = runBlocking {
        val battleApi = createBattleApi()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""[ { "battleId": "array-instead-of-object" } ]"""),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun startBattleMutationPreservesClientRequestIdAcrossRetries() = runBlocking {
        val battleApi = createBattleApi()
        val idempotencyKey = "req-idemp-start-1"
        val requestDto = StartBattleRequestDto(clientRequestId = idempotencyKey)

        val sessionResponse = """
            {
                "sessionId": "s-100",
                "battleId": "b-1",
                "mode": "OFFICIAL",
                "status": "IN_PROGRESS",
                "currentChallengeIndex": 0,
                "startedAt": 1700000000000
            }
        """.trimIndent()

        // First attempt succeeds
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(sessionResponse),
        )
        // Retry attempt also succeeds
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(sessionResponse),
        )

        val result1 = SafeApiCall.execute { battleApi.startBattle("b-1", requestDto) }
        val recorded1 = server.takeRequest()

        val result2 = SafeApiCall.execute { battleApi.startBattle("b-1", requestDto) }
        val recorded2 = server.takeRequest()

        assertTrue(result1 is DomainResult.Success)
        assertTrue(result2 is DomainResult.Success)

        // Both requests transmitted identical clientRequestId in JSON payload
        val body1 = recorded1.body.readUtf8()
        val body2 = recorded2.body.readUtf8()
        assertTrue(body1.contains(idempotencyKey))
        assertTrue(body2.contains(idempotencyKey))
        assertEquals(body1, body2)
    }

    @Test
    fun submitChallengeResultMutationPreservesClientRequestIdAcrossRetries() = runBlocking {
        val battleApi = createBattleApi()
        val idempotencyKey = "req-idemp-result-2"
        val requestDto = SubmitChallengeResultRequestDto(
            clientRequestId = idempotencyKey,
            score = 300,
            maxScore = 300,
            completedAt = 1700000010000L,
        )

        val challengeResultResponse = """
            {
                "challengeId": "c-1",
                "score": 300,
                "maxScore": 300,
                "completed": true,
                "completedAt": 1700000010000
            }
        """.trimIndent()

        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(challengeResultResponse),
        )
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(challengeResultResponse),
        )

        SafeApiCall.execute { battleApi.submitChallengeResult("s-100", "c-1", requestDto) }
        val recorded1 = server.takeRequest()

        SafeApiCall.execute { battleApi.submitChallengeResult("s-100", "c-1", requestDto) }
        val recorded2 = server.takeRequest()

        val body1 = recorded1.body.readUtf8()
        val body2 = recorded2.body.readUtf8()
        assertTrue(body1.contains(idempotencyKey))
        assertTrue(body2.contains(idempotencyKey))
        assertEquals(body1, body2)
    }

    @Test
    fun completeBattleMutationPreservesClientRequestIdAcrossRetries() = runBlocking {
        val battleApi = createBattleApi()
        val idempotencyKey = "req-idemp-complete-3"
        val requestDto = CompleteBattleRequestDto(clientRequestId = idempotencyKey)

        val battleResultResponse = """
            {
                "battleId": "b-1",
                "userId": "u-1",
                "finalScore": 900,
                "completedAt": 1700000030000,
                "challengeScores": []
            }
        """.trimIndent()

        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(battleResultResponse),
        )
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(battleResultResponse),
        )

        SafeApiCall.execute { battleApi.completeBattle("s-100", requestDto) }
        val recorded1 = server.takeRequest()

        SafeApiCall.execute { battleApi.completeBattle("s-100", requestDto) }
        val recorded2 = server.takeRequest()

        val body1 = recorded1.body.readUtf8()
        val body2 = recorded2.body.readUtf8()
        assertTrue(body1.contains(idempotencyKey))
        assertTrue(body2.contains(idempotencyKey))
        assertEquals(body1, body2)
    }

    @Test
    fun addFriendMutationPreservesClientRequestIdAcrossRetries() = runBlocking {
        val socialApi = createSocialApi()
        val idempotencyKey = "req-idemp-addfriend-4"
        val requestDto = AddFriendRequestDto(
            userId = "usr-1",
            battleCode = "CODE123",
            clientRequestId = idempotencyKey,
        )

        val addFriendResponse = """
            {
                "friendshipId": "fs-new-1",
                "userId": "usr-1",
                "friendUserId": "usr-2",
                "friendName": "RivalPlayer",
                "status": "ACCEPTED",
                "createdAt": 1700000000000,
                "updatedAt": 1700000000000
            }
        """.trimIndent()

        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(addFriendResponse),
        )
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(addFriendResponse),
        )

        SafeApiCall.execute { socialApi.addFriend(requestDto) }
        val recorded1 = server.takeRequest()

        SafeApiCall.execute { socialApi.addFriend(requestDto) }
        val recorded2 = server.takeRequest()

        val body1 = recorded1.body.readUtf8()
        val body2 = recorded2.body.readUtf8()
        assertTrue(body1.contains(idempotencyKey))
        assertTrue(body2.contains(idempotencyKey))
        assertEquals(body1, body2)
    }
}

package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.api.BattleApiService
import com.sanket_satpute_20.dailybattle.data.remote.api.ProfileApiService
import com.sanket_satpute_20.dailybattle.data.remote.api.SocialApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.error.HttpErrorMapper
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/**
 * Sprint 16.5 — HTTP Status Code & Error Payload Validation Test Suite.
 *
 * Verifies end-to-end HTTP status code mapping and error payload handling via MockWebServer:
 * - 200 valid: successful parsing into DTO and domain model.
 * - 200 malformed: 200 OK with malformed JSON body maps to [AppError.Validation].
 * - 204: No Content handled gracefully without unhandled crashes.
 * - 400: Bad Request maps to [AppError.Validation] with structured error parsing.
 * - 401: Unauthorized maps to [AppError.Authentication] with structured error parsing.
 * - 403: Forbidden maps to [AppError.Authorization] with structured error parsing.
 * - 404: Not Found maps to [AppError.Unknown].
 * - 409: Conflict maps to [AppError.Unknown] with structured error parsing.
 * - 422: Unprocessable Entity maps to [AppError.Validation] with structured error parsing.
 * - 429: Too Many Requests maps to [AppError.Unknown] with structured error parsing.
 * - 500: Internal Server Error maps to [AppError.Server] with structured error parsing.
 * - 502: Bad Gateway maps to [AppError.Server].
 * - 503: Service Unavailable maps to [AppError.Server].
 * - 504: Gateway Timeout maps to [AppError.Server].
 * - Error payload contract (code, message, details, requestId) conformance.
 */
class ApiHttpStatusValidationTest {

    private lateinit var server: MockWebServer
    private lateinit var battleApi: BattleApiService
    private lateinit var socialApi: SocialApiService
    private lateinit var profileApi: ProfileApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()

        battleApi = retrofit.create(BattleApiService::class.java)
        socialApi = retrofit.create(SocialApiService::class.java)
        profileApi = retrofit.create(ProfileApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun http200ValidParsesCorrectlyAcrossServices() = runBlocking {
        // Battle API 200 OK
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "battleId": "battle-today-200",
                        "battleDate": 1700000000000,
                        "status": "AVAILABLE",
                        "version": 1,
                        "challenges": [
                            { "challengeId": "c-1", "type": "SNAP", "order": 1, "version": 1 }
                        ]
                    }
                    """.trimIndent(),
                ),
        )
        val battleResult = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(battleResult is DomainResult.Success)
        assertEquals("battle-today-200", (battleResult as DomainResult.Success).value.battleId)

        // Social API 200 OK
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "friends": [] }"""),
        )
        val socialResult = SafeApiCall.execute { socialApi.getFriends("user-1") }
        assertTrue(socialResult is DomainResult.Success)
        assertTrue((socialResult as DomainResult.Success).value.friends.isEmpty())

        // Profile API 200 OK
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userId": "user-1",
                        "battleName": "Hero",
                        "momentum": 4,
                        "bestScore": 950,
                        "averageScore": 880,
                        "battleDNA": {},
                        "records": { "bestScore": 950, "bestMomentum": 4, "battlesPlayed": 15 }
                    }
                    """.trimIndent(),
                ),
        )
        val profileResult = SafeApiCall.execute { profileApi.getProfile("user-1") }
        assertTrue(profileResult is DomainResult.Success)
        assertEquals("Hero", (profileResult as DomainResult.Success).value.battleName)
    }

    @Test
    fun http200MalformedBodyMapsToValidationError() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "battleId": "b-1", "battleDate": "NOT_A_LONG" }"""),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun http204NoContentHandledGracefully() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(204),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        // 204 with missing body on typed response produces either EOF/Null body error mapped safely
        val error = (result as DomainResult.Failure).error
        assertTrue(error is AppError.Network || error is AppError.Validation || error is AppError.Unknown)
    }

    @Test
    fun http400BadRequestMapsToValidationErrorAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "INVALID_PARAMETERS",
                "message": "Challenge order is invalid",
                "details": "order must be positive integer",
                "requestId": "req-400-abc"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                battleApi.getTodayBattle()
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertNotNull(parsedError)
        assertEquals("INVALID_PARAMETERS", parsedError?.code)
        assertEquals("Challenge order is invalid", parsedError?.message)
        assertEquals("order must be positive integer", parsedError?.details)
        assertEquals("req-400-abc", parsedError?.requestId)
    }

    @Test
    fun http401UnauthorizedMapsToAuthenticationErrorAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "UNAUTHORIZED",
                "message": "Session token expired or missing",
                "requestId": "req-401-xyz"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                profileApi.getProfile("u-1")
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertEquals("UNAUTHORIZED", parsedError?.code)
        assertEquals("Session token expired or missing", parsedError?.message)
    }

    @Test
    fun http403ForbiddenMapsToAuthorizationErrorAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "FORBIDDEN",
                "message": "User is not permitted to view this battle",
                "requestId": "req-403"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                battleApi.getTodayBattle()
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertEquals("FORBIDDEN", parsedError?.code)
    }

    @Test
    fun http404NotFoundMapsToUnknownError() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "code": "NOT_FOUND", "message": "Resource not found" }"""),
        )

        val result = SafeApiCall.execute { profileApi.getProfile("unknown-user") }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Unknown, (result as DomainResult.Failure).error)
    }

    @Test
    fun http409ConflictMapsToUnknownErrorAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "BATTLE_ALREADY_STARTED",
                "message": "An active official battle session already exists",
                "details": "Session s-active is in progress",
                "requestId": "req-409"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                socialApi.getFriends("user-1")
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Unknown, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertEquals("BATTLE_ALREADY_STARTED", parsedError?.code)
        assertEquals("Session s-active is in progress", parsedError?.details)
    }

    @Test
    fun http422UnprocessableEntityMapsToValidationErrorAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "INVALID_CHALLENGE_STATE",
                "message": "Challenge already completed",
                "requestId": "req-422"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                battleApi.getTodayBattle()
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertEquals("INVALID_CHALLENGE_STATE", parsedError?.code)
    }

    @Test
    fun http429TooManyRequestsMapsToUnknownErrorAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "RATE_LIMITED",
                "message": "Rate limit exceeded. Try again in 60 seconds.",
                "requestId": "req-429"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                battleApi.getTodayBattle()
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Unknown, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertEquals("RATE_LIMITED", parsedError?.code)
    }

    @Test
    fun http500InternalServerErrorMapsToServerAndParsesErrorPayload() = runBlocking {
        val errorBody = """
            {
                "code": "SERVER_ERROR",
                "message": "An internal server error occurred",
                "requestId": "req-500"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody),
        )

        var capturedException: HttpException? = null
        val result = SafeApiCall.execute {
            try {
                battleApi.getTodayBattle()
            } catch (e: HttpException) {
                capturedException = e
                throw e
            }
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)

        assertNotNull(capturedException)
        val parsedError = HttpErrorMapper.parseErrorResponse(capturedException?.response()?.errorBody()?.string())
        assertEquals("SERVER_ERROR", parsedError?.code)
    }

    @Test
    fun http502BadGatewayMapsToServer() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(502)
                .setBody("Bad Gateway"),
        )

        val result = SafeApiCall.execute { battleApi.getTodayBattle() }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }

    @Test
    fun http503ServiceUnavailableMapsToServer() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody("Service Unavailable"),
        )

        val result = SafeApiCall.execute { profileApi.getProfile("u-1") }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }

    @Test
    fun http504GatewayTimeoutMapsToServer() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(504)
                .setBody("Gateway Timeout"),
        )

        val result = SafeApiCall.execute { socialApi.getFriends("u-1") }
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }
}

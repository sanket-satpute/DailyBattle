package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.api.SocialApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class SocialApiServiceIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: SocialApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        apiService = retrofit.create(SocialApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getFriendsVerifiesPathMethodAndHeaders() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "friends": [
                            {
                                "friendshipId": "fs-1",
                                "userId": "usr-1",
                                "friendUserId": "usr-2",
                                "friendName": "Alex",
                                "status": "ACCEPTED",
                                "todayScore": 880,
                                "createdAt": 1700000000000,
                                "updatedAt": 1700000010000
                            },
                            {
                                "friendshipId": "fs-2",
                                "userId": "usr-1",
                                "friendUserId": "usr-3",
                                "friendName": "Sam",
                                "status": "PENDING",
                                "todayScore": null,
                                "createdAt": 1700000020000,
                                "updatedAt": 1700000030000
                            }
                        ]
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getFriends("usr-1")

        assertEquals(2, response.friends.size)
        assertEquals("Alex", response.friends[0].friendName)
        assertEquals(880, response.friends[0].todayScore)
        assertNull(response.friends[1].todayScore)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/friends/usr-1", recorded.path)
        assertEquals("application/json", recorded.getHeader("Accept"))
    }

    @Test
    fun getFriendsHandlesEmptyListSafely() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "friends": [] }""")
        )

        val response = apiService.getFriends("usr-1")
        assertTrue(response.friends.isEmpty())

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/friends/usr-1", recorded.path)
    }

    @Test
    fun addFriendVerifiesPathPayloadAndIdempotencyKey() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "friendship": {
                            "friendshipId": "fs-new-99",
                            "userId": "usr-1",
                            "friendUserId": "usr-88",
                            "friendName": "Charlie",
                            "status": "PENDING",
                            "todayScore": null,
                            "createdAt": 1700000050000,
                            "updatedAt": 1700000050000
                        },
                        "status": "PENDING"
                    }
                    """.trimIndent()
                )
        )

        val request = AddFriendRequestDto(
            userId = "usr-1",
            battleCode = "BATTLE-OPAQUE-CODE-XYZ",
            clientRequestId = "idempotent-key-add-friend-1",
        )
        val response = apiService.addFriend(request)

        assertEquals("fs-new-99", response.friendship.friendshipId)
        assertEquals("PENDING", response.status)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/friends/add", recorded.path)
        assertTrue(recorded.getHeader("Content-Type")?.contains("application/json") == true)

        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"userId\":\"usr-1\""))
        assertTrue(body.contains("\"battleCode\":\"BATTLE-OPAQUE-CODE-XYZ\""))
        assertTrue(body.contains("\"clientRequestId\":\"idempotent-key-add-friend-1\""))
    }

    @Test
    fun getCurrentRivalVerifiesPathMethodAndDeserialization() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userId": "usr-1",
                        "rivalUserId": "usr-55",
                        "rivalName": "RivalPlayer",
                        "selectedAt": 1700000090000,
                        "status": "ACTIVE"
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getCurrentRival("usr-1")

        assertEquals("usr-1", response.userId)
        assertEquals("usr-55", response.rivalUserId)
        assertEquals("RivalPlayer", response.rivalName)
        assertEquals(1700000090000L, response.selectedAt)
        assertEquals("ACTIVE", response.status)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/rivals/usr-1", recorded.path)
    }

    @Test
    fun getCurrentRivalThrowsHttpExceptionWhenAbsentVia404() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "code": "RIVAL_NOT_FOUND", "message": "No rival assigned" }""")
        )

        try {
            apiService.getCurrentRival("usr-1")
            fail("Expected HttpException to be thrown on 404")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/rivals/usr-1", recorded.path)
    }

    @Test
    fun getRivalComparisonVerifiesPathMethodAndValues() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userScore": 920,
                        "rivalScore": 905,
                        "scoreGap": 15,
                        "battleId": "b-2026-10-12",
                        "direction": "AHEAD"
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.getRivalComparison("usr-1", "b-2026-10-12")

        assertEquals(920, response.userScore)
        assertEquals(905, response.rivalScore)
        assertEquals(15, response.scoreGap)
        assertEquals("b-2026-10-12", response.battleId)
        assertEquals("AHEAD", response.direction)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/api/v1/rivals/usr-1/comparison/b-2026-10-12", recorded.path)
    }

    @Test
    fun propagatesHttpErrorsCorrectly() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{ "code": "NOT_FOUND", "message": "User not found" }""")
        )

        try {
            apiService.getFriends("unknown-user")
            fail("Expected HttpException to be thrown")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }
}

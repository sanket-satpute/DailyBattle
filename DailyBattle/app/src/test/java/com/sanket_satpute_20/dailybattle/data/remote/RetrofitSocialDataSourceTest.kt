package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.api.SocialApiService
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RetrofitSocialDataSource
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
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

class RetrofitSocialDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitSocialDataSource

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val config = NetworkConfig(baseUrl = server.url("/").toString())
        val okHttpClient = NetworkClientFactory.createOkHttpClient(config, UnconfiguredAuthTokenProvider())
        val retrofit = NetworkClientFactory.createRetrofitBuilder(okHttpClient, DailyBattleJson, config).build()
        val apiService = retrofit.create(SocialApiService::class.java)

        dataSource = RetrofitSocialDataSource(apiService)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getFriendsReturnsSuccessDomainFriendList() = runBlocking {
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
                                "todayScore": 890,
                                "createdAt": 1700000000000,
                                "updatedAt": 1700000010000
                            },
                            {
                                "friendshipId": "fs-2",
                                "userId": "usr-1",
                                "friendUserId": "usr-3",
                                "friendName": "Jordan",
                                "status": "PENDING",
                                "todayScore": null,
                                "createdAt": 1700000020000,
                                "updatedAt": 1700000020000
                            }
                        ]
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getFriends(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val friends = (result as DomainResult.Success).value
        assertEquals(2, friends.size)
        assertEquals(FriendshipId("fs-1"), friends[0].friendshipId)
        assertEquals(890, friends[0].todayScore)
        assertEquals(FriendStatus.ACCEPTED, friends[0].status)
        assertNull(friends[1].todayScore)
        assertEquals(FriendStatus.PENDING, friends[1].status)
    }

    @Test
    fun getFriendsHandlesEmptyFriendList() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "friends": [] }""")
        )

        val result = dataSource.getFriends(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val friends = (result as DomainResult.Success).value
        assertTrue(friends.isEmpty())
    }

    @Test
    fun addFriendByBattleCodeSendsIdempotencyKeyAndOpaqueBattleCode() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "friendship": {
                            "friendshipId": "fs-100",
                            "userId": "usr-1",
                            "friendUserId": "usr-9",
                            "friendName": "Morgan",
                            "status": "PENDING",
                            "todayScore": null,
                            "createdAt": 1700000090000,
                            "updatedAt": 1700000090000
                        },
                        "status": "PENDING"
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.addFriendByBattleCode(
            userId = UserId("usr-1"),
            battleCode = "ANY_OPAQUE_BATTLE_CODE_FORMAT_123",
            clientRequestId = "req-idempotent-add-99",
        )

        assertTrue(result is DomainResult.Success)
        val friend = (result as DomainResult.Success).value
        assertEquals(FriendshipId("fs-100"), friend.friendshipId)
        assertEquals("Morgan", friend.friendName)

        val recorded = server.takeRequest()
        assertEquals("/api/v1/friends/add", recorded.path)
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"battleCode\":\"ANY_OPAQUE_BATTLE_CODE_FORMAT_123\""))
        assertTrue(body.contains("\"clientRequestId\":\"req-idempotent-add-99\""))
    }

    @Test
    fun getCurrentRivalReturnsServerSelectedRivalWithoutClientSideSelection() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userId": "usr-1",
                        "rivalUserId": "usr-rival-7",
                        "rivalName": "ServerSelectedRival",
                        "selectedAt": 1700000150000,
                        "status": "ACTIVE"
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getCurrentRival(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val rival = (result as DomainResult.Success).value
        assertEquals(UserId("usr-1"), rival?.userId)
        assertEquals(UserId("usr-rival-7"), rival?.rivalUserId)
        assertEquals("ServerSelectedRival", rival?.rivalName)
        assertEquals(RivalStatus.ACTIVE, rival?.status)
    }

    @Test
    fun getCurrentRivalReturnsNullWhenAbsentVia404() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "code": "RIVAL_NOT_FOUND", "message": "No rival assigned" }""")
        )

        val result = dataSource.getCurrentRival(UserId("usr-1"))

        assertTrue(result is DomainResult.Success)
        val rival = (result as DomainResult.Success).value
        assertNull(rival)
    }

    @Test
    fun getRivalComparisonReturnsSuccessDomainComparison() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "userScore": 915,
                        "rivalScore": 900,
                        "scoreGap": 15,
                        "battleId": "battle-today-1",
                        "direction": "AHEAD"
                    }
                    """.trimIndent()
                )
        )

        val result = dataSource.getRivalComparison(
            userId = UserId("usr-1"),
            battleId = BattleId("battle-today-1"),
        )

        assertTrue(result is DomainResult.Success)
        val comparison = (result as DomainResult.Success).value
        assertEquals(915, comparison.userScore)
        assertEquals(900, comparison.rivalScore)
        assertEquals(15, comparison.scoreGap)
        assertEquals(BattleId("battle-today-1"), comparison.battleId)
        assertEquals(ScoreDirection.AHEAD, comparison.direction)
    }

    @Test
    fun validation400MapsToDomainFailureAppErrorValidation() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "code": "FRIEND_CODE_INVALID", "message": "Invalid code" }""")
        )

        val result = dataSource.addFriendByBattleCode(
            userId = UserId("usr-1"),
            battleCode = "INVALID_CODE",
            clientRequestId = "req-1",
        )

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun serverError500MapsToDomainFailureAppErrorServer() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val result = dataSource.getFriends(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }

    @Test
    fun unauthorized401MapsToDomainFailureAppErrorAuthentication() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))

        val result = dataSource.getFriends(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun forbidden403MapsToDomainFailureAppErrorAuthorization() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("{}"))

        val result = dataSource.getFriends(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
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
        val customDataSource = RetrofitSocialDataSource(retrofit.create(SocialApiService::class.java))

        val result = customDataSource.getFriends(UserId("usr-1"))

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }
}

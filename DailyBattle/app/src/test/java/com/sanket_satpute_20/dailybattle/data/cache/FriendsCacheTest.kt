package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteSocialDataSource
import com.sanket_satpute_20.dailybattle.domain.authority.DomainType
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsCacheTest {

    private lateinit var fakeDao: FakeFriendDao
    private lateinit var fakeRemoteDataSource: FakeRemoteSocialDataSource
    private lateinit var metadataStore: InMemoryCacheMetadataStore
    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var cache: FriendsCache

    private val sampleFriends = listOf(
        Friend(
            friendshipId = FriendshipId("fs_1"),
            userId = UserId("user_test"),
            friendUserId = UserId("friend_1"),
            friendName = "Alice",
            status = FriendStatus.ACCEPTED,
            todayScore = 850,
            createdAt = 500_000L,
            updatedAt = 500_000L,
        ),
        Friend(
            friendshipId = FriendshipId("fs_2"),
            userId = UserId("user_test"),
            friendUserId = UserId("friend_2"),
            friendName = "Bob",
            status = FriendStatus.ACCEPTED,
            todayScore = null,
            createdAt = 600_000L,
            updatedAt = 600_000L,
        ),
    )

    @Before
    fun setup() {
        fakeDao = FakeFriendDao()
        fakeRemoteDataSource = FakeRemoteSocialDataSource()
        metadataStore = InMemoryCacheMetadataStore()
        cache = FriendsCache(
            friendDao = fakeDao,
            remoteDataSource = fakeRemoteDataSource,
            metadataStore = metadataStore,
            clock = clock,
            policy = CachePolicy(
                domain = DomainType.FRIENDS,
                ttlMillis = 60_000L,
                maxStaleMillis = 300_000L,
            ),
        )
    }

    @Test
    fun readReturnsNullWhenCacheIsEmpty() = runTest {
        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        assertNull((result as DomainResult.Success).value)
    }

    @Test
    fun writeAndReadReturnsFreshCachedFriends() = runTest {
        cache.write(UserId("user_test"), sampleFriends, timestamp = currentTime)

        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals(2, cached!!.data.size)
        assertEquals("Alice", cached.data[0].friendName)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertTrue(cached.isFresh)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun readReturnsStaleWhenPastTtl() = runTest {
        cache.write(UserId("user_test"), sampleFriends, timestamp = currentTime)

        // Advance past TTL (60s) but within maxStale (300s)
        currentTime += 100_000L

        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals(CacheFreshness.STALE, cached!!.freshness)
        assertFalse(cached.isFresh)
        assertTrue(cached.isUsable)
    }

    @Test
    fun refreshWithoutForceReturnsFreshCacheWithoutCallingRemote() = runTest {
        cache.write(UserId("user_test"), sampleFriends, timestamp = currentTime)

        fakeRemoteDataSource.friendsResult = DomainResult.Success(emptyList())

        val result = cache.refresh(UserId("user_test"), force = false)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(2, cached.data.size) // Did not call remote
        assertEquals(0, fakeRemoteDataSource.getFriendsCallCount)
    }

    @Test
    fun refreshWithForceCallsRemoteAndUpdatesCache() = runTest {
        cache.write(UserId("user_test"), sampleFriends, timestamp = currentTime)

        val updatedFriends = listOf(
            sampleFriends[0].copy(friendName = "Alice Updated"),
        )
        fakeRemoteDataSource.friendsResult = DomainResult.Success(updatedFriends)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(1, cached.data.size)
        assertEquals("Alice Updated", cached.data[0].friendName)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertFalse(cached.isFromCache)
        assertEquals(1, fakeRemoteDataSource.getFriendsCallCount)

        // Verify write persisted to DAO
        assertEquals(1, fakeDao.friends.size)
    }

    @Test
    fun refreshFallsBackToStaleCacheOnRemoteNetworkError() = runTest {
        cache.write(UserId("user_test"), sampleFriends, timestamp = currentTime)

        fakeRemoteDataSource.friendsResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(2, cached.data.size)
        assertEquals(CacheFreshness.STALE, cached.freshness)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun refreshReturnsFailureOnRemoteNetworkErrorWhenNoCacheExists() = runTest {
        fakeRemoteDataSource.friendsResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun invalidateClearsMetadataStore() = runTest {
        cache.write(UserId("user_test"), sampleFriends, timestamp = currentTime)
        assertNotNull(metadataStore.getLastUpdated("friends_user_test"))

        cache.invalidate(UserId("user_test"))
        assertNull(metadataStore.getLastUpdated("friends_user_test"))
    }

    private class FakeFriendDao : FriendDao {
        val friends = mutableMapOf<String, FriendEntity>()

        override suspend fun insertFriend(friend: FriendEntity) {
            friends[friend.friendshipId] = friend
        }

        override suspend fun insertFriends(friendsList: List<FriendEntity>) {
            friends.clear()
            friendsList.forEach { friends[it.friendshipId] = it }
        }

        override suspend fun getFriendsForUser(userId: String): List<FriendEntity> =
            friends.values.filter { it.userId == userId || it.friendUserId == userId }

        override fun observeFriendsForUser(userId: String): Flow<List<FriendEntity>> =
            flowOf(friends.values.filter { it.userId == userId || it.friendUserId == userId })

        override suspend fun getFriend(userId: String, friendUserId: String): FriendEntity? =
            friends.values.find {
                (it.userId == userId && it.friendUserId == friendUserId) ||
                (it.userId == friendUserId && it.friendUserId == userId)
            }

        override suspend fun getFriendById(friendshipId: String): FriendEntity? =
            friends[friendshipId]

        override suspend fun deleteFriendById(friendshipId: String): Int =
            if (friends.remove(friendshipId) != null) 1 else 0

        override suspend fun clearFriendsForUser(userId: String): Int {
            val toRemove = friends.values.filter { it.userId == userId || it.friendUserId == userId }
            toRemove.forEach { friends.remove(it.friendshipId) }
            return toRemove.size
        }
    }

    private class FakeRemoteSocialDataSource : RemoteSocialDataSource {
        var friendsResult: DomainResult<List<Friend>> = DomainResult.Failure(AppError.Network)
        var getFriendsCallCount = 0

        override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> {
            getFriendsCallCount++
            return friendsResult
        }

        override suspend fun addFriendByBattleCode(
            userId: UserId,
            battleCode: String,
            clientRequestId: String,
        ): DomainResult<Friend> = DomainResult.Failure(AppError.Network)

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> =
            DomainResult.Failure(AppError.Network)

        override suspend fun getRivalComparison(
            userId: UserId,
            battleId: BattleId,
        ): DomainResult<RivalScoreComparison> = DomainResult.Failure(AppError.Network)
    }
}

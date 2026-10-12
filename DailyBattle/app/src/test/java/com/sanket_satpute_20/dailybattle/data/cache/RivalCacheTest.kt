package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteSocialDataSource
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
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
class RivalCacheTest {

    private lateinit var fakeDao: FakeRivalDao
    private lateinit var fakeRemoteDataSource: FakeRemoteSocialDataSource
    private lateinit var metadataStore: InMemoryCacheMetadataStore
    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var cache: RivalCache

    private val sampleRival = Rival(
        userId = UserId("user_test"),
        rivalUserId = UserId("rival_1"),
        rivalName = "Hector",
        selectedAt = 900_000L,
        status = RivalStatus.ACTIVE,
    )

    @Before
    fun setup() {
        fakeDao = FakeRivalDao()
        fakeRemoteDataSource = FakeRemoteSocialDataSource()
        metadataStore = InMemoryCacheMetadataStore()
        cache = RivalCache(
            rivalDao = fakeDao,
            remoteDataSource = fakeRemoteDataSource,
            metadataStore = metadataStore,
            clock = clock,
            policy = CachePolicy(
                domain = com.sanket_satpute_20.dailybattle.domain.authority.DomainType.RIVAL,
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
    fun writeAndReadReturnsFreshCachedRival() = runTest {
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)

        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals("rival_1", cached!!.data.rivalUserId.value)
        assertEquals("Hector", cached.data.rivalName)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertTrue(cached.isFresh)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun writeWithNullDeletesRival() = runTest {
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)
        assertEquals("rival_1", fakeDao.rivals["user_test"]?.rivalUserId)

        cache.write(UserId("user_test"), null, timestamp = currentTime)
        assertNull(fakeDao.rivals["user_test"])

        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        assertNull((result as DomainResult.Success).value)
    }

    @Test
    fun readReturnsStaleWhenPastTtl() = runTest {
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)

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
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)

        fakeRemoteDataSource.rivalResult = DomainResult.Success(null)

        val result = cache.refresh(UserId("user_test"), force = false)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals("Hector", cached.data?.rivalName)
        assertEquals(0, fakeRemoteDataSource.getCurrentRivalCallCount)
    }

    @Test
    fun refreshWithForceCallsRemoteAndUpdatesCache() = runTest {
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)

        val updatedRival = sampleRival.copy(rivalName = "Hector Updated")
        fakeRemoteDataSource.rivalResult = DomainResult.Success(updatedRival)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals("Hector Updated", cached.data?.rivalName)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertFalse(cached.isFromCache)
        assertEquals(1, fakeRemoteDataSource.getCurrentRivalCallCount)

        assertEquals("Hector Updated", fakeDao.rivals["user_test"]?.rivalName)
    }

    @Test
    fun refreshFallsBackToStaleCacheOnRemoteNetworkError() = runTest {
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)

        fakeRemoteDataSource.rivalResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals("Hector", cached.data?.rivalName)
        assertEquals(CacheFreshness.STALE, cached.freshness)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun refreshReturnsFailureOnRemoteNetworkErrorWhenNoCacheExists() = runTest {
        fakeRemoteDataSource.rivalResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun invalidateClearsMetadataStore() = runTest {
        cache.write(UserId("user_test"), sampleRival, timestamp = currentTime)
        assertNotNull(metadataStore.getLastUpdated("rival_user_test"))

        cache.invalidate(UserId("user_test"))
        assertNull(metadataStore.getLastUpdated("rival_user_test"))
    }

    private class FakeRivalDao : RivalDao {
        val rivals = mutableMapOf<String, RivalEntity>()

        override suspend fun insertRival(rival: RivalEntity) {
            rivals[rival.userId] = rival
        }

        override suspend fun getRivalForUser(userId: String): RivalEntity? = rivals[userId]

        override fun observeRivalForUser(userId: String): Flow<RivalEntity?> = flowOf(rivals[userId])

        override suspend fun deleteRivalForUser(userId: String): Int {
            return if (rivals.remove(userId) != null) 1 else 0
        }
    }

    private class FakeRemoteSocialDataSource : RemoteSocialDataSource {
        var rivalResult: DomainResult<Rival?> = DomainResult.Failure(AppError.Network)
        var getCurrentRivalCallCount = 0

        override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> =
            DomainResult.Failure(AppError.Network)

        override suspend fun addFriendByBattleCode(
            userId: UserId,
            battleCode: String,
            clientRequestId: String,
        ): DomainResult<Friend> = DomainResult.Failure(AppError.Network)

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
            getCurrentRivalCallCount++
            return rivalResult
        }

        override suspend fun getRivalComparison(
            userId: UserId,
            battleId: BattleId,
        ): DomainResult<RivalScoreComparison> = DomainResult.Failure(AppError.Network)
    }
}

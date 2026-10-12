package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteProfileDataSource
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class BattleHistoryCacheTest {

    private lateinit var fakeDao: FakeBattleHistoryDao
    private lateinit var fakeRemoteDataSource: FakeRemoteProfileDataSource
    private lateinit var metadataStore: InMemoryCacheMetadataStore
    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var cache: BattleHistoryCache

    private val sampleHistory = listOf(
        BattleResult(
            resultId = BattleResultId("res_battle_1"),
            userId = UserId("user_test"),
            battleId = BattleId("battle_1"),
            sessionId = BattleSessionId("sess_1"),
            snapScore = 300,
            shiftScore = 250,
            crowdCallScore = 350,
            consistencyScore = 900,
            totalScore = 900,
            percentile = 92.5,
            completedAt = 800_000L,
        ),
        BattleResult(
            resultId = BattleResultId("res_battle_2"),
            userId = UserId("user_test"),
            battleId = BattleId("battle_2"),
            sessionId = BattleSessionId("sess_2"),
            snapScore = 280,
            shiftScore = 240,
            crowdCallScore = 330,
            consistencyScore = 850,
            totalScore = 850,
            percentile = 85.0,
            completedAt = 700_000L,
        ),
    )

    @Before
    fun setup() {
        fakeDao = FakeBattleHistoryDao()
        fakeRemoteDataSource = FakeRemoteProfileDataSource()
        metadataStore = InMemoryCacheMetadataStore()
        cache = BattleHistoryCache(
            battleHistoryDao = fakeDao,
            remoteDataSource = fakeRemoteDataSource,
            metadataStore = metadataStore,
            clock = clock,
            policy = CachePolicy(
                domain = com.sanket_satpute_20.dailybattle.domain.authority.DomainType.BATTLE_HISTORY,
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
    fun writeAndReadReturnsFreshCachedHistory() = runTest {
        cache.write(UserId("user_test"), sampleHistory, timestamp = currentTime)

        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals(2, cached!!.data.size)
        assertEquals("battle_1", cached.data[0].battleId.value)
        assertEquals(900, cached.data[0].totalScore)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertTrue(cached.isFresh)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun readReturnsStaleWhenPastTtl() = runTest {
        cache.write(UserId("user_test"), sampleHistory, timestamp = currentTime)

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
        cache.write(UserId("user_test"), sampleHistory, timestamp = currentTime)

        fakeRemoteDataSource.historyResult = DomainResult.Success(emptyList())

        val result = cache.refresh(UserId("user_test"), force = false)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(2, cached.data.size) // Did not call remote
        assertEquals(0, fakeRemoteDataSource.getBattleHistoryCallCount)
    }

    @Test
    fun refreshWithForceCallsRemoteAndUpdatesCache() = runTest {
        cache.write(UserId("user_test"), sampleHistory, timestamp = currentTime)

        val updatedHistory = listOf(
            sampleHistory[0].copy(totalScore = 950),
        )
        fakeRemoteDataSource.historyResult = DomainResult.Success(updatedHistory)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(1, cached.data.size)
        assertEquals(950, cached.data[0].totalScore)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertFalse(cached.isFromCache)
        assertEquals(1, fakeRemoteDataSource.getBattleHistoryCallCount)

        assertEquals(1, fakeDao.items.size)
    }

    @Test
    fun refreshFallsBackToStaleCacheOnRemoteNetworkError() = runTest {
        cache.write(UserId("user_test"), sampleHistory, timestamp = currentTime)

        fakeRemoteDataSource.historyResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(2, cached.data.size)
        assertEquals(CacheFreshness.STALE, cached.freshness)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun refreshReturnsFailureOnRemoteNetworkErrorWhenNoCacheExists() = runTest {
        fakeRemoteDataSource.historyResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun invalidateClearsMetadataStore() = runTest {
        cache.write(UserId("user_test"), sampleHistory, timestamp = currentTime)
        assertNotNull(metadataStore.getLastUpdated("history_user_test"))

        cache.invalidate(UserId("user_test"))
        assertNull(metadataStore.getLastUpdated("history_user_test"))
    }

    private class FakeBattleHistoryDao : BattleHistoryDao {
        val items = mutableListOf<BattleHistoryItemEntity>()

        override suspend fun insertHistoryItems(newItems: List<BattleHistoryItemEntity>) {
            items.clear()
            items.addAll(newItems)
        }

        override suspend fun getHistoryForUser(userId: String): List<BattleHistoryItemEntity> =
            items.filter { it.userId == userId }

        override suspend fun clearHistoryForUser(userId: String): Int {
            val count = items.size
            items.clear()
            return count
        }
    }

    private class FakeRemoteProfileDataSource : RemoteProfileDataSource {
        var historyResult: DomainResult<List<BattleResult>> = DomainResult.Failure(AppError.Network)
        var getBattleHistoryCallCount = 0

        override suspend fun getProfile(userId: UserId): DomainResult<Profile> =
            DomainResult.Failure(AppError.Network)

        override suspend fun getBattleHistory(userId: UserId): DomainResult<List<BattleResult>> {
            getBattleHistoryCallCount++
            return historyResult
        }

        override suspend fun getBattleHistoryItem(
            userId: UserId,
            battleId: BattleId,
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)
    }
}

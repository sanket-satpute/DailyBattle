package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteProfileDataSource
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
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
class ProfileCacheTest {

    private lateinit var fakeDao: FakeProfileDao
    private lateinit var fakeRemoteDataSource: FakeRemoteProfileDataSource
    private lateinit var metadataStore: InMemoryCacheMetadataStore
    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var cache: ProfileCache

    private val sampleProfile = Profile(
        userId = UserId("user_test"),
        battleName = "Spartan",
        momentum = 3,
        bestScore = 800,
        averageScore = 650,
        battleDNA = BattleDNA(speed = 70, memory = 80, people = 90),
        records = PersonalRecords(bestScore = 800, bestMomentum = 5, battlesPlayed = 20),
    )

    @Before
    fun setup() {
        fakeDao = FakeProfileDao()
        fakeRemoteDataSource = FakeRemoteProfileDataSource()
        metadataStore = InMemoryCacheMetadataStore()
        cache = ProfileCache(
            profileDao = fakeDao,
            remoteDataSource = fakeRemoteDataSource,
            metadataStore = metadataStore,
            clock = clock,
            policy = CachePolicy(
                domain = com.sanket_satpute_20.dailybattle.domain.authority.DomainType.USER_PROFILE,
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
    fun writeAndReadReturnsFreshCachedProfile() = runTest {
        cache.write(sampleProfile, timestamp = currentTime)

        val result = cache.read(UserId("user_test"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals(sampleProfile.userId, cached!!.data.userId)
        assertEquals(sampleProfile.battleName, cached.data.battleName)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertTrue(cached.isFresh)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun readReturnsStaleWhenPastTtl() = runTest {
        cache.write(sampleProfile, timestamp = currentTime)

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
        cache.write(sampleProfile, timestamp = currentTime)

        fakeRemoteDataSource.profileResult = DomainResult.Success(
            sampleProfile.copy(battleName = "RemoteName")
        )

        val result = cache.refresh(UserId("user_test"), force = false)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals("Spartan", cached.data.battleName) // Did not call remote
        assertEquals(0, fakeRemoteDataSource.getProfileCallCount)
    }

    @Test
    fun refreshWithForceCallsRemoteAndUpdatesCache() = runTest {
        cache.write(sampleProfile, timestamp = currentTime)

        val updatedProfile = sampleProfile.copy(battleName = "UpdatedGladiator")
        fakeRemoteDataSource.profileResult = DomainResult.Success(updatedProfile)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals("UpdatedGladiator", cached.data.battleName)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertFalse(cached.isFromCache)
        assertEquals(1, fakeRemoteDataSource.getProfileCallCount)

        // Verify write persisted to DAO
        assertEquals("UpdatedGladiator", fakeDao.profiles["user_test"]?.battleName)
    }

    @Test
    fun refreshFallsBackToStaleCacheOnRemoteNetworkError() = runTest {
        cache.write(sampleProfile, timestamp = currentTime)

        fakeRemoteDataSource.profileResult = DomainResult.Failure(AppError.Network)

        // Force refresh to trigger network call
        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(sampleProfile.userId, cached.data.userId)
        assertEquals(CacheFreshness.STALE, cached.freshness)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun refreshReturnsFailureOnRemoteNetworkErrorWhenNoCacheExists() = runTest {
        fakeRemoteDataSource.profileResult = DomainResult.Failure(AppError.Network)

        val result = cache.refresh(UserId("user_test"), force = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun invalidateClearsMetadataStore() = runTest {
        cache.write(sampleProfile, timestamp = currentTime)
        assertNotNull(metadataStore.getLastUpdated("profile_user_test"))

        cache.invalidate(UserId("user_test"))
        assertNull(metadataStore.getLastUpdated("profile_user_test"))
    }

    private class FakeProfileDao : ProfileDao {
        val profiles = mutableMapOf<String, ProfileEntity>()

        override suspend fun insertProfile(profile: ProfileEntity) {
            profiles[profile.userId] = profile
        }

        override suspend fun getProfileForUser(userId: String): ProfileEntity? = profiles[userId]

        override fun observeProfileForUser(userId: String): Flow<ProfileEntity?> = flowOf(profiles[userId])

        override suspend fun deleteProfileForUser(userId: String): Int {
            return if (profiles.remove(userId) != null) 1 else 0
        }
    }

    private class FakeRemoteProfileDataSource : RemoteProfileDataSource {
        var profileResult: DomainResult<Profile> = DomainResult.Failure(AppError.Network)
        var getProfileCallCount = 0

        override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
            getProfileCallCount++
            return profileResult
        }

        override suspend fun getBattleHistory(userId: UserId): DomainResult<List<BattleResult>> =
            DomainResult.Failure(AppError.Network)

        override suspend fun getBattleHistoryItem(
            userId: UserId,
            battleId: BattleId,
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)
    }
}

package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteBattleDataSource
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
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
class BattleDefinitionCacheTest {

    private lateinit var fakeBattleDao: FakeBattleDao
    private lateinit var fakeChallengeDao: FakeChallengeDao
    private lateinit var fakeRemoteDataSource: FakeRemoteBattleDataSource
    private lateinit var metadataStore: InMemoryCacheMetadataStore
    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var cache: BattleDefinitionCache

    private val sampleBattle = Battle(
        battleId = BattleId("battle_today"),
        battleDate = 20261012L,
        status = BattleStatus.ACTIVE,
        version = 1,
        challenges = listOf(
            Challenge(
                challengeId = ChallengeId("ch_1"),
                battleId = BattleId("battle_today"),
                type = ChallengeType.SNAP,
                order = 1,
                version = 1,
            ),
            Challenge(
                challengeId = ChallengeId("ch_2"),
                battleId = BattleId("battle_today"),
                type = ChallengeType.SHIFT,
                order = 2,
                version = 1,
            ),
            Challenge(
                challengeId = ChallengeId("ch_3"),
                battleId = BattleId("battle_today"),
                type = ChallengeType.CROWD_CALL,
                order = 3,
                version = 1,
            ),
        ),
    )

    @Before
    fun setup() {
        fakeBattleDao = FakeBattleDao()
        fakeChallengeDao = FakeChallengeDao()
        fakeRemoteDataSource = FakeRemoteBattleDataSource()
        metadataStore = InMemoryCacheMetadataStore()
        cache = BattleDefinitionCache(
            battleDao = fakeBattleDao,
            challengeDao = fakeChallengeDao,
            remoteDataSource = fakeRemoteDataSource,
            metadataStore = metadataStore,
            clock = clock,
            policy = CachePolicy(
                domain = com.sanket_satpute_20.dailybattle.domain.authority.DomainType.OFFICIAL_BATTLE_DEFINITION,
                ttlMillis = 60_000L,
                maxStaleMillis = 300_000L,
            ),
        )
    }

    @Test
    fun readReturnsNullWhenCacheIsEmpty() = runTest {
        val result = cache.read(BattleId("battle_today"))
        assertTrue(result is DomainResult.Success)
        assertNull((result as DomainResult.Success).value)
    }

    @Test
    fun writeAndReadByIdReturnsFreshCachedBattleWithChallenges() = runTest {
        cache.write(sampleBattle, timestamp = currentTime)

        val result = cache.read(BattleId("battle_today"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals("battle_today", cached!!.data.battleId.value)
        assertEquals(3, cached.data.challenges.size)
        assertEquals(ChallengeType.SNAP, cached.data.challenges[0].type)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertTrue(cached.isFresh)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun readByDateReturnsCachedBattle() = runTest {
        cache.write(sampleBattle, timestamp = currentTime)

        val result = cache.readByDate(20261012L)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals("battle_today", cached!!.data.battleId.value)
        assertEquals(3, cached.data.challenges.size)
    }

    @Test
    fun readReturnsStaleWhenPastTtl() = runTest {
        cache.write(sampleBattle, timestamp = currentTime)

        currentTime += 100_000L

        val result = cache.read(BattleId("battle_today"))
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertNotNull(cached)
        assertEquals(CacheFreshness.STALE, cached!!.freshness)
        assertFalse(cached.isFresh)
        assertTrue(cached.isUsable)
    }

    @Test
    fun refreshTodayBattleWithoutForceReturnsFreshCacheWithoutCallingRemote() = runTest {
        // Pre-populate via refresh to initialize tracked state
        fakeRemoteDataSource.todayBattleResult = DomainResult.Success(sampleBattle)
        val initial = cache.refreshTodayBattle(force = true)
        assertTrue(initial is DomainResult.Success)
        assertEquals(1, fakeRemoteDataSource.getTodayBattleCallCount)

        // Second call without force should use fresh cache
        val result = cache.refreshTodayBattle(force = false)
        assertTrue(result is DomainResult.Success)
        assertEquals(1, fakeRemoteDataSource.getTodayBattleCallCount) // Call count did not increase
    }

    @Test
    fun refreshTodayBattleWithForceCallsRemoteAndUpdatesCache() = runTest {
        val updatedBattle = sampleBattle.copy(version = 2)
        fakeRemoteDataSource.todayBattleResult = DomainResult.Success(updatedBattle)

        val result = cache.refreshTodayBattle(force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals(2, cached.data.version)
        assertEquals(CacheFreshness.FRESH, cached.freshness)
        assertFalse(cached.isFromCache)
        assertEquals(1, fakeRemoteDataSource.getTodayBattleCallCount)

        assertEquals(2, fakeBattleDao.battles["battle_today"]?.version)
    }

    @Test
    fun refreshTodayBattleFallsBackToStaleCacheOnRemoteNetworkError() = runTest {
        fakeRemoteDataSource.todayBattleResult = DomainResult.Success(sampleBattle)
        cache.refreshTodayBattle(force = true)

        // Now set network failure
        fakeRemoteDataSource.todayBattleResult = DomainResult.Failure(AppError.Network)

        val result = cache.refreshTodayBattle(force = true)
        assertTrue(result is DomainResult.Success)
        val cached = (result as DomainResult.Success).value
        assertEquals("battle_today", cached.data.battleId.value)
        assertEquals(CacheFreshness.STALE, cached.freshness)
        assertTrue(cached.isFromCache)
    }

    @Test
    fun refreshTodayBattleReturnsFailureOnRemoteNetworkErrorWhenNoCacheExists() = runTest {
        fakeRemoteDataSource.todayBattleResult = DomainResult.Failure(AppError.Network)

        val result = cache.refreshTodayBattle(force = true)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun invalidateClearsMetadataStore() = runTest {
        cache.write(sampleBattle, timestamp = currentTime)
        assertNotNull(metadataStore.getLastUpdated("battle_battle_today"))

        cache.invalidate(BattleId("battle_today"))
        assertNull(metadataStore.getLastUpdated("battle_battle_today"))
    }

    private class FakeBattleDao : BattleDao {
        val battles = mutableMapOf<String, BattleEntity>()

        override suspend fun insertBattle(battle: BattleEntity) {
            battles[battle.battleId] = battle
        }

        override suspend fun getBattleById(battleId: String): BattleEntity? = battles[battleId]

        override suspend fun getBattleByDate(date: Long): BattleEntity? =
            battles.values.find { it.battleDate == date }

        override suspend fun deleteBattleById(battleId: String): Int {
            return if (battles.remove(battleId) != null) 1 else 0
        }

        override suspend fun clearBattles(): Int {
            val count = battles.size
            battles.clear()
            return count
        }
    }

    private class FakeChallengeDao : ChallengeDao {
        val challenges = mutableMapOf<String, ChallengeEntity>()

        override suspend fun insertChallenges(list: List<ChallengeEntity>) {
            list.forEach { challenges[it.challengeId] = it }
        }

        override suspend fun getChallengesForBattle(battleId: String): List<ChallengeEntity> =
            challenges.values.filter { it.battleId == battleId }.sortedBy { it.order }

        override suspend fun getChallengeById(challengeId: String): ChallengeEntity? =
            challenges[challengeId]

        override suspend fun deleteChallengesForBattle(battleId: String): Int {
            val toRemove = challenges.values.filter { it.battleId == battleId }
            toRemove.forEach { challenges.remove(it.challengeId) }
            return toRemove.size
        }
    }

    private class FakeRemoteBattleDataSource : RemoteBattleDataSource {
        var todayBattleResult: DomainResult<Battle> = DomainResult.Failure(AppError.Network)
        var getTodayBattleCallCount = 0

        override suspend fun getTodayBattle(): DomainResult<Battle> {
            getTodayBattleCallCount++
            return todayBattleResult
        }

        override suspend fun startBattle(
            battleId: BattleId,
            clientRequestId: String,
            userId: UserId,
        ): DomainResult<BattleSession> = DomainResult.Failure(AppError.Network)

        override suspend fun submitChallengeResult(
            sessionId: BattleSessionId,
            challengeId: ChallengeId,
            clientRequestId: String,
            score: Int,
            maxScore: Int,
            completedAt: Long,
        ): DomainResult<ChallengeResult> = DomainResult.Failure(AppError.Network)

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String,
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)

        override suspend fun getBattleResult(
            battleId: BattleId,
            userId: UserId,
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)
    }
}

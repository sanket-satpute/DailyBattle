package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteBattleDataSource
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for Battle definition and Challenge specifications implementing Sprint 17.2 — Cache:
 * - cache read: loads local Battle and associated Challenge entities with freshness evaluation.
 * - cache write: writes Battle and Challenge entities, updating cache metadata.
 * - expiration/staleness rules: evaluates age against [CachePolicy.BATTLE_DEFINITION].
 * - refresh: coordinates remote fetch, writing on success and falling back to stale cache on network failure.
 */
@Singleton
class BattleDefinitionCache @Inject constructor(
    private val battleDao: BattleDao,
    private val challengeDao: ChallengeDao,
    private val remoteDataSource: RemoteBattleDataSource,
    private val metadataStore: CacheMetadataStore,
    private val clock: Clock,
    private val policy: CachePolicy = CachePolicy.BATTLE_DEFINITION,
) {

    suspend fun read(battleId: BattleId): DomainResult<CachedResource<Battle>?> {
        return try {
            val battleEntity = battleDao.getBattleById(battleId.value)
            if (battleEntity != null) {
                val challengeEntities = challengeDao.getChallengesForBattle(battleId.value)
                val challenges = challengeEntities.map { it.toDomain() }
                val battle = battleEntity.toDomain(challenges)
                val cachedAt = metadataStore.getLastUpdated(cacheKey(battleId)) ?: 0L
                val freshness = policy.evaluateFreshness(cachedAt, clock.millis())
                DomainResult.Success(
                    CachedResource(
                        data = battle,
                        freshness = freshness,
                        cachedAtMillis = cachedAt,
                        isFromCache = true,
                    )
                )
            } else {
                DomainResult.Success(null)
            }
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun readByDate(date: Long): DomainResult<CachedResource<Battle>?> {
        return try {
            val battleEntity = battleDao.getBattleByDate(date)
            if (battleEntity != null) {
                val challengeEntities = challengeDao.getChallengesForBattle(battleEntity.battleId)
                val challenges = challengeEntities.map { it.toDomain() }
                val battle = battleEntity.toDomain(challenges)
                val cachedAt = metadataStore.getLastUpdated(dateCacheKey(date))
                    ?: metadataStore.getLastUpdated(cacheKey(battle.battleId))
                    ?: 0L
                val freshness = policy.evaluateFreshness(cachedAt, clock.millis())
                DomainResult.Success(
                    CachedResource(
                        data = battle,
                        freshness = freshness,
                        cachedAtMillis = cachedAt,
                        isFromCache = true,
                    )
                )
            } else {
                DomainResult.Success(null)
            }
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun write(battle: Battle, timestamp: Long = clock.millis()): DomainResult<Unit> {
        return try {
            battleDao.insertBattle(battle.toEntity())
            challengeDao.insertChallenges(battle.challenges.map { it.toEntity() })
            metadataStore.setLastUpdated(cacheKey(battle.battleId), timestamp)
            metadataStore.setLastUpdated(dateCacheKey(battle.battleDate), timestamp)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    @Volatile
    private var lastTodayBattleId: String? = null

    suspend fun refreshTodayBattle(force: Boolean = false): DomainResult<CachedResource<Battle>> {
        val now = clock.millis()

        if (!force && lastTodayBattleId != null) {
            val cachedResult = read(BattleId(lastTodayBattleId!!))
            if (cachedResult is DomainResult.Success && cachedResult.value != null && cachedResult.value.isFresh) {
                return DomainResult.Success(cachedResult.value)
            }
        }

        return when (val remoteResult = remoteDataSource.getTodayBattle()) {
            is DomainResult.Success -> {
                val freshBattle = remoteResult.value
                lastTodayBattleId = freshBattle.battleId.value
                write(freshBattle, now)
                metadataStore.setLastUpdated("today_battle", now)
                DomainResult.Success(
                    CachedResource(
                        data = freshBattle,
                        freshness = CacheFreshness.FRESH,
                        cachedAtMillis = now,
                        isFromCache = false,
                    )
                )
            }
            is DomainResult.Failure -> {
                // Fall back to cached battle if available (stale-while-error per 09_OFFLINE_AND_ERROR_HANDLING.md §43, §54)
                val fallbackBattleId = lastTodayBattleId
                val fallback = if (fallbackBattleId != null) {
                    read(BattleId(fallbackBattleId))
                } else null

                if (fallback is DomainResult.Success && fallback.value != null) {
                    DomainResult.Success(
                        CachedResource(
                            data = fallback.value.data,
                            freshness = CacheFreshness.STALE,
                            cachedAtMillis = fallback.value.cachedAtMillis,
                            isFromCache = true,
                        )
                    )
                } else {
                    DomainResult.Failure(remoteResult.error)
                }
            }
        }
    }

    suspend fun invalidate(battleId: BattleId) {
        if (lastTodayBattleId == battleId.value) {
            lastTodayBattleId = null
        }
        metadataStore.clear(cacheKey(battleId))
        metadataStore.clear("today_battle")
    }

    private fun cacheKey(battleId: BattleId): String = "battle_${battleId.value}"
    private fun dateCacheKey(date: Long): String = "battle_date_$date"
}

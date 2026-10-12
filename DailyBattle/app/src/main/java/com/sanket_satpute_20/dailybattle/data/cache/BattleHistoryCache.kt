package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toBattleResult
import com.sanket_satpute_20.dailybattle.data.local.mapper.toHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteProfileDataSource
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for user battle history implementing Sprint 17.2 — Cache:
 * - cache read: loads local Room battle history entities with freshness evaluation.
 * - cache write: writes history items and updates cache metadata timestamp.
 * - expiration/staleness rules: evaluates age against [CachePolicy.BATTLE_HISTORY].
 * - refresh: coordinates remote fetch, writing on success and falling back to stale cache on network failure.
 */
@Singleton
class BattleHistoryCache @Inject constructor(
    private val battleHistoryDao: BattleHistoryDao,
    private val remoteDataSource: RemoteProfileDataSource,
    private val metadataStore: CacheMetadataStore,
    private val clock: Clock,
    private val policy: CachePolicy = CachePolicy.BATTLE_HISTORY,
) {

    suspend fun read(userId: UserId): DomainResult<CachedResource<List<BattleResult>>?> {
        return try {
            val entities = battleHistoryDao.getHistoryForUser(userId.value)
            val cachedAt = metadataStore.getLastUpdated(cacheKey(userId))
            if (entities.isNotEmpty() || (cachedAt != null && cachedAt > 0L)) {
                val results = entities.map { it.toBattleResult() }
                val timestamp = cachedAt ?: 0L
                val freshness = policy.evaluateFreshness(timestamp, clock.millis())
                DomainResult.Success(
                    CachedResource(
                        data = results,
                        freshness = freshness,
                        cachedAtMillis = timestamp,
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

    suspend fun write(
        userId: UserId,
        history: List<BattleResult>,
        timestamp: Long = clock.millis(),
    ): DomainResult<Unit> {
        return try {
            battleHistoryDao.insertHistoryItems(history.map { it.toHistoryItemEntity() })
            metadataStore.setLastUpdated(cacheKey(userId), timestamp)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun refresh(
        userId: UserId,
        force: Boolean = false,
    ): DomainResult<CachedResource<List<BattleResult>>> {
        val now = clock.millis()

        if (!force) {
            val cachedResult = read(userId)
            if (cachedResult is DomainResult.Success && cachedResult.value != null && cachedResult.value.isFresh) {
                return DomainResult.Success(cachedResult.value)
            }
        }

        return when (val remoteResult = remoteDataSource.getBattleHistory(userId)) {
            is DomainResult.Success -> {
                val freshHistory = remoteResult.value
                write(userId, freshHistory, now)
                DomainResult.Success(
                    CachedResource(
                        data = freshHistory,
                        freshness = CacheFreshness.FRESH,
                        cachedAtMillis = now,
                        isFromCache = false,
                    )
                )
            }
            is DomainResult.Failure -> {
                // Fall back to cached history if available (stale-while-error per 09_OFFLINE_AND_ERROR_HANDLING.md §43, §54)
                val fallback = read(userId)
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

    suspend fun invalidate(userId: UserId) {
        metadataStore.clear(cacheKey(userId))
    }

    private fun cacheKey(userId: UserId): String = "history_${userId.value}"
}

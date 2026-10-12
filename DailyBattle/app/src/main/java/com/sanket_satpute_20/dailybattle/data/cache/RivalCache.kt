package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteSocialDataSource
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for Rival relationships implementing Sprint 17.2 — Cache:
 * - cache read: loads local Room rival entity with freshness evaluation.
 * - cache write: writes rival entity and updates cache metadata timestamp.
 * - expiration/staleness rules: evaluates age against [CachePolicy.RIVAL].
 * - refresh: coordinates remote fetch, writing on success and falling back to stale cache on network failure.
 */
@Singleton
class RivalCache @Inject constructor(
    private val rivalDao: RivalDao,
    private val remoteDataSource: RemoteSocialDataSource,
    private val metadataStore: CacheMetadataStore,
    private val clock: Clock,
    private val policy: CachePolicy = CachePolicy.RIVAL,
) {

    suspend fun read(userId: UserId): DomainResult<CachedResource<Rival>?> {
        return try {
            val entity = rivalDao.getRivalForUser(userId.value)
            if (entity != null) {
                val cachedAt = metadataStore.getLastUpdated(cacheKey(userId))?.takeIf { it > 0L }
                    ?: entity.selectedAt
                val freshness = policy.evaluateFreshness(cachedAt, clock.millis())
                DomainResult.Success(
                    CachedResource(
                        data = entity.toDomain(),
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

    suspend fun write(userId: UserId, rival: Rival?, timestamp: Long = clock.millis()): DomainResult<Unit> {
        return try {
            if (rival != null) {
                rivalDao.insertRival(rival.toEntity())
            } else {
                rivalDao.deleteRivalForUser(userId.value)
            }
            metadataStore.setLastUpdated(cacheKey(userId), timestamp)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun refresh(userId: UserId, force: Boolean = false): DomainResult<CachedResource<Rival?>> {
        val now = clock.millis()

        if (!force) {
            val cachedResult = read(userId)
            if (cachedResult is DomainResult.Success && cachedResult.value != null && cachedResult.value.isFresh) {
                return DomainResult.Success(
                    CachedResource(
                        data = cachedResult.value.data,
                        freshness = cachedResult.value.freshness,
                        cachedAtMillis = cachedResult.value.cachedAtMillis,
                        isFromCache = true,
                    )
                )
            }
        }

        return when (val remoteResult = remoteDataSource.getCurrentRival(userId)) {
            is DomainResult.Success -> {
                val freshRival = remoteResult.value
                write(userId, freshRival, now)
                DomainResult.Success(
                    CachedResource(
                        data = freshRival,
                        freshness = CacheFreshness.FRESH,
                        cachedAtMillis = now,
                        isFromCache = false,
                    )
                )
            }
            is DomainResult.Failure -> {
                // Fall back to cached rival if available (stale-while-error per 09_OFFLINE_AND_ERROR_HANDLING.md §43, §54)
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

    private fun cacheKey(userId: UserId): String = "rival_${userId.value}"
}

package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteSocialDataSource
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for friend relationships and friend scores implementing Sprint 17.2 — Cache:
 * - cache read: loads local Room friends list with freshness evaluation.
 * - cache write: writes friend entities with timestamp.
 * - expiration/staleness rules: evaluates age against [CachePolicy.FRIENDS].
 * - refresh: coordinates remote fetch, writing on success and falling back to stale cache on network failure.
 */
@Singleton
class FriendsCache @Inject constructor(
    private val friendDao: FriendDao,
    private val remoteDataSource: RemoteSocialDataSource,
    private val metadataStore: CacheMetadataStore,
    private val clock: Clock,
    private val policy: CachePolicy = CachePolicy.FRIENDS,
) {

    suspend fun read(userId: UserId): DomainResult<CachedResource<List<Friend>>?> {
        return try {
            val entities = friendDao.getFriendsForUser(userId.value)
            val lastUpdated = metadataStore.getLastUpdated(cacheKey(userId))
                ?: entities.maxOfOrNull { it.updatedAt }
                ?: 0L

            if (entities.isNotEmpty() || lastUpdated > 0L) {
                val freshness = policy.evaluateFreshness(lastUpdated, clock.millis())
                DomainResult.Success(
                    CachedResource(
                        data = entities.map { it.toDomain() },
                        freshness = freshness,
                        cachedAtMillis = lastUpdated,
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

    suspend fun write(userId: UserId, friends: List<Friend>, timestamp: Long = clock.millis()): DomainResult<Unit> {
        return try {
            friendDao.insertFriends(friends.map { it.toEntity() })
            metadataStore.setLastUpdated(cacheKey(userId), timestamp)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun refresh(userId: UserId, force: Boolean = false): DomainResult<CachedResource<List<Friend>>> {
        val now = clock.millis()

        if (!force) {
            val cachedResult = read(userId)
            if (cachedResult is DomainResult.Success && cachedResult.value != null && cachedResult.value.isFresh) {
                return DomainResult.Success(cachedResult.value)
            }
        }

        return when (val remoteResult = remoteDataSource.getFriends(userId)) {
            is DomainResult.Success -> {
                val freshFriends = remoteResult.value
                write(userId, freshFriends, now)
                DomainResult.Success(
                    CachedResource(
                        data = freshFriends,
                        freshness = CacheFreshness.FRESH,
                        cachedAtMillis = now,
                        isFromCache = false,
                    )
                )
            }
            is DomainResult.Failure -> {
                // Fall back to cached friends if available (09_OFFLINE_AND_ERROR_HANDLING.md §54)
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

    private fun cacheKey(userId: UserId): String = "friends_${userId.value}"
}

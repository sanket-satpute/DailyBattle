package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteProfileDataSource
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for user profile data implementing Sprint 17.2 — Cache:
 * - cache read: loads local Room profile with freshness evaluation.
 * - cache write: writes profile entity with timestamp.
 * - expiration/staleness rules: evaluates age against [CachePolicy.PROFILE].
 * - refresh: coordinates remote fetch, writing on success and falling back to stale cache on network failure.
 */
@Singleton
class ProfileCache @Inject constructor(
    private val profileDao: ProfileDao,
    private val remoteDataSource: RemoteProfileDataSource,
    private val metadataStore: CacheMetadataStore,
    private val clock: Clock,
    private val policy: CachePolicy = CachePolicy.PROFILE,
) {

    suspend fun read(userId: UserId): DomainResult<CachedResource<Profile>?> {
        return try {
            val entity = profileDao.getProfileForUser(userId.value)
            if (entity != null) {
                val freshness = policy.evaluateFreshness(entity.updatedAt, clock.millis())
                DomainResult.Success(
                    CachedResource(
                        data = entity.toDomain(),
                        freshness = freshness,
                        cachedAtMillis = entity.updatedAt,
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

    suspend fun write(profile: Profile, timestamp: Long = clock.millis()): DomainResult<Unit> {
        return try {
            profileDao.insertProfile(profile.toEntity(updatedAt = timestamp))
            metadataStore.setLastUpdated(cacheKey(profile.userId), timestamp)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun refresh(userId: UserId, force: Boolean = false): DomainResult<CachedResource<Profile>> {
        val now = clock.millis()

        if (!force) {
            val cachedResult = read(userId)
            if (cachedResult is DomainResult.Success && cachedResult.value != null && cachedResult.value.isFresh) {
                return DomainResult.Success(cachedResult.value)
            }
        }

        return when (val remoteResult = remoteDataSource.getProfile(userId)) {
            is DomainResult.Success -> {
                val freshProfile = remoteResult.value
                write(freshProfile, now)
                DomainResult.Success(
                    CachedResource(
                        data = freshProfile,
                        freshness = CacheFreshness.FRESH,
                        cachedAtMillis = now,
                        isFromCache = false,
                    )
                )
            }
            is DomainResult.Failure -> {
                // Fall back to cached profile if available (stale-while-error per 09_OFFLINE_AND_ERROR_HANDLING.md §43, §54)
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

    private fun cacheKey(userId: UserId): String = "profile_${userId.value}"
}

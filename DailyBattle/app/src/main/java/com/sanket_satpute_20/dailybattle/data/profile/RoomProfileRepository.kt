package com.sanket_satpute_20.dailybattle.data.profile

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.profile.ProfileRepository
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local Room-backed implementation of [ProfileRepository].
 *
 * Implements Sprint 15.3 repository integration connecting the profile
 * repository domain contract to the local Room storage layer.
 */
@Singleton
class RoomProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
) : ProfileRepository {

    override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
        return try {
            val entity = profileDao.getProfileForUser(userId.value)
            if (entity != null) {
                DomainResult.Success(entity.toDomain())
            } else {
                DomainResult.Failure(AppError.Domain)
            }
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend fun saveProfile(profile: Profile) {
        runCatching {
            profileDao.insertProfile(profile.toEntity(updatedAt = System.currentTimeMillis()))
        }
    }
}

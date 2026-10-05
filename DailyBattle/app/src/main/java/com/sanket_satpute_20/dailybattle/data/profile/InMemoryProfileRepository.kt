package com.sanket_satpute_20.dailybattle.data.profile

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.profile.ProfileRepository
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryProfileRepository @Inject constructor() : ProfileRepository {

    private val profiles = mutableMapOf<UserId, Profile>()

    init {
        // Mock data for Sprint 14.1
        profiles[UserId("current-user")] = Profile(
            userId = UserId("current-user"),
            battleName = "Sanket",
            momentum = 7,
            bestScore = 927,
            averageScore = 806,
            battleDNA = BattleDNA(
                speed = 91,
                memory = 78,
                people = 94
            ),
            records = PersonalRecords(
                bestScore = 927,
                bestMomentum = 12,
                battlesPlayed = 42
            )
        )
    }

    override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
        val profile = profiles[userId]
        return if (profile != null) {
            DomainResult.Success(profile)
        } else {
            DomainResult.Failure(AppError.Domain)
        }
    }
}

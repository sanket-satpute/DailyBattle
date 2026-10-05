package com.sanket_satpute_20.dailybattle.domain.profile

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend fun execute(userId: UserId): DomainResult<Profile> {
        return profileRepository.getProfile(userId)
    }
}

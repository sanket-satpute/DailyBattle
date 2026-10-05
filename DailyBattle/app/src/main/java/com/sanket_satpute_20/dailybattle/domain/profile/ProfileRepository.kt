package com.sanket_satpute_20.dailybattle.domain.profile

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

interface ProfileRepository {
    suspend fun getProfile(userId: UserId): DomainResult<Profile>
}

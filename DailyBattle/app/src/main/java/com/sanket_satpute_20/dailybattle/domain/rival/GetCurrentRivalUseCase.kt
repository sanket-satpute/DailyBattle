package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class GetCurrentRivalUseCase @Inject constructor(
    private val rivalRepository: RivalRepository
) {
    suspend fun execute(userId: UserId): DomainResult<Rival?> {
        return rivalRepository.getCurrentRival(userId)
    }
}

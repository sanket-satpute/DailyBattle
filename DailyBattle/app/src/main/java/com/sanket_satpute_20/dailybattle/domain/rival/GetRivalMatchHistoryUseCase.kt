package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class GetRivalMatchHistoryUseCase @Inject constructor(
    private val rivalRepository: RivalRepository
) {
    suspend fun execute(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>> {
        return rivalRepository.getRivalMatchHistory(userId, rivalId)
    }
}

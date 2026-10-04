package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class GetRivalComparisonUseCase @Inject constructor(
    private val rivalRepository: RivalRepository
) {
    suspend fun execute(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison> {
        return rivalRepository.getRivalComparison(userId, rivalId, battleId)
    }
}

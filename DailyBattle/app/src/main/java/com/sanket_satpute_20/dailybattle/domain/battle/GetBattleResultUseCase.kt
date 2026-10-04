package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

import javax.inject.Inject

/**
 * Use case to retrieve an existing BattleResult.
 *
 * Requirements (Sprint 7.5):
 * - Navigates to Results screen when the user has already completed a battle.
 */
class GetBattleResultUseCase @Inject constructor(
    private val resultRepository: BattleResultRepository
) {
    suspend fun execute(userId: UserId, battleId: BattleId): DomainResult<BattleResult> {
        return resultRepository.getBattleResult(userId, battleId)
    }
}

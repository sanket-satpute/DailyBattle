package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

interface RivalRepository {
    suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?>
    suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison>
    suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>>
}

package com.sanket_satpute_20.dailybattle.data.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.*
import com.sanket_satpute_20.dailybattle.core.error.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryRivalRepository @Inject constructor() : RivalRepository {

    private var currentRival: Rival? = null
    private val rivalComparisons = mutableMapOf<Pair<String, String>, RivalScoreComparison>()
    private val rivalHistory = mutableMapOf<Pair<String, String>, List<RivalMatchHistoryItem>>()

    override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
        return DomainResult.Success(currentRival?.takeIf { it.userId == userId })
    }

    override suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison> {
        val comparison = rivalComparisons[Pair(userId.value, battleId.value)]
        return if (comparison != null) {
            DomainResult.Success(comparison)
        } else {
            DomainResult.Failure(AppError.Domain)
        }
    }

    override suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>> {
        val history = rivalHistory[Pair(userId.value, rivalId.value)] ?: emptyList()
        return DomainResult.Success(history)
    }

    // Test helpers
    fun setRival(rival: Rival?) {
        currentRival = rival
    }

    fun setRivalComparison(userId: UserId, battleId: BattleId, comparison: RivalScoreComparison) {
        rivalComparisons[Pair(userId.value, battleId.value)] = comparison
    }

    fun setRivalMatchHistory(userId: UserId, rivalId: UserId, history: List<RivalMatchHistoryItem>) {
        rivalHistory[Pair(userId.value, rivalId.value)] = history
    }
    
    fun clear() {
        currentRival = null
        rivalComparisons.clear()
        rivalHistory.clear()
    }
}

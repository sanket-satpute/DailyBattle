package com.sanket_satpute_20.dailybattle.data.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.UUID
import javax.inject.Inject

class InMemoryBattleResultRepository @Inject constructor() : BattleResultRepository {
    
    private val results = mutableMapOf<Pair<String, String>, BattleResult>()
    
    // Simulate idempotency by keeping track of clientRequestId
    private val requests = mutableMapOf<String, BattleResult>()

    override suspend fun completeBattle(
        sessionId: BattleSessionId,
        clientRequestId: String
    ): DomainResult<BattleResult> {
        // If we already processed this request, return the cached result (idempotency)
        if (requests.containsKey(clientRequestId)) {
            return DomainResult.Success(requests[clientRequestId]!!)
        }

        // Simulate calculating the result (normally done by the backend)
        // Since we are in-memory and don't have access to the actual session details here,
        // we'll simulate a dummy BattleResult. 
        val result = BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = UserId("simulated-user"),
            battleId = BattleId("simulated-battle"),
            sessionId = sessionId,
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 99.9,
            completedAt = System.currentTimeMillis()
        )

        requests[clientRequestId] = result
        // We key by userId and battleId for getBattleResult. We'll use the simulated IDs.
        results[Pair(result.userId.value, result.battleId.value)] = result

        return DomainResult.Success(result)
    }

    override suspend fun getBattleResult(
        userId: UserId,
        battleId: BattleId
    ): DomainResult<BattleResult> {
        val result = results[Pair(userId.value, battleId.value)]
        return if (result != null) {
            DomainResult.Success(result)
        } else {
            DomainResult.Failure(AppError.Domain)
        }
    }
}

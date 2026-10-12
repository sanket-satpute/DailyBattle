package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Mapper for [BattleHistoryItemEntity], connecting historical records
 * with domain [BattleResult].
 */
fun BattleResult.toHistoryItemEntity(personalBest: Boolean = false): BattleHistoryItemEntity =
    BattleHistoryItemEntity(
        battleId = battleId.value,
        userId = userId.value,
        date = completedAt,
        score = totalScore,
        percentile = percentile,
        personalBest = personalBest,
        status = "COMPLETED",
    )

fun BattleHistoryItemEntity.toBattleResult(
    sessionId: String = "",
    snapScore: Int = 0,
    shiftScore: Int = 0,
    crowdCallScore: Int = 0,
    consistencyScore: Int = 0,
): BattleResult = BattleResult(
    resultId = BattleResultId("res_$battleId"),
    userId = UserId(userId),
    battleId = BattleId(battleId),
    sessionId = BattleSessionId(sessionId),
    snapScore = snapScore,
    shiftScore = shiftScore,
    crowdCallScore = crowdCallScore,
    consistencyScore = consistencyScore,
    totalScore = score,
    percentile = percentile,
    completedAt = date,
)

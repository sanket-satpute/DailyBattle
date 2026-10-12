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
): BattleResult {
    val sanitizedPercentile = when {
        percentile.isNaN() || percentile.isInfinite() -> 0.0
        else -> percentile.coerceIn(0.0, 100.0)
    }
    return BattleResult(
        resultId = BattleResultId("res_$battleId"),
        userId = UserId(userId),
        battleId = BattleId(battleId),
        sessionId = BattleSessionId(sessionId),
        snapScore = maxOf(0, snapScore),
        shiftScore = maxOf(0, shiftScore),
        crowdCallScore = maxOf(0, crowdCallScore),
        consistencyScore = maxOf(0, consistencyScore),
        totalScore = maxOf(0, score),
        percentile = sanitizedPercentile,
        completedAt = maxOf(0L, date),
    )
}

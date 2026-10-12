package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Bidirectional mapper between [BattleResultEntity] and [BattleResult].
 */
fun BattleResultEntity.toDomain(): BattleResult = BattleResult(
    resultId = BattleResultId(resultId),
    userId = UserId(userId),
    battleId = BattleId(battleId),
    sessionId = BattleSessionId(sessionId),
    snapScore = snapScore,
    shiftScore = shiftScore,
    crowdCallScore = crowdCallScore,
    consistencyScore = consistencyScore,
    totalScore = totalScore,
    percentile = percentile,
    completedAt = completedAt,
)

fun BattleResult.toEntity(): BattleResultEntity = BattleResultEntity(
    resultId = resultId.value,
    userId = userId.value,
    battleId = battleId.value,
    sessionId = sessionId.value,
    snapScore = snapScore,
    shiftScore = shiftScore,
    crowdCallScore = crowdCallScore,
    consistencyScore = consistencyScore,
    totalScore = totalScore,
    percentile = percentile,
    completedAt = completedAt,
)

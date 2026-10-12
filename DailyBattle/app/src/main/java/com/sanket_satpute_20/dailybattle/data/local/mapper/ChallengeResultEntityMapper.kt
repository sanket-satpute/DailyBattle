package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId

/**
 * Bidirectional mapper between [ChallengeResultEntity] and [ChallengeResult].
 */
fun ChallengeResultEntity.toDomain(): ChallengeResult = ChallengeResult(
    resultId = ChallengeResultId(resultId),
    challengeId = ChallengeId(challengeId),
    sessionId = BattleSessionId(sessionId),
    score = score,
    maxScore = maxScore,
    completedAt = completedAt,
    metadata = null,
)

fun ChallengeResult.toEntity(): ChallengeResultEntity = ChallengeResultEntity(
    resultId = resultId.value,
    challengeId = challengeId.value,
    sessionId = sessionId.value,
    score = score,
    maxScore = maxScore,
    completedAt = completedAt,
    metadataJson = null,
)

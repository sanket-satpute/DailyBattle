package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
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
    score = maxOf(0, score),
    maxScore = maxOf(0, maxScore),
    completedAt = maxOf(0L, completedAt),
    metadata = metadataJson?.let { json ->
        try {
            DailyBattleJson.decodeFromString<Map<String, String>>(json)
        } catch (_: Exception) {
            null
        }
    },
)

fun ChallengeResult.toEntity(): ChallengeResultEntity = ChallengeResultEntity(
    resultId = resultId.value,
    challengeId = challengeId.value,
    sessionId = sessionId.value,
    score = score,
    maxScore = maxScore,
    completedAt = completedAt,
    metadataJson = metadata?.let { map ->
        try {
            DailyBattleJson.encodeToString(map)
        } catch (_: Exception) {
            null
        }
    },
)

/**
 * Maps [ChallengeResult] into a [ChallengeResultEntity] with a deterministic primary key:
 * `resultId = "${sessionId}_${challengeId}"`.
 */
fun ChallengeResult.toDeterministicEntity(
    deterministicResultId: String = "${sessionId.value}_${challengeId.value}"
): ChallengeResultEntity = toEntity().copy(resultId = deterministicResultId)

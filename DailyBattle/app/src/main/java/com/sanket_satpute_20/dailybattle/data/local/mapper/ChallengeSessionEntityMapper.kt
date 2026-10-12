package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId

/**
 * Bidirectional mapper between [ChallengeSessionEntity] and [ChallengeSession].
 */
fun ChallengeSessionEntity.toDomain(): ChallengeSession {
    val sessionStatus = runCatching { ChallengeSessionStatus.valueOf(status) }
        .getOrDefault(ChallengeSessionStatus.READY)
    return ChallengeSession(
        challengeId = ChallengeId(challengeId),
        sessionId = BattleSessionId(sessionId),
        status = sessionStatus,
        startedAt = startedAt,
        completedAt = completedAt,
    )
}

fun ChallengeSession.toEntity(): ChallengeSessionEntity = ChallengeSessionEntity(
    challengeId = challengeId.value,
    sessionId = sessionId.value,
    status = status.name,
    startedAt = startedAt,
    completedAt = completedAt,
)

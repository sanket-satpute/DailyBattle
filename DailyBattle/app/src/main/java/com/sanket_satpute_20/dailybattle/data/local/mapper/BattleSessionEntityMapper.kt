package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Bidirectional mapper between [BattleSessionEntity] and [BattleSession].
 */
fun BattleSessionEntity.toDomain(
    challengeSessions: List<ChallengeSession> = emptyList()
): BattleSession {
    val battleMode = runCatching { BattleMode.valueOf(mode) }
        .getOrDefault(BattleMode.OFFICIAL)
    val sessionStatus = runCatching { BattleSessionStatus.valueOf(status) }
        .getOrDefault(BattleSessionStatus.NOT_STARTED)
    val sanitizedCurrentChallenge = currentChallenge?.takeIf { it in 1..3 }
    val sanitizedStartedAt = startedAt?.takeIf { it >= 0L }
    val sanitizedCompletedAt = completedAt?.takeIf { it >= 0L }
    return BattleSession(
        sessionId = BattleSessionId(sessionId),
        userId = UserId(userId),
        battleId = BattleId(battleId),
        mode = battleMode,
        status = sessionStatus,
        currentChallenge = sanitizedCurrentChallenge,
        startedAt = sanitizedStartedAt,
        completedAt = sanitizedCompletedAt,
        challengeSessions = challengeSessions,
    )
}

fun BattleSession.toEntity(): BattleSessionEntity = BattleSessionEntity(
    sessionId = sessionId.value,
    userId = userId.value,
    battleId = battleId.value,
    mode = mode.name,
    status = status.name,
    currentChallenge = currentChallenge,
    startedAt = startedAt,
    completedAt = completedAt,
)

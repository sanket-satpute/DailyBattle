package com.sanket_satpute_20.dailybattle.data.remote.mapper

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleSessionResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeResultDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.TodayBattleResponseDto
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Maps remote transport DTOs to domain models.
 *
 * Adheres strictly to the contract:
 * - Does not recalculate percentiles, consistency, or total scores in the transport layer.
 * - Preserves domain boundaries without coupling remote schemas to local persistence.
 */
fun TodayBattleResponseDto.toDomain(): Battle {
    val bId = BattleId(battleId)
    return Battle(
        battleId = bId,
        battleDate = battleDate,
        status = runCatching { BattleStatus.valueOf(status) }.getOrDefault(BattleStatus.AVAILABLE),
        version = version,
        challenges = challenges.map { it.toDomain(bId) },
    )
}

fun ChallengeDto.toDomain(battleId: BattleId): Challenge = Challenge(
    challengeId = ChallengeId(challengeId),
    battleId = battleId,
    type = runCatching { ChallengeType.valueOf(type) }.getOrDefault(ChallengeType.SNAP),
    order = order,
    version = version,
)

fun BattleSessionResponseDto.toDomain(userId: UserId): BattleSession = BattleSession(
    sessionId = BattleSessionId(sessionId),
    userId = userId,
    battleId = BattleId(battleId),
    mode = runCatching { BattleMode.valueOf(mode) }.getOrDefault(BattleMode.OFFICIAL),
    status = runCatching { BattleSessionStatus.valueOf(status) }.getOrDefault(BattleSessionStatus.IN_PROGRESS),
    currentChallenge = currentChallenge,
    startedAt = startedAt,
    completedAt = null,
)

fun ChallengeResultDto.toDomain(): ChallengeResult = ChallengeResult(
    resultId = ChallengeResultId(resultId),
    challengeId = ChallengeId(challengeId),
    sessionId = BattleSessionId(sessionId),
    score = score,
    maxScore = maxScore,
    completedAt = completedAt,
)

fun BattleResultResponseDto.toDomain(): BattleResult = BattleResult(
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

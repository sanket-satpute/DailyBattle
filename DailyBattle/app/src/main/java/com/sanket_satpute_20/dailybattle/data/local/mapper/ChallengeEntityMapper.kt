package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeEntity
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId

/**
 * Bidirectional mapper between [ChallengeEntity] and [Challenge].
 */
fun ChallengeEntity.toDomain(): Challenge {
    val challengeType = runCatching { ChallengeType.valueOf(type) }
        .getOrDefault(ChallengeType.SNAP)
    return Challenge(
        challengeId = ChallengeId(challengeId),
        battleId = BattleId(battleId),
        type = challengeType,
        order = maxOf(1, order),
        version = version,
    )
}

fun Challenge.toEntity(): ChallengeEntity = ChallengeEntity(
    challengeId = challengeId.value,
    battleId = battleId.value,
    type = type.name,
    order = order,
    version = version,
)

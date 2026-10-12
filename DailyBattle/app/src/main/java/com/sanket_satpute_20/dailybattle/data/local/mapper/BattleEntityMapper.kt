package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleEntity
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId

/**
 * Bidirectional mapper between [BattleEntity] and [Battle].
 */
fun BattleEntity.toDomain(challenges: List<Challenge> = emptyList()): Battle {
    val battleStatus = runCatching { BattleStatus.valueOf(status) }
        .getOrDefault(BattleStatus.NOT_AVAILABLE)
    return Battle(
        battleId = BattleId(battleId),
        battleDate = battleDate,
        status = battleStatus,
        version = version,
        challenges = challenges,
    )
}

fun Battle.toEntity(): BattleEntity = BattleEntity(
    battleId = battleId.value,
    battleDate = battleDate,
    status = status.name,
    version = version,
)

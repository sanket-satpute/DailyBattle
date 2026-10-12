package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus

/**
 * Bidirectional mappers for Rival and Rival Match History persistence models.
 */
fun RivalEntity.toDomain(): Rival {
    val rivalStatus = runCatching { RivalStatus.valueOf(status) }
        .getOrDefault(RivalStatus.ACTIVE)
    return Rival(
        userId = UserId(userId),
        rivalUserId = UserId(rivalUserId),
        rivalName = rivalName,
        selectedAt = selectedAt,
        status = rivalStatus,
    )
}

fun Rival.toEntity(): RivalEntity = RivalEntity(
    userId = userId.value,
    rivalUserId = rivalUserId.value,
    rivalName = rivalName,
    selectedAt = selectedAt,
    status = status.name,
)

fun RivalMatchHistoryEntity.toDomain(): RivalMatchHistoryItem {
    val matchOutcome = runCatching { MatchOutcome.valueOf(outcome) }
        .getOrDefault(MatchOutcome.TIE)
    return RivalMatchHistoryItem(
        battleId = BattleId(battleId),
        date = date,
        userScore = userScore,
        rivalScore = rivalScore,
        outcome = matchOutcome,
    )
}

fun RivalMatchHistoryItem.toEntity(userId: String): RivalMatchHistoryEntity = RivalMatchHistoryEntity(
    battleId = battleId.value,
    userId = userId,
    date = date,
    userScore = userScore,
    rivalScore = rivalScore,
    outcome = outcome.name,
)

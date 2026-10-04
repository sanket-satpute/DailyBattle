package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId

data class RivalMatchHistoryItem(
    val battleId: BattleId,
    val date: Long,
    val userScore: Int,
    val rivalScore: Int,
    val outcome: MatchOutcome
)

enum class MatchOutcome {
    WIN,
    LOSS,
    TIE
}

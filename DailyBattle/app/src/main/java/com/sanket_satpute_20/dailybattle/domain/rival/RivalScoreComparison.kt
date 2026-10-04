package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId

enum class ScoreDirection {
    AHEAD,
    BEHIND,
    TIED
}

data class RivalScoreComparison(
    val userScore: Int,
    val rivalScore: Int,
    val scoreGap: Int,
    val battleId: BattleId,
    val direction: ScoreDirection
)

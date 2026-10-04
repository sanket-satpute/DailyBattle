package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId

/**
 * Indicates who is leading in the rival comparison.
 */
enum class RivalComparisonDirection {
    USER_AHEAD,
    RIVAL_AHEAD,
    TIED
}

/**
 * Represents a direct one-person competitive goal result, per `08_DATA_AND_API.md` §37.
 */
data class RivalScoreComparison(
    val userScore: Int,
    val rivalScore: Int,
    val scoreGap: Int,
    val battleId: BattleId,
    val direction: RivalComparisonDirection,
    val rivalName: String // Required for UI like "BEAT RAHUL" or "RAHUL 914"
)

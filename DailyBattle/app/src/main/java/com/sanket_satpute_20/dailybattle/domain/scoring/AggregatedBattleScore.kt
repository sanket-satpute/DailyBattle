package com.sanket_satpute_20.dailybattle.domain.scoring

/**
 * Result of the battle score aggregation.
 *
 * Enforces the structure from `02_GAMEPLAY.md` §12:
 * Total = Snap + Shift + Crowd Call + Consistency
 */
data class AggregatedBattleScore(
    val snapScore: Int,
    val shiftScore: Int,
    val crowdCallScore: Int,
    val consistencyScore: Int,
    val totalScore: Int
)

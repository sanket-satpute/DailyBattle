package com.sanket_satpute_20.dailybattle.domain.history

import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult

/**
 * Domain model representing historical performance data.
 * 
 * - [best] The highest totalScore across all official Battles.
 * - [average] The average totalScore across all official Battles.
 * - [momentum] The consecutive calendar days of official completed Battles, ending today.
 * - [recentBattles] A list of recent official Battles (e.g. for display).
 * - [graphData] Historical BattleResults used for charting progress.
 */
data class History(
    val best: Int,
    val average: Int,
    val momentum: Int,
    val recentBattles: List<BattleResult>,
    val graphData: List<BattleResult>
)

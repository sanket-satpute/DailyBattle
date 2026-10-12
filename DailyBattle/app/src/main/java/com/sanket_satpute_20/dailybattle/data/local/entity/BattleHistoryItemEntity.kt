package com.sanket_satpute_20.dailybattle.data.local.entity

import androidx.room.Entity

/**
 * Local persistence entity representing a cached item in the user's Battle History.
 *
 * Implements the local persistence representation for the History domain
 * per `08_DATA_AND_API.md` §40.
 *
 * Fields:
 * - [battleId]     — battle identifier
 * - [userId]       — user identifier
 * - [date]         — battle date timestamp (epoch ms)
 * - [score]        — total score achieved
 * - [percentile]   — competitive percentile rank
 * - [personalBest] — whether this battle set a new personal best score
 * - [status]       — status string (e.g. COMPLETED)
 */
@Entity(
    tableName = "battle_history_items",
    primaryKeys = ["battleId", "userId"],
)
data class BattleHistoryItemEntity(
    val battleId: String,
    val userId: String,
    val date: Long,
    val score: Int,
    val percentile: Double,
    val personalBest: Boolean,
    val status: String,
)

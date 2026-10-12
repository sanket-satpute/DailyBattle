package com.sanket_satpute_20.dailybattle.data.local.entity

import androidx.room.Entity

/**
 * Local persistence entity representing a historical match item against a rival.
 *
 * Implements the local persistence representation for rival match comparisons
 * per `08_DATA_AND_API.md` §37.
 *
 * Fields:
 * - [battleId]   — battle ID of the match
 * - [userId]     — user ID
 * - [date]       — date timestamp (epoch ms)
 * - [userScore]  — user's total score
 * - [rivalScore] — rival's total score
 * - [outcome]    — serialized name of [com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome] (WIN, LOSS, TIE)
 */
@Entity(
    tableName = "rival_match_history",
    primaryKeys = ["battleId", "userId"],
)
data class RivalMatchHistoryEntity(
    val battleId: String,
    val userId: String,
    val date: Long,
    val userScore: Int,
    val rivalScore: Int,
    val outcome: String,
)

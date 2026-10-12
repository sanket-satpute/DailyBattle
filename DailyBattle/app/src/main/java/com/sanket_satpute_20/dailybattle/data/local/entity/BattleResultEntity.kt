package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing the final official Battle result.
 *
 * Implements the local persistence representation for the Battle Result domain
 * per `08_DATA_AND_API.md` §25, §26, §27.
 *
 * Total score formula:
 * Snap (0–300) + Shift (0–300) + Crowd Call (0–300) + Consistency (0–100) = Total (0–1000)
 *
 * Fields:
 * - [resultId]         — primary key / unique result identifier
 * - [userId]           — owner user ID
 * - [battleId]         — associated Battle ID
 * - [sessionId]        — associated session ID
 * - [snapScore]        — score achieved on Snap (0–300)
 * - [shiftScore]       — score achieved on Shift (0–300)
 * - [crowdCallScore]   — score achieved on Crowd Call (0–300)
 * - [consistencyScore] — score achieved for consistency bonus (0–100)
 * - [totalScore]       — aggregate score (0–1000)
 * - [percentile]       — competitive rank percentile (0.0–100.0)
 * - [completedAt]      — completion epoch ms
 */
data class BattleResultEntity(
    val resultId: String,
    val userId: String,
    val battleId: String,
    val sessionId: String,
    val snapScore: Int,
    val shiftScore: Int,
    val crowdCallScore: Int,
    val consistencyScore: Int,
    val totalScore: Int,
    val percentile: Double,
    val completedAt: Long,
)

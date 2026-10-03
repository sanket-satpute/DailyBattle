package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Domain model for the final result of an official Battle.
 *
 * Per `08_DATA_AND_API.md` §25:
 * - [resultId]          — opaque unique result identifier
 * - [userId]            — the user who owns this result
 * - [battleId]          — the Battle this result belongs to
 * - [sessionId]         — the Battle Session that produced this result
 * - [snapScore]         — score for the Snap challenge (0–300)
 * - [shiftScore]        — score for the Shift challenge (0–300)
 * - [crowdCallScore]    — score for the Crowd Call challenge (0–300)
 * - [consistencyScore]  — score for the consistency bonus (0–100); calculation is PENDING
 * - [totalScore]        — aggregate score (0–1000); must not be recomputed by the client
 * - [percentile]        — competitive rank as a percentile (0.0–100.0); authority = backend
 * - [completedAt]       — epoch ms when the Battle was completed
 *
 * Per §26, the total score structure:
 *   Snap (300) + Shift (300) + Crowd Call (300) + Consistency (100) = 1000
 *
 * Per §27, the client must not allow arbitrary modification of [totalScore],
 * [percentile], or [consistencyScore] after official completion.
 *
 * The exact Consistency calculation is PENDING. The [consistencyScore] value
 * is received from the authoritative backend; the client must not calculate it.
 */
data class BattleResult(
    val resultId: BattleResultId,
    val userId: UserId,
    val battleId: BattleId,
    val sessionId: BattleSessionId,
    val snapScore: Int,
    val shiftScore: Int,
    val crowdCallScore: Int,
    val consistencyScore: Int,
    val totalScore: Int,
    val percentile: Double,
    val completedAt: Long,
)

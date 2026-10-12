package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for Battle results.
 *
 * Implements approved REST response for:
 * - POST /api/v1/battles/sessions/{sessionId}/complete (08_DATA_AND_API.md §50)
 * - GET /api/v1/battles/{battleId}/results/{userId} (08_DATA_AND_API.md §51)
 */
@Serializable
data class BattleResultResponseDto(
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

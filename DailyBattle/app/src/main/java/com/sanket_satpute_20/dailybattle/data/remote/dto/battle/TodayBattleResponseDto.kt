package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for today's Battle response.
 *
 * Implements approved REST contract for GET /api/v1/battles/today (08_DATA_AND_API.md §47).
 */
@Serializable
data class TodayBattleResponseDto(
    val battleId: String,
    val battleDate: Long,
    val status: String,
    val version: Int,
    val challenges: List<ChallengeDto>,
)

@Serializable
data class ChallengeDto(
    val challengeId: String,
    val type: String,
    val order: Int,
    val version: Int,
)

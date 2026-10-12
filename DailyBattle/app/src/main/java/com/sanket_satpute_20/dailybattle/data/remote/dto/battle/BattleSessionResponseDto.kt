package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for an official Battle session.
 *
 * Implements approved REST response for POST /api/v1/battles/{battleId}/start (08_DATA_AND_API.md §48).
 */
@Serializable
data class BattleSessionResponseDto(
    val sessionId: String,
    val battleId: String,
    val mode: String,
    val status: String,
    val currentChallenge: Int? = null,
    val startedAt: Long,
)

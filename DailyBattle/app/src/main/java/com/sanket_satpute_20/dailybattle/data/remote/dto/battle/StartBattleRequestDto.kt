package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for starting an official Battle session.
 *
 * Implements approved REST contract for POST /api/v1/battles/{battleId}/start (08_DATA_AND_API.md §48).
 * Includes [clientRequestId] for idempotency protection (§57).
 */
@Serializable
data class StartBattleRequestDto(
    val clientRequestId: String,
)

package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for completing an official Battle session.
 *
 * Implements approved REST request for POST /api/v1/battles/sessions/{sessionId}/complete (08_DATA_AND_API.md §50).
 * Preserves idempotency via [clientRequestId] (§57).
 */
@Serializable
data class CompleteBattleRequestDto(
    val clientRequestId: String,
)

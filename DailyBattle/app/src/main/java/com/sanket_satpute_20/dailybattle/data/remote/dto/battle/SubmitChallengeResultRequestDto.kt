package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for submitting a challenge result within a session.
 *
 * Implements approved REST request for POST /api/v1/battles/sessions/{sessionId}/challenges/{challengeId}/result (08_DATA_AND_API.md §49).
 * Preserves idempotency via [clientRequestId] (§57).
 */
@Serializable
data class SubmitChallengeResultRequestDto(
    val clientRequestId: String,
    val score: Int,
    val maxScore: Int,
    val completedAt: Long,
)

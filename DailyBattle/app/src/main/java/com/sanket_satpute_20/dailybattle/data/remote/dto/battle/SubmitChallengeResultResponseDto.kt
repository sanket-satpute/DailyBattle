package com.sanket_satpute_20.dailybattle.data.remote.dto.battle

import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for response after submitting a challenge result.
 *
 * Implements approved REST response for POST /api/v1/battles/sessions/{sessionId}/challenges/{challengeId}/result (08_DATA_AND_API.md §49).
 */
@Serializable
data class SubmitChallengeResultResponseDto(
    val challengeResult: ChallengeResultDto,
    val nextChallenge: Int? = null,
    val sessionStatus: String,
)

@Serializable
data class ChallengeResultDto(
    val resultId: String,
    val challengeId: String,
    val sessionId: String,
    val score: Int,
    val maxScore: Int,
    val completedAt: Long,
)

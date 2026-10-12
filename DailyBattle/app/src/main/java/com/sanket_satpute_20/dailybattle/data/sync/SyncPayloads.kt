package com.sanket_satpute_20.dailybattle.data.sync

import kotlinx.serialization.Serializable

/**
 * Strongly-typed payload definitions serialized within [PendingOperation.payload].
 */
@Serializable
data class StartBattlePayload(
    val battleId: String,
    val clientRequestId: String,
    val userId: String,
)

@Serializable
data class SubmitChallengeResultPayload(
    val sessionId: String,
    val challengeId: String,
    val clientRequestId: String,
    val score: Int,
    val maxScore: Int,
    val completedAt: Long,
)

@Serializable
data class CompleteBattlePayload(
    val sessionId: String,
    val clientRequestId: String,
)

@Serializable
data class AddFriendPayload(
    val userId: String,
    val battleCode: String,
    val clientRequestId: String,
)

@Serializable
data class RefreshProfilePayload(
    val userId: String,
)

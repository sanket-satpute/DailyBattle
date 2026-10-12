package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing the outcome of one completed challenge within a Battle Session.
 *
 * Implements the local persistence representation for the Challenge Result domain
 * per `08_DATA_AND_API.md` §24.
 *
 * Fields:
 * - [resultId]      — primary key / unique result identifier
 * - [challengeId]   — associated challenge identifier
 * - [sessionId]     — associated battle session identifier
 * - [score]         — achieved score (0–300)
 * - [maxScore]      — maximum possible score (300)
 * - [completedAt]   — challenge completion epoch ms
 * - [metadataJson]  — optional serialized metadata payload
 */
data class ChallengeResultEntity(
    val resultId: String,
    val challengeId: String,
    val sessionId: String,
    val score: Int,
    val maxScore: Int,
    val completedAt: Long,
    val metadataJson: String? = null,
)

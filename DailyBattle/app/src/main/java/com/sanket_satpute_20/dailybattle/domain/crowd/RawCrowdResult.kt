package com.sanket_satpute_20.dailybattle.domain.crowd

/**
 * Captures the raw output from the Crowd Call challenge.
 *
 * This represents the exact input from the user (their prediction).
 * It is NOT the official score. The official scoring logic is determined by
 * the authoritative scoring engine (DEC-GAME-003 PENDING).
 */
data class RawCrowdResult(
    val questionId: String,
    val selectedChoiceId: String?,
    val timeToAnswerMs: Long
)

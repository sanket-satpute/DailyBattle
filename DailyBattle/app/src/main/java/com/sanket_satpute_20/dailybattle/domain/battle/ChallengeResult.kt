package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId

/**
 * Domain model for the outcome of one completed challenge within a Battle Session.
 *
 * Per `08_DATA_AND_API.md` §24:
 * - [resultId]     — opaque unique result identifier
 * - [challengeId]  — the challenge this result belongs to
 * - [sessionId]    — the Battle Session this result belongs to
 * - [score]        — the user's achieved score for this challenge
 * - [maxScore]     — the maximum possible score for this challenge type
 * - [completedAt]  — epoch ms when the challenge was completed
 *
 * Per §24, current maximum scores:
 * - Snap:       300
 * - Shift:      300
 * - Crowd Call: 300
 *
 * The exact scoring algorithm is PENDING. Scores are received from the
 * authoritative source; the client must not calculate them independently.
 */
data class ChallengeResult(
    val resultId: ChallengeResultId,
    val challengeId: ChallengeId,
    val sessionId: BattleSessionId,
    val score: Int,
    val maxScore: Int,
    val completedAt: Long,
    val metadata: Map<String, String>? = null
)

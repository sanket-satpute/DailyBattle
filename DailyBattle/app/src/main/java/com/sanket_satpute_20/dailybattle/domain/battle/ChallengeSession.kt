package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId

/**
 * Domain model for a user's active or completed interaction with one challenge.
 *
 * Per `08_DATA_AND_API.md` §21:
 * - [challengeId]  — the challenge this session belongs to
 * - [sessionId]    — the Battle Session this challenge session belongs to
 * - [status]       — lifecycle state: READY → ACTIVE → CORRECT/INCORRECT → COMPLETE (§22)
 * - [startedAt]    — epoch ms when the challenge was started; null if not yet started
 * - [completedAt]  — epoch ms when the challenge was completed; null if not yet complete
 *
 * `input` and `result` fields are omitted as their exact structures are either
 * challenge-specific (PENDING per §23) or already modelled separately ([ChallengeResult]).
 */
data class ChallengeSession(
    val challengeId: ChallengeId,
    val sessionId: BattleSessionId,
    val status: ChallengeSessionStatus,
    val startedAt: Long?,
    val completedAt: Long?,
)

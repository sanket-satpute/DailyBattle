package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Domain model for a user's active or completed attempt at a Battle.
 *
 * Per `08_DATA_AND_API.md` §16:
 * - [sessionId]         — opaque unique session identifier
 * - [userId]            — the user who owns this session
 * - [battleId]          — the Battle this session is an attempt of
 * - [mode]              — OFFICIAL or PRACTICE (§17); must not be inferred from navigation
 * - [status]            — lifecycle state: NOT_STARTED → IN_PROGRESS → COMPLETED (§20)
 * - [currentChallenge]  — 1-based index of the active challenge (1=Snap, 2=Shift, 3=CrowdCall);
 *                         null when the session has not started or is completed
 * - [startedAt]         — epoch ms when the session began; null if not yet started
 * - [completedAt]       — epoch ms when the session completed; null if not yet complete
 *
 * - [challengeSessions]   — list of associated ChallengeSession models
 *
 * Official session uniqueness constraint: (userId, battleId, OFFICIAL)
 * must identify exactly one official attempt (§18).
 */
data class BattleSession(
    val sessionId: BattleSessionId,
    val userId: UserId,
    val battleId: BattleId,
    val mode: BattleMode,
    val status: BattleSessionStatus,
    val currentChallenge: Int?,
    val startedAt: Long?,
    val completedAt: Long?,
    val challengeSessions: List<ChallengeSession> = emptyList(),
)

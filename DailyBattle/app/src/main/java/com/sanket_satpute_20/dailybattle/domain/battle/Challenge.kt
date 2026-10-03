package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId

/**
 * Domain model for one challenge inside a Battle.
 *
 * Per `08_DATA_AND_API.md` §10:
 * - [challengeId]  — opaque unique identifier
 * - [battleId]     — the Battle this challenge belongs to
 * - [type]         — SNAP, SHIFT, or CROWD_CALL (§11)
 * - [order]        — position in the Battle sequence (1 = Snap, 2 = Shift, 3 = Crowd Call)
 * - [version]      — configuration version
 *
 * Challenge configuration (§12–15) contains challenge-specific parameters whose exact
 * fields and scoring details are PENDING. Configuration will be modelled in a future sprint
 * once the gameplay specification finalises those details.
 *
 * The official order is enforced by [order]; the implementation must not randomly
 * reorder challenges (`08_DATA_AND_API.md` §9).
 */
data class Challenge(
    val challengeId: ChallengeId,
    val battleId: BattleId,
    val type: ChallengeType,
    val order: Int,
    val version: Int,
)

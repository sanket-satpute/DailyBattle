package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId

/**
 * Domain model for an official daily Battle definition.
 *
 * Per `08_DATA_AND_API.md` §7:
 * - [battleId]   — opaque unique identifier
 * - [battleDate] — the official date this Battle belongs to (epoch days, UTC)
 * - [status]     — current availability state (§8)
 * - [version]    — configuration version
 * - [challenges] — exactly three challenges in official order: Snap → Shift → Crowd Call (§9)
 *
 * The [challenges] list must contain exactly 3 entries.
 * Their [Challenge.order] values must be 1, 2, 3 in ascending order.
 * The official challenge sequence must not be reordered.
 *
 * The `metadata` field from §7 is omitted here as it is described as "non-game-critical";
 * it will be added when an approved feature requires it.
 */
data class Battle(
    val battleId: BattleId,
    val battleDate: Long,
    val status: BattleStatus,
    val version: Int,
    val challenges: List<Challenge>,
)

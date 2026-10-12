package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing a user's attempt at a Battle.
 *
 * Implements the local persistence representation for the Battle Session domain
 * per `08_DATA_AND_API.md` §16, §17, §18, §20.
 *
 * Fields:
 * - [sessionId]        — primary key / unique session identifier
 * - [userId]           — owner user ID
 * - [battleId]         — associated Battle ID
 * - [mode]             — serialized name of [com.sanket_satpute_20.dailybattle.domain.battle.BattleMode] (OFFICIAL, PRACTICE)
 * - [status]           — serialized name of [com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus]
 * - [currentChallenge] — active challenge index (1=Snap, 2=Shift, 3=CrowdCall, null if not active)
 * - [startedAt]        — session start epoch ms (nullable)
 * - [completedAt]      — session completion epoch ms (nullable)
 */
data class BattleSessionEntity(
    val sessionId: String,
    val userId: String,
    val battleId: String,
    val mode: String,
    val status: String,
    val currentChallenge: Int?,
    val startedAt: Long?,
    val completedAt: Long?,
)

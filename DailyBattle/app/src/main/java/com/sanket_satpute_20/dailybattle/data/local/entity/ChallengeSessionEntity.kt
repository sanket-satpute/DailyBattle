package com.sanket_satpute_20.dailybattle.data.local.entity

import androidx.room.Entity

/**
 * Local persistence entity representing a user's interaction with a single challenge inside a Battle Session.
 *
 * Implements the local persistence representation for the Challenge Session domain
 * per `08_DATA_AND_API.md` §21, §22.
 *
 * Fields:
 * - [challengeId] — unique challenge identifier
 * - [sessionId]   — parent battle session identifier
 * - [status]      — serialized name of [com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus]
 * - [startedAt]   — challenge start epoch ms (nullable)
 * - [completedAt] — challenge completion epoch ms (nullable)
 */
@Entity(
    tableName = "challenge_sessions",
    primaryKeys = ["challengeId", "sessionId"],
)
data class ChallengeSessionEntity(
    val challengeId: String,
    val sessionId: String,
    val status: String,
    val startedAt: Long?,
    val completedAt: Long?,
)

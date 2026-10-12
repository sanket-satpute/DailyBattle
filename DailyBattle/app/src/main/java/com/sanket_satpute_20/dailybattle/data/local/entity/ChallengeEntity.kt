package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing one challenge definition within a Battle.
 *
 * Implements the local persistence representation for the Challenge domain
 * per `08_DATA_AND_API.md` §9, §10, §11.
 *
 * Fields:
 * - [challengeId] — primary key / unique challenge identifier
 * - [battleId]    — identifier of the Battle this challenge belongs to
 * - [type]        — serialized name of [com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType]
 * - [order]       — official sequence position (1 = Snap, 2 = Shift, 3 = Crowd Call)
 * - [version]     — configuration version
 */
data class ChallengeEntity(
    val challengeId: String,
    val battleId: String,
    val type: String,
    val order: Int,
    val version: Int,
)

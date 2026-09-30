package com.sanket_satpute_20.dailybattle.domain.identifier

/**
 * Opaque domain identities. Their format, generation, ownership, and persistence are intentionally
 * unspecified until the corresponding data and backend decisions are approved.
 */
@JvmInline value class UserId(val value: String)
@JvmInline value class BattleId(val value: String)
@JvmInline value class BattleSessionId(val value: String)
@JvmInline value class ChallengeId(val value: String)
@JvmInline value class ChallengeResultId(val value: String)
@JvmInline value class BattleResultId(val value: String)
@JvmInline value class FriendshipId(val value: String)

package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing a Friend competition relationship.
 *
 * Implements the local persistence representation for the Friend domain
 * per `08_DATA_AND_API.md` §33, §34.
 *
 * Fields:
 * - [friendshipId] — primary key / unique relationship identifier
 * - [userId]       — owner user ID
 * - [friendUserId] — friend's user ID
 * - [friendName]   — display name of the friend
 * - [status]       — serialized name of [com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus]
 * - [todayScore]   — cached score of the friend for today's Battle (nullable)
 * - [createdAt]    — relationship creation timestamp (epoch ms)
 * - [updatedAt]    — relationship update timestamp (epoch ms)
 */
data class FriendEntity(
    val friendshipId: String,
    val userId: String,
    val friendUserId: String,
    val friendName: String,
    val status: String,
    val todayScore: Int?,
    val createdAt: Long,
    val updatedAt: Long,
)

package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Domain model representing a Friend competition relationship.
 *
 * Defined in `08_DATA_AND_API.md` section 33.
 * Includes friendName and todayScore to support UI rendering per Sprint 13.3 requirements.
 */
data class Friend(
    val friendshipId: FriendshipId,
    val userId: UserId,
    val friendUserId: UserId,
    val friendName: String,
    val status: FriendStatus,
    val todayScore: Int?,
    val createdAt: Long,
    val updatedAt: Long
)

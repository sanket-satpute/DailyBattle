package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Bidirectional mapper between [FriendEntity] and [Friend].
 */
fun FriendEntity.toDomain(): Friend {
    val friendStatus = runCatching { FriendStatus.valueOf(status) }
        .getOrDefault(FriendStatus.PENDING)
    return Friend(
        friendshipId = FriendshipId(friendshipId),
        userId = UserId(userId),
        friendUserId = UserId(friendUserId),
        friendName = friendName,
        status = friendStatus,
        todayScore = todayScore,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Friend.toEntity(): FriendEntity = FriendEntity(
    friendshipId = friendshipId.value,
    userId = userId.value,
    friendUserId = friendUserId.value,
    friendName = friendName,
    status = status.name,
    todayScore = todayScore,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

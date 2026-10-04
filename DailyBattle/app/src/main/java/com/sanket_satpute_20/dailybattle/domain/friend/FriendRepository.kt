package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Contract for managing the Friend relationship and state.
 *
 * Exposes logical operations from `08_DATA_AND_API.md` section 52 and 53.
 */
interface FriendRepository {
    /**
     * Get all friends for the user.
     */
    suspend fun getFriends(userId: UserId): DomainResult<List<Friend>>

    /**
     * Get a specific friend.
     */
    suspend fun getFriend(userId: UserId, friendUserId: UserId): DomainResult<Friend?>

    /**
     * Send a friend request (pending status).
     */
    suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId): DomainResult<Friend>

    /**
     * Add a friend by battle code.
     */
    suspend fun addFriendByBattleCode(userId: UserId, battleCode: String): DomainResult<Friend>

    /**
     * Accept a pending friend request.
     */
    suspend fun acceptFriendRequest(friendshipId: FriendshipId): DomainResult<Friend>

    /**
     * Reject a pending friend request.
     */
    suspend fun rejectFriendRequest(friendshipId: FriendshipId): DomainResult<Unit>

    /**
     * Block a friend or user.
     */
    suspend fun blockFriend(friendshipId: FriendshipId): DomainResult<Friend>
}

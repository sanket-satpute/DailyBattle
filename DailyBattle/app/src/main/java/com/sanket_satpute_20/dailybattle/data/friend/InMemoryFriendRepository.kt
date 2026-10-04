package com.sanket_satpute_20.dailybattle.data.friend

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryFriendRepository @Inject constructor() : FriendRepository {

    private val friends = mutableMapOf<FriendshipId, Friend>()

    init {
        // Pre-populate some friends for development (DATA-PENDING-014)
        val f1 = Friend(
            friendshipId = FriendshipId("f-1"),
            userId = UserId("current-user"),
            friendUserId = UserId("u-2"),
            friendName = "Rahul",
            status = FriendStatus.ACCEPTED,
            todayScore = 914,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val f2 = Friend(
            friendshipId = FriendshipId("f-2"),
            userId = UserId("current-user"),
            friendUserId = UserId("u-3"),
            friendName = "Priya",
            status = FriendStatus.PENDING,
            todayScore = null,
            createdAt = 2000L,
            updatedAt = 2000L
        )
        friends[f1.friendshipId] = f1
        friends[f2.friendshipId] = f2
    }

    override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> {
        val userFriends = friends.values.filter { it.userId == userId || it.friendUserId == userId }.toList()
        return DomainResult.Success(userFriends)
    }

    override suspend fun getFriend(userId: UserId, friendUserId: UserId): DomainResult<Friend?> {
        val friend = friends.values.find {
            (it.userId == userId && it.friendUserId == friendUserId) ||
            (it.userId == friendUserId && it.friendUserId == userId)
        }
        return DomainResult.Success(friend)
    }

    override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId): DomainResult<Friend> {
        val existing = friends.values.find {
            (it.userId == userId && it.friendUserId == friendUserId) ||
            (it.userId == friendUserId && it.friendUserId == userId)
        }
        if (existing != null) {
            return DomainResult.Failure(AppError.Domain) // FRIEND_ALREADY_EXISTS mapping conceptually
        }

        val newFriend = Friend(
            friendshipId = FriendshipId(UUID.randomUUID().toString()),
            userId = userId,
            friendUserId = friendUserId,
            friendName = "User ${friendUserId.value}", // Mock name
            status = FriendStatus.PENDING,
            todayScore = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        friends[newFriend.friendshipId] = newFriend
        return DomainResult.Success(newFriend)
    }

    override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String): DomainResult<Friend> {
        if (battleCode.isBlank()) {
            return DomainResult.Failure(AppError.Domain) // FRIEND_CODE_INVALID
        }
        
        // Mocking: Assume battleCode is "CODE-123" maps to user "u-code"
        val friendUserId = UserId("u-code")
        
        return sendFriendRequest(userId, friendUserId)
    }

    override suspend fun acceptFriendRequest(friendshipId: FriendshipId): DomainResult<Friend> {
        val existing = friends[friendshipId] ?: return DomainResult.Failure(AppError.Domain)
        
        if (existing.status != FriendStatus.PENDING) {
            return DomainResult.Failure(AppError.Domain)
        }

        val updated = existing.copy(
            status = FriendStatus.ACCEPTED,
            updatedAt = System.currentTimeMillis()
        )
        friends[friendshipId] = updated
        return DomainResult.Success(updated)
    }

    override suspend fun rejectFriendRequest(friendshipId: FriendshipId): DomainResult<Unit> {
        val existing = friends[friendshipId] ?: return DomainResult.Failure(AppError.Domain)
        
        if (existing.status != FriendStatus.PENDING) {
            return DomainResult.Failure(AppError.Domain)
        }

        // Conceptual rejection can remove or mark rejected
        friends.remove(friendshipId)
        return DomainResult.Success(Unit)
    }

    override suspend fun blockFriend(friendshipId: FriendshipId): DomainResult<Friend> {
        val existing = friends[friendshipId] ?: return DomainResult.Failure(AppError.Domain)
        
        val updated = existing.copy(
            status = FriendStatus.BLOCKED,
            updatedAt = System.currentTimeMillis()
        )
        friends[friendshipId] = updated
        return DomainResult.Success(updated)
    }
}

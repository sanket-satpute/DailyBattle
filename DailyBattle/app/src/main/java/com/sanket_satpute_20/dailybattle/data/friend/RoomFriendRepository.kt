package com.sanket_satpute_20.dailybattle.data.friend

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local Room-backed implementation of [FriendRepository].
 *
 * Implements Sprint 15.3 repository integration connecting the friend
 * repository domain contract to the local Room storage layer.
 */
@Singleton
class RoomFriendRepository @Inject constructor(
    private val friendDao: FriendDao,
) : FriendRepository {

    override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> {
        return try {
            val entities = friendDao.getFriendsForUser(userId.value)
            DomainResult.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    override suspend fun getFriend(userId: UserId, friendUserId: UserId): DomainResult<Friend?> {
        return try {
            val entity = friendDao.getFriend(userId.value, friendUserId.value)
            DomainResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId): DomainResult<Friend> {
        return try {
            val existing = friendDao.getFriend(userId.value, friendUserId.value)
            if (existing != null) {
                return DomainResult.Failure(AppError.Domain)
            }

            val newFriend = Friend(
                friendshipId = FriendshipId(UUID.randomUUID().toString()),
                userId = userId,
                friendUserId = friendUserId,
                friendName = "User ${friendUserId.value}",
                status = FriendStatus.PENDING,
                todayScore = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            friendDao.insertFriend(newFriend.toEntity())
            DomainResult.Success(newFriend)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String): DomainResult<Friend> {
        if (battleCode.isBlank()) {
            return DomainResult.Failure(AppError.Domain)
        }
        val friendUserId = UserId("u-$battleCode")
        return sendFriendRequest(userId, friendUserId)
    }

    override suspend fun acceptFriendRequest(friendshipId: FriendshipId): DomainResult<Friend> {
        return try {
            val existing = friendDao.getFriendById(friendshipId.value)
                ?: return DomainResult.Failure(AppError.Domain)

            if (existing.status != FriendStatus.PENDING.name) {
                return DomainResult.Failure(AppError.Domain)
            }

            val updated = existing.copy(
                status = FriendStatus.ACCEPTED.name,
                updatedAt = System.currentTimeMillis(),
            )
            friendDao.insertFriend(updated)
            DomainResult.Success(updated.toDomain())
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    override suspend fun rejectFriendRequest(friendshipId: FriendshipId): DomainResult<Unit> {
        return try {
            val existing = friendDao.getFriendById(friendshipId.value)
                ?: return DomainResult.Failure(AppError.Domain)

            if (existing.status != FriendStatus.PENDING.name) {
                return DomainResult.Failure(AppError.Domain)
            }

            friendDao.deleteFriendById(friendshipId.value)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    override suspend fun blockFriend(friendshipId: FriendshipId): DomainResult<Friend> {
        return try {
            val existing = friendDao.getFriendById(friendshipId.value)
                ?: return DomainResult.Failure(AppError.Domain)

            val updated = existing.copy(
                status = FriendStatus.BLOCKED.name,
                updatedAt = System.currentTimeMillis(),
            )
            friendDao.insertFriend(updated)
            DomainResult.Success(updated.toDomain())
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    override suspend fun getBattleCode(userId: UserId): DomainResult<String> {
        return DomainResult.Success("K4X8M9")
    }

    suspend fun saveFriends(friends: List<Friend>) {
        runCatching {
            friendDao.insertFriends(friends.map { it.toEntity() })
        }
    }
}

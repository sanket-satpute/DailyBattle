package com.sanket_satpute_20.dailybattle.data.friend

import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomFriendRepositoryTest {

    private lateinit var fakeFriendDao: FakeFriendDao
    private lateinit var repository: RoomFriendRepository

    @Before
    fun setup() {
        fakeFriendDao = FakeFriendDao()
        repository = RoomFriendRepository(fakeFriendDao)
    }

    @Test
    fun getFriendsReturnsEmptyWhenNoFriends() = runTest {
        val result = repository.getFriends(UserId("u1"))
        assertTrue(result is DomainResult.Success)
        assertTrue((result as DomainResult.Success).value.isEmpty())
    }

    @Test
    fun sendFriendRequestCreatesPendingFriend() = runTest {
        val result = repository.sendFriendRequest(UserId("u1"), UserId("u2"))
        assertTrue(result is DomainResult.Success)
        val friend = (result as DomainResult.Success).value
        assertEquals("u1", friend.userId.value)
        assertEquals("u2", friend.friendUserId.value)
        assertEquals(FriendStatus.PENDING, friend.status)

        // DAO verification
        val inDao = fakeFriendDao.getFriend("u1", "u2")
        assertNotNull(inDao)
        assertEquals(FriendStatus.PENDING.name, inDao?.status)
    }

    @Test
    fun sendDuplicateFriendRequestFails() = runTest {
        repository.sendFriendRequest(UserId("u1"), UserId("u2"))
        val duplicateResult = repository.sendFriendRequest(UserId("u1"), UserId("u2"))
        assertTrue(duplicateResult is DomainResult.Failure)
    }

    @Test
    fun acceptFriendRequestUpdatesStatus() = runTest {
        val created = (repository.sendFriendRequest(UserId("u1"), UserId("u2")) as DomainResult.Success).value
        val acceptResult = repository.acceptFriendRequest(created.friendshipId)
        assertTrue(acceptResult is DomainResult.Success)
        assertEquals(FriendStatus.ACCEPTED, (acceptResult as DomainResult.Success).value.status)
    }

    @Test
    fun rejectFriendRequestDeletesFriend() = runTest {
        val created = (repository.sendFriendRequest(UserId("u1"), UserId("u2")) as DomainResult.Success).value
        val rejectResult = repository.rejectFriendRequest(created.friendshipId)
        assertTrue(rejectResult is DomainResult.Success)
        assertNull(fakeFriendDao.getFriendById(created.friendshipId.value))
    }

    @Test
    fun blockFriendUpdatesStatusToBlocked() = runTest {
        val created = (repository.sendFriendRequest(UserId("u1"), UserId("u2")) as DomainResult.Success).value
        val blockResult = repository.blockFriend(created.friendshipId)
        assertTrue(blockResult is DomainResult.Success)
        assertEquals(FriendStatus.BLOCKED, (blockResult as DomainResult.Success).value.status)
    }

    private class FakeFriendDao : FriendDao {
        val friends = mutableMapOf<String, FriendEntity>()

        override suspend fun insertFriends(friends: List<FriendEntity>) {
            friends.forEach { insertFriend(it) }
        }

        override suspend fun insertFriend(friend: FriendEntity) {
            friends[friend.friendshipId] = friend
        }

        override suspend fun getFriendsForUser(userId: String): List<FriendEntity> {
            return friends.values.filter { it.userId == userId || it.friendUserId == userId }
        }

        override fun observeFriendsForUser(userId: String): Flow<List<FriendEntity>> {
            return flowOf(friends.values.filter { it.userId == userId || it.friendUserId == userId })
        }

        override suspend fun getFriend(userId: String, friendUserId: String): FriendEntity? {
            return friends.values.find {
                (it.userId == userId && it.friendUserId == friendUserId) ||
                (it.userId == friendUserId && it.friendUserId == userId)
            }
        }

        override suspend fun getFriendById(friendshipId: String): FriendEntity? = friends[friendshipId]

        override suspend fun deleteFriendById(friendshipId: String): Int {
            return if (friends.remove(friendshipId) != null) 1 else 0
        }

        override suspend fun clearFriendsForUser(userId: String): Int {
            val matchingKeys = friends.filter { it.value.userId == userId }.keys.toList()
            matchingKeys.forEach { friends.remove(it) }
            return matchingKeys.size
        }
    }
}

package com.sanket_satpute_20.dailybattle.data.friend

import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryFriendRepositoryTest {

    private lateinit var repository: InMemoryFriendRepository

    @Before
    fun setup() {
        repository = InMemoryFriendRepository()
    }

    @Test
    fun `getFriends returns populated friends for user`() = runTest {
        val result = repository.getFriends(UserId("current-user"))
        assertTrue(result is DomainResult.Success)
        val friends = (result as DomainResult.Success).value
        assertEquals(2, friends.size)
        assertTrue(friends.any { it.friendName == "Rahul" })
        assertTrue(friends.any { it.friendName == "Priya" })
    }

    @Test
    fun `getFriend returns specific friend`() = runTest {
        val result = repository.getFriend(UserId("current-user"), UserId("u-2"))
        assertTrue(result is DomainResult.Success)
        assertEquals("Rahul", (result as DomainResult.Success).value?.friendName)
    }

    @Test
    fun `sendFriendRequest creates new pending friend`() = runTest {
        val result = repository.sendFriendRequest(UserId("current-user"), UserId("new-user"))
        assertTrue(result is DomainResult.Success)
        val newFriend = (result as DomainResult.Success).value
        assertEquals(FriendStatus.PENDING, newFriend.status)
        assertEquals(UserId("new-user"), newFriend.friendUserId)
    }

    @Test
    fun `sendFriendRequest fails if already exists`() = runTest {
        val result = repository.sendFriendRequest(UserId("current-user"), UserId("u-2"))
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `acceptFriendRequest updates status to ACCEPTED`() = runTest {
        val result = repository.acceptFriendRequest(FriendshipId("f-2"))
        assertTrue(result is DomainResult.Success)
        assertEquals(FriendStatus.ACCEPTED, (result as DomainResult.Success).value.status)
    }

    @Test
    fun `acceptFriendRequest fails if not PENDING`() = runTest {
        val result = repository.acceptFriendRequest(FriendshipId("f-1")) // Already accepted
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `rejectFriendRequest removes pending friend`() = runTest {
        val result = repository.rejectFriendRequest(FriendshipId("f-2"))
        assertTrue(result is DomainResult.Success)
        
        val getResult = repository.getFriend(UserId("current-user"), UserId("u-3"))
        assertTrue(getResult is DomainResult.Success)
        assertNull((getResult as DomainResult.Success).value)
    }

    @Test
    fun `blockFriend updates status to BLOCKED`() = runTest {
        val result = repository.blockFriend(FriendshipId("f-1"))
        assertTrue(result is DomainResult.Success)
        assertEquals(FriendStatus.BLOCKED, (result as DomainResult.Success).value.status)
    }
}

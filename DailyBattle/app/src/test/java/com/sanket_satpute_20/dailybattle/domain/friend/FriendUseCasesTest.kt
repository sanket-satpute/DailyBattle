package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendUseCasesTest {

    private class FakeFriendRepository : FriendRepository {
        var getFriendsResult: DomainResult<List<Friend>> = DomainResult.Success(emptyList())
        var getFriendResult: DomainResult<Friend?> = DomainResult.Success(null)
        var sendRequestResult: DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        var acceptRequestResult: DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        var rejectRequestResult: DomainResult<Unit> = DomainResult.Failure(AppError.Domain)
        var blockFriendResult: DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        var addFriendResult: DomainResult<Friend> = DomainResult.Failure(AppError.Domain)

        override suspend fun getFriends(userId: UserId) = getFriendsResult
        override suspend fun getFriend(userId: UserId, friendUserId: UserId) = getFriendResult
        override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId) = sendRequestResult
        override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String) = addFriendResult
        override suspend fun acceptFriendRequest(friendshipId: FriendshipId) = acceptRequestResult
        override suspend fun rejectFriendRequest(friendshipId: FriendshipId) = rejectRequestResult
        override suspend fun blockFriend(friendshipId: FriendshipId) = blockFriendResult
    }

    @Test
    fun `GetFriendsUseCase returns friends list`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = GetFriendsUseCase(repo)
        
        val friend = Friend(FriendshipId("f1"), UserId("u1"), UserId("u2"), "Friend", FriendStatus.ACCEPTED, null, 0L, 0L)
        repo.getFriendsResult = DomainResult.Success(listOf(friend))
        
        val result = useCase.execute(UserId("u1"))
        
        assertTrue(result is DomainResult.Success)
        assertEquals(1, (result as DomainResult.Success).value.size)
        assertEquals("f1", result.value[0].friendshipId.value)
    }

    @Test
    fun `GetFriendUseCase returns single friend`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = GetFriendUseCase(repo)
        
        val friend = Friend(FriendshipId("f1"), UserId("u1"), UserId("u2"), "Friend", FriendStatus.ACCEPTED, null, 0L, 0L)
        repo.getFriendResult = DomainResult.Success(friend)
        
        val result = useCase.execute(UserId("u1"), UserId("u2"))
        
        assertTrue(result is DomainResult.Success)
        assertEquals("f1", (result as DomainResult.Success).value?.friendshipId?.value)
    }

    @Test
    fun `SendFriendRequestUseCase sends request`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = SendFriendRequestUseCase(repo)
        
        val friend = Friend(FriendshipId("f1"), UserId("u1"), UserId("u2"), "Friend", FriendStatus.PENDING, null, 0L, 0L)
        repo.sendRequestResult = DomainResult.Success(friend)
        
        val result = useCase.execute(UserId("u1"), UserId("u2"))
        
        assertTrue(result is DomainResult.Success)
        assertEquals(FriendStatus.PENDING, (result as DomainResult.Success).value.status)
    }

    @Test
    fun `AcceptFriendRequestUseCase accepts request`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = AcceptFriendRequestUseCase(repo)
        
        val friend = Friend(FriendshipId("f1"), UserId("u1"), UserId("u2"), "Friend", FriendStatus.ACCEPTED, null, 0L, 0L)
        repo.acceptRequestResult = DomainResult.Success(friend)
        
        val result = useCase.execute(FriendshipId("f1"))
        
        assertTrue(result is DomainResult.Success)
        assertEquals(FriendStatus.ACCEPTED, (result as DomainResult.Success).value.status)
    }

    @Test
    fun `RejectFriendRequestUseCase rejects request`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = RejectFriendRequestUseCase(repo)
        
        repo.rejectRequestResult = DomainResult.Success(Unit)
        
        val result = useCase.execute(FriendshipId("f1"))
        
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `BlockFriendUseCase blocks friend`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = BlockFriendUseCase(repo)
        
        val friend = Friend(FriendshipId("f1"), UserId("u1"), UserId("u2"), "Friend", FriendStatus.BLOCKED, null, 0L, 0L)
        repo.blockFriendResult = DomainResult.Success(friend)
        
        val result = useCase.execute(FriendshipId("f1"))
        
        assertTrue(result is DomainResult.Success)
        assertEquals(FriendStatus.BLOCKED, (result as DomainResult.Success).value.status)
    }

    @Test
    fun `AddFriendByBattleCodeUseCase adds friend`() = runTest {
        val repo = FakeFriendRepository()
        val useCase = AddFriendByBattleCodeUseCase(repo)
        
        val friend = Friend(FriendshipId("f1"), UserId("u1"), UserId("u2"), "Friend", FriendStatus.PENDING, null, 0L, 0L)
        repo.addFriendResult = DomainResult.Success(friend)
        
        val result = useCase.execute(UserId("u1"), "CODE-123")
        
        assertTrue(result is DomainResult.Success)
        assertEquals(FriendStatus.PENDING, (result as DomainResult.Success).value.status)
    }
}

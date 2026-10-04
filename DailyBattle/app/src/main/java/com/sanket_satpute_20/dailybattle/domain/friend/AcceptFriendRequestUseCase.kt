package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class AcceptFriendRequestUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend fun execute(friendshipId: FriendshipId): DomainResult<Friend> {
        return repository.acceptFriendRequest(friendshipId)
    }
}

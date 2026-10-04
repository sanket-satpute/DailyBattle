package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class RejectFriendRequestUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend fun execute(friendshipId: FriendshipId): DomainResult<Unit> {
        return repository.rejectFriendRequest(friendshipId)
    }
}

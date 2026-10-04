package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class GetFriendUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend fun execute(userId: UserId, friendUserId: UserId): DomainResult<Friend?> {
        return repository.getFriend(userId, friendUserId)
    }
}

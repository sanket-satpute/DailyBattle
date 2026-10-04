package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend fun execute(userId: UserId): DomainResult<List<Friend>> {
        return repository.getFriends(userId)
    }
}

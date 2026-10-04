package com.sanket_satpute_20.dailybattle.domain.friend

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class AddFriendByBattleCodeUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend fun execute(userId: UserId, battleCode: String): DomainResult<Friend> {
        return repository.addFriendByBattleCode(userId, battleCode)
    }
}

package com.sanket_satpute_20.dailybattle.data.user

import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of [UserRepository].
 * 
 * Used temporarily while final persistence strategy (Room/DataStore) is PENDING.
 */
@Singleton
class InMemoryUserRepository @Inject constructor() : UserRepository {
    private var storedBattleName: String? = null

    override suspend fun saveBattleName(name: String) {
        storedBattleName = name
    }

    override suspend fun getBattleName(): String? {
        return storedBattleName
    }
}

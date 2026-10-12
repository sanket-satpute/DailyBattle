package com.sanket_satpute_20.dailybattle.data.user

import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local Room-backed implementation of [UserRepository].
 *
 * Implements Sprint 15.3 repository integration connecting the user
 * repository domain contract to the local Room storage layer.
 */
@Singleton
class RoomUserRepository @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {

    override suspend fun saveBattleName(name: String) {
        val existing = userDao.getUserById(LOCAL_USER_ID)
        val updated = if (existing != null) {
            existing.copy(
                battleName = name,
                updatedAt = System.currentTimeMillis(),
            )
        } else {
            UserEntity(
                userId = LOCAL_USER_ID,
                battleName = name,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                accountStatus = "ACTIVE",
            )
        }
        userDao.insertUser(updated)
    }

    override suspend fun getBattleName(): String? {
        return userDao.getUserById(LOCAL_USER_ID)?.battleName
    }

    companion object {
        const val LOCAL_USER_ID = "local_user"
    }
}

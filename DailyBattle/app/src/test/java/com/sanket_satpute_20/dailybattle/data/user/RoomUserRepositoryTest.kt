package com.sanket_satpute_20.dailybattle.data.user

import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomUserRepositoryTest {

    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var repository: RoomUserRepository

    @Before
    fun setup() {
        fakeUserDao = FakeUserDao()
        repository = RoomUserRepository(fakeUserDao)
    }

    @Test
    fun getBattleNameReturnsNullWhenNoUser() = runTest {
        assertNull(repository.getBattleName())
    }

    @Test
    fun saveBattleNameCreatesNewUserWhenNotExists() = runTest {
        repository.saveBattleName("WarriorOne")

        assertEquals("WarriorOne", repository.getBattleName())
        val user = fakeUserDao.getUserById(RoomUserRepository.LOCAL_USER_ID)
        assertEquals("WarriorOne", user?.battleName)
        assertEquals("ACTIVE", user?.accountStatus)
    }

    @Test
    fun saveBattleNameUpdatesExistingUser() = runTest {
        repository.saveBattleName("InitialName")
        repository.saveBattleName("UpdatedName")

        assertEquals("UpdatedName", repository.getBattleName())
    }

    private class FakeUserDao : UserDao {
        private val users = mutableMapOf<String, UserEntity>()

        override suspend fun insertUser(user: UserEntity) {
            users[user.userId] = user
        }

        override suspend fun getUserById(userId: String): UserEntity? = users[userId]

        override suspend fun deleteUserById(userId: String): Int {
            return if (users.remove(userId) != null) 1 else 0
        }

        override suspend fun clearUsers(): Int {
            val count = users.size
            users.clear()
            return count
        }
    }
}

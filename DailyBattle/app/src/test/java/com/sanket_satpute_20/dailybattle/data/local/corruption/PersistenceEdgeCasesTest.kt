package com.sanket_satpute_20.dailybattle.data.local.corruption

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.profile.RoomProfileRepository
import com.sanket_satpute_20.dailybattle.data.rival.RoomRivalRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class PersistenceEdgeCasesTest {

    @Test
    fun firstInstallEmptyDatabaseReturnsControlledDefaults() = runBlocking {
        val emptyUserDao = createDummyDao<UserDao>()
        val emptyProfileDao = createDummyDao<ProfileDao>()
        val emptyFriendDao = Proxy.newProxyInstance(
            FriendDao::class.java.classLoader,
            arrayOf(FriendDao::class.java),
        ) { _, method, _ ->
            if (method.name == "getFriendsForUser") emptyList<Any>() else null
        } as FriendDao
        val emptyRivalDao = createDummyDao<RivalDao>()

        val userRepo = RoomUserRepository(emptyUserDao)
        val profileRepo = RoomProfileRepository(emptyProfileDao)
        val friendRepo = RoomFriendRepository(emptyFriendDao)
        val rivalRepo = RoomRivalRepository(emptyRivalDao, createDummyDao())

        // Reads on empty database must return safe values without crash
        assertNull(userRepo.getBattleName())

        val profileResult = profileRepo.getProfile(UserId("u-1"))
        assertTrue("Profile on first install must return Domain failure (not found)", profileResult is DomainResult.Failure)

        val friendsResult = friendRepo.getFriends(UserId("u-1"))
        assertTrue("Friends on first install must return empty list", friendsResult is DomainResult.Success)
        assertTrue((friendsResult as DomainResult.Success).value.isEmpty())

        val rivalResult = rivalRepo.getCurrentRival(UserId("u-1"))
        assertTrue("Rival on first install must return null", rivalResult is DomainResult.Success)
        assertNull((rivalResult as DomainResult.Success).value)
    }

    @Test
    fun storageUnavailableSafelyFailsAcrossRepositories() = runBlocking {
        val failingUserDao = Proxy.newProxyInstance(
            UserDao::class.java.classLoader,
            arrayOf(UserDao::class.java),
        ) { _, _, _ -> throw IllegalStateException("Storage volume unmounted or full") } as UserDao

        val failingProfileDao = Proxy.newProxyInstance(
            ProfileDao::class.java.classLoader,
            arrayOf(ProfileDao::class.java),
        ) { _, _, _ -> throw IllegalStateException("Disk I/O failure") } as ProfileDao

        val failingFriendDao = Proxy.newProxyInstance(
            FriendDao::class.java.classLoader,
            arrayOf(FriendDao::class.java),
        ) { _, _, _ -> throw IllegalStateException("Disk I/O failure") } as FriendDao

        val userRepo = RoomUserRepository(failingUserDao)
        val profileRepo = RoomProfileRepository(failingProfileDao)
        val friendRepo = RoomFriendRepository(failingFriendDao)

        // None of these should throw unhandled exceptions
        assertNull("getBattleName must return null on storage failure", userRepo.getBattleName())
        userRepo.saveBattleName("NewName") // must not throw

        val profileResult = profileRepo.getProfile(UserId("u-1"))
        assertTrue(profileResult is DomainResult.Failure)
        assertEquals(AppError.Persistence, (profileResult as DomainResult.Failure).error)

        val friendsResult = friendRepo.getFriends(UserId("u-1"))
        assertTrue(friendsResult is DomainResult.Failure)
        assertEquals(AppError.Persistence, (friendsResult as DomainResult.Failure).error)
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> createDummyDao(): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, _, _ -> null } as T
    }
}

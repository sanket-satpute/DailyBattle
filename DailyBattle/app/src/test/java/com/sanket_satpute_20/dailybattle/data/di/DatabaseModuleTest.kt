package com.sanket_satpute_20.dailybattle.data.di

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import org.junit.Assert.assertSame
import org.junit.Test
import java.lang.reflect.Proxy

class DatabaseModuleTest {

    @Test
    fun databaseModuleProvidesAll12DaosFromDatabase() {
        val dummyUserDao = createDummyDao<UserDao>()
        val dummyBattleDao = createDummyDao<BattleDao>()
        val dummyChallengeDao = createDummyDao<ChallengeDao>()
        val dummyBattleSessionDao = createDummyDao<BattleSessionDao>()
        val dummyChallengeSessionDao = createDummyDao<ChallengeSessionDao>()
        val dummyChallengeResultDao = createDummyDao<ChallengeResultDao>()
        val dummyBattleResultDao = createDummyDao<BattleResultDao>()
        val dummyFriendDao = createDummyDao<FriendDao>()
        val dummyRivalDao = createDummyDao<RivalDao>()
        val dummyRivalMatchHistoryDao = createDummyDao<RivalMatchHistoryDao>()
        val dummyProfileDao = createDummyDao<ProfileDao>()
        val dummyBattleHistoryDao = createDummyDao<BattleHistoryDao>()

        val fakeDb = object : DailyBattleDatabase() {
            override fun userDao(): UserDao = dummyUserDao
            override fun battleDao(): BattleDao = dummyBattleDao
            override fun challengeDao(): ChallengeDao = dummyChallengeDao
            override fun battleSessionDao(): BattleSessionDao = dummyBattleSessionDao
            override fun challengeSessionDao(): ChallengeSessionDao = dummyChallengeSessionDao
            override fun challengeResultDao(): ChallengeResultDao = dummyChallengeResultDao
            override fun battleResultDao(): BattleResultDao = dummyBattleResultDao
            override fun friendDao(): FriendDao = dummyFriendDao
            override fun rivalDao(): RivalDao = dummyRivalDao
            override fun rivalMatchHistoryDao(): RivalMatchHistoryDao = dummyRivalMatchHistoryDao
            override fun profileDao(): ProfileDao = dummyProfileDao
            override fun battleHistoryDao(): BattleHistoryDao = dummyBattleHistoryDao
            override fun clearAllTables() {}
            override fun createInvalidationTracker(): InvalidationTracker = error("Not needed")
            override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper = error("Not needed")
        }

        assertSame(dummyUserDao, DatabaseModule.provideUserDao(fakeDb))
        assertSame(dummyBattleDao, DatabaseModule.provideBattleDao(fakeDb))
        assertSame(dummyChallengeDao, DatabaseModule.provideChallengeDao(fakeDb))
        assertSame(dummyBattleSessionDao, DatabaseModule.provideBattleSessionDao(fakeDb))
        assertSame(dummyChallengeSessionDao, DatabaseModule.provideChallengeSessionDao(fakeDb))
        assertSame(dummyChallengeResultDao, DatabaseModule.provideChallengeResultDao(fakeDb))
        assertSame(dummyBattleResultDao, DatabaseModule.provideBattleResultDao(fakeDb))
        assertSame(dummyFriendDao, DatabaseModule.provideFriendDao(fakeDb))
        assertSame(dummyRivalDao, DatabaseModule.provideRivalDao(fakeDb))
        assertSame(dummyRivalMatchHistoryDao, DatabaseModule.provideRivalMatchHistoryDao(fakeDb))
        assertSame(dummyProfileDao, DatabaseModule.provideProfileDao(fakeDb))
        assertSame(dummyBattleHistoryDao, DatabaseModule.provideBattleHistoryDao(fakeDb))
    }

    @Test
    fun databaseModuleProvidesDatabaseCorruptionHandler() {
        val handler = DatabaseModule.provideDatabaseCorruptionHandler()
        org.junit.Assert.assertNotNull(handler)
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> createDummyDao(): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, _, _ -> null } as T
    }
}

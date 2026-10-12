package com.sanket_satpute_20.dailybattle.data.local.database

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyBattleDatabaseTest {

    @Test
    fun databaseNameConstantMatchesSpecification() {
        assertEquals("daily_battle.db", DailyBattleDatabase.DATABASE_NAME)
    }

    @Test
    fun roomCompilerGeneratesDatabaseImplementation() {
        val implClass = Class.forName("com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase_Impl")
        assertNotNull("Room must generate DailyBattleDatabase_Impl", implClass)
        assertTrue(
            "DailyBattleDatabase_Impl must extend DailyBattleDatabase",
            DailyBattleDatabase::class.java.isAssignableFrom(implClass),
        )
    }

    @Test
    fun roomCompilerGeneratesImplementationsForAll12Daos() {
        val expectedDaoImplNames = listOf(
            "com.sanket_satpute_20.dailybattle.data.local.dao.UserDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.BattleDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeResultDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao_Impl",
            "com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao_Impl",
        )

        expectedDaoImplNames.forEach { className ->
            val clazz = Class.forName(className)
            assertNotNull("Room must generate implementation $className", clazz)
        }
    }

    @Test
    fun databaseDeclaresAllRequiredDaoGetters() {
        val methods = DailyBattleDatabase::class.java.declaredMethods.map { it.name to it.returnType }

        val expectedDaos = listOf(
            "userDao" to UserDao::class.java,
            "battleDao" to BattleDao::class.java,
            "challengeDao" to ChallengeDao::class.java,
            "battleSessionDao" to BattleSessionDao::class.java,
            "challengeSessionDao" to ChallengeSessionDao::class.java,
            "challengeResultDao" to ChallengeResultDao::class.java,
            "battleResultDao" to BattleResultDao::class.java,
            "friendDao" to FriendDao::class.java,
            "rivalDao" to RivalDao::class.java,
            "rivalMatchHistoryDao" to RivalMatchHistoryDao::class.java,
            "profileDao" to ProfileDao::class.java,
            "battleHistoryDao" to BattleHistoryDao::class.java,
        )

        expectedDaos.forEach { (daoMethodName, daoType) ->
            val match = methods.find { it.first == daoMethodName }
            assertNotNull("DailyBattleDatabase must declare $daoMethodName", match)
            assertEquals("Method $daoMethodName must return $daoType", daoType, match!!.second)
        }
    }
}

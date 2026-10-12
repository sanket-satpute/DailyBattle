package com.sanket_satpute_20.dailybattle.data.local.corruption

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
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
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class PartialWriteAndTransactionTest {

    @Test
    fun battleSessionRepositoryExecutesInTransactionAndProtectsAgainstPartialWrite() {
        var transactionStarted = false
        var transactionRolledBack = false

        val fakeDb = object : DailyBattleDatabase() {
            override suspend fun <R> withSafeTransaction(block: suspend () -> R): R {
                transactionStarted = true
                try {
                    return block()
                } catch (e: Exception) {
                    transactionRolledBack = true
                    throw e
                }
            }
            override fun userDao(): UserDao = error("Not needed")
            override fun battleDao(): BattleDao = error("Not needed")
            override fun challengeDao(): ChallengeDao = error("Not needed")
            override fun battleSessionDao(): BattleSessionDao = error("Not needed")
            override fun challengeSessionDao(): ChallengeSessionDao = error("Not needed")
            override fun challengeResultDao(): ChallengeResultDao = error("Not needed")
            override fun battleResultDao(): BattleResultDao = error("Not needed")
            override fun friendDao(): FriendDao = error("Not needed")
            override fun rivalDao(): RivalDao = error("Not needed")
            override fun rivalMatchHistoryDao(): RivalMatchHistoryDao = error("Not needed")
            override fun profileDao(): ProfileDao = error("Not needed")
            override fun battleHistoryDao(): BattleHistoryDao = error("Not needed")
            override fun clearAllTables() {}
            override fun createInvalidationTracker(): InvalidationTracker = error("Not needed")
            override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper = error("Not needed")
        }

        val fakeSessionDao = createDummyDao<BattleSessionDao>()
        // Challenge DAO that throws halfway through multi-table write
        val failingChallengeDao = Proxy.newProxyInstance(
            ChallengeSessionDao::class.java.classLoader,
            arrayOf(ChallengeSessionDao::class.java),
        ) { _, method, _ ->
            if (method.name == "insertChallengeSessions") {
                throw IllegalStateException("Disk write failure midway through session save")
            }
            null
        } as ChallengeSessionDao

        val repository = RoomBattleSessionRepository(
            battleSessionDao = fakeSessionDao,
            challengeSessionDao = failingChallengeDao,
            database = fakeDb,
        )

        val session = BattleSession(
            sessionId = BattleSessionId("sess-1"),
            userId = UserId("u-1"),
            battleId = BattleId("b-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c-1"), BattleSessionId("sess-1"), ChallengeSessionStatus.READY, null, null)
            ),
        )

        // Must not crash
        repository.saveSession(session)

        assertTrue("Multi-table write must run inside database transaction", transactionStarted)
        assertTrue("When partial write fails, transaction must roll back", transactionRolledBack)
    }

    @Test
    fun battleResultRepositoryHandlesDatabaseFailureSafely() = runBlocking {
        var transactionRolledBack = false

        val fakeDb = object : DailyBattleDatabase() {
            override suspend fun <R> withSafeTransaction(block: suspend () -> R): R {
                try {
                    return block()
                } catch (e: Exception) {
                    transactionRolledBack = true
                    throw e
                }
            }
            override fun userDao(): UserDao = error("Not needed")
            override fun battleDao(): BattleDao = error("Not needed")
            override fun challengeDao(): ChallengeDao = error("Not needed")
            override fun battleSessionDao(): BattleSessionDao = error("Not needed")
            override fun challengeSessionDao(): ChallengeSessionDao = error("Not needed")
            override fun challengeResultDao(): ChallengeResultDao = error("Not needed")
            override fun battleResultDao(): BattleResultDao = error("Not needed")
            override fun friendDao(): FriendDao = error("Not needed")
            override fun rivalDao(): RivalDao = error("Not needed")
            override fun rivalMatchHistoryDao(): RivalMatchHistoryDao = error("Not needed")
            override fun profileDao(): ProfileDao = error("Not needed")
            override fun battleHistoryDao(): BattleHistoryDao = error("Not needed")
            override fun clearAllTables() {}
            override fun createInvalidationTracker(): InvalidationTracker = error("Not needed")
            override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper = error("Not needed")
        }

        val fakeResultDao = createDummyDao<BattleResultDao>()
        val failingHistoryDao = Proxy.newProxyInstance(
            BattleHistoryDao::class.java.classLoader,
            arrayOf(BattleHistoryDao::class.java),
        ) { _, method, _ ->
            if (method.name == "insertHistoryItems") {
                throw IllegalStateException("Database lock or constraint error")
            }
            null
        } as BattleHistoryDao
        val fakeSessionDao = createDummyDao<BattleSessionDao>()

        val repository = RoomBattleResultRepository(
            battleResultDao = fakeResultDao,
            battleHistoryDao = failingHistoryDao,
            battleSessionDao = fakeSessionDao,
            database = fakeDb,
        )

        val result = repository.completeBattle(
            sessionId = BattleSessionId("sess-fail"),
            clientRequestId = "req-1",
        )

        assertTrue("Failure must be returned when transaction fails", result is DomainResult.Failure)
        assertEquals(AppError.Persistence, (result as DomainResult.Failure).error)
        assertTrue("Transaction must roll back on failure", transactionRolledBack)
    }

    @Test
    fun battleResultRepositoryQueryFailureMapsToPersistenceError() = runBlocking {
        val failingResultDao = Proxy.newProxyInstance(
            BattleResultDao::class.java.classLoader,
            arrayOf(BattleResultDao::class.java),
        ) { _, method, _ ->
            if (method.name == "getResultForBattle") {
                throw IllegalStateException("Database corrupted or unavailable")
            }
            null
        } as BattleResultDao

        val repository = RoomBattleResultRepository(
            battleResultDao = failingResultDao,
            battleHistoryDao = createDummyDao(),
            battleSessionDao = createDummyDao(),
        )

        val result = repository.getBattleResult(UserId("u-1"), BattleId("b-1"))
        assertTrue("Query failure must be mapped to DomainResult.Failure", result is DomainResult.Failure)
        assertEquals(AppError.Persistence, (result as DomainResult.Failure).error)
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> createDummyDao(): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, _, _ -> null } as T
    }
}

package com.sanket_satpute_20.dailybattle.data.battle

import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomBattleResultRepositoryTest {

    private lateinit var fakeResultDao: FakeBattleResultDao
    private lateinit var fakeHistoryDao: FakeBattleHistoryDao
    private lateinit var fakeSessionDao: FakeBattleSessionDao
    private lateinit var repository: RoomBattleResultRepository

    @Before
    fun setup() {
        fakeResultDao = FakeBattleResultDao()
        fakeHistoryDao = FakeBattleHistoryDao()
        fakeSessionDao = FakeBattleSessionDao()
        repository = RoomBattleResultRepository(fakeResultDao, fakeHistoryDao, fakeSessionDao)
    }

    @Test
    fun completeBattlePersistsResultAndHistoryItem() = runTest {
        val sessionId = BattleSessionId("session_1")
        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_1",
                userId = "u1",
                battleId = "b1",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        val result = repository.completeBattle(sessionId, "request_1")
        assertTrue(result is DomainResult.Success)
        val battleResult = (result as DomainResult.Success).value
        assertEquals("session_1", battleResult.sessionId.value)
        assertEquals("u1", battleResult.userId.value)
        assertEquals("b1", battleResult.battleId.value)
        assertEquals(1000, battleResult.totalScore)

        // Check DAO writes
        assertEquals(1, fakeResultDao.results.size)
        assertEquals(1, fakeHistoryDao.historyItems.size)
    }

    @Test
    fun completeBattleIsIdempotentForSameClientRequestId() = runTest {
        val sessionId = BattleSessionId("session_1")
        val firstResult = repository.completeBattle(sessionId, "req_same")
        val secondResult = repository.completeBattle(sessionId, "req_same")

        assertTrue(firstResult is DomainResult.Success)
        assertTrue(secondResult is DomainResult.Success)
        assertEquals(
            (firstResult as DomainResult.Success).value.resultId,
            (secondResult as DomainResult.Success).value.resultId,
        )
        assertEquals(1, fakeResultDao.results.size)
    }

    @Test
    fun getBattleResultRetrievesFromDatabase() = runTest {
        val sessionId = BattleSessionId("session_1")
        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_1",
                userId = "user_abc",
                battleId = "battle_xyz",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )
        repository.completeBattle(sessionId, "req_get")

        val retrieved = repository.getBattleResult(UserId("user_abc"), BattleId("battle_xyz"))
        assertTrue(retrieved is DomainResult.Success)
        assertEquals("user_abc", (retrieved as DomainResult.Success).value.userId.value)
    }

    @Test
    fun getUserBattleResultsReturnsAllResultsForUser() = runTest {
        fakeResultDao.insertResult(
            BattleResultEntity("r1", "u1", "b1", "s1", 300, 300, 300, 100, 1000, 99.0, 1000L)
        )
        fakeResultDao.insertResult(
            BattleResultEntity("r2", "u1", "b2", "s2", 250, 250, 250, 90, 840, 85.0, 2000L)
        )
        fakeResultDao.insertResult(
            BattleResultEntity("r3", "u2", "b1", "s3", 300, 300, 300, 100, 1000, 99.0, 3000L)
        )

        val results = repository.getUserBattleResults(UserId("u1"))
        assertTrue(results is DomainResult.Success)
        assertEquals(2, (results as DomainResult.Success).value.size)
    }

    private class FakeBattleResultDao : BattleResultDao {
        val results = mutableMapOf<String, BattleResultEntity>()

        override suspend fun insertResult(result: BattleResultEntity) {
            results[result.resultId] = result
        }

        override suspend fun getResultById(resultId: String): BattleResultEntity? = results[resultId]

        override suspend fun getResultBySessionId(sessionId: String): BattleResultEntity? {
            return results.values.find { it.sessionId == sessionId }
        }

        override suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity? {
            return results.values.find { it.userId == userId && it.battleId == battleId }
        }

        override suspend fun getAllResultsForUser(userId: String): List<BattleResultEntity> {
            return results.values.filter { it.userId == userId }
        }

        override fun observeAllResultsForUser(userId: String): Flow<List<BattleResultEntity>> {
            return flowOf(results.values.filter { it.userId == userId })
        }

        override suspend fun deleteResultById(resultId: String): Int {
            return if (results.remove(resultId) != null) 1 else 0
        }
    }

    private class FakeBattleHistoryDao : BattleHistoryDao {
        val historyItems = mutableListOf<BattleHistoryItemEntity>()

        override suspend fun insertHistoryItems(items: List<BattleHistoryItemEntity>) {
            historyItems.addAll(items)
        }

        override suspend fun getHistoryForUser(userId: String): List<BattleHistoryItemEntity> {
            return historyItems.filter { it.userId == userId }
        }

        override suspend fun clearHistoryForUser(userId: String): Int {
            val count = historyItems.count { it.userId == userId }
            historyItems.removeAll { it.userId == userId }
            return count
        }
    }

    private class FakeBattleSessionDao : BattleSessionDao {
        val sessions = mutableMapOf<String, BattleSessionEntity>()

        override suspend fun insertSession(session: BattleSessionEntity) {
            sessions[session.sessionId] = session
        }

        override suspend fun getSessionById(sessionId: String): BattleSessionEntity? = sessions[sessionId]

        override suspend fun getSession(userId: String, battleId: String, mode: String): BattleSessionEntity? {
            return sessions.values.find { it.userId == userId && it.battleId == battleId && it.mode == mode }
        }

        override suspend fun getActiveSession(userId: String, mode: String): BattleSessionEntity? {
            return sessions.values.find { it.userId == userId && it.mode == mode && it.status == "IN_PROGRESS" }
        }

        override suspend fun getSessionsByModeAndStatus(mode: String, status: String): List<BattleSessionEntity> {
            return sessions.values.filter { it.mode == mode && it.status == status }
        }

        override suspend fun deleteSessionById(sessionId: String): Int {
            return if (sessions.remove(sessionId) != null) 1 else 0
        }
    }
}

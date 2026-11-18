package com.sanket_satpute_20.dailybattle.data.battle

import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity
import com.sanket_satpute_20.dailybattle.data.local.fake.FakeChallengeResultDao
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BattleScoreAggregationTest {

    private lateinit var fakeResultDao: FakeBattleResultDao
    private lateinit var fakeHistoryDao: FakeBattleHistoryDao
    private lateinit var fakeSessionDao: FakeBattleSessionDao
    private lateinit var fakeChallengeDao: FakeChallengeResultDao
    private lateinit var repository: RoomBattleResultRepository

    @Before
    fun setUp() {
        fakeResultDao = FakeBattleResultDao()
        fakeHistoryDao = FakeBattleHistoryDao()
        fakeSessionDao = FakeBattleSessionDao()
        fakeChallengeDao = FakeChallengeResultDao()

        repository = RoomBattleResultRepository(
            battleResultDao = fakeResultDao,
            battleHistoryDao = fakeHistoryDao,
            battleSessionDao = fakeSessionDao,
            challengeResultDao = fakeChallengeDao,
        )
    }

    @Test
    fun `completeBattle aggregates actual persisted challenge scores and calculates consistency`() = runTest {
        val sessionId = BattleSessionId("session_full")
        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_full",
                userId = "user_1",
                battleId = "battle_1",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        // Insert Snap (270), Shift (270), Crowd Call (270) -> Perfect balance, Consistency = 100
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_full_snap",
                challengeId = "snap",
                sessionId = "session_full",
                score = 270,
                maxScore = 300,
                completedAt = 1100L,
            )
        )
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_full_shift",
                challengeId = "shift",
                sessionId = "session_full",
                score = 270,
                maxScore = 300,
                completedAt = 1200L,
            )
        )
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_full_crowd_call",
                challengeId = "crowd_call",
                sessionId = "session_full",
                score = 270,
                maxScore = 300,
                completedAt = 1300L,
            )
        )

        val result = repository.completeBattle(sessionId, "req_1")
        assertTrue(result is DomainResult.Success)

        val battleResult = (result as DomainResult.Success).value
        assertEquals(270, battleResult.snapScore)
        assertEquals(270, battleResult.shiftScore)
        assertEquals(270, battleResult.crowdCallScore)
        assertEquals(100, battleResult.consistencyScore)
        assertEquals(270 + 270 + 270 + 100, battleResult.totalScore)
        assertEquals(910, battleResult.totalScore)

        // Verify written to database
        val savedEntity = fakeResultDao.getResultBySessionId("session_full")
        assertEquals(910, savedEntity!!.totalScore)
        assertEquals(1, fakeHistoryDao.historyItems.size)
        assertEquals(910, fakeHistoryDao.historyItems.first().score)
    }

    @Test
    fun `missing challenge results are treated as 0 and not fabricated`() = runTest {
        val sessionId = BattleSessionId("session_partial")
        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_partial",
                userId = "user_2",
                battleId = "battle_2",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 1,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        // Only Snap is completed; Shift and Crowd Call are missing
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_partial_snap",
                challengeId = "snap",
                sessionId = "session_partial",
                score = 240,
                maxScore = 300,
                completedAt = 1100L,
            )
        )

        val result = repository.completeBattle(sessionId, "req_partial")
        assertTrue(result is DomainResult.Success)

        val battleResult = (result as DomainResult.Success).value
        assertEquals(240, battleResult.snapScore)
        assertEquals(0, battleResult.shiftScore)
        assertEquals(0, battleResult.crowdCallScore)
        // Consistency is 0 when any challenge is incomplete
        assertEquals(0, battleResult.consistencyScore)
        assertEquals(240, battleResult.totalScore)
    }

    @Test
    fun `zero completed challenges produces 0 total score`() = runTest {
        val sessionId = BattleSessionId("session_empty")
        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_empty",
                userId = "user_3",
                battleId = "battle_3",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 1,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        val result = repository.completeBattle(sessionId, "req_empty")
        assertTrue(result is DomainResult.Success)

        val battleResult = (result as DomainResult.Success).value
        assertEquals(0, battleResult.snapScore)
        assertEquals(0, battleResult.shiftScore)
        assertEquals(0, battleResult.crowdCallScore)
        assertEquals(0, battleResult.consistencyScore)
        assertEquals(0, battleResult.totalScore)
    }

    @Test
    fun `session isolation guarantees results from other sessions are never included`() = runTest {
        val targetSession = BattleSessionId("session_target")
        val otherSession = BattleSessionId("session_other")

        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_target",
                userId = "user_4",
                battleId = "battle_4",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        // Insert results for otherSession
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_other_snap",
                challengeId = "snap",
                sessionId = "session_other",
                score = 300,
                maxScore = 300,
                completedAt = 1000L,
            )
        )
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_other_shift",
                challengeId = "shift",
                sessionId = "session_other",
                score = 300,
                maxScore = 300,
                completedAt = 1000L,
            )
        )
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_other_crowd_call",
                challengeId = "crowd_call",
                sessionId = "session_other",
                score = 300,
                maxScore = 300,
                completedAt = 1000L,
            )
        )

        // Insert only Snap for targetSession
        fakeChallengeDao.insertResult(
            ChallengeResultEntity(
                resultId = "session_target_snap",
                challengeId = "snap",
                sessionId = "session_target",
                score = 180,
                maxScore = 300,
                completedAt = 1000L,
            )
        )

        val result = repository.completeBattle(targetSession, "req_target")
        assertTrue(result is DomainResult.Success)

        val battleResult = (result as DomainResult.Success).value
        // Must reflect ONLY targetSession (Snap = 180, Shift = 0, Crowd = 0, Consistency = 0)
        assertEquals(180, battleResult.snapScore)
        assertEquals(0, battleResult.shiftScore)
        assertEquals(0, battleResult.crowdCallScore)
        assertEquals(0, battleResult.consistencyScore)
        assertEquals(180, battleResult.totalScore)
    }

    @Test
    fun `fallback when challengeResultDao is null preserves legacy mock score behavior`() = runTest {
        val legacyRepo = RoomBattleResultRepository(
            battleResultDao = fakeResultDao,
            battleHistoryDao = fakeHistoryDao,
            battleSessionDao = fakeSessionDao,
            challengeResultDao = null,
        )

        val sessionId = BattleSessionId("session_legacy")
        fakeSessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "session_legacy",
                userId = "user_legacy",
                battleId = "battle_legacy",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        val result = legacyRepo.completeBattle(sessionId, "req_legacy")
        assertTrue(result is DomainResult.Success)

        val battleResult = (result as DomainResult.Success).value
        assertEquals(300, battleResult.snapScore)
        assertEquals(300, battleResult.shiftScore)
        assertEquals(300, battleResult.crowdCallScore)
        assertEquals(100, battleResult.consistencyScore)
        assertEquals(1000, battleResult.totalScore)
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

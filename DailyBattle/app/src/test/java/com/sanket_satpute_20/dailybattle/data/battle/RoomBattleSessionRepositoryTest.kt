package com.sanket_satpute_20.dailybattle.data.battle

import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RoomBattleSessionRepositoryTest {

    private lateinit var fakeBattleSessionDao: FakeBattleSessionDao
    private lateinit var fakeChallengeSessionDao: FakeChallengeSessionDao
    private lateinit var repository: RoomBattleSessionRepository

    @Before
    fun setup() {
        fakeBattleSessionDao = FakeBattleSessionDao()
        fakeChallengeSessionDao = FakeChallengeSessionDao()
        repository = RoomBattleSessionRepository(fakeBattleSessionDao, fakeChallengeSessionDao)
    }

    @Test
    fun getOfficialSessionReturnsNullWhenNotFound() {
        val result = repository.getOfficialSession(UserId("u1"), BattleId("b1"))
        assertNull(result)
    }

    @Test
    fun saveSessionAndRetrieveOfficialSession() {
        val session = BattleSession(
            sessionId = BattleSessionId("s1"),
            userId = UserId("u1"),
            battleId = BattleId("b1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("s1"), ChallengeSessionStatus.COMPLETE, 1000L, 1030L),
                ChallengeSession(ChallengeId("c2"), BattleSessionId("s1"), ChallengeSessionStatus.READY, null, null),
            ),
        )

        repository.saveSession(session)

        val retrieved = repository.getOfficialSession(UserId("u1"), BattleId("b1"))
        assertNotNull(retrieved)
        assertEquals("s1", retrieved!!.sessionId.value)
        assertEquals(BattleMode.OFFICIAL, retrieved.mode)
        assertEquals(2, retrieved.challengeSessions.size)
        assertEquals(ChallengeSessionStatus.COMPLETE, retrieved.challengeSessions[0].status)
    }

    @Test
    fun getUnacknowledgedSessionsReturnsOnlyEligibleSessions() {
        val completeOfficial = BattleSession(
            sessionId = BattleSessionId("s_complete"),
            userId = UserId("u1"),
            battleId = BattleId("b1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("s_complete"), ChallengeSessionStatus.COMPLETE, 1000L, 1030L),
                ChallengeSession(ChallengeId("c2"), BattleSessionId("s_complete"), ChallengeSessionStatus.COMPLETE, 1031L, 1060L),
                ChallengeSession(ChallengeId("c3"), BattleSessionId("s_complete"), ChallengeSessionStatus.COMPLETE, 1061L, 1090L),
            ),
        )
        val incompleteOfficial = BattleSession(
            sessionId = BattleSessionId("s_incomplete"),
            userId = UserId("u2"),
            battleId = BattleId("b1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("s_incomplete"), ChallengeSessionStatus.COMPLETE, 1000L, 1030L),
            ),
        )

        repository.saveSession(completeOfficial)
        repository.saveSession(incompleteOfficial)

        val unacknowledged = repository.getUnacknowledgedSessions()
        assertEquals(1, unacknowledged.size)
        assertEquals("s_complete", unacknowledged[0].sessionId.value)
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

    private class FakeChallengeSessionDao : ChallengeSessionDao {
        val challengeSessions = mutableListOf<ChallengeSessionEntity>()

        override suspend fun insertChallengeSession(session: ChallengeSessionEntity) {
            challengeSessions.removeAll { it.challengeId == session.challengeId && it.sessionId == session.sessionId }
            challengeSessions.add(session)
        }

        override suspend fun insertChallengeSessions(sessions: List<ChallengeSessionEntity>) {
            sessions.forEach { insertChallengeSession(it) }
        }

        override suspend fun getChallengeSessionsForSession(sessionId: String): List<ChallengeSessionEntity> {
            return challengeSessions.filter { it.sessionId == sessionId }
        }

        override suspend fun getChallengeSession(challengeId: String, sessionId: String): ChallengeSessionEntity? {
            return challengeSessions.find { it.challengeId == challengeId && it.sessionId == sessionId }
        }

        override suspend fun deleteChallengeSessionsForSession(sessionId: String): Int {
            val count = challengeSessions.count { it.sessionId == sessionId }
            challengeSessions.removeAll { it.sessionId == sessionId }
            return count
        }
    }
}

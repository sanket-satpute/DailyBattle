package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetOrStartOfficialBattleUseCaseTest {

    private class FakeRepository : BattleSessionRepository {
        var existingSession: BattleSession? = null
        var savedSession: BattleSession? = null

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return existingSession
        }

        override fun saveSession(session: BattleSession) {
            savedSession = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> = emptyList()
    }

    private val userId = UserId("u-1")
    private val battleId = BattleId("b-1")

    @Test
    fun `creates new session if none exists`() {
        val repo = FakeRepository()
        val useCase = GetOrStartOfficialBattleUseCase(repo)

        val result = useCase.execute(userId, battleId)

        assertTrue(result is DomainResult.Success)
        val engine = (result as DomainResult.Success).value
        assertEquals(BattleSessionStatus.NOT_STARTED, engine.session.status)
        assertEquals(BattleMode.OFFICIAL, engine.session.mode)
        
        // Assert it was saved
        assertEquals(engine.session, repo.savedSession)
    }

    @Test
    fun `resumes session if one exists and is IN_PROGRESS`() {
        val repo = FakeRepository()
        val useCase = GetOrStartOfficialBattleUseCase(repo)

        val existing = BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 100L,
            completedAt = null,
            challengeSessions = emptyList() // simplified
        )
        repo.existingSession = existing

        val result = useCase.execute(userId, battleId)

        assertTrue(result is DomainResult.Success)
        val engine = (result as DomainResult.Success).value
        assertEquals(BattleSessionStatus.IN_PROGRESS, engine.session.status)
        assertEquals(existing.sessionId, engine.session.sessionId)
    }

    @Test
    fun `returns failure if session exists and is COMPLETED`() {
        val repo = FakeRepository()
        val useCase = GetOrStartOfficialBattleUseCase(repo)

        val existing = BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 100L,
            completedAt = 500L,
            challengeSessions = emptyList()
        )
        repo.existingSession = existing

        val result = useCase.execute(userId, battleId)

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }
}

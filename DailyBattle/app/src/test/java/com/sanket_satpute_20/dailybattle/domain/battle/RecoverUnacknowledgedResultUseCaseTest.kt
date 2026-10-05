package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RecoverUnacknowledgedResultUseCaseTest {

    private val fakeSessionRepository = object : BattleSessionRepository {
        var testUnacknowledgedSessions: List<BattleSession> = emptyList()
        var savedSession: BattleSession? = null

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? = null
        override fun saveSession(session: BattleSession) {
            savedSession = session
        }
        override fun getUnacknowledgedSessions(): List<BattleSession> = testUnacknowledgedSessions
    }

    private val fakeResultRepository = object : BattleResultRepository {
        var completeBattleCalled = false
        var lastIdempotencyKey: String? = null

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> {
            completeBattleCalled = true
            lastIdempotencyKey = clientRequestId
            return DomainResult.Success(
                BattleResult(
                    resultId = BattleResultId("res1"),
                    userId = UserId("user1"),
                    battleId = BattleId("battle1"),
                    sessionId = sessionId,
                    snapScore = 100,
                    shiftScore = 100,
                    crowdCallScore = 100,
                    consistencyScore = 100,
                    totalScore = 300,
                    percentile = 50.0,
                    completedAt = 12345L
                )
            )
        }

        override suspend fun getBattleResult(
            userId: UserId,
            battleId: BattleId
        ): DomainResult<BattleResult> {
            return DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            return DomainResult.Success(emptyList())
        }
    }

    private val completeBattleUseCase = CompleteBattleUseCase(fakeResultRepository, fakeSessionRepository)
    private val recoverUseCase = RecoverUnacknowledgedResultUseCase(fakeSessionRepository, completeBattleUseCase)

    @Test
    fun `when there are unacknowledged sessions, it recovers them`() = runTest {
        val sessionId = BattleSessionId(UUID.randomUUID().toString())
        val session = BattleSession(
            sessionId = sessionId,
            userId = UserId("user1"),
            battleId = BattleId("battle1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = emptyList() // simplified
        )
        fakeSessionRepository.testUnacknowledgedSessions = listOf(session)

        val results = recoverUseCase.execute()

        assertEquals(1, results.size)
        assertTrue(results[0] is DomainResult.Success)
        
        // Assert idempotency key is the sessionId
        assertEquals(sessionId.value, fakeResultRepository.lastIdempotencyKey)

        // Assert session was saved with COMPLETED status
        assertEquals(BattleSessionStatus.COMPLETED, fakeSessionRepository.savedSession?.status)
    }

    @Test
    fun `when there are no unacknowledged sessions, it does nothing`() = runTest {
        fakeSessionRepository.testUnacknowledgedSessions = emptyList()

        val results = recoverUseCase.execute()

        assertTrue(results.isEmpty())
        assertEquals(false, fakeResultRepository.completeBattleCalled)
    }
}

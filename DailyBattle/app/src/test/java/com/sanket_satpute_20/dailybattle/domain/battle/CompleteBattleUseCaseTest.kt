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
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CompleteBattleUseCaseTest {

    private lateinit var resultRepository: FakeBattleResultRepository
    private lateinit var sessionRepository: FakeBattleSessionRepository
    private lateinit var useCase: CompleteBattleUseCase

    @Before
    fun setup() {
        resultRepository = FakeBattleResultRepository()
        sessionRepository = FakeBattleSessionRepository()
        useCase = CompleteBattleUseCase(resultRepository, sessionRepository)
    }

    private fun createSession(status: BattleSessionStatus): BattleSession {
        return BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = UserId("u-1"),
            battleId = BattleId("b-1"),
            mode = BattleMode.OFFICIAL,
            status = status,
            currentChallenge = if (status == BattleSessionStatus.IN_PROGRESS) 3 else null,
            startedAt = 1000L,
            completedAt = if (status == BattleSessionStatus.COMPLETED) 5000L else null,
            challengeSessions = listOf(
                ChallengeSession(com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId("c-1"), BattleSessionId("s-1"), ChallengeSessionStatus.COMPLETE, 1000L, 2000L),
                ChallengeSession(com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId("c-2"), BattleSessionId("s-1"), ChallengeSessionStatus.COMPLETE, 2000L, 3000L),
                ChallengeSession(com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId("c-3"), BattleSessionId("s-1"), ChallengeSessionStatus.COMPLETE, 3000L, 4000L)
            )
        )
    }

    @Test
    fun `successful completion updates session and returns result`() = runTest {
        val session = createSession(BattleSessionStatus.IN_PROGRESS)
        
        val result = useCase.execute(session)
        
        assertTrue(result is DomainResult.Success)
        val savedSession = sessionRepository.getOfficialSession(session.userId, session.battleId)
        assertEquals(BattleSessionStatus.COMPLETED, savedSession?.status)
    }

    @Test
    fun `not started session fails validation`() = runTest {
        val session = createSession(BattleSessionStatus.NOT_STARTED)
        
        val result = useCase.execute(session)
        
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `already completed session returns existing result`() = runTest {
        val session = createSession(BattleSessionStatus.COMPLETED)
        // Ensure result exists in repo
        val existingResult = BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = session.userId,
            battleId = session.battleId,
            sessionId = session.sessionId,
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 99.0,
            completedAt = 5000L
        )
        resultRepository.saveResult(existingResult)

        val result = useCase.execute(session)
        
        assertTrue(result is DomainResult.Success)
        assertEquals(1000, (result as DomainResult.Success).value.totalScore)
        assertEquals(0, resultRepository.completeCallCount) // Should not call completeBattle again
    }

    @Test
    fun `network error returns failure and does not complete session locally`() = runTest {
        val session = createSession(BattleSessionStatus.IN_PROGRESS)
        resultRepository.shouldFailWith = AppError.Network
        
        val result = useCase.execute(session)
        
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
        
        val savedSession = sessionRepository.getOfficialSession(session.userId, session.battleId)
        // Session should not be saved as completed if API call failed
        assertEquals(null, savedSession) 
    }
}

class FakeBattleResultRepository : BattleResultRepository {
    var shouldFailWith: AppError? = null
    var completeCallCount = 0
    private val results = mutableMapOf<Pair<String, String>, BattleResult>()
    private val requests = mutableMapOf<String, BattleResult>()

    override suspend fun completeBattle(
        sessionId: BattleSessionId,
        clientRequestId: String
    ): DomainResult<BattleResult> {
        completeCallCount++
        if (shouldFailWith != null) {
            return DomainResult.Failure(shouldFailWith!!)
        }

        if (requests.containsKey(clientRequestId)) {
            return DomainResult.Success(requests[clientRequestId]!!)
        }

        val result = BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = UserId("u-1"),
            battleId = BattleId("b-1"),
            sessionId = sessionId,
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 99.9,
            completedAt = 5000L
        )

        requests[clientRequestId] = result
        results[Pair(result.userId.value, result.battleId.value)] = result

        return DomainResult.Success(result)
    }

    override suspend fun getBattleResult(
        userId: UserId,
        battleId: BattleId
    ): DomainResult<BattleResult> {
        val result = results[Pair(userId.value, battleId.value)]
        return if (result != null) {
            DomainResult.Success(result)
        } else {
            DomainResult.Failure(AppError.Domain)
        }
    }

    fun saveResult(result: BattleResult) {
        results[Pair(result.userId.value, result.battleId.value)] = result
    }
}

class FakeBattleSessionRepository : BattleSessionRepository {
    private val sessions = mutableMapOf<Pair<String, String>, BattleSession>()

    override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
        return sessions[Pair(userId.value, battleId.value)]
    }

    override fun saveSession(session: BattleSession) {
        sessions[Pair(session.userId.value, session.battleId.value)] = session
    }
}

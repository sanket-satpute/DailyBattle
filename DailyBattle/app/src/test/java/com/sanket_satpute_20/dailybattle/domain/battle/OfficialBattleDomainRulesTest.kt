package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Sprint 23.1 — Unit Tests (Domain Rules)
 * Validates core business and domain integrity rules:
 * 1. Official battle single-attempt enforcement (cannot start a second official battle if already completed).
 * 2. In-progress official battle resumption.
 * 3. Practice isolation rule: Practice mode must NEVER silently enter official completion or mutate official results.
 * 4. Completed session idempotency and recovery.
 *
 * Per `docs/00_MASTER_SPEC.md`, `docs/02_GAMEPLAY.md`, and `docs/12_TESTING_AND_QA.md` §8.
 */
class OfficialBattleDomainRulesTest {

    private val testUserId = UserId("user_test_1")
    private val testBattleId = BattleId("battle_today")
    private val testSessionId = BattleSessionId("session_test_1")

    private lateinit var fakeSessionRepository: FakeBattleSessionRepository
    private lateinit var fakeResultRepository: FakeBattleResultRepository

    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase
    private lateinit var completeBattleUseCase: CompleteBattleUseCase

    @Before
    fun setup() {
        fakeSessionRepository = FakeBattleSessionRepository()
        fakeResultRepository = FakeBattleResultRepository()
        getOrStartOfficialBattleUseCase = GetOrStartOfficialBattleUseCase(fakeSessionRepository)
        completeBattleUseCase = CompleteBattleUseCase(fakeResultRepository, fakeSessionRepository)
    }

    @Test
    fun `starting official battle when none exists creates and persists new official session`() {
        val result = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
        assertTrue("Expected Success when starting fresh official battle", result is DomainResult.Success)

        val engine = (result as DomainResult.Success).value
        assertEquals(BattleMode.OFFICIAL, engine.session.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, engine.session.status)
        assertEquals(testUserId, engine.session.userId)
        assertEquals(testBattleId, engine.session.battleId)

        // Session must be persisted in repository
        val persisted = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(persisted)
        assertEquals(engine.session.sessionId, persisted?.sessionId)
    }

    @Test
    fun `resuming in-progress official battle returns existing engine without creating duplicate session`() {
        // Pre-populate an in-progress session (currentChallenge = 2 for Shift)
        val inProgressSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = emptyList()
        )
        fakeSessionRepository.saveSession(inProgressSession)

        val result = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
        assertTrue(result is DomainResult.Success)

        val engine = (result as DomainResult.Success).value
        assertEquals(testSessionId, engine.session.sessionId)
        assertEquals(BattleSessionStatus.IN_PROGRESS, engine.session.status)
        assertEquals(2, engine.session.currentChallenge)
    }

    @Test
    fun `attempting to start official battle when already completed returns domain failure`() {
        // Pre-populate completed official session
        val completedSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 2000L,
            challengeSessions = emptyList()
        )
        fakeSessionRepository.saveSession(completedSession)

        val result = getOrStartOfficialBattleUseCase.execute(testUserId, testBattleId)
        assertTrue("Duplicate official attempt must be rejected", result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }

    @Test
    fun `practice session cannot be finalized through official completion use case`() = runTest {
        // Practice isolation rule (08_DATA_AND_API.md §19):
        // Practice mode must NEVER enter official battle completion
        val practiceSession = BattleSession(
            sessionId = BattleSessionId("practice_sess_1"),
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.PRACTICE,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = emptyList()
        )

        val result = completeBattleUseCase.execute(practiceSession)
        assertTrue("Practice sessions must be rejected by CompleteBattleUseCase", result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }

    @Test
    fun `completing battle with status NOT_STARTED returns validation failure`() = runTest {
        val notStartedSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList()
        )

        val result = completeBattleUseCase.execute(notStartedSession)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `completing already completed battle is idempotent and returns existing result`() = runTest {
        val completedSession = BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 2000L,
            challengeSessions = emptyList()
        )
        val existingResult = BattleResult(
            resultId = BattleResultId("res_1"),
            userId = testUserId,
            battleId = testBattleId,
            sessionId = testSessionId,
            totalScore = 850,
            snapScore = 250,
            shiftScore = 250,
            crowdCallScore = 250,
            consistencyScore = 100,
            percentile = 0.85,
            completedAt = 2000L
        )
        fakeResultRepository.saveBattleResult(existingResult)

        val result = completeBattleUseCase.execute(completedSession)
        assertTrue(result is DomainResult.Success)
        assertEquals(existingResult, (result as DomainResult.Success).value)
    }

    // ── Fake Test Doubles ───────────────────────────────────────────────

    private class FakeBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<String, BattleSession>()

        override fun getSessionById(sessionId: BattleSessionId): BattleSession? = sessions[sessionId.value]

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions.values.firstOrNull {
                it.userId == userId && it.battleId == battleId && it.mode == BattleMode.OFFICIAL
            }
        }

        override fun saveSession(session: BattleSession) {
            sessions[session.sessionId.value] = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return sessions.values.filter { it.status != BattleSessionStatus.COMPLETED }
        }
    }

    private class FakeBattleResultRepository : BattleResultRepository {
        private val results = mutableMapOf<String, BattleResult>()
        private val resultsBySession = mutableMapOf<String, BattleResult>()

        fun saveBattleResult(result: BattleResult) {
            results["${result.userId.value}_${result.battleId.value}"] = result
            resultsBySession[result.sessionId.value] = result
        }

        override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> {
            val key = "${userId.value}_${battleId.value}"
            return results[key]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(AppError.Persistence)
        }

        override suspend fun getBattleResultBySessionId(sessionId: BattleSessionId): DomainResult<BattleResult> {
            return resultsBySession[sessionId.value]?.let { DomainResult.Success(it) } ?: DomainResult.Failure(AppError.Persistence)
        }

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            return DomainResult.Success(results.values.filter { it.userId == userId })
        }

        override suspend fun completeBattle(sessionId: BattleSessionId, idempotencyKey: String): DomainResult<BattleResult> {
            val result = BattleResult(
                resultId = BattleResultId("res_${sessionId.value}"),
                userId = UserId("user_test"),
                battleId = BattleId("battle_test"),
                sessionId = sessionId,
                totalScore = 900,
                snapScore = 300,
                shiftScore = 300,
                crowdCallScore = 200,
                consistencyScore = 100,
                percentile = 0.90,
                completedAt = 3000L
            )
            resultsBySession[sessionId.value] = result
            return DomainResult.Success(result)
        }
    }
}

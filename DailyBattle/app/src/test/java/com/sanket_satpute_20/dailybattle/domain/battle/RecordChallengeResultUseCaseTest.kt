package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.fake.FakeChallengeResultDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecordChallengeResultUseCaseTest {

    private lateinit var fakeDao: FakeChallengeResultDao
    private lateinit var useCase: RecordChallengeResultUseCase

    @Before
    fun setUp() {
        fakeDao = FakeChallengeResultDao()
        useCase = RecordChallengeResultUseCase(fakeDao)
    }

    @Test
    fun `domainToEntityMapping maps all fields accurately into dao`() = runTest {
        val inputResult = ChallengeResult(
            resultId = ChallengeResultId("arbitrary_temp_id"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("session_100"),
            score = 250,
            maxScore = 300,
            completedAt = 1700000000000L,
            metadata = mapOf("customKey" to "customVal")
        )

        val result = useCase.execute(inputResult)
        assertTrue(result is DomainResult.Success)

        val entity = fakeDao.getResultById("session_100_snap")
        assertNotNull(entity)
        assertEquals("session_100_snap", entity!!.resultId)
        assertEquals("session_100", entity.sessionId)
        assertEquals("snap", entity.challengeId)
        assertEquals(250, entity.score)
        assertEquals(300, entity.maxScore)
        assertEquals(1700000000000L, entity.completedAt)
        assertNotNull(entity.metadataJson)

        val domainRestored = entity.toDomain()
        assertEquals("customVal", domainRestored.metadata?.get("customKey"))
    }

    @Test
    fun `deterministicResultId is formatted as sessionId_challengeId`() = runTest {
        val inputResult = ChallengeResult(
            resultId = ChallengeResultId("temp_uuid_123"),
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("battle_session_abc"),
            score = 280,
            maxScore = 300,
            completedAt = 1000L,
        )

        val result = useCase.execute(inputResult)
        assertTrue(result is DomainResult.Success)
        val successValue = (result as DomainResult.Success).value
        assertEquals("battle_session_abc_c-1", successValue.resultId.value)

        val persisted = fakeDao.getResultById("battle_session_abc_c-1")
        assertNotNull(persisted)
        assertEquals("battle_session_abc_c-1", persisted!!.resultId)
    }

    @Test
    fun `repeated insertion for the same challenge is idempotent and replaces row`() = runTest {
        val firstAttempt = ChallengeResult(
            resultId = ChallengeResultId("temp_1"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("session_1"),
            score = 150,
            maxScore = 300,
            completedAt = 1000L,
        )
        val secondAttempt = ChallengeResult(
            resultId = ChallengeResultId("temp_2"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("session_1"),
            score = 290,
            maxScore = 300,
            completedAt = 2000L,
        )

        useCase.execute(firstAttempt)
        assertEquals(1, fakeDao.results.size)
        assertEquals(150, fakeDao.getResultById("session_1_snap")!!.score)

        useCase.execute(secondAttempt)
        // Size remains 1 — no duplicate rows created
        assertEquals(1, fakeDao.results.size)
        assertEquals(290, fakeDao.getResultById("session_1_snap")!!.score)
        assertEquals(2000L, fakeDao.getResultById("session_1_snap")!!.completedAt)
    }

    @Test
    fun `different sessions with the same challenge ID are stored independently`() = runTest {
        val session1Result = ChallengeResult(
            resultId = ChallengeResultId("temp_s1"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("session_A"),
            score = 200,
            maxScore = 300,
            completedAt = 1000L,
        )
        val session2Result = ChallengeResult(
            resultId = ChallengeResultId("temp_s2"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("session_B"),
            score = 300,
            maxScore = 300,
            completedAt = 2000L,
        )

        useCase.execute(session1Result)
        useCase.execute(session2Result)

        assertEquals(2, fakeDao.results.size)
        assertEquals(200, fakeDao.getResultById("session_A_snap")!!.score)
        assertEquals(300, fakeDao.getResultById("session_B_snap")!!.score)

        val sessionAResults = fakeDao.getResultsForSession("session_A")
        assertEquals(1, sessionAResults.size)
        assertEquals("session_A", sessionAResults.first().sessionId)

        val sessionBResults = fakeDao.getResultsForSession("session_B")
        assertEquals(1, sessionBResults.size)
        assertEquals("session_B", sessionBResults.first().sessionId)
    }

    @Test
    fun `different challenges within one session are all preserved`() = runTest {
        val sessionId = BattleSessionId("session_multi")
        val snap = ChallengeResult(
            resultId = ChallengeResultId("temp_snap"),
            challengeId = ChallengeId("snap"),
            sessionId = sessionId,
            score = 250,
            maxScore = 300,
            completedAt = 1000L,
        )
        val shift = ChallengeResult(
            resultId = ChallengeResultId("temp_shift"),
            challengeId = ChallengeId("shift"),
            sessionId = sessionId,
            score = 270,
            maxScore = 300,
            completedAt = 2000L,
        )
        val crowd = ChallengeResult(
            resultId = ChallengeResultId("temp_crowd"),
            challengeId = ChallengeId("crowd_call"),
            sessionId = sessionId,
            score = 290,
            maxScore = 300,
            completedAt = 3000L,
        )

        useCase.execute(snap)
        useCase.execute(shift)
        useCase.execute(crowd)

        val sessionResults = fakeDao.getResultsForSession("session_multi")
        assertEquals(3, sessionResults.size)
        assertTrue(sessionResults.any { it.challengeId == "snap" && it.score == 250 })
        assertTrue(sessionResults.any { it.challengeId == "shift" && it.score == 270 })
        assertTrue(sessionResults.any { it.challengeId == "crowd_call" && it.score == 290 })
    }

    @Test
    fun `score validation clamps negative scores to 0`() = runTest {
        val negativeScoreResult = ChallengeResult(
            resultId = ChallengeResultId("temp_neg"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("sess_1"),
            score = -45,
            maxScore = 300,
            completedAt = 1000L,
        )

        val result = useCase.execute(negativeScoreResult)
        assertTrue(result is DomainResult.Success)
        val domain = (result as DomainResult.Success).value
        assertEquals(0, domain.score)

        val entity = fakeDao.getResultById("sess_1_snap")
        assertEquals(0, entity!!.score)
    }

    @Test
    fun `score validation clamps scores exceeding 300 to approved maximum`() = runTest {
        val excessiveScoreResult = ChallengeResult(
            resultId = ChallengeResultId("temp_high"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("sess_1"),
            score = 550,
            maxScore = 300,
            completedAt = 1000L,
        )

        val result = useCase.execute(excessiveScoreResult)
        assertTrue(result is DomainResult.Success)
        val domain = (result as DomainResult.Success).value
        assertEquals(300, domain.score)

        val entity = fakeDao.getResultById("sess_1_snap")
        assertEquals(300, entity!!.score)
    }

    @Test
    fun `score validation enforces approved maxScore constant`() = runTest {
        val corruptMaxScoreResult = ChallengeResult(
            resultId = ChallengeResultId("temp_max"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("sess_1"),
            score = 250,
            maxScore = 999, // Corrupt maxScore
            completedAt = 1000L,
        )

        val result = useCase.execute(corruptMaxScoreResult)
        assertTrue(result is DomainResult.Success)
        val domain = (result as DomainResult.Success).value
        assertEquals(300, domain.maxScore)

        val entity = fakeDao.getResultById("sess_1_snap")
        assertEquals(300, entity!!.maxScore)
    }

    @Test
    fun `preserves timeSpentMs in metadata when provided`() = runTest {
        val inputResult = ChallengeResult(
            resultId = ChallengeResultId("temp_time"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("sess_1"),
            score = 280,
            maxScore = 300,
            completedAt = 1000L,
        )

        val result = useCase.execute(inputResult, timeSpentMs = 1450L)
        assertTrue(result is DomainResult.Success)
        val domain = (result as DomainResult.Success).value
        assertEquals("1450", domain.metadata?.get("timeSpentMs"))

        val entity = fakeDao.getResultById("sess_1_snap")
        assertNotNull(entity!!.metadataJson)
        val restored = entity.toDomain()
        assertEquals("1450", restored.metadata?.get("timeSpentMs"))
    }

    @Test
    fun `blank sessionId or challengeId returns validation error`() = runTest {
        val blankSession = ChallengeResult(
            resultId = ChallengeResultId("temp"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId(""),
            score = 200,
            maxScore = 300,
            completedAt = 1000L,
        )
        val blankChallenge = ChallengeResult(
            resultId = ChallengeResultId("temp"),
            challengeId = ChallengeId("   "),
            sessionId = BattleSessionId("sess_1"),
            score = 200,
            maxScore = 300,
            completedAt = 1000L,
        )

        val res1 = useCase.execute(blankSession)
        assertTrue(res1 is DomainResult.Failure)
        assertEquals(AppError.Validation, (res1 as DomainResult.Failure).error)

        val res2 = useCase.execute(blankChallenge)
        assertTrue(res2 is DomainResult.Failure)
        assertEquals(AppError.Validation, (res2 as DomainResult.Failure).error)
    }

    @Test
    fun `dao persistence error maps to AppError Persistence`() = runTest {
        fakeDao.shouldFail = true
        val validResult = ChallengeResult(
            resultId = ChallengeResultId("temp"),
            challengeId = ChallengeId("snap"),
            sessionId = BattleSessionId("sess_fail"),
            score = 200,
            maxScore = 300,
            completedAt = 1000L,
        )

        val result = useCase.execute(validResult)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Persistence, (result as DomainResult.Failure).error)
    }
}

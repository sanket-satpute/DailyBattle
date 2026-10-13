package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeResultRepository
import com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Verification of Sprint 19.3 — Practice Results.
 *
 * Requirements:
 * - Practice results remain isolated (`ROADMAP.md` Phase 19, Sprint 19.3).
 * - Critical invariant: Practice must not mutate official battle results.
 * - All 9 Practice Edge Cases tested:
 *   1. Practice started
 *   2. Practice interrupted
 *   3. Practice completed
 *   4. Practice repeated
 *   5. Official battle active
 *   6. Official battle completed
 *   7. Offline
 *   8. No practice data
 *   9. Invalid practice configuration
 */
class PracticeResultTest {

    private val userId = UserId("user-test-193")
    private val otherUser = UserId("user-other-193")
    private val sessionId = PracticeSessionId("practice-session-193")

    private lateinit var practiceSessionRepository: InMemoryPracticeSessionRepository
    private lateinit var practiceResultRepository: InMemoryPracticeResultRepository
    private lateinit var officialSessionRepository: InMemoryBattleSessionRepository
    private lateinit var officialResultRepository: InMemoryBattleResultRepository

    private lateinit var activePracticeManager: ActivePracticeSessionManager
    private lateinit var startPracticeUseCase: StartPracticeSessionUseCase
    private lateinit var completePracticeUseCase: CompletePracticeSessionUseCase
    private lateinit var interruptPracticeUseCase: InterruptPracticeSessionUseCase
    private lateinit var repeatPracticeUseCase: RepeatPracticeSessionUseCase
    private lateinit var getPracticeResultUseCase: GetPracticeResultUseCase
    private lateinit var getLatestPracticeResultUseCase: GetLatestPracticeResultUseCase
    private lateinit var getUserPracticeResultsUseCase: GetUserPracticeResultsUseCase

    @Before
    fun setUp() {
        practiceSessionRepository = InMemoryPracticeSessionRepository()
        practiceResultRepository = InMemoryPracticeResultRepository()
        officialSessionRepository = InMemoryBattleSessionRepository()
        officialResultRepository = InMemoryBattleResultRepository()

        activePracticeManager = ActivePracticeSessionManager(
            repository = practiceSessionRepository,
            resultRepository = practiceResultRepository
        )

        startPracticeUseCase = StartPracticeSessionUseCase(activePracticeManager)
        completePracticeUseCase = CompletePracticeSessionUseCase(activePracticeManager)
        interruptPracticeUseCase = InterruptPracticeSessionUseCase(activePracticeManager)
        repeatPracticeUseCase = RepeatPracticeSessionUseCase(activePracticeManager)
        getPracticeResultUseCase = GetPracticeResultUseCase(practiceResultRepository)
        getLatestPracticeResultUseCase = GetLatestPracticeResultUseCase(practiceResultRepository)
        getUserPracticeResultsUseCase = GetUserPracticeResultsUseCase(practiceResultRepository)
    }

    // ------------------------------------------------------------------------
    // Domain Model & Validation Tests
    // ------------------------------------------------------------------------

    @Test
    fun `practice result domain model enforces valid boundaries`() {
        val validResult = PracticeResult(
            resultId = PracticeResultId("pr-valid"),
            userId = userId,
            sessionId = sessionId,
            difficulty = PracticeDifficulty.STANDARD,
            snapScore = 250,
            shiftScore = 200,
            crowdCallScore = 280,
            totalScore = 730,
            repeatCount = 0,
            completedAt = 1000L
        )

        assertEquals(250, validResult.snapScore)
        assertEquals(200, validResult.shiftScore)
        assertEquals(280, validResult.crowdCallScore)
        assertEquals(730, validResult.totalScore)
        assertEquals(BattleMode.PRACTICE, validResult.mode)
    }

    @Test
    fun `practice result rejects invalid score boundaries`() {
        // Negative snap score
        assertThrows(IllegalArgumentException::class.java) {
            PracticeResult(
                resultId = PracticeResultId("pr-inv1"),
                userId = userId,
                sessionId = sessionId,
                difficulty = PracticeDifficulty.STANDARD,
                snapScore = -1,
                shiftScore = 100,
                crowdCallScore = 100,
                totalScore = 200,
                completedAt = 1000L
            )
        }

        // Snap score > 300
        assertThrows(IllegalArgumentException::class.java) {
            PracticeResult(
                resultId = PracticeResultId("pr-inv2"),
                userId = userId,
                sessionId = sessionId,
                difficulty = PracticeDifficulty.STANDARD,
                snapScore = 301,
                shiftScore = 100,
                crowdCallScore = 100,
                totalScore = 200,
                completedAt = 1000L
            )
        }

        // Total score > 900
        assertThrows(IllegalArgumentException::class.java) {
            PracticeResult(
                resultId = PracticeResultId("pr-inv3"),
                userId = userId,
                sessionId = sessionId,
                difficulty = PracticeDifficulty.STANDARD,
                snapScore = 300,
                shiftScore = 300,
                crowdCallScore = 300,
                totalScore = 901,
                completedAt = 1000L
            )
        }

        // Invalid completedAt
        assertThrows(IllegalArgumentException::class.java) {
            PracticeResult(
                resultId = PracticeResultId("pr-inv4"),
                userId = userId,
                sessionId = sessionId,
                difficulty = PracticeDifficulty.STANDARD,
                snapScore = 100,
                shiftScore = 100,
                crowdCallScore = 100,
                totalScore = 300,
                completedAt = 0L
            )
        }
    }

    // ------------------------------------------------------------------------
    // Edge Case 1: Practice Started (no result yet)
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 1 - practice started does not generate practice result`() {
        val startResult = startPracticeUseCase.execute(userId)
        assertTrue(startResult is DomainResult.Success)
        val activeSession = (startResult as DomainResult.Success).value

        // No result should exist in repository yet
        val queryResult = getPracticeResultUseCase.execute(activeSession.sessionId)
        assertTrue(queryResult is DomainResult.Failure)
        assertEquals(AppError.Domain, (queryResult as DomainResult.Failure).error)

        assertNull(activePracticeManager.getLatestPracticeResult())
    }

    // ------------------------------------------------------------------------
    // Edge Case 2: Practice Interrupted (no result created)
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 2 - practice interrupted does not create practice result`() {
        startPracticeUseCase.execute(userId)
        val interruptResult = interruptPracticeUseCase.execute("User exited early")
        assertTrue(interruptResult is DomainResult.Success)

        val session = (interruptResult as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.INTERRUPTED, session.status)

        // Conversion fails on interrupted session
        val convResult = session.toPracticeResult()
        assertTrue(convResult is DomainResult.Failure)
        assertEquals(AppError.Validation, (convResult as DomainResult.Failure).error)

        // Repository contains no result
        val queryResult = getPracticeResultUseCase.execute(session.sessionId)
        assertTrue(queryResult is DomainResult.Failure)
        assertEquals(AppError.Domain, (queryResult as DomainResult.Failure).error)
    }

    // ------------------------------------------------------------------------
    // Edge Case 3: Practice Completed (result generated, aggregated, and stored)
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 3 - practice completed aggregates scores and stores isolated result`() {
        val startRes = startPracticeUseCase.execute(userId)
        val session = (startRes as DomainResult.Success).value

        // Record challenge scores in practice
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SNAP, 275)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 260)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 290)

        // Complete practice
        val completeRes = completePracticeUseCase.execute(now = 2000L)
        assertTrue(completeRes is DomainResult.Success)

        // Practice result should be stored in practice result repository
        val resultFromSessionId = getPracticeResultUseCase.execute(session.sessionId)
        assertTrue(resultFromSessionId is DomainResult.Success)
        val pr = (resultFromSessionId as DomainResult.Success).value

        assertEquals(275, pr.snapScore)
        assertEquals(260, pr.shiftScore)
        assertEquals(290, pr.crowdCallScore)
        assertEquals(825, pr.totalScore)
        assertEquals(2000L, pr.completedAt)
        assertEquals(0, pr.repeatCount)
        assertEquals(PracticeDifficulty.STANDARD, pr.difficulty)

        // Verify latest practice result matches
        val latestRes = getLatestPracticeResultUseCase.execute(userId)
        assertTrue(latestRes is DomainResult.Success)
        assertEquals(pr.resultId, (latestRes as DomainResult.Success).value.resultId)
    }

    // ------------------------------------------------------------------------
    // Edge Case 4: Practice Repeated
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 4 - practice repeated increments repeat count and creates distinct updated result`() {
        startPracticeUseCase.execute(userId)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SNAP, 200)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 200)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 200)
        completePracticeUseCase.execute(now = 1000L)

        val firstResult = (getLatestPracticeResultUseCase.execute(userId) as DomainResult.Success).value
        assertEquals(600, firstResult.totalScore)
        assertEquals(0, firstResult.repeatCount)

        // Repeat practice
        val repeatRes = repeatPracticeUseCase.execute(now = 1500L)
        assertTrue(repeatRes is DomainResult.Success)
        assertNull(activePracticeManager.getLatestPracticeResult())

        // Play second run with higher scores
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SNAP, 290)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 280)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 295)
        completePracticeUseCase.execute(now = 2500L)

        val secondResult = (getLatestPracticeResultUseCase.execute(userId) as DomainResult.Success).value
        assertEquals(865, secondResult.totalScore)
        assertEquals(1, secondResult.repeatCount)
        assertEquals(2500L, secondResult.completedAt)

        val userResults = (getUserPracticeResultsUseCase.execute(userId) as DomainResult.Success).value
        assertTrue(userResults.isNotEmpty())
        assertEquals(865, userResults[0].totalScore)
    }

    // ------------------------------------------------------------------------
    // Edge Case 5: Official Battle Active (remains isolated)
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 5 - practice results do not affect active official battle`() = runTest {
        val officialSession = BattleSession(
            sessionId = BattleSessionId("official-active-193"),
            userId = userId,
            battleId = BattleId("battle-today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 500L,
            completedAt = null
        )
        officialSessionRepository.saveSession(officialSession)

        // Play and complete practice
        startPracticeUseCase.execute(userId)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SNAP, 300)
        completePracticeUseCase.execute()

        // Official session is 100% unchanged
        val postSession = officialSessionRepository.getSessionById(officialSession.sessionId)
        assertNotNull(postSession)
        assertEquals(BattleSessionStatus.IN_PROGRESS, postSession?.status)
        assertEquals(2, postSession?.currentChallenge)
        assertEquals(BattleMode.OFFICIAL, postSession?.mode)

        // No official result was created
        val officialResult = officialResultRepository.getBattleResult(userId, BattleId("battle-today"))
        assertTrue(officialResult is DomainResult.Failure)
    }

    // ------------------------------------------------------------------------
    // Edge Case 6: Official Battle Completed (official results cannot be modified or replaced)
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 6 - critical invariant official results cannot be replaced or modified by practice results`() = runTest {
        val battleId = BattleId("official-battle-done")
        val officialResult = BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = userId,
            battleId = battleId,
            sessionId = BattleSessionId("official-done-session"),
            snapScore = 290,
            shiftScore = 280,
            crowdCallScore = 295,
            consistencyScore = 95,
            totalScore = 960,
            percentile = 99.2,
            completedAt = 1200L
        )
        officialResultRepository.saveResult(officialResult)

        // Play practice and complete with different scores
        startPracticeUseCase.execute(userId)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SNAP, 150)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 160)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 170)
        completePracticeUseCase.execute(now = 3000L)

        // Practice result exists in practice repo
        val practiceRes = (getLatestPracticeResultUseCase.execute(userId) as DomainResult.Success).value
        assertEquals(480, practiceRes.totalScore)

        // Official result is 100% UNTOUCHED
        val postOfficial = (officialResultRepository.getBattleResult(userId, battleId) as DomainResult.Success).value
        assertEquals(960, postOfficial.totalScore)
        assertEquals(99.2, postOfficial.percentile, 0.001)
        assertEquals(95, postOfficial.consistencyScore)
        assertEquals(1200L, postOfficial.completedAt)

        // Official repository has ZERO practice results
        val officialHistory = (officialResultRepository.getUserBattleResults(userId) as DomainResult.Success).value
        assertEquals(1, officialHistory.size)
        assertEquals(960, officialHistory[0].totalScore)
    }

    // ------------------------------------------------------------------------
    // Edge Case 7: Offline Operation
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 7 - practice results operate completely offline without network synchronization`() {
        startPracticeUseCase.execute(userId)
        activePracticeManager.recordPracticeChallengeScore(ChallengeType.SNAP, 250)
        val completeResult = completePracticeUseCase.completeAndGetResult(now = 5000L)

        assertTrue(completeResult is DomainResult.Success)
        val pr = (completeResult as DomainResult.Success).value
        assertEquals(250, pr.snapScore)
        assertEquals(250, pr.totalScore)

        // Stored and retrieved entirely locally
        val stored = getPracticeResultUseCase.execute(pr.resultId)
        assertTrue(stored is DomainResult.Success)
        assertEquals(pr, (stored as DomainResult.Success).value)
    }

    // ------------------------------------------------------------------------
    // Edge Case 8: No Practice Data
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 8 - no practice data returns empty list or NotFound safely`() {
        val nonExistentUser = UserId("ghost-user")

        val userResults = getUserPracticeResultsUseCase.execute(nonExistentUser)
        assertTrue(userResults is DomainResult.Success)
        assertTrue((userResults as DomainResult.Success).value.isEmpty())

        val latestResult = getLatestPracticeResultUseCase.execute(nonExistentUser)
        assertTrue(latestResult is DomainResult.Failure)
        assertEquals(AppError.Domain, (latestResult as DomainResult.Failure).error)

        val sessionResult = getPracticeResultUseCase.execute(PracticeSessionId("non-existent-session"))
        assertTrue(sessionResult is DomainResult.Failure)
        assertEquals(AppError.Domain, (sessionResult as DomainResult.Failure).error)
    }

    // ------------------------------------------------------------------------
    // Edge Case 9: Invalid Practice Configuration
    // ------------------------------------------------------------------------

    @Test
    fun `edge case 9 - invalid practice configuration rejects session and prevents result generation`() {
        val invalidConfig = PracticeConfiguration(challengeTypes = emptyList())
        val startResult = startPracticeUseCase.execute(userId, invalidConfig)

        assertTrue(startResult is DomainResult.Failure)
        assertEquals(AppError.Validation, (startResult as DomainResult.Failure).error)

        // Attempting to complete practice when no session was started
        val completeResult = completePracticeUseCase.execute()
        assertTrue(completeResult is DomainResult.Failure)

        // No practice result created
        val latestRes = getLatestPracticeResultUseCase.execute(userId)
        assertTrue(latestRes is DomainResult.Failure)
        assertEquals(AppError.Domain, (latestRes as DomainResult.Failure).error)
    }

    // ------------------------------------------------------------------------
    // Repository Isolation & Clearing
    // ------------------------------------------------------------------------

    @Test
    fun `clearing practice results removes only specified user results`() {
        val res1 = PracticeResult(
            resultId = PracticeResultId("pr-1"),
            userId = userId,
            sessionId = PracticeSessionId("ps-1"),
            difficulty = PracticeDifficulty.STANDARD,
            snapScore = 200,
            shiftScore = 200,
            crowdCallScore = 200,
            totalScore = 600,
            completedAt = 1000L
        )
        val res2 = PracticeResult(
            resultId = PracticeResultId("pr-2"),
            userId = otherUser,
            sessionId = PracticeSessionId("ps-2"),
            difficulty = PracticeDifficulty.STANDARD,
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            totalScore = 900,
            completedAt = 2000L
        )

        practiceResultRepository.savePracticeResult(res1)
        practiceResultRepository.savePracticeResult(res2)

        // Clear user 1 results
        practiceResultRepository.clearPracticeResults(userId)

        assertTrue(getPracticeResultUseCase.execute(res1.resultId) is DomainResult.Failure)
        assertTrue(getPracticeResultUseCase.execute(res2.resultId) is DomainResult.Success)
    }
}

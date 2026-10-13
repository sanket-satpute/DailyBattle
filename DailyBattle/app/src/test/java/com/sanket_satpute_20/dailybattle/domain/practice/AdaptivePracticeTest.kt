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
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verification of Sprint 19.4 — Adaptive Practice.
 *
 * Requirements:
 * - `ROADMAP.md` Sprint 19.4: "Only implement approved adaptive/AI behavior."
 * - `02_GAMEPLAY.md` §36: adaptive personalization is STATUS: PENDING.
 * - `02_GAMEPLAY.md` §40: practice-only adaptive difficulty is DECISION_REQUIRED.
 * - `15_ANTIGRAVITY_RULES.md` Rule 102: no arbitrary AI personalization unless approved.
 * - Critical invariant: Practice must not mutate official battle results.
 *
 * All 9 Practice Edge Cases tested through the adaptive guard-rail lens:
 *   1. Practice started — with approved vs. unapproved difficulty
 *   2. Practice interrupted — adaptive session cannot be started to interrupt
 *   3. Practice completed — only standard difficulty completes
 *   4. Practice repeated — repeat does not introduce adaptive behavior
 *   5. Official battle active — adaptive practice rejection with active official battle
 *   6. Official battle completed — adaptive practice rejection with completed official battle
 *   7. Offline — adaptive guard applies regardless of network state
 *   8. No practice data — adaptive policy state on empty repository
 *   9. Invalid practice configuration — adaptive difficulty is an invalid configuration
 */
class AdaptivePracticeTest {

    private val userId = UserId("user-test-194")

    private lateinit var practiceSessionRepository: InMemoryPracticeSessionRepository
    private lateinit var practiceResultRepository: InMemoryPracticeResultRepository
    private lateinit var officialSessionRepository: InMemoryBattleSessionRepository
    private lateinit var officialResultRepository: InMemoryBattleResultRepository
    private lateinit var practiceManager: ActivePracticeSessionManager
    private lateinit var startPracticeUseCase: StartPracticeSessionUseCase

    @Before
    fun setup() {
        practiceSessionRepository = InMemoryPracticeSessionRepository()
        practiceResultRepository = InMemoryPracticeResultRepository()
        officialSessionRepository = InMemoryBattleSessionRepository()
        officialResultRepository = InMemoryBattleResultRepository()
        practiceManager = ActivePracticeSessionManager(practiceSessionRepository, practiceResultRepository)
        startPracticeUseCase = StartPracticeSessionUseCase(practiceManager)
    }

    // ============================================================================
    // AdaptivePracticePolicy Unit Tests
    // ============================================================================

    @Test
    fun `policy - STANDARD difficulty is approved`() {
        assertTrue(AdaptivePracticePolicy.isApproved(PracticeDifficulty.STANDARD))
    }

    @Test
    fun `policy - ADAPTIVE_PENDING difficulty is not approved`() {
        assertFalse(AdaptivePracticePolicy.isApproved(PracticeDifficulty.ADAPTIVE_PENDING))
    }

    @Test
    fun `policy - approvedDifficulties contains only STANDARD`() {
        assertEquals(setOf(PracticeDifficulty.STANDARD), AdaptivePracticePolicy.approvedDifficulties)
    }

    @Test
    fun `policy - adaptiveStatus returns PENDING`() {
        assertEquals(AdaptiveFeatureStatus.PENDING, AdaptivePracticePolicy.adaptiveStatus())
    }

    @Test
    fun `policy - validateDifficulty succeeds for STANDARD`() {
        val result = AdaptivePracticePolicy.validateDifficulty(PracticeDifficulty.STANDARD)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `policy - validateDifficulty fails for ADAPTIVE_PENDING`() {
        val result = AdaptivePracticePolicy.validateDifficulty(PracticeDifficulty.ADAPTIVE_PENDING)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `policy - validateConfiguration succeeds for standard config`() {
        val config = PracticeConfiguration.STANDARD
        val result = AdaptivePracticePolicy.validateConfiguration(config)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `policy - validateConfiguration fails for adaptive config`() {
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        val result = AdaptivePracticePolicy.validateConfiguration(config)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    // ============================================================================
    // PracticeConfiguration Adaptive Validation
    // ============================================================================

    @Test
    fun `configuration - isValid returns true for standard difficulty`() {
        val config = PracticeConfiguration.STANDARD
        assertTrue(config.isValid())
    }

    @Test
    fun `configuration - isValid returns false for adaptive pending difficulty`() {
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        assertFalse(config.isValid())
    }

    @Test
    fun `configuration - validate returns Success for standard difficulty`() {
        val config = PracticeConfiguration.STANDARD
        val result = config.validate()
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `configuration - validate returns Failure for adaptive pending difficulty`() {
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        val result = config.validate()
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `configuration - validate rejects empty challenge types even with standard difficulty`() {
        val config = PracticeConfiguration(challengeTypes = emptyList(), difficulty = PracticeDifficulty.STANDARD)
        val result = config.validate()
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `configuration - validate rejects adaptive difficulty even with valid challenge types`() {
        val config = PracticeConfiguration(
            challengeTypes = listOf(ChallengeType.SNAP),
            difficulty = PracticeDifficulty.ADAPTIVE_PENDING
        )
        val result = config.validate()
        assertTrue(result is DomainResult.Failure)
    }

    // ============================================================================
    // Edge Case 1: Practice Started — Adaptive Guard
    // ============================================================================

    @Test
    fun `edge case 1 - practice starts successfully with standard difficulty`() {
        val config = PracticeConfiguration.STANDARD
        val result = startPracticeUseCase.execute(userId, config, now = 1000L)
        assertTrue(result is DomainResult.Success)
        val session = (result as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.IN_PROGRESS, session.status)
        assertEquals(PracticeDifficulty.STANDARD, session.configuration.difficulty)
    }

    @Test
    fun `edge case 1 - practice start rejected with adaptive pending difficulty`() {
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        val result = startPracticeUseCase.execute(userId, config, now = 1000L)
        assertTrue(result is DomainResult.Failure)
        // No session should be created
        assertNull(practiceManager.activeSession.value)
    }

    @Test
    fun `edge case 1 - practice start with adaptive difficulty does not persist session`() {
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        startPracticeUseCase.execute(userId, config, now = 1000L)
        val sessions = practiceSessionRepository.getRecentPracticeSessions(userId)
        assertTrue(sessions.isEmpty())
    }

    // ============================================================================
    // Edge Case 2: Practice Interrupted — Adaptive Guard
    // ============================================================================

    @Test
    fun `edge case 2 - standard practice can be interrupted normally`() {
        val startResult = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(startResult is DomainResult.Success)

        val interruptResult = practiceManager.interruptPractice(reason = "user navigated away", now = 2000L)
        assertTrue(interruptResult is DomainResult.Success)
        val interrupted = (interruptResult as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.INTERRUPTED, interrupted.status)
        assertEquals(PracticeDifficulty.STANDARD, interrupted.configuration.difficulty)
    }

    @Test
    fun `edge case 2 - adaptive session cannot be started therefore cannot be interrupted`() {
        val startResult = startPracticeUseCase.execute(
            userId,
            PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING),
            now = 1000L
        )
        assertTrue(startResult is DomainResult.Failure)

        // No session to interrupt
        val interruptResult = practiceManager.interruptPractice(reason = "attempt", now = 2000L)
        assertTrue(interruptResult is DomainResult.Failure)
    }

    // ============================================================================
    // Edge Case 3: Practice Completed — Adaptive Guard
    // ============================================================================

    @Test
    fun `edge case 3 - standard practice completes and generates result`() {
        val startResult = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(startResult is DomainResult.Success)

        // Begin + Finish all 3 challenges, then complete
        practiceManager.beginChallenge(1, now = 1100L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SNAP, 200)
        practiceManager.finishChallenge(1, now = 1200L)
        practiceManager.continueNext(now = 1300L)

        practiceManager.beginChallenge(2, now = 1400L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 150)
        practiceManager.finishChallenge(2, now = 1500L)
        practiceManager.continueNext(now = 1600L)

        practiceManager.beginChallenge(3, now = 1700L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 100)
        practiceManager.finishChallenge(3, now = 1800L)

        val completeResult = practiceManager.completePractice(now = 2000L)
        assertTrue(completeResult is DomainResult.Success)

        val latestResult = practiceManager.getLatestPracticeResult()
        assertNotNull(latestResult)
        assertEquals(PracticeDifficulty.STANDARD, latestResult!!.difficulty)
        assertEquals(BattleMode.PRACTICE, latestResult.mode)
    }

    @Test
    fun `edge case 3 - adaptive practice cannot reach completion`() {
        val startResult = startPracticeUseCase.execute(
            userId,
            PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING),
            now = 1000L
        )
        assertTrue(startResult is DomainResult.Failure)

        val completeResult = practiceManager.completePractice(now = 2000L)
        assertTrue(completeResult is DomainResult.Failure)
        assertNull(practiceManager.getLatestPracticeResult())
    }

    // ============================================================================
    // Edge Case 4: Practice Repeated — Adaptive Guard
    // ============================================================================

    @Test
    fun `edge case 4 - repeated practice retains standard difficulty`() {
        val startResult = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(startResult is DomainResult.Success)

        // Complete the practice
        practiceManager.beginChallenge(1, now = 1100L)
        practiceManager.finishChallenge(1, now = 1200L)
        practiceManager.continueNext(now = 1300L)
        practiceManager.beginChallenge(2, now = 1400L)
        practiceManager.finishChallenge(2, now = 1500L)
        practiceManager.continueNext(now = 1600L)
        practiceManager.beginChallenge(3, now = 1700L)
        practiceManager.finishChallenge(3, now = 1800L)
        practiceManager.completePractice(now = 2000L)

        // Repeat
        val repeatResult = practiceManager.repeatPractice(now = 3000L)
        assertTrue(repeatResult is DomainResult.Success)
        val repeated = (repeatResult as DomainResult.Success).value
        assertEquals(PracticeDifficulty.STANDARD, repeated.configuration.difficulty)
        assertEquals(1, repeated.repeatCount)
    }

    @Test
    fun `edge case 4 - repeat does not silently change difficulty to adaptive`() {
        startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)

        // Complete the practice
        practiceManager.beginChallenge(1, now = 1100L)
        practiceManager.finishChallenge(1, now = 1200L)
        practiceManager.continueNext(now = 1300L)
        practiceManager.beginChallenge(2, now = 1400L)
        practiceManager.finishChallenge(2, now = 1500L)
        practiceManager.continueNext(now = 1600L)
        practiceManager.beginChallenge(3, now = 1700L)
        practiceManager.finishChallenge(3, now = 1800L)
        practiceManager.completePractice(now = 2000L)

        // Repeat multiple times
        for (i in 1..3) {
            val repeatResult = practiceManager.repeatPractice(now = (3000L + i * 1000))
            assertTrue(repeatResult is DomainResult.Success)
            val session = (repeatResult as DomainResult.Success).value
            assertEquals(PracticeDifficulty.STANDARD, session.configuration.difficulty)
            assertTrue(AdaptivePracticePolicy.isApproved(session.configuration.difficulty))
        }
    }

    // ============================================================================
    // Edge Case 5: Official Battle Active — Adaptive Practice Rejection
    // ============================================================================

    @Test
    fun `edge case 5 - adaptive practice rejected even with active official battle`() {
        // Seed an active official battle
        val officialSession = BattleSession(
            sessionId = BattleSessionId("official-session-1"),
            userId = userId,
            battleId = BattleId("battle-daily-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 500L,
            completedAt = null
        )
        officialSessionRepository.saveSession(officialSession)

        // Attempt adaptive practice — should fail at configuration validation
        val result = startPracticeUseCase.execute(
            userId,
            PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING),
            now = 1000L
        )
        assertTrue(result is DomainResult.Failure)

        // Official session unaffected
        val savedOfficial = officialSessionRepository.getSessionById(officialSession.sessionId)
        assertNotNull(savedOfficial)
        assertEquals(BattleSessionStatus.IN_PROGRESS, savedOfficial!!.status)
    }

    @Test
    fun `edge case 5 - standard practice allowed with active official battle`() {
        val officialSession = BattleSession(
            sessionId = BattleSessionId("official-session-2"),
            userId = userId,
            battleId = BattleId("battle-daily-2"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 500L,
            completedAt = null
        )
        officialSessionRepository.saveSession(officialSession)

        val result = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(result is DomainResult.Success)

        // Official session unaffected
        val savedOfficial = officialSessionRepository.getSessionById(officialSession.sessionId)
        assertNotNull(savedOfficial)
        assertEquals(BattleSessionStatus.IN_PROGRESS, savedOfficial!!.status)
    }

    // ============================================================================
    // Edge Case 6: Official Battle Completed — Adaptive Practice Rejection
    // ============================================================================

    @Test
    fun `edge case 6 - adaptive practice rejected even with completed official battle`() = runTest {
        val officialResult = BattleResult(
            resultId = BattleResultId("official-result-1"),
            userId = userId,
            sessionId = BattleSessionId("official-session-3"),
            battleId = BattleId("battle-daily-3"),
            snapScore = 250,
            shiftScore = 200,
            crowdCallScore = 180,
            totalScore = 630,
            consistencyScore = 0,
            percentile = 75.0,
            completedAt = 800L
        )
        officialResultRepository.saveResult(officialResult)

        val result = startPracticeUseCase.execute(
            userId,
            PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING),
            now = 1000L
        )
        assertTrue(result is DomainResult.Failure)

        // Official result unaffected
        val savedResults = officialResultRepository.getUserBattleResults(userId)
        assertTrue(savedResults is DomainResult.Success)
        val userResults = (savedResults as DomainResult.Success).value
        assertTrue(userResults.isNotEmpty())
        assertEquals(630, userResults.first().totalScore)
    }

    @Test
    fun `edge case 6 - completed official battle is not mutated by standard practice`() = runTest {
        val officialResult = BattleResult(
            resultId = BattleResultId("official-result-2"),
            userId = userId,
            sessionId = BattleSessionId("official-session-4"),
            battleId = BattleId("battle-daily-4"),
            snapScore = 250,
            shiftScore = 200,
            crowdCallScore = 180,
            totalScore = 630,
            consistencyScore = 0,
            percentile = 75.0,
            completedAt = 800L
        )
        officialResultRepository.saveResult(officialResult)

        // Start and complete standard practice
        startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        practiceManager.beginChallenge(1, now = 1100L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SNAP, 300)
        practiceManager.finishChallenge(1, now = 1200L)
        practiceManager.continueNext(now = 1300L)
        practiceManager.beginChallenge(2, now = 1400L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 300)
        practiceManager.finishChallenge(2, now = 1500L)
        practiceManager.continueNext(now = 1600L)
        practiceManager.beginChallenge(3, now = 1700L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 300)
        practiceManager.finishChallenge(3, now = 1800L)
        practiceManager.completePractice(now = 2000L)

        // Official result unchanged — even though practice scored higher
        val savedResults = officialResultRepository.getUserBattleResults(userId)
        assertTrue(savedResults is DomainResult.Success)
        val userResults = (savedResults as DomainResult.Success).value
        assertTrue(userResults.isNotEmpty())
        assertEquals(630, userResults.first().totalScore)
        assertEquals(250, userResults.first().snapScore)
    }

    // ============================================================================
    // Edge Case 7: Offline — Adaptive Guard Applies
    // ============================================================================

    @Test
    fun `edge case 7 - adaptive guard applies regardless of network state`() {
        // The adaptive policy is a pure domain check, independent of connectivity
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        val result = AdaptivePracticePolicy.validateConfiguration(config)
        assertTrue(result is DomainResult.Failure)

        // Standard config works offline
        val standardResult = AdaptivePracticePolicy.validateConfiguration(PracticeConfiguration.STANDARD)
        assertTrue(standardResult is DomainResult.Success)
    }

    @Test
    fun `edge case 7 - standard practice works in offline mode`() {
        val result = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(result is DomainResult.Success)
        val session = (result as DomainResult.Success).value
        assertEquals(PracticeDifficulty.STANDARD, session.configuration.difficulty)
    }

    // ============================================================================
    // Edge Case 8: No Practice Data — Adaptive Policy on Empty Repository
    // ============================================================================

    @Test
    fun `edge case 8 - adaptive policy rejects even on empty repository`() {
        val sessions = practiceSessionRepository.getRecentPracticeSessions(userId)
        assertTrue(sessions.isEmpty())

        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        val result = startPracticeUseCase.execute(userId, config, now = 1000L)
        assertTrue(result is DomainResult.Failure)

        // Repository still empty
        val sessionsAfter = practiceSessionRepository.getRecentPracticeSessions(userId)
        assertTrue(sessionsAfter.isEmpty())
    }

    @Test
    fun `edge case 8 - standard practice works on empty repository`() {
        val sessions = practiceSessionRepository.getRecentPracticeSessions(userId)
        assertTrue(sessions.isEmpty())

        val result = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(result is DomainResult.Success)
    }

    // ============================================================================
    // Edge Case 9: Invalid Practice Configuration — Adaptive Difficulty Is Invalid
    // ============================================================================

    @Test
    fun `edge case 9 - adaptive pending difficulty makes configuration invalid`() {
        val config = PracticeConfiguration(difficulty = PracticeDifficulty.ADAPTIVE_PENDING)
        assertFalse(config.isValid())
    }

    @Test
    fun `edge case 9 - empty challenges with adaptive difficulty is doubly invalid`() {
        val config = PracticeConfiguration(
            challengeTypes = emptyList(),
            difficulty = PracticeDifficulty.ADAPTIVE_PENDING
        )
        assertFalse(config.isValid())
        val result = config.validate()
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `edge case 9 - too many challenges with adaptive difficulty is doubly invalid`() {
        val config = PracticeConfiguration(
            challengeTypes = listOf(
                ChallengeType.SNAP, ChallengeType.SHIFT,
                ChallengeType.CROWD_CALL, ChallengeType.SNAP
            ),
            difficulty = PracticeDifficulty.ADAPTIVE_PENDING
        )
        assertFalse(config.isValid())
    }

    @Test
    fun `edge case 9 - single challenge with standard difficulty is valid`() {
        val config = PracticeConfiguration(
            challengeTypes = listOf(ChallengeType.SNAP),
            difficulty = PracticeDifficulty.STANDARD
        )
        assertTrue(config.isValid())
    }

    // ============================================================================
    // Critical Invariant: Practice must not mutate official battle results
    // ============================================================================

    @Test
    fun `critical invariant - practice mode is always PRACTICE`() {
        val startResult = startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(startResult is DomainResult.Success)
        val session = (startResult as DomainResult.Success).value
        assertEquals(BattleMode.PRACTICE, session.mode)
    }

    @Test
    fun `critical invariant - practice result mode is always PRACTICE`() {
        startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        practiceManager.beginChallenge(1, now = 1100L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SNAP, 100)
        practiceManager.finishChallenge(1, now = 1200L)
        practiceManager.continueNext(now = 1300L)
        practiceManager.beginChallenge(2, now = 1400L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 100)
        practiceManager.finishChallenge(2, now = 1500L)
        practiceManager.continueNext(now = 1600L)
        practiceManager.beginChallenge(3, now = 1700L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 100)
        practiceManager.finishChallenge(3, now = 1800L)
        practiceManager.completePractice(now = 2000L)

        val practiceResult = practiceManager.getLatestPracticeResult()
        assertNotNull(practiceResult)
        assertEquals(BattleMode.PRACTICE, practiceResult!!.mode)
    }

    @Test
    fun `critical invariant - official result repository not written by practice`() = runTest {
        startPracticeUseCase.execute(userId, PracticeConfiguration.STANDARD, now = 1000L)
        practiceManager.beginChallenge(1, now = 1100L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SNAP, 300)
        practiceManager.finishChallenge(1, now = 1200L)
        practiceManager.continueNext(now = 1300L)
        practiceManager.beginChallenge(2, now = 1400L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.SHIFT, 300)
        practiceManager.finishChallenge(2, now = 1500L)
        practiceManager.continueNext(now = 1600L)
        practiceManager.beginChallenge(3, now = 1700L)
        practiceManager.recordPracticeChallengeScore(ChallengeType.CROWD_CALL, 300)
        practiceManager.finishChallenge(3, now = 1800L)
        practiceManager.completePractice(now = 2000L)

        // Official repositories remain empty
        val officialResults = officialResultRepository.getUserBattleResults(userId)
        assertTrue(officialResults is DomainResult.Success)
        assertTrue((officialResults as DomainResult.Success).value.isEmpty())

        assertNull(officialSessionRepository.getSessionById(BattleSessionId("any")))
    }

    @Test
    fun `critical invariant - PracticeSession requires PRACTICE mode`() {
        // Attempting to create a PracticeSession with OFFICIAL mode should throw
        val thrown = org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            PracticeSession(
                sessionId = PracticeSessionId("test"),
                userId = userId,
                mode = BattleMode.OFFICIAL,
            )
        }
        assertTrue(thrown.message!!.contains("PRACTICE"))
    }

    // ============================================================================
    // AdaptiveFeatureStatus Enum Coverage
    // ============================================================================

    @Test
    fun `adaptive feature status - all values exist`() {
        val values = AdaptiveFeatureStatus.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(AdaptiveFeatureStatus.PENDING))
        assertTrue(values.contains(AdaptiveFeatureStatus.APPROVED))
        assertTrue(values.contains(AdaptiveFeatureStatus.REJECTED))
    }

    @Test
    fun `adaptive feature status - current status is PENDING`() {
        assertEquals(AdaptiveFeatureStatus.PENDING, AdaptivePracticePolicy.adaptiveStatus())
    }
}

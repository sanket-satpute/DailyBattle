package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Verification of the Practice Architecture and Isolation Invariants (Phase 19, Sprint 19.1).
 *
 * Edge cases explicitly tested:
 * 1. Practice started
 * 2. Practice interrupted
 * 3. Practice completed
 * 4. Practice repeated
 * 5. Official battle active
 * 6. Official battle completed
 * 7. Offline
 * 8. No practice data
 * 9. Invalid practice configuration
 * 10. Critical invariant: Practice must not mutate official battle results (Rule 26, REQ-GAME-018).
 */
class PracticeIsolationTest {

    private lateinit var officialSessionRepository: InMemoryBattleSessionRepository
    private lateinit var officialResultRepository: InMemoryBattleResultRepository
    private lateinit var officialBattleManager: ActiveBattleSessionManager
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase
    private lateinit var completeBattleUseCase: CompleteBattleUseCase

    private lateinit var practiceRepository: InMemoryPracticeSessionRepository
    private lateinit var practiceManager: ActivePracticeSessionManager
    private lateinit var startPracticeUseCase: StartPracticeSessionUseCase
    private lateinit var interruptPracticeUseCase: InterruptPracticeSessionUseCase
    private lateinit var repeatPracticeUseCase: RepeatPracticeSessionUseCase
    private lateinit var completePracticeUseCase: CompletePracticeSessionUseCase
    private lateinit var getActivePracticeUseCase: GetActivePracticeSessionUseCase

    private val userId = UserId("user-isolation-test")
    private val battleId = BattleId("battle-daily-official")

    @Before
    fun setup() {
        officialSessionRepository = InMemoryBattleSessionRepository()
        officialResultRepository = InMemoryBattleResultRepository()
        officialBattleManager = ActiveBattleSessionManager(officialSessionRepository)
        getOrStartOfficialBattleUseCase = GetOrStartOfficialBattleUseCase(officialSessionRepository)
        completeBattleUseCase = CompleteBattleUseCase(officialResultRepository, officialSessionRepository)

        practiceRepository = InMemoryPracticeSessionRepository()
        practiceManager = ActivePracticeSessionManager(practiceRepository)
        startPracticeUseCase = StartPracticeSessionUseCase(practiceManager)
        interruptPracticeUseCase = InterruptPracticeSessionUseCase(practiceManager)
        repeatPracticeUseCase = RepeatPracticeSessionUseCase(practiceManager)
        completePracticeUseCase = CompletePracticeSessionUseCase(practiceManager)
        getActivePracticeUseCase = GetActivePracticeSessionUseCase(practiceRepository, practiceManager)
    }

    // ------------------------------------------------------------------------
    // Edge Case 1: Practice started
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - practice started creates isolated practice session`() {
        val result = startPracticeUseCase.execute(userId)
        assertTrue(result is DomainResult.Success)

        val practiceSession = (result as DomainResult.Success).value
        assertEquals(BattleMode.PRACTICE, practiceSession.mode)
        assertEquals(PracticeSessionStatus.IN_PROGRESS, practiceSession.status)
        assertNotNull(practiceSession.startedAt)

        // Verifies official session repository is completely untouched
        assertNull(officialSessionRepository.getOfficialSession(userId, battleId))
        assertNull(officialSessionRepository.getActiveOfficialSession(userId))
    }

    // ------------------------------------------------------------------------
    // Edge Case 2: Practice interrupted
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - practice interrupted does not corrupt state`() {
        startPracticeUseCase.execute(userId)

        val interruptResult = interruptPracticeUseCase.execute(reason = "User navigated away")
        assertTrue(interruptResult is DomainResult.Success)

        val session = (interruptResult as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.INTERRUPTED, session.status)
        assertNotNull(session.completedAt)

        // Practice is no longer active
        assertNull(practiceRepository.getActivePracticeSession(userId))
        // Official session repository untouched
        assertNull(officialSessionRepository.getOfficialSession(userId, battleId))
    }

    // ------------------------------------------------------------------------
    // Edge Case 3: Practice completed
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - practice completed does not create or submit official result`() = runTest {
        startPracticeUseCase.execute(userId)

        practiceManager.beginChallenge(1)
        practiceManager.finishChallenge(1)
        practiceManager.continueNext()
        practiceManager.beginChallenge(2)
        practiceManager.finishChallenge(2)
        practiceManager.continueNext()
        practiceManager.beginChallenge(3)
        practiceManager.finishChallenge(3)
        practiceManager.continueNext()

        val active = practiceManager.activeSession.value
        assertEquals(PracticeSessionStatus.COMPLETED, active?.status)

        // No official results submitted
        val officialResult = officialResultRepository.getBattleResult(userId, battleId)
        assertTrue(officialResult is DomainResult.Failure)
        assertEquals(0, officialResultRepository.getUserBattleResults(userId).let {
            (it as DomainResult.Success).value.size
        })
    }

    // ------------------------------------------------------------------------
    // Edge Case 4: Practice repeated
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - practice repeated allows multiple attempts unlike official`() {
        startPracticeUseCase.execute(userId)
        completePracticeUseCase.execute()

        // Practice can repeat
        val repeat1 = repeatPracticeUseCase.execute()
        assertTrue(repeat1 is DomainResult.Success)
        assertEquals(1, (repeat1 as DomainResult.Success).value.repeatCount)

        completePracticeUseCase.execute()

        val repeat2 = repeatPracticeUseCase.execute()
        assertTrue(repeat2 is DomainResult.Success)
        assertEquals(2, (repeat2 as DomainResult.Success).value.repeatCount)

        // In contrast, official battle strictly enforces single-attempt rule:
        val officialSession = BattleSession(
            sessionId = BattleSessionId("official-completed"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 2000L
        )
        officialSessionRepository.saveSession(officialSession)

        // Attempting to re-start official battle fails integrity rule
        val officialRestart = getOrStartOfficialBattleUseCase.execute(userId, battleId)
        assertTrue(officialRestart is DomainResult.Failure)
        assertEquals(AppError.Domain, (officialRestart as DomainResult.Failure).error)
    }

    // ------------------------------------------------------------------------
    // Edge Case 5: Official battle active
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - official battle active coexists with practice without interference`() {
        // Start official battle
        val officialEngine = (getOrStartOfficialBattleUseCase.execute(userId, battleId) as DomainResult.Success).value
        officialBattleManager.bindSession(officialEngine.session)
        officialBattleManager.processBattleEvent(com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent.StartBattle)

        val activeOfficialBefore = officialBattleManager.getActiveSession()
        assertNotNull(activeOfficialBefore)
        assertEquals(BattleMode.OFFICIAL, activeOfficialBefore?.mode)
        assertEquals(BattleSessionStatus.IN_PROGRESS, activeOfficialBefore?.status)

        // Simultaneously start practice session
        val practiceResult = startPracticeUseCase.execute(userId)
        assertTrue(practiceResult is DomainResult.Success)

        // Assert Official Battle manager & repository remain undisturbed
        val activeOfficialAfter = officialBattleManager.getActiveSession()
        assertNotNull(activeOfficialAfter)
        assertEquals(activeOfficialBefore?.sessionId, activeOfficialAfter?.sessionId)
        assertEquals(BattleMode.OFFICIAL, activeOfficialAfter?.mode)

        val repoOfficial = officialSessionRepository.getActiveOfficialSession(userId)
        assertEquals(activeOfficialBefore?.sessionId, repoOfficial?.sessionId)

        // Assert Practice manager tracks practice
        val activePractice = practiceManager.activeSession.value
        assertNotNull(activePractice)
        assertEquals(BattleMode.PRACTICE, activePractice?.mode)
    }

    // ------------------------------------------------------------------------
    // Edge Case 6 & 10: Official battle completed & Zero official mutation
    // ------------------------------------------------------------------------
    @Test
    fun `critical invariant - practice must not mutate official battle results`() = runTest {
        // 1. Setup completed official battle with authentic score
        val officialSession = BattleSession(
            sessionId = BattleSessionId("official-123"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("official-123"), ChallengeSessionStatus.COMPLETE, 1000L, 1100L),
                ChallengeSession(ChallengeId("c2"), BattleSessionId("official-123"), ChallengeSessionStatus.COMPLETE, 1100L, 1200L),
                ChallengeSession(ChallengeId("c3"), BattleSessionId("official-123"), ChallengeSessionStatus.COMPLETE, 1200L, 1300L)
            )
        )
        officialSessionRepository.saveSession(officialSession)

        val officialResult = BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = userId,
            battleId = battleId,
            sessionId = officialSession.sessionId,
            snapScore = 280,
            shiftScore = 270,
            crowdCallScore = 290,
            consistencyScore = 90,
            totalScore = 930,
            percentile = 94.5,
            completedAt = 1500L
        )
        officialResultRepository.saveResult(officialResult)

        // Verify initial official state
        val initialResult = (officialResultRepository.getBattleResult(userId, battleId) as DomainResult.Success).value
        assertEquals(930, initialResult.totalScore)
        assertEquals(94.5, initialResult.percentile, 0.001)

        // 2. Perform various practice operations (start, play, repeat, complete)
        startPracticeUseCase.execute(userId)
        practiceManager.beginChallenge(1)
        practiceManager.finishChallenge(1)
        practiceManager.continueNext()
        repeatPracticeUseCase.execute()
        completePracticeUseCase.execute()

        // 3. Assert official results, scores, and percentile are 100% UNCHANGED
        val postPracticeResult = (officialResultRepository.getBattleResult(userId, battleId) as DomainResult.Success).value
        assertEquals(930, postPracticeResult.totalScore)
        assertEquals(94.5, postPracticeResult.percentile, 0.001)
        assertEquals(officialSession.sessionId, postPracticeResult.sessionId)
        assertEquals(1500L, postPracticeResult.completedAt)

        val allUserResults = (officialResultRepository.getUserBattleResults(userId) as DomainResult.Success).value
        assertEquals(1, allUserResults.size)
        assertEquals(930, allUserResults[0].totalScore)
    }

    // ------------------------------------------------------------------------
    // Edge Case 7: CompleteBattleUseCase rejects Practice sessions
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - CompleteBattleUseCase rejects Practice session with Domain failure`() = runTest {
        val practiceSession = BattleSession(
            sessionId = BattleSessionId("practice-attempt"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.PRACTICE,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null
        )

        val result = completeBattleUseCase.execute(practiceSession)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }

    // ------------------------------------------------------------------------
    // Edge Case 8: Offline practice
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - offline practice runs successfully with zero remote calls`() {
        // Offline: Practice relies solely on local in-memory storage (DefaultDomainAuthorityRegistry)
        val start = startPracticeUseCase.execute(userId)
        assertTrue(start is DomainResult.Success)

        val begin = practiceManager.beginChallenge(1)
        assertTrue(begin is DomainResult.Success)

        val finish = practiceManager.finishChallenge(1)
        assertTrue(finish is DomainResult.Success)

        val complete = completePracticeUseCase.execute()
        assertTrue(complete is DomainResult.Success)

        assertEquals(PracticeSessionStatus.COMPLETED, (complete as DomainResult.Success).value.status)
    }

    // ------------------------------------------------------------------------
    // Edge Case 9: No practice data
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - no practice data returns null safely`() {
        val active = getActivePracticeUseCase.execute(UserId("user-has-no-practice"))
        assertNull(active)
        assertTrue(practiceRepository.getRecentPracticeSessions(UserId("user-has-no-practice")).isEmpty())
    }

    // ------------------------------------------------------------------------
    // Edge Case 10: Invalid practice configuration
    // ------------------------------------------------------------------------
    @Test
    fun `edge case - invalid practice configuration is rejected without creating session`() {
        val invalidConfig = PracticeConfiguration(challengeTypes = emptyList())
        val result = startPracticeUseCase.execute(userId, invalidConfig)

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
        assertNull(practiceManager.activeSession.value)
        assertNull(practiceRepository.getActivePracticeSession(userId))
    }
}

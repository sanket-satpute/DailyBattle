package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeEvent
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeState
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest

/**
 * Verification of Practice Gameplay (Phase 19, Sprint 19.2):
 * - Reusing challenge infrastructure safely.
 * - Integration of ChallengeStateMachine (Ready -> Active -> Correct/Incorrect -> Complete).
 * - Immediate feedback per REQ-GAME-016.
 * - ChallengeType tracking.
 * - Safe score clamping and aggregation (Consistency = 0).
 * - Out-of-order event rejection.
 * - Total isolation from Official Battle gameplay.
 */
class PracticeGameplayTest {

    private lateinit var practiceRepository: InMemoryPracticeSessionRepository
    private lateinit var practiceManager: ActivePracticeSessionManager
    private lateinit var processChallengeEventUseCase: ProcessPracticeChallengeEventUseCase
    private lateinit var recordChallengeResultUseCase: RecordPracticeChallengeResultUseCase
    private lateinit var getGameplayStateUseCase: GetPracticeGameplayStateUseCase

    private lateinit var officialSessionRepository: InMemoryBattleSessionRepository
    private lateinit var officialResultRepository: InMemoryBattleResultRepository
    private lateinit var officialBattleManager: ActiveBattleSessionManager
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase

    private val userId = UserId("user-gameplay-test")
    private val battleId = BattleId("battle-daily-official")

    @Before
    fun setup() {
        practiceRepository = InMemoryPracticeSessionRepository()
        practiceManager = ActivePracticeSessionManager(practiceRepository)
        processChallengeEventUseCase = ProcessPracticeChallengeEventUseCase(practiceManager)
        recordChallengeResultUseCase = RecordPracticeChallengeResultUseCase(practiceManager)
        getGameplayStateUseCase = GetPracticeGameplayStateUseCase(practiceManager)

        officialSessionRepository = InMemoryBattleSessionRepository()
        officialResultRepository = InMemoryBattleResultRepository()
        officialBattleManager = ActiveBattleSessionManager(officialSessionRepository)
        getOrStartOfficialBattleUseCase = GetOrStartOfficialBattleUseCase(officialSessionRepository)
    }

    @Test
    fun `reusing challenge state machine safely through practice challenge lifecycle`() {
        practiceManager.startPractice(userId, PracticeConfiguration.STANDARD)

        // Challenge 1 begins: Snap
        assertEquals(ChallengeType.SNAP, practiceManager.currentChallengeType.value)
        assertEquals(ChallengeState.Ready, practiceManager.currentChallengeState.value)

        // 1. Start Challenge -> Active
        val startResult = processChallengeEventUseCase.execute(ChallengeEvent.Start)
        assertTrue(startResult is DomainResult.Success)
        assertEquals(ChallengeState.Active, practiceManager.currentChallengeState.value)
        assertEquals(ChallengeSessionStatus.ACTIVE, practiceManager.activeSession.value?.challengeSessions?.get(0)?.status)

        // 2. Submit Correct -> Immediate feedback (REQ-GAME-016)
        val correctResult = processChallengeEventUseCase.execute(ChallengeEvent.SubmitCorrect)
        assertTrue(correctResult is DomainResult.Success)
        assertEquals(ChallengeState.Correct, practiceManager.currentChallengeState.value)
        assertEquals(ChallengeSessionStatus.CORRECT, practiceManager.activeSession.value?.challengeSessions?.get(0)?.status)

        // 3. Continue -> Back to Active for subsequent input
        val continueResult = processChallengeEventUseCase.execute(ChallengeEvent.Continue)
        assertTrue(continueResult is DomainResult.Success)
        assertEquals(ChallengeState.Active, practiceManager.currentChallengeState.value)

        // 4. Submit Incorrect -> Immediate feedback (REQ-GAME-016)
        val incorrectResult = processChallengeEventUseCase.execute(ChallengeEvent.SubmitIncorrect)
        assertTrue(incorrectResult is DomainResult.Success)
        assertEquals(ChallengeState.Incorrect, practiceManager.currentChallengeState.value)
        assertEquals(ChallengeSessionStatus.INCORRECT, practiceManager.activeSession.value?.challengeSessions?.get(0)?.status)

        // 5. Finish Challenge -> Complete
        val finishResult = processChallengeEventUseCase.execute(ChallengeEvent.Finish)
        assertTrue(finishResult is DomainResult.Success)
        assertEquals(ChallengeState.Complete, practiceManager.currentChallengeState.value)
        assertEquals(ChallengeSessionStatus.COMPLETE, practiceManager.activeSession.value?.challengeSessions?.get(0)?.status)

        // Record Snap practice score
        val recordSnap = recordChallengeResultUseCase.execute(ChallengeType.SNAP, 285)
        assertTrue(recordSnap is DomainResult.Success)
        assertEquals(285, (recordSnap as DomainResult.Success).value.score)
    }

    @Test
    fun `transition across all three challenges in standard practice sequence`() {
        practiceManager.startPractice(userId, PracticeConfiguration.STANDARD)

        // Challenge 1: Snap
        assertEquals(ChallengeType.SNAP, practiceManager.currentChallengeType.value)
        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        processChallengeEventUseCase.execute(ChallengeEvent.Finish)
        recordChallengeResultUseCase.execute(ChallengeType.SNAP, 270)

        // Transition to Challenge 2: Shift
        practiceManager.continueNext()
        assertEquals(2, practiceManager.activeSession.value?.currentChallengeIndex)
        assertEquals(ChallengeType.SHIFT, practiceManager.currentChallengeType.value)
        assertEquals(ChallengeState.Ready, practiceManager.currentChallengeState.value)

        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        processChallengeEventUseCase.execute(ChallengeEvent.Finish)
        recordChallengeResultUseCase.execute(ChallengeType.SHIFT, 250)

        // Transition to Challenge 3: Crowd Call
        practiceManager.continueNext()
        assertEquals(3, practiceManager.activeSession.value?.currentChallengeIndex)
        assertEquals(ChallengeType.CROWD_CALL, practiceManager.currentChallengeType.value)
        assertEquals(ChallengeState.Ready, practiceManager.currentChallengeState.value)

        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        processChallengeEventUseCase.execute(ChallengeEvent.Finish)
        recordChallengeResultUseCase.execute(ChallengeType.CROWD_CALL, 280)

        // Finish all -> Complete
        practiceManager.continueNext()
        assertEquals(PracticeSessionStatus.COMPLETED, practiceManager.activeSession.value?.status)
        assertNull(practiceManager.currentChallengeState.value)

        // Check aggregated score: Snap(270) + Shift(250) + CrowdCall(280) + Consistency(0) = 800
        val aggScore = practiceManager.getAggregatedPracticeScore()
        assertNotNull(aggScore)
        assertEquals(270, aggScore?.snapScore)
        assertEquals(250, aggScore?.shiftScore)
        assertEquals(280, aggScore?.crowdCallScore)
        assertEquals(0, aggScore?.consistencyScore) // Consistency is strictly 0 in practice mode
        assertEquals(800, aggScore?.totalScore)
    }

    @Test
    fun `out-of-order challenge events are rejected safely`() {
        practiceManager.startPractice(userId, PracticeConfiguration.STANDARD)
        // Challenge is currently Ready

        // Submitting answer before Starting fails
        val submitBeforeStart = processChallengeEventUseCase.execute(ChallengeEvent.SubmitCorrect)
        assertTrue(submitBeforeStart is DomainResult.Failure)
        assertEquals(AppError.Domain, (submitBeforeStart as DomainResult.Failure).error)
        assertEquals(ChallengeState.Ready, practiceManager.currentChallengeState.value)

        // Start challenge
        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        assertEquals(ChallengeState.Active, practiceManager.currentChallengeState.value)

        // Duplicate Start fails
        val duplicateStart = processChallengeEventUseCase.execute(ChallengeEvent.Start)
        assertTrue(duplicateStart is DomainResult.Failure)

        // Finish challenge
        processChallengeEventUseCase.execute(ChallengeEvent.Finish)
        assertEquals(ChallengeState.Complete, practiceManager.currentChallengeState.value)

        // Submitting answer after Finish fails
        val submitAfterFinish = processChallengeEventUseCase.execute(ChallengeEvent.SubmitCorrect)
        assertTrue(submitAfterFinish is DomainResult.Failure)
    }

    @Test
    fun `challenge score validator clamps scores exceeding boundaries in practice`() {
        practiceManager.startPractice(userId)

        // Score > 300 is clamped to 300
        val overResult = recordChallengeResultUseCase.execute(ChallengeType.SNAP, 350)
        assertTrue(overResult is DomainResult.Success)
        assertEquals(300, (overResult as DomainResult.Success).value.score)

        // Negative score is clamped to 0
        val negativeResult = recordChallengeResultUseCase.execute(ChallengeType.SHIFT, -50)
        assertTrue(negativeResult is DomainResult.Success)
        assertEquals(0, (negativeResult as DomainResult.Success).value.score)
    }

    @Test
    fun `practice gameplay state reflects active session and scoring reactively`() {
        practiceManager.startPractice(userId)
        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        recordChallengeResultUseCase.execute(ChallengeType.SNAP, 200)

        val state = getGameplayStateUseCase.execute()
        assertNotNull(state.session)
        assertEquals(1, state.currentChallengeIndex)
        assertEquals(3, state.totalChallenges)
        assertEquals(ChallengeType.SNAP, state.currentChallengeType)
        assertEquals(ChallengeState.Active, state.challengeState)
        assertEquals(1, state.challengeResults.size)
        assertEquals(200, state.challengeResults[ChallengeType.SNAP]?.score)
        assertEquals(200, state.aggregatedScore?.totalScore)
        assertEquals(false, state.isPracticeComplete)
        assertEquals(false, state.isInterrupted)
    }

    @Test
    fun `interruption during active challenge gameplay cleans up challenge state`() {
        practiceManager.startPractice(userId)
        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        assertEquals(ChallengeState.Active, practiceManager.currentChallengeState.value)

        val interruptResult = practiceManager.interruptPractice("User closed app")
        assertTrue(interruptResult is DomainResult.Success)

        assertEquals(PracticeSessionStatus.INTERRUPTED, practiceManager.activeSession.value?.status)
        assertNull(practiceManager.currentChallengeState.value)
    }

    @Test
    fun `practice gameplay events do not contaminate official gameplay engine`() = runTest {
        // Start official battle in official manager
        val officialEngine = (getOrStartOfficialBattleUseCase.execute(userId, battleId) as DomainResult.Success).value
        officialBattleManager.bindSession(officialEngine.session)
        officialBattleManager.processBattleEvent(BattleEvent.StartBattle)
        officialBattleManager.processBattleEvent(BattleEvent.BeginChallenge)
        officialBattleManager.processChallengeEvent(ChallengeEvent.Start)

        val officialBefore = officialBattleManager.getActiveSession()
        assertEquals(BattleMode.OFFICIAL, officialBefore?.mode)
        assertEquals(ChallengeSessionStatus.ACTIVE, officialBefore?.challengeSessions?.get(0)?.status)

        // Play full practice gameplay
        practiceManager.startPractice(userId)
        processChallengeEventUseCase.execute(ChallengeEvent.Start)
        processChallengeEventUseCase.execute(ChallengeEvent.SubmitCorrect)
        processChallengeEventUseCase.execute(ChallengeEvent.Finish)
        recordChallengeResultUseCase.execute(ChallengeType.SNAP, 300)

        // Assert Official Battle remains in its own active state undisturbed
        val officialAfter = officialBattleManager.getActiveSession()
        assertEquals(BattleMode.OFFICIAL, officialAfter?.mode)
        assertEquals(officialBefore?.sessionId, officialAfter?.sessionId)
        assertEquals(ChallengeSessionStatus.ACTIVE, officialAfter?.challengeSessions?.get(0)?.status)

        // Assert no official battle results were produced
        val officialResult = officialResultRepository.getBattleResult(userId, battleId)
        assertTrue(officialResult is DomainResult.Failure)
    }
}

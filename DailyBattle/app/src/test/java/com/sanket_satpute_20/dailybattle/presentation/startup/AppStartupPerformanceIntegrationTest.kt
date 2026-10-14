package com.sanket_satpute_20.dailybattle.presentation.startup

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.lifecycle.InterruptedSubmissionRecoverer
import com.sanket_satpute_20.dailybattle.core.lifecycle.InterruptedSubmissionRecoveryReport
import com.sanket_satpute_20.dailybattle.core.lifecycle.ProcessDeathRecoverer
import com.sanket_satpute_20.dailybattle.core.lifecycle.ProcessDeathRecoveryResult
import com.sanket_satpute_20.dailybattle.core.performance.StartupMetrics
import com.sanket_satpute_20.dailybattle.core.performance.StartupPerformanceTracker
import com.sanket_satpute_20.dailybattle.domain.battle.*
import com.sanket_satpute_20.dailybattle.domain.identifier.*
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration test verifying that [AppStartupViewModel] interacts cleanly with
 * [StartupPerformanceTracker] on application launch (Sprint 24.1).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppStartupPerformanceIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    private class RecordingStartupPerformanceTracker : StartupPerformanceTracker {
        private val _metrics = MutableStateFlow(StartupMetrics())
        override val metrics: StateFlow<StartupMetrics> = _metrics.asStateFlow()

        var coldStartCompletedCalled = false
        var firstScreenRenderCompletedCalled = false
        var recordedScreenName: String? = null

        override fun recordProcessStart(timestampMs: Long?) {}

        override fun recordColdStartCompleted(timestampMs: Long?): Long? {
            coldStartCompletedCalled = true
            _metrics.update { it.copy(isColdStartCompleted = true, coldStartDurationMs = 250L) }
            return 250L
        }

        override fun recordWarmStartBegin(timestampMs: Long?) {}
        override fun recordWarmStartCompleted(timestampMs: Long?): Long? = null
        override fun recordFirstScreenRenderBegin(screenName: String, timestampMs: Long?) {}

        override fun recordFirstScreenRenderCompleted(screenName: String, timestampMs: Long?): Long? {
            firstScreenRenderCompletedCalled = true
            recordedScreenName = screenName
            _metrics.update {
                it.copy(
                    isFirstScreenRendered = true,
                    firstScreenName = screenName,
                    firstScreenRenderDurationMs = 120L
                )
            }
            return 120L
        }

        override fun resetForTesting() {
            coldStartCompletedCalled = false
            firstScreenRenderCompletedCalled = false
            recordedScreenName = null
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startup view model records cold start completion and first screen rendering`() = runTest {
        val fakeSessionRepo = object : BattleSessionRepository {
            override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? = null
            override fun saveSession(session: BattleSession) {}
            override fun getUnacknowledgedSessions(): List<BattleSession> = emptyList()
        }
        val fakeResultRepo = object : BattleResultRepository {
            override suspend fun completeBattle(sessionId: BattleSessionId, clientRequestId: String): DomainResult<BattleResult> =
                DomainResult.Failure(AppError.Domain)
            override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> =
                DomainResult.Failure(AppError.Domain)
            override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> =
                DomainResult.Success(emptyList())
        }
        val completeBattleUseCase = CompleteBattleUseCase(fakeResultRepo, fakeSessionRepo)
        val recoverUseCase = RecoverUnacknowledgedResultUseCase(fakeSessionRepo, completeBattleUseCase)

        val fakeProcessDeathRecoverer = object : ProcessDeathRecoverer {
            override fun recoverBattleAfterProcessDeath(userId: UserId, battleId: BattleId?): ProcessDeathRecoveryResult =
                ProcessDeathRecoveryResult.NoActiveBattle
        }
        val fakeInterruptedSubmissionRecoverer = object : InterruptedSubmissionRecoverer {
            override suspend fun recoverInterruptedSubmissions(userId: UserId): DomainResult<InterruptedSubmissionRecoveryReport> =
                DomainResult.Success(
                    InterruptedSubmissionRecoveryReport(
                        recoveredSessions = emptyList(),
                        reconciledResults = emptyList(),
                        recoveredQueueOperations = emptyList(),
                    )
                )
        }

        val tracker = RecordingStartupPerformanceTracker()
        val viewModel = AppStartupViewModel(
            recoverUnacknowledgedResultUseCase = recoverUseCase,
            processDeathRecoverer = fakeProcessDeathRecoverer,
            interruptedSubmissionRecoverer = fakeInterruptedSubmissionRecoverer,
            startupPerformanceTracker = tracker,
        )

        // Advance coroutines in viewModelScope
        advanceUntilIdle()

        assertTrue("Cold start completed must be recorded by AppStartupViewModel", tracker.coldStartCompletedCalled)
        assertTrue(tracker.metrics.value.isColdStartCompleted)
        assertEquals(ScreenState.Success(Unit), viewModel.state.value)

        // Simulate first screen composition
        viewModel.onFirstScreenRendered("Startup")
        assertTrue("First screen render must be recorded", tracker.firstScreenRenderCompletedCalled)
        assertEquals("Startup", tracker.recordedScreenName)
        assertTrue(tracker.metrics.value.isFirstScreenRendered)
    }

    @Test
    fun `startup view model backward-compatibility constructor uses NoOp tracker safely`() = runTest {
        val fakeSessionRepo = object : BattleSessionRepository {
            override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? = null
            override fun saveSession(session: BattleSession) {}
            override fun getUnacknowledgedSessions(): List<BattleSession> = emptyList()
        }
        val fakeResultRepo = object : BattleResultRepository {
            override suspend fun completeBattle(sessionId: BattleSessionId, clientRequestId: String): DomainResult<BattleResult> =
                DomainResult.Failure(AppError.Domain)
            override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> =
                DomainResult.Failure(AppError.Domain)
            override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> =
                DomainResult.Success(emptyList())
        }
        val completeBattleUseCase = CompleteBattleUseCase(fakeResultRepo, fakeSessionRepo)
        val recoverUseCase = RecoverUnacknowledgedResultUseCase(fakeSessionRepo, completeBattleUseCase)

        val fakeProcessDeathRecoverer = object : ProcessDeathRecoverer {
            override fun recoverBattleAfterProcessDeath(userId: UserId, battleId: BattleId?): ProcessDeathRecoveryResult =
                ProcessDeathRecoveryResult.NoActiveBattle
        }
        val fakeInterruptedSubmissionRecoverer = object : InterruptedSubmissionRecoverer {
            override suspend fun recoverInterruptedSubmissions(userId: UserId): DomainResult<InterruptedSubmissionRecoveryReport> =
                DomainResult.Success(
                    InterruptedSubmissionRecoveryReport(
                        recoveredSessions = emptyList(),
                        reconciledResults = emptyList(),
                        recoveredQueueOperations = emptyList(),
                    )
                )
        }

        // Call 3-arg constructor
        val viewModel = AppStartupViewModel(
            recoverUnacknowledgedResultUseCase = recoverUseCase,
            processDeathRecoverer = fakeProcessDeathRecoverer,
            interruptedSubmissionRecoverer = fakeInterruptedSubmissionRecoverer,
        )

        advanceUntilIdle()
        assertEquals(ScreenState.Success(Unit), viewModel.state.value)
        // Ensure onFirstScreenRendered doesn't throw with NoOp tracker
        viewModel.onFirstScreenRendered("Startup")
    }
}

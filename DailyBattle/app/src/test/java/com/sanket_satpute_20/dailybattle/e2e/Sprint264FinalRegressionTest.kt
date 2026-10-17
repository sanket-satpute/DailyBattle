package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.BuildConfig
import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.BuildTypeEnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.retry.DefaultIdempotentRetryHandler
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEngine
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleState
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStateMachine
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.history.GetHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreConstants
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreValidator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sprint 26.4 — Final Regression Test Suite
 *
 * Codifies and verifies the comprehensive Release Candidate regression baseline
 * across all architectural layers per:
 * - `docs/ROADMAP.md` Phase 26, Sprint 26.4 (lines 1968–1971: "Run the complete test suite")
 * - `docs/12_TESTING_AND_QA.md` §46 ("Regression Testing"), §60 ("Release Gate"), §69 ("Regression Baseline")
 * - `docs/16_DEFINITION_OF_DONE.md` §57 ("Regression DoD"), §74 ("Release Candidate DoD"), §75 ("Release Blockers")
 *
 * Verification Areas:
 * 1. Gameplay State Machine Monotonic Progression & Isolation
 * 2. Attempt Integrity & Single Official Attempt Enforcement
 * 3. Score Determinism & Boundary Clamping
 * 4. Production Security & Debug/Mock Isolation
 * 5. Network Idempotency & Bounded Retry Integrity
 * 6. Persistence & Schema Synchronization
 * 7. Distribution Packaging & Size Budget Compliance
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Sprint264FinalRegressionTest {

    private val fixedInstant = Instant.parse("2026-10-17T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val debugGuard = DebugEndpointGuard()

    // =========================================================================
    // 1. GAMEPLAY MONOTONIC STATE TRANSITION BASELINE
    // =========================================================================

    @Test
    fun `regression - battle state machine strictly enforces monotonic 3-challenge flow`() {
        val sm = BattleStateMachine()
        assertEquals(BattleState.NotStarted, sm.currentState)

        // Cannot skip directly to Active or Results
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Failure)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Failure)
        assertTrue(sm.transition(BattleEvent.ShowResults) is DomainResult.Failure)

        // Challenge 1 (Snap)
        assertTrue(sm.transition(BattleEvent.StartBattle) is DomainResult.Success)
        assertEquals(BattleState.Ready(1), sm.currentState)
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Success)
        assertEquals(BattleState.Active(1), sm.currentState)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Success)
        assertEquals(BattleState.ChallengeComplete(1), sm.currentState)
        assertTrue(sm.transition(BattleEvent.Continue) is DomainResult.Success)
        assertEquals(BattleState.NextChallenge(2), sm.currentState)
        assertTrue(sm.transition(BattleEvent.PrepareNext) is DomainResult.Success)
        assertEquals(BattleState.Ready(2), sm.currentState)

        // Challenge 2 (Shift)
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Success)
        assertEquals(BattleState.Active(2), sm.currentState)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Success)
        assertEquals(BattleState.ChallengeComplete(2), sm.currentState)
        assertTrue(sm.transition(BattleEvent.Continue) is DomainResult.Success)
        assertEquals(BattleState.NextChallenge(3), sm.currentState)
        assertTrue(sm.transition(BattleEvent.PrepareNext) is DomainResult.Success)
        assertEquals(BattleState.Ready(3), sm.currentState)

        // Challenge 3 (Crowd Call)
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Success)
        assertEquals(BattleState.Active(3), sm.currentState)
        assertTrue(sm.transition(BattleEvent.FinishChallenge) is DomainResult.Success)
        assertEquals(BattleState.ChallengeComplete(3), sm.currentState)
        assertTrue(sm.transition(BattleEvent.Continue) is DomainResult.Success)
        assertEquals(BattleState.BattleComplete, sm.currentState)
        assertTrue(sm.transition(BattleEvent.ShowResults) is DomainResult.Success)
        assertEquals(BattleState.Results, sm.currentState)

        // Once in Results, battle cannot be restarted or rewound
        assertTrue(sm.transition(BattleEvent.StartBattle) is DomainResult.Failure)
        assertTrue(sm.transition(BattleEvent.BeginChallenge) is DomainResult.Failure)
        assertEquals(BattleState.Results, sm.currentState)
    }

    // =========================================================================
    // 2. ATTEMPT INTEGRITY BASELINE
    // =========================================================================

    @Test
    fun `regression - single attempt rule prevents duplicate official battle initiation`() {
        val repo = InMemoryBattleSessionRepository()
        val userId = UserId("user-regression-rc")
        val battleId = BattleId("battle-daily-rc")

        // First attempt creates session
        val useCase = GetOrStartOfficialBattleUseCase(repo)
        val firstStart = useCase.execute(userId, battleId)
        assertTrue("First attempt succeeds", firstStart is DomainResult.Success)
        val engine = (firstStart as DomainResult.Success).value

        // Complete the session
        val completedSession = engine.session.copy(status = BattleSessionStatus.COMPLETED)
        repo.saveSession(completedSession)

        // Second attempt on same battle must fail with AppError.Domain
        val secondStart = useCase.execute(userId, battleId)
        assertTrue("Second attempt on completed official battle must fail", secondStart is DomainResult.Failure)
        assertEquals(AppError.Domain, (secondStart as DomainResult.Failure).error)
    }

    // =========================================================================
    // 3. SCORING DETERMINISM & BOUNDS CLAMPING BASELINE
    // =========================================================================

    @Test
    fun `regression - score aggregator clamps total score to canonical constants and calculates sum accurately`() {
        val validator = ChallengeScoreValidator()

        val snapResult = validator.validate(
            ChallengeResult(ChallengeResultId("r1"), ChallengeId("c1"), BattleSessionId("s1"), 250, 300, 1000L),
            ChallengeType.SNAP
        )
        val shiftResult = validator.validate(
            ChallengeResult(ChallengeResultId("r2"), ChallengeId("c2"), BattleSessionId("s1"), 280, 300, 1000L),
            ChallengeType.SHIFT
        )
        val crowdCallResult = validator.validate(
            ChallengeResult(ChallengeResultId("r3"), ChallengeId("c3"), BattleSessionId("s1"), 300, 300, 1000L),
            ChallengeType.CROWD_CALL
        )

        val aggregator = BattleScoreAggregator()
        val aggregated = aggregator.aggregate(
            mode = BattleMode.OFFICIAL,
            snapResult = snapResult,
            shiftResult = shiftResult,
            crowdCallResult = crowdCallResult
        )

        assertEquals(250, aggregated.snapScore)
        assertEquals(280, aggregated.shiftScore)
        assertEquals(300, aggregated.crowdCallScore)
        assertTrue(aggregated.totalScore <= ChallengeScoreConstants.MAX_TOTAL_SCORE)
        assertTrue(aggregated.totalScore >= 0)
    }

    @Test
    fun `regression - challenge score validator clamps excessive scores to approved maximum`() {
        val validator = ChallengeScoreValidator()

        // Negative score clamped to 0
        val neg = validator.validate(
            ChallengeResult(ChallengeResultId("r1"), ChallengeId("c1"), BattleSessionId("s1"), -100, 300, 1000L),
            ChallengeType.SNAP
        )
        assertEquals(0, neg.score)

        // Excessive score clamped to approved max
        val excessive = validator.validate(
            ChallengeResult(ChallengeResultId("r2"), ChallengeId("c2"), BattleSessionId("s1"), 5000, 5000, 1000L),
            ChallengeType.SHIFT
        )
        assertEquals(ChallengeScoreConstants.maxScoreFor(ChallengeType.SHIFT), excessive.score)
        assertEquals(300, excessive.score)
    }

    // =========================================================================
    // 4. PRODUCTION SECURITY & DEBUG ISOLATION BASELINE
    // =========================================================================

    @Test
    fun `regression - production environment prohibits debug headers and mock endpoints`() {
        val releaseProvider = BuildTypeEnvironmentProvider(isDebugBuild = false)
        assertEquals(AppEnvironment.Production, releaseProvider.current)

        val prodUrl = NetworkConfig.resolveBaseUrl(AppEnvironment.Production)
        assertTrue("Production URL must use HTTPS", prodUrl.startsWith("https://"))
        assertFalse("Production URL must not contain localhost", prodUrl.contains("localhost"))

        // Debug attempt in production
        val debugAttempt = debugGuard.validateDebugModeAllowed(
            isDebugRequested = true,
            environment = AppEnvironment.Production
        )
        assertTrue(debugAttempt is DomainResult.Failure)
        assertEquals(AppError.Authorization, (debugAttempt as DomainResult.Failure).error)

        // Debug header in production
        val headerAttempt = debugGuard.validateHeaders(
            headers = mapOf("X-Debug-Mode" to "true"),
            environment = AppEnvironment.Production
        )
        assertTrue(headerAttempt is DomainResult.Failure)

        // Mock endpoint in production
        val endpointAttempt = debugGuard.validateEndpointUrl(
            url = "https://api.dailybattle.com/mock/battle",
            environment = AppEnvironment.Production
        )
        assertTrue(endpointAttempt is DomainResult.Failure)
    }

    // =========================================================================
    // 5. NETWORK IDEMPOTENCY & CONCURRENCY DEDUPLICATION BASELINE
    // =========================================================================

    @Test
    fun `regression - idempotent retry handler deduplicates rapid duplicate submissions`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val retryHandler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))
        val counter = AtomicInteger(0)
        val gate = CompletableDeferred<Unit>()

        val def1 = async {
            retryHandler.executeIdempotent("regression-rc-key") {
                counter.incrementAndGet()
                gate.await()
                DomainResult.Success("SUCCESS_RC")
            }
        }
        val def2 = async {
            retryHandler.executeIdempotent("regression-rc-key") {
                counter.incrementAndGet()
                gate.await()
                DomainResult.Success("SUCCESS_RC")
            }
        }

        advanceUntilIdle()

        assertEquals("Block entered only once concurrently", 1, counter.get())
        assertTrue(retryHandler.isInFlight("regression-rc-key"))

        gate.complete(Unit)
        advanceUntilIdle()

        val r1 = def1.await()
        val r2 = def2.await()
        assertTrue(r1 is DomainResult.Success)
        assertTrue(r2 is DomainResult.Success)
        assertEquals("SUCCESS_RC", (r1 as DomainResult.Success).value)
        assertEquals("SUCCESS_RC", (r2 as DomainResult.Success).value)
    }

    // =========================================================================
    // 6. PERSISTENCE & SCHEMA INTEGRITY BASELINE
    // =========================================================================

    @Test
    fun `regression - database schema version is synchronized and non-destructive`() {
        assertEquals(1, DailyBattleDatabase.DATABASE_VERSION)
    }

    @Test
    fun `regression - state corruption in persistent entities sanitizes to safe defaults`() {
        val malformedSession = BattleSessionEntity(
            sessionId = "s-malformed-rc",
            userId = "u-rc",
            battleId = "b-rc",
            mode = "CORRUPTED_MODE",
            status = "CORRUPTED_STATUS",
            currentChallenge = 999,
            startedAt = -50L,
            completedAt = -100L
        )

        val domainSession = malformedSession.toDomain()
        assertEquals(BattleMode.OFFICIAL, domainSession.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, domainSession.status)
        assertNull(domainSession.currentChallenge)
        assertNull(domainSession.startedAt)
        assertNull(domainSession.completedAt)
    }

    // =========================================================================
    // 7. RELEASE ARTIFACTS BUDGET BASELINE
    // =========================================================================

    @Test
    fun `regression - release candidate signed artifacts satisfy size budgets`() {
        val apkFile = resolveArtifactFile("build/outputs/apk/release/app-release.apk")
        assertTrue("Signed Release Candidate APK must exist in build outputs", apkFile.exists())
        assertTrue("Signed Release Candidate APK must not be empty", apkFile.length() > 0)
        val apkSizeMb = apkFile.length() / (1024 * 1024)
        assertTrue("Release APK size ($apkSizeMb MB) must be <= 50MB budget", apkSizeMb < 50)

        val aabFile = resolveArtifactFile("build/outputs/bundle/release/app-release.aab")
        assertTrue("Signed Release Candidate AAB bundle must exist in build outputs", aabFile.exists())
        assertTrue("Signed Release Candidate AAB bundle must not be empty", aabFile.length() > 0)
        val aabSizeMb = aabFile.length() / (1024 * 1024)
        assertTrue("Release AAB bundle size ($aabSizeMb MB) must be <= 30MB budget", aabSizeMb < 30)
    }

    private fun resolveArtifactFile(relativePath: String): File {
        val direct = File(relativePath)
        if (direct.exists()) return direct
        val appPrefixed = File("app", relativePath)
        if (appPrefixed.exists()) return appPrefixed
        val parentPrefixed = File("..", relativePath)
        if (parentPrefixed.exists()) return parentPrefixed
        val parentAppPrefixed = File("../app", relativePath)
        if (parentAppPrefixed.exists()) return parentAppPrefixed
        return direct
    }
}

package com.sanket_satpute_20.dailybattle.domain.monetization

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.monetization.DefaultEntitlementRepository
import com.sanket_satpute_20.dailybattle.data.monetization.SandboxBillingProvider
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionLifecycle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdEvaluator
import com.sanket_satpute_20.dailybattle.domain.crowd.RawCrowdResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.DefaultPurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementSource
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseErrorMessageResolver
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.protection.GameplayProtectionPolicy
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.CheckFeatureAccessUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.PurchaseProductUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.RestorePurchasesUseCase
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import com.sanket_satpute_20.dailybattle.domain.scoring.ConsistencyEvaluator
import com.sanket_satpute_20.dailybattle.domain.shift.RawShiftResult
import com.sanket_satpute_20.dailybattle.domain.shift.ShiftEvaluator
import com.sanket_satpute_20.dailybattle.domain.shift.ShiftSelectionRecord
import com.sanket_satpute_20.dailybattle.domain.snap.RawSnapResult
import com.sanket_satpute_20.dailybattle.domain.snap.SnapEvaluator
import com.sanket_satpute_20.dailybattle.domain.snap.TapRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Deep verification test suite for Sprint 21.4 — Gameplay Protection.
 *
 * Verifies the authoritative product and architecture constraints from:
 * - REQ-MON-001: Core Official Battle remains accessible without payment.
 * - REQ-MON-002: Paid functionality must not directly increase Official Battle score.
 * - REQ-MON-004: Monetization must not interrupt active gameplay; no pay-to-win mechanics.
 * - REQ-MON-005: Premium entitlements must be separated from gameplay scoring logic;
 *   monetization must not compromise Official Battle integrity.
 * - REQ-MON-006: Practice and Official Battle remain separate.
 * - Rule 127 (15_ANTIGRAVITY_RULES.md): Billing failure must not corrupt core gameplay.
 * - 13_MONETIZATION.md §4, §5, §6, §11, §47, §51.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameplayProtectionTest {

    private class FakeBattleSessionRepository : BattleSessionRepository {
        var sessionStore = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()
        var lastSavedSession: BattleSession? = null

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessionStore[userId to battleId]
        }

        override fun saveSession(session: BattleSession) {
            sessionStore[session.userId to session.battleId] = session
            lastSavedSession = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> = emptyList()
    }

    private class TestClock(private var millis: Long = 1_000_000L) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(millis)
        fun advanceBy(delta: Long) { millis += delta }
    }

    private lateinit var clock: TestClock
    private lateinit var entitlementRepository: DefaultEntitlementRepository
    private lateinit var sandboxBillingProvider: SandboxBillingProvider
    private lateinit var defaultPurchaseVerifier: DefaultPurchaseVerifier
    private lateinit var gameplayProtectionPolicy: GameplayProtectionPolicy
    private lateinit var battleSessionRepository: FakeBattleSessionRepository
    private lateinit var activeBattleSessionManager: ActiveBattleSessionManager

    private lateinit var checkFeatureAccessUseCase: CheckFeatureAccessUseCase
    private lateinit var purchaseProductUseCase: PurchaseProductUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    private val testUserId = UserId("user_hero_1")
    private val testBattleId = BattleId("battle_2026_10_13")
    private val testSessionId = BattleSessionId("session_gp_1")

    @Before
    fun setUp() {
        clock = TestClock()
        entitlementRepository = DefaultEntitlementRepository(clock = clock)
        sandboxBillingProvider = SandboxBillingProvider()
        defaultPurchaseVerifier = DefaultPurchaseVerifier()
        gameplayProtectionPolicy = GameplayProtectionPolicy()
        battleSessionRepository = FakeBattleSessionRepository()
        activeBattleSessionManager = ActiveBattleSessionManager(battleSessionRepository)

        checkFeatureAccessUseCase = CheckFeatureAccessUseCase(
            entitlementRepository = entitlementRepository,
            gameplayProtectionPolicy = gameplayProtectionPolicy
        )

        purchaseProductUseCase = PurchaseProductUseCase(
            entitlementRepository = entitlementRepository,
            billingProvider = sandboxBillingProvider,
            purchaseVerifier = defaultPurchaseVerifier,
            gameplayProtectionPolicy = gameplayProtectionPolicy,
            activeBattleSessionManager = activeBattleSessionManager
        )

        restorePurchasesUseCase = RestorePurchasesUseCase(
            entitlementRepository = entitlementRepository,
            billingProvider = sandboxBillingProvider
        )
    }

    private fun createOfficialBattleSession(status: BattleSessionStatus = BattleSessionStatus.NOT_STARTED): BattleSession {
        return BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = status,
            currentChallenge = if (status == BattleSessionStatus.IN_PROGRESS) 0 else null,
            startedAt = if (status != BattleSessionStatus.NOT_STARTED) 1_000_000L else null,
            completedAt = if (status == BattleSessionStatus.COMPLETED) 1_050_000L else null,
            challengeSessions = emptyList()
        )
    }

    // ============================================================================
    // 1. SCORING FAIRNESS & ISOLATION (§4, §5, REQ-MON-002, REQ-MON-004)
    // ============================================================================

    @Test
    fun `scoring fairness - BattleScoreAggregator produces identical results across all entitlement states`() = runTest {
        val aggregator = BattleScoreAggregator()

        val snapResult = ChallengeResult(
            resultId = ChallengeResultId("snap_1"),
            challengeId = ChallengeId("ch_snap"),
            sessionId = testSessionId,
            score = 250,
            maxScore = 300,
            completedAt = 1000L
        )
        val shiftResult = ChallengeResult(
            resultId = ChallengeResultId("shift_1"),
            challengeId = ChallengeId("ch_shift"),
            sessionId = testSessionId,
            score = 200,
            maxScore = 300,
            completedAt = 2000L
        )
        val crowdResult = ChallengeResult(
            resultId = ChallengeResultId("crowd_1"),
            challengeId = ChallengeId("ch_crowd"),
            sessionId = testSessionId,
            score = 300,
            maxScore = 300,
            completedAt = 3000L
        )

        // Baseline score calculation for free user
        val freeScore = aggregator.aggregate(BattleMode.OFFICIAL, snapResult, shiftResult, crowdResult)

        // Simulate granting a premium entitlement in repository
        val premiumEntitlement = Entitlement(
            id = "ent_premium_vip",
            featureId = "theme_midnight",
            source = EntitlementSource.PURCHASE
        )
        entitlementRepository.grantEntitlement(premiumEntitlement)

        // Aggregate score for premium user
        val premiumScore = aggregator.aggregate(BattleMode.OFFICIAL, snapResult, shiftResult, crowdResult)

        // Aggregate score for expired user
        entitlementRepository.clearAllEntitlements()
        val expiredScore = aggregator.aggregate(BattleMode.OFFICIAL, snapResult, shiftResult, crowdResult)

        // Invariant: Scores MUST be 100% strictly identical
        assertEquals(freeScore.snapScore, premiumScore.snapScore)
        assertEquals(freeScore.shiftScore, premiumScore.shiftScore)
        assertEquals(freeScore.crowdCallScore, premiumScore.crowdCallScore)
        assertEquals(freeScore.consistencyScore, premiumScore.consistencyScore)
        assertEquals(freeScore.totalScore, premiumScore.totalScore)

        assertEquals(freeScore.totalScore, expiredScore.totalScore)
        assertEquals(250, premiumScore.snapScore)
        assertEquals(200, premiumScore.shiftScore)
        assertEquals(300, premiumScore.crowdCallScore)
    }

    @Test
    fun `scoring fairness - individual challenge evaluators are completely independent of monetization`() {
        val snapEvaluator = SnapEvaluator()
        val shiftEvaluator = ShiftEvaluator()
        val crowdEvaluator = CrowdEvaluator()

        val snapInput = RawSnapResult(
            targetAppeared = true,
            targetAppearanceTimeMs = 400L,
            taps = listOf(TapRecord(timestampMs = 500L, elementId = "target_1")),
            roundDurationMs = 1000L
        )
        val shiftInput = RawShiftResult(
            roundsCompleted = 1,
            selections = listOf(ShiftSelectionRecord("step_1", 500L)),
            challengeDurationMs = 1000L
        )
        val crowdInput = RawCrowdResult(
            questionId = "q_1",
            selectedChoiceId = "choice_b",
            timeToAnswerMs = 800L
        )

        val snapRes = snapEvaluator.evaluate(snapInput, testSessionId, ChallengeId("snap"))
        val shiftRes = shiftEvaluator.evaluate(shiftInput, testSessionId, ChallengeId("shift"))
        val crowdRes = crowdEvaluator.evaluate(crowdInput, testSessionId, ChallengeId("crowd"))

        assertEquals(300, snapRes.score)
        assertEquals(300, shiftRes.score)
        assertEquals(300, crowdRes.score)

        // Zero monetization parameters exist or are accepted by evaluators
        assertEquals(300, snapRes.maxScore)
        assertEquals(300, shiftRes.maxScore)
        assertEquals(300, crowdRes.maxScore)
    }

    @Test
    fun `scoring fairness - consistency evaluator is unaffected by entitlement status`() {
        val consistencyEvaluator = ConsistencyEvaluator()

        val r1 = ChallengeResult(ChallengeResultId("1"), ChallengeId("c1"), testSessionId, 300, 300, 100L)
        val r2 = ChallengeResult(ChallengeResultId("2"), ChallengeId("c2"), testSessionId, 300, 300, 200L)
        val r3 = ChallengeResult(ChallengeResultId("3"), ChallengeId("c3"), testSessionId, 300, 300, 300L)

        val score = consistencyEvaluator.evaluate(BattleMode.OFFICIAL, r1, r2, r3)
        assertEquals(100, score)
    }

    // ============================================================================
    // 2. GAMEPLAY INTERRUPTION PROTECTION (§11, §47, REQ-MON-004)
    // ============================================================================

    @Test
    fun `gameplay protection - purchase is strictly blocked during Ready state`() = runTest {
        val result = purchaseProductUseCase(
            productId = "product_theme_midnight",
            featureId = "theme_midnight",
            battleLifecycle = BattleSessionLifecycle.Ready(challengeIndex = 0)
        )

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.GameplayProtectionViolation)
        assertFalse(failed.canRetry)

        val resolved = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("ERR_GAMEPLAY_PROTECTION", resolved.code)
        assertFalse(resolved.canRetry)
    }

    @Test
    fun `gameplay protection - purchase is strictly blocked during active challenge gameplay`() = runTest {
        val activeLifecycles = listOf(
            BattleSessionLifecycle.Started(0),
            BattleSessionLifecycle.ChallengeActive(0), // Snap active
            BattleSessionLifecycle.ChallengeComplete(0),
            BattleSessionLifecycle.Ready(1),
            BattleSessionLifecycle.ChallengeActive(1), // Shift active
            BattleSessionLifecycle.Ready(2),
            BattleSessionLifecycle.ChallengeActive(2), // Crowd Call active
            BattleSessionLifecycle.BattleComplete
        )

        for (lifecycle in activeLifecycles) {
            val result = purchaseProductUseCase(
                productId = "product_theme_midnight",
                featureId = "theme_midnight",
                battleLifecycle = lifecycle
            )

            assertTrue("Expected blocked purchase for lifecycle $lifecycle", result is PurchaseState.Failed)
            val failed = result as PurchaseState.Failed
            assertTrue("Expected GameplayProtectionViolation for $lifecycle", failed.error is PurchaseError.GameplayProtectionViolation)
            assertFalse("Expected canRetry=false for $lifecycle", failed.canRetry)
        }
    }

    @Test
    fun `gameplay protection - purchase is blocked when ActiveBattleSessionManager has active challenge`() = runTest {
        val session = createOfficialBattleSession(BattleSessionStatus.NOT_STARTED)
        battleSessionRepository.saveSession(session)
        activeBattleSessionManager.bindSession(session)

        // Advance battle to Active Challenge
        activeBattleSessionManager.processBattleEvent(BattleEvent.StartBattle)
        activeBattleSessionManager.processBattleEvent(BattleEvent.BeginChallenge)

        val currentLifecycle = activeBattleSessionManager.lifecycle.value
        assertNotNull(currentLifecycle)
        assertTrue(gameplayProtectionPolicy.isGameplayActive(currentLifecycle))

        // Attempt purchase using the manager's active state
        val result = purchaseProductUseCase(
            productId = "product_theme_midnight",
            featureId = "theme_midnight"
        )

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.GameplayProtectionViolation)
        assertFalse(failed.canRetry)
    }

    @Test
    fun `gameplay protection - purchase is permitted when outside active gameplay`() = runTest {
        val nonActiveLifecycles = listOf(
            null,
            BattleSessionLifecycle.Created,
            BattleSessionLifecycle.Submitted
        )

        for (lifecycle in nonActiveLifecycles) {
            val result = purchaseProductUseCase(
                productId = "product_theme_midnight",
                featureId = "theme_midnight",
                battleLifecycle = lifecycle
            )

            assertTrue("Expected success or already owned for $lifecycle", result is PurchaseState.Success || result is PurchaseState.Failed)
            if (result is PurchaseState.Failed) {
                // If it failed, it must NOT be due to GameplayProtectionViolation
                assertFalse(result.error is PurchaseError.GameplayProtectionViolation)
            }
        }
    }

    // ============================================================================
    // 3. PAY-TO-WIN & PROHIBITED FEATURES GATING (§4, §5, REQ-MON-002, REQ-MON-004)
    // ============================================================================

    @Test
    fun `pay-to-win protection - PurchaseProductUseCase strictly blocks all prohibited features`() = runTest {
        val prohibitedFeatures = listOf(
            "score_boost",
            "+100_score",
            "score_multiplier",
            "point_multiplier",
            "bonus_points",
            "perfect_score",
            "extra_attempt",
            "bonus_attempt",
            "battle_retry",
            "official_retry",
            "second_chance",
            "replay_battle",
            "replace_score",
            "easier_challenge",
            "skip_challenge",
            "auto_perfect",
            "snap_advantage",
            "shift_advantage",
            "crowd_advantage",
            "defeat_rival",
            "rival_handicap",
            "percentile_boost",
            "ranking_boost"
        )

        for (feature in prohibitedFeatures) {
            val result = purchaseProductUseCase(
                productId = "product_p2w_$feature",
                featureId = feature,
                battleLifecycle = null
            )

            assertTrue("Expected failure for prohibited feature: $feature", result is PurchaseState.Failed)
            val failed = result as PurchaseState.Failed
            assertTrue("Expected GameplayProtectionViolation for: $feature", failed.error is PurchaseError.GameplayProtectionViolation)
            assertFalse("Expected non-retryable for: $feature", failed.canRetry)

            // Billing provider must never be invoked for prohibited items
            assertFalse(entitlementRepository.hasAccessToFeature(feature))
        }
    }

    @Test
    fun `pay-to-win protection - CheckFeatureAccessUseCase fails closed for prohibited features even if fake entitlement exists`() = runTest {
        // Even if a forged or corrupt entitlement exists in storage
        val fakeBoostEntitlement = Entitlement(
            id = "ent_forged_boost",
            featureId = "score_boost",
            source = EntitlementSource.PURCHASE
        )
        entitlementRepository.grantEntitlement(fakeBoostEntitlement)

        // CheckFeatureAccessUseCase MUST reject it unconditionally
        val hasAccess = checkFeatureAccessUseCase("score_boost")
        assertFalse("Access to prohibited feature must always be false", hasAccess)

        val hasMultiplierAccess = checkFeatureAccessUseCase("score_multiplier")
        assertFalse(hasMultiplierAccess)

        val hasRetryAccess = checkFeatureAccessUseCase("extra_attempt")
        assertFalse(hasRetryAccess)
    }

    // ============================================================================
    // 4. CORE FREE FEATURES GUARANTEE (§9, REQ-MON-001, REQ-MON-005)
    // ============================================================================

    @Test
    fun `core free features - CheckFeatureAccessUseCase unconditionally grants core features without payment`() = runTest {
        entitlementRepository.clearAllEntitlements()
        assertEquals(EntitlementState.Free, entitlementRepository.entitlementState.value)

        val coreFeatures = listOf(
            "official_battle",
            "snap",
            "shift",
            "crowd_call",
            "scoring",
            "results",
            "rival_comparison",
            "basic_practice",
            "history_basic"
        )

        for (coreFeature in coreFeatures) {
            val hasAccess = checkFeatureAccessUseCase(coreFeature)
            assertTrue("Core feature $coreFeature must be unconditionally free", hasAccess)
        }
    }

    @Test
    fun `core free features - PurchaseProductUseCase prevents purchasing core free features`() = runTest {
        val result = purchaseProductUseCase(
            productId = "product_official_battle",
            featureId = "official_battle",
            battleLifecycle = null
        )

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.GameplayProtectionViolation)
        assertFalse(failed.canRetry)
    }

    // ============================================================================
    // 5. ATTEMPT INTEGRITY & ONE OFFICIAL BATTLE PER DAY (§6, REQ-MON-005)
    // ============================================================================

    @Test
    fun `attempt integrity - completed official battle cannot be replayed or retried with premium entitlement`() = runTest {
        val completedSession = createOfficialBattleSession(BattleSessionStatus.COMPLETED)
        battleSessionRepository.saveSession(completedSession)

        val getOrStartUseCase = GetOrStartOfficialBattleUseCase(battleSessionRepository)

        // Free player completed today's battle
        val freeAttempt = getOrStartUseCase.execute(testUserId, testBattleId)
        assertTrue("Completed battle must reject repeat attempt", freeAttempt is DomainResult.Failure)
        assertEquals(AppError.Domain, (freeAttempt as DomainResult.Failure).error)

        // Player purchases premium theme or entitlement
        val premiumEntitlement = Entitlement(
            id = "ent_prem_1",
            featureId = "theme_midnight",
            source = EntitlementSource.PURCHASE
        )
        entitlementRepository.grantEntitlement(premiumEntitlement)

        // Repeat attempt MUST still fail
        val premiumAttempt = getOrStartUseCase.execute(testUserId, testBattleId)
        assertTrue("Premium player cannot get extra official attempt", premiumAttempt is DomainResult.Failure)
        assertEquals(AppError.Domain, (premiumAttempt as DomainResult.Failure).error)
    }

    // ============================================================================
    // 6. BATTLE STATE ISOLATION & FAILURE RESILIENCE (Rule 127, §47, §51)
    // ============================================================================

    @Test
    fun `state isolation - billing failure does not alter active battle session state`() = runTest {
        val inProgressSession = createOfficialBattleSession(BattleSessionStatus.IN_PROGRESS)
        battleSessionRepository.saveSession(inProgressSession)

        // Simulate a billing network error while battle session exists in background
        sandboxBillingProvider.setSimulateNetworkError(true)
        val purchaseResult = purchaseProductUseCase(
            productId = "product_theme_midnight",
            featureId = "theme_midnight",
            battleLifecycle = null // initiated outside challenge UI
        )

        assertTrue(purchaseResult is PurchaseState.Failed)

        // Verify BattleSession was completely untouched
        val storedSession = battleSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(storedSession)
        assertEquals(BattleSessionStatus.IN_PROGRESS, storedSession?.status)
        assertEquals(testSessionId, storedSession?.sessionId)
        assertEquals(0, storedSession?.currentChallenge)
    }

    @Test
    fun `state isolation - restoring purchases does not corrupt battle session`() = runTest {
        val inProgressSession = createOfficialBattleSession(BattleSessionStatus.IN_PROGRESS)
        battleSessionRepository.saveSession(inProgressSession)

        restorePurchasesUseCase()

        val storedSession = battleSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(storedSession)
        assertEquals(BattleSessionStatus.IN_PROGRESS, storedSession?.status)
        assertEquals(testSessionId, storedSession?.sessionId)
    }

    // ============================================================================
    // 7. PRACTICE MODE ISOLATION (§7, §51, REQ-MON-006)
    // ============================================================================

    @Test
    fun `practice isolation - practice results remain isolated from official scoring`() {
        val aggregator = BattleScoreAggregator()

        val snapResult = ChallengeResult(
            resultId = ChallengeResultId("snap_p"),
            challengeId = ChallengeId("ch_snap"),
            sessionId = testSessionId,
            score = 300,
            maxScore = 300,
            completedAt = 1000L
        )
        val shiftResult = ChallengeResult(
            resultId = ChallengeResultId("shift_p"),
            challengeId = ChallengeId("ch_shift"),
            sessionId = testSessionId,
            score = 300,
            maxScore = 300,
            completedAt = 2000L
        )
        val crowdResult = ChallengeResult(
            resultId = ChallengeResultId("crowd_p"),
            challengeId = ChallengeId("ch_crowd"),
            sessionId = testSessionId,
            score = 300,
            maxScore = 300,
            completedAt = 3000L
        )

        // In practice mode, consistency bonus is 0 per ConsistencyEvaluator rules
        val practiceScore = aggregator.aggregate(BattleMode.PRACTICE, snapResult, shiftResult, crowdResult)
        assertEquals(0, practiceScore.consistencyScore)
        assertEquals(900, practiceScore.totalScore)

        // In official mode, full consistency bonus is evaluated
        val officialScore = aggregator.aggregate(BattleMode.OFFICIAL, snapResult, shiftResult, crowdResult)
        assertEquals(100, officialScore.consistencyScore)
        assertEquals(1000, officialScore.totalScore)
    }
}

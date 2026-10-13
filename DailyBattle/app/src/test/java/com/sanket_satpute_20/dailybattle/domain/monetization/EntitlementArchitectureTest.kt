package com.sanket_satpute_20.dailybattle.domain.monetization

import com.sanket_satpute_20.dailybattle.data.monetization.DefaultEntitlementRepository
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingPurchaseResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingRestoreResult
import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementSource
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreState
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.CheckFeatureAccessUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.GetEntitlementStateUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.PurchaseProductUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class EntitlementArchitectureTest {

    private lateinit var testClock: MutableTestClock
    private lateinit var entitlementRepository: DefaultEntitlementRepository
    private lateinit var fakeBillingProvider: FakeBillingProvider

    private lateinit var getEntitlementStateUseCase: GetEntitlementStateUseCase
    private lateinit var checkFeatureAccessUseCase: CheckFeatureAccessUseCase
    private lateinit var purchaseProductUseCase: PurchaseProductUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    @Before
    fun setUp() {
        testClock = MutableTestClock(currentMillis = 1_000_000L)
        entitlementRepository = DefaultEntitlementRepository(clock = testClock)
        fakeBillingProvider = FakeBillingProvider()

        getEntitlementStateUseCase = GetEntitlementStateUseCase(entitlementRepository)
        checkFeatureAccessUseCase = CheckFeatureAccessUseCase(entitlementRepository)
        purchaseProductUseCase = PurchaseProductUseCase(entitlementRepository, fakeBillingProvider)
        restorePurchasesUseCase = RestorePurchasesUseCase(entitlementRepository, fakeBillingProvider)
    }

    // ============================================================================
    // 1. Entitlement State & Dynamic Expiration Tests
    // ============================================================================

    @Test
    fun `entitlement state - defaults to Free core experience`() = runTest {
        val state = getEntitlementStateUseCase().value
        assertEquals(EntitlementState.Free, state)
        assertEquals(0, entitlementRepository.getActiveEntitlements().size)
    }

    @Test
    fun `entitlement state - granting permanent entitlement transitions state to Premium`() = runTest {
        val entitlement = Entitlement(
            id = "ent_lifetime_001",
            featureId = "premium_theme",
            grantedAt = testClock.millis(),
            expiresAt = null,
            source = EntitlementSource.PURCHASE
        )

        entitlementRepository.grantEntitlement(entitlement)

        val state = getEntitlementStateUseCase().value
        assertTrue(state is EntitlementState.Premium)
        val premium = state as EntitlementState.Premium
        assertEquals(1, premium.activeEntitlements.size)
        assertEquals("premium_theme", premium.activeEntitlements.first().featureId)
        assertFalse(entitlement.isExpired(testClock.millis()))
    }

    @Test
    fun `entitlement state - expiring entitlement transitions to Expired when clock advances`() = runTest {
        val grantedAt = testClock.millis()
        val expiresAt = grantedAt + 10_000L

        val expiringEntitlement = Entitlement(
            id = "ent_trial_001",
            featureId = "practice_insights",
            grantedAt = grantedAt,
            expiresAt = expiresAt,
            source = EntitlementSource.PROMOTION
        )

        entitlementRepository.grantEntitlement(expiringEntitlement)

        // Initially active
        assertTrue(getEntitlementStateUseCase().value is EntitlementState.Premium)
        assertTrue(entitlementRepository.hasAccessToFeature("practice_insights"))

        // Advance time past expiration
        testClock.advanceMillis(15_000L)
        entitlementRepository.refreshExpirations()

        val state = getEntitlementStateUseCase().value
        assertTrue("State must become Expired", state is EntitlementState.Expired)
        assertFalse("Access must be revoked after expiration", entitlementRepository.hasAccessToFeature("practice_insights"))
        assertEquals(0, entitlementRepository.getActiveEntitlements().size)
    }

    @Test
    fun `entitlement state - revoking entitlement updates state cleanly`() = runTest {
        val entitlement = Entitlement(
            id = "ent_cancel_001",
            featureId = "custom_badge",
            grantedAt = testClock.millis(),
            expiresAt = null
        )

        entitlementRepository.grantEntitlement(entitlement)
        assertTrue(getEntitlementStateUseCase().value is EntitlementState.Premium)

        entitlementRepository.revokeEntitlement("ent_cancel_001")
        assertEquals(EntitlementState.Free, getEntitlementStateUseCase().value)
        assertFalse(entitlementRepository.hasAccessToFeature("custom_badge"))
    }

    @Test
    fun `entitlement state - clearAllEntitlements wipes all records and resets state to Free`() = runTest {
        entitlementRepository.grantEntitlement(
            Entitlement("e1", "feature_a", grantedAt = testClock.millis())
        )
        entitlementRepository.grantEntitlement(
            Entitlement("e2", "feature_b", grantedAt = testClock.millis())
        )
        assertEquals(2, entitlementRepository.getActiveEntitlements().size)

        entitlementRepository.clearAllEntitlements()
        assertEquals(EntitlementState.Free, getEntitlementStateUseCase().value)
        assertEquals(0, entitlementRepository.getActiveEntitlements().size)
        assertEquals(PurchaseState.Idle, entitlementRepository.purchaseState.value)
        assertEquals(RestoreState.Idle, entitlementRepository.restoreState.value)
    }

    // ============================================================================
    // 2. Purchase State & Duplicate Protection Tests
    // ============================================================================

    @Test
    fun `purchase state - successful purchase grants entitlement and transitions to Success`() = runTest {
        val entitlement = Entitlement("ent_p1", "feature_theme_dark_gold", grantedAt = testClock.millis())
        fakeBillingProvider.nextPurchaseResult = BillingPurchaseResult.Success(
            productId = "prod_theme_gold",
            entitlement = entitlement,
            purchaseToken = "tok_xyz_123"
        )

        val result = purchaseProductUseCase("prod_theme_gold", "feature_theme_dark_gold")

        assertTrue(result is PurchaseState.Success)
        val success = result as PurchaseState.Success
        assertEquals("prod_theme_gold", success.productId)
        assertEquals("tok_xyz_123", success.purchaseToken)

        // Entitlement granted in repository
        assertTrue(entitlementRepository.hasAccessToFeature("feature_theme_dark_gold"))
        assertTrue(getEntitlementStateUseCase().value is EntitlementState.Premium)
    }

    @Test
    fun `purchase state - cancelled purchase transitions to Cancelled without granting entitlement`() = runTest {
        fakeBillingProvider.nextPurchaseResult = BillingPurchaseResult.Cancelled("prod_cancelled")

        val result = purchaseProductUseCase("prod_cancelled", "feature_unowned")

        assertTrue(result is PurchaseState.Cancelled)
        assertEquals(PurchaseState.Cancelled("prod_cancelled"), entitlementRepository.purchaseState.value)
        assertFalse(entitlementRepository.hasAccessToFeature("feature_unowned"))
        assertEquals(EntitlementState.Free, getEntitlementStateUseCase().value)
    }

    @Test
    fun `purchase state - failed network purchase transitions to Failed with canRetry true`() = runTest {
        fakeBillingProvider.nextPurchaseResult = BillingPurchaseResult.Error(
            productId = "prod_network_err",
            message = "Connection timeout",
            isNetworkError = true
        )

        val result = purchaseProductUseCase("prod_network_err", "feature_insights")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.NetworkFailure)
        assertTrue("Network errors should be retryable", failed.canRetry)
        assertFalse(entitlementRepository.hasAccessToFeature("feature_insights"))
    }

    @Test
    fun `duplicate purchase protection - cannot purchase item already actively owned`() = runTest {
        entitlementRepository.grantEntitlement(
            Entitlement("e_owned", "feature_already_owned", grantedAt = testClock.millis())
        )

        val result = purchaseProductUseCase("prod_owned", "feature_already_owned")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.ItemAlreadyOwned)
        assertFalse("Already owned items should not be retryable", failed.canRetry)
        assertEquals(0, fakeBillingProvider.initiatePurchaseCallCount)
    }

    @Test
    fun `duplicate purchase protection - concurrent pending purchase is blocked`() = runTest {
        entitlementRepository.updatePurchaseState(PurchaseState.Pending("prod_in_flight"))

        val result = purchaseProductUseCase("prod_in_flight", "feature_xyz")

        assertTrue(result is PurchaseState.Pending)
        assertEquals(0, fakeBillingProvider.initiatePurchaseCallCount)
    }

    // ============================================================================
    // 3. Restore State Tests
    // ============================================================================

    @Test
    fun `restore state - successful restore grants all valid entitlements`() = runTest {
        val restoredList = listOf(
            Entitlement("r1", "theme_retro", grantedAt = testClock.millis(), source = EntitlementSource.RESTORE),
            Entitlement("r2", "custom_soundpack", grantedAt = testClock.millis(), source = EntitlementSource.RESTORE)
        )
        fakeBillingProvider.nextRestoreResult = BillingRestoreResult.Success(restoredList)

        val result = restorePurchasesUseCase()

        assertTrue(result is RestoreState.Success)
        val success = result as RestoreState.Success
        assertEquals(2, success.restoredEntitlements.size)

        assertTrue(entitlementRepository.hasAccessToFeature("theme_retro"))
        assertTrue(entitlementRepository.hasAccessToFeature("custom_soundpack"))
        assertTrue(getEntitlementStateUseCase().value is EntitlementState.Premium)
    }

    @Test
    fun `restore state - no purchases found transitions to Failed with NoPurchasesFound`() = runTest {
        fakeBillingProvider.nextRestoreResult = BillingRestoreResult.Success(emptyList())

        val result = restorePurchasesUseCase()

        assertTrue(result is RestoreState.Failed)
        val failed = result as RestoreState.Failed
        assertEquals(RestoreError.NoPurchasesFound, failed.error)
        assertFalse(failed.canRetry)
    }

    @Test
    fun `restore state - network error transitions to Failed with NetworkFailure`() = runTest {
        fakeBillingProvider.nextRestoreResult = BillingRestoreResult.Error(
            message = "Store unavailable",
            isNetworkError = true
        )

        val result = restorePurchasesUseCase()

        assertTrue(result is RestoreState.Failed)
        val failed = result as RestoreState.Failed
        assertTrue(failed.error is RestoreError.NetworkFailure)
        assertTrue(failed.canRetry)
    }

    // ============================================================================
    // 4. Gameplay Protection & Official Battle Isolation Tests (REQ-MON-001 - 005)
    // ============================================================================

    @Test
    fun `gameplay protection - core Official Battle is unconditionally accessible without entitlements`() = runTest {
        // Ensure user is completely unentitled / Free state
        assertEquals(EntitlementState.Free, getEntitlementStateUseCase().value)

        assertTrue(checkFeatureAccessUseCase("official_battle"))
        assertTrue(checkFeatureAccessUseCase("snap"))
        assertTrue(checkFeatureAccessUseCase("shift"))
        assertTrue(checkFeatureAccessUseCase("crowd_call"))
        assertTrue(checkFeatureAccessUseCase("scoring"))
        assertTrue(checkFeatureAccessUseCase("results"))
        assertTrue(checkFeatureAccessUseCase("rival_comparison"))
    }

    @Test
    fun `gameplay protection - core gameplay remains accessible even if user entitlements are Expired`() = runTest {
        // Simulate expired state
        val expired = Entitlement(
            "exp1",
            "optional_cosmetic",
            grantedAt = 1000L,
            expiresAt = 2000L
        )
        entitlementRepository.grantEntitlement(expired)
        testClock.advanceMillis(5000L)
        entitlementRepository.refreshExpirations()

        assertTrue(getEntitlementStateUseCase().value is EntitlementState.Expired)

        // Core official battle remains 100% accessible
        assertTrue(checkFeatureAccessUseCase("official_battle"))
        assertTrue(checkFeatureAccessUseCase("snap"))
        assertTrue(checkFeatureAccessUseCase("shift"))
        assertTrue(checkFeatureAccessUseCase("crowd_call"))

        // Non-core feature is rejected
        assertFalse(checkFeatureAccessUseCase("optional_cosmetic"))
    }

    // ============================================================================
    // Test Helpers
    // ============================================================================

    private class MutableTestClock(private var currentMillis: Long) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentMillis)
        override fun millis(): Long = currentMillis

        fun advanceMillis(delta: Long) {
            currentMillis += delta
        }
    }

    private class FakeBillingProvider : BillingProvider {
        override var isAvailable: Boolean = true
        var initiatePurchaseCallCount = 0

        var nextPurchaseResult: BillingPurchaseResult = BillingPurchaseResult.Error(
            productId = "default",
            message = "Unconfigured"
        )

        var nextRestoreResult: BillingRestoreResult = BillingRestoreResult.Error(
            message = "Unconfigured"
        )

        override suspend fun initiatePurchase(productId: String): BillingPurchaseResult {
            initiatePurchaseCallCount++
            return nextPurchaseResult
        }

        override suspend fun restorePurchases(): BillingRestoreResult {
            return nextRestoreResult
        }
    }
}

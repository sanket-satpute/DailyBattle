package com.sanket_satpute_20.dailybattle.domain.monetization

import com.sanket_satpute_20.dailybattle.data.monetization.DefaultEntitlementRepository
import com.sanket_satpute_20.dailybattle.data.monetization.SandboxBillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.DefaultPurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerificationResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementSource
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseErrorMessageResolver
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreState
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.CheckFeatureAccessUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.PurchaseProductUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

@OptIn(ExperimentalCoroutinesApi::class)
class MonetizationFailureHandlingTest {

    private lateinit var testClock: MutableTestClock
    private lateinit var entitlementRepository: DefaultEntitlementRepository
    private lateinit var sandboxBillingProvider: SandboxBillingProvider
    private lateinit var defaultPurchaseVerifier: DefaultPurchaseVerifier

    private lateinit var purchaseProductUseCase: PurchaseProductUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase
    private lateinit var checkFeatureAccessUseCase: CheckFeatureAccessUseCase

    @Before
    fun setUp() {
        testClock = MutableTestClock(currentMillis = 5_000_000L)
        entitlementRepository = DefaultEntitlementRepository(clock = testClock)
        sandboxBillingProvider = SandboxBillingProvider()
        defaultPurchaseVerifier = DefaultPurchaseVerifier()

        purchaseProductUseCase = PurchaseProductUseCase(
            entitlementRepository = entitlementRepository,
            billingProvider = sandboxBillingProvider,
            purchaseVerifier = defaultPurchaseVerifier
        )
        restorePurchasesUseCase = RestorePurchasesUseCase(
            entitlementRepository = entitlementRepository,
            billingProvider = sandboxBillingProvider
        )
        checkFeatureAccessUseCase = CheckFeatureAccessUseCase(entitlementRepository)
    }

    // ============================================================================
    // 1. Purchase Success Failure Handling
    // ============================================================================

    @Test
    fun `failure handling - purchase success transitions cleanly and resets to Idle`() = runTest {
        assertEquals(PurchaseState.Idle, entitlementRepository.purchaseState.value)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Success)
        val success = result as PurchaseState.Success
        assertNotNull(success.purchaseToken)
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))

        // Reset state after UI dismiss
        entitlementRepository.resetPurchaseState()
        assertEquals(PurchaseState.Idle, entitlementRepository.purchaseState.value)
        // Entitlement remains active after purchase state reset
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))
    }

    // ============================================================================
    // 2. Purchase Cancelled Failure Handling
    // ============================================================================

    @Test
    fun `failure handling - purchase cancelled allows immediate re-attempt`() = runTest {
        sandboxBillingProvider.setSimulateCancel(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Cancelled)
        assertEquals(PurchaseState.Cancelled("product_theme_midnight"), entitlementRepository.purchaseState.value)
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))

        val errorFeedback = PurchaseErrorMessageResolver.resolve(PurchaseError.UserCancelled)
        assertEquals("USER_CANCELLED", errorFeedback.code)
        assertTrue(errorFeedback.canRetry)

        // Reset state and re-attempt with cancel disabled
        entitlementRepository.resetPurchaseState()
        sandboxBillingProvider.setSimulateCancel(false)

        val retryResult = purchaseProductUseCase("product_theme_midnight", "theme_midnight")
        assertTrue(retryResult is PurchaseState.Success)
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))
    }

    // ============================================================================
    // 3. Purchase Failed Handling (Declined / Unavailable / Forged)
    // ============================================================================

    @Test
    fun `failure handling - store item unavailable sets non-retryable failure`() = runTest {
        sandboxBillingProvider.setSimulateItemUnavailable(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.ItemUnavailable)
        assertFalse("Item unavailable cannot be immediately retried", failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("ERR_ITEM_UNAVAILABLE", feedback.code)
        assertFalse(feedback.canRetry)

        // Entitlement must not be granted
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))
        assertEquals(EntitlementState.Free, entitlementRepository.entitlementState.value)
    }

    @Test
    fun `failure handling - invalid token verification failure prevents entitlement grant`() = runTest {
        val failingVerifier = object : PurchaseVerifier {
            override suspend fun verifyPurchase(productId: String, purchaseToken: String?): PurchaseVerificationResult {
                return PurchaseVerificationResult.Invalid("Signature verification failed: untrusted authority")
            }
        }

        val useCaseWithFailingVerifier = PurchaseProductUseCase(
            entitlementRepository = entitlementRepository,
            billingProvider = sandboxBillingProvider,
            purchaseVerifier = failingVerifier
        )

        val result = useCaseWithFailingVerifier("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.InvalidVerification)
        assertFalse(failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("ERR_INVALID_TOKEN", feedback.code)

        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))
    }

    // ============================================================================
    // 4. Network Failure Handling
    // ============================================================================

    @Test
    fun `failure handling - network failure during purchase allows safe retry`() = runTest {
        sandboxBillingProvider.setSimulateNetworkError(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.NetworkFailure)
        assertTrue("Network error must be retryable", failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("ERR_PURCHASE_NETWORK", feedback.code)
        assertTrue(feedback.canRetry)

        // Network recovers
        sandboxBillingProvider.setSimulateNetworkError(false)
        val recoveredResult = purchaseProductUseCase("product_theme_midnight", "theme_midnight")
        assertTrue(recoveredResult is PurchaseState.Success)
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))
    }

    @Test
    fun `failure handling - store billing unavailable failure is retryable`() = runTest {
        sandboxBillingProvider.setSimulateBillingUnavailable(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.BillingUnavailable)
        assertTrue(failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("ERR_BILLING_UNAVAILABLE", feedback.code)
    }

    // ============================================================================
    // 5. Duplicate Purchase Protection Handling
    // ============================================================================

    @Test
    fun `failure handling - concurrent double tap is safely deduplicated`() = runTest {
        // Run two purchase requests concurrently on the same product
        val deferred1 = async { purchaseProductUseCase("product_theme_midnight", "theme_midnight") }
        val deferred2 = async { purchaseProductUseCase("product_theme_midnight", "theme_midnight") }

        val results = awaitAll(deferred1, deferred2)

        // Exactly one should succeed, or one is Pending / ItemAlreadyOwned
        val successCount = results.count { it is PurchaseState.Success }
        assertTrue("At most one flow may succeed", successCount <= 1)
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))
    }

    @Test
    fun `failure handling - repeated purchase of already owned item is rejected safely`() = runTest {
        // Complete purchase first
        purchaseProductUseCase("product_theme_midnight", "theme_midnight")
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))

        // Attempt second purchase of the same item
        val secondResult = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(secondResult is PurchaseState.Failed)
        val failed = secondResult as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.ItemAlreadyOwned)
        assertFalse(failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("ERR_ALREADY_OWNED", feedback.code)
    }

    // ============================================================================
    // 6. Restore Failure Handling
    // ============================================================================

    @Test
    fun `failure handling - restore with empty purchases transitions to NoPurchasesFound`() = runTest {
        sandboxBillingProvider.clearPurchases()

        val result = restorePurchasesUseCase()

        assertTrue(result is RestoreState.Failed)
        val failed = result as RestoreState.Failed
        assertEquals(RestoreError.NoPurchasesFound, failed.error)
        assertFalse(failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("RESTORE_EMPTY", feedback.code)
        assertFalse(feedback.canRetry)

        // Reset state back to Idle
        entitlementRepository.resetRestoreState()
        assertEquals(RestoreState.Idle, entitlementRepository.restoreState.value)
    }

    @Test
    fun `failure handling - network failure during restore is retryable`() = runTest {
        sandboxBillingProvider.setSimulateNetworkError(true)

        val result = restorePurchasesUseCase()

        assertTrue(result is RestoreState.Failed)
        val failed = result as RestoreState.Failed
        assertTrue(failed.error is RestoreError.NetworkFailure)
        assertTrue(failed.canRetry)

        val feedback = PurchaseErrorMessageResolver.resolve(failed.error)
        assertEquals("RESTORE_NETWORK", feedback.code)
        assertTrue(feedback.canRetry)
    }

    // ============================================================================
    // 7. Expired Entitlement Failure Handling
    // ============================================================================

    @Test
    fun `failure handling - expired subscription revokes premium access but preserves core gameplay`() = runTest {
        val grantedAt = testClock.millis()
        val expiresAt = grantedAt + 5_000L

        val subscription = Entitlement(
            id = "ent_sub_01",
            featureId = "expanded_practice_insights",
            grantedAt = grantedAt,
            expiresAt = expiresAt,
            source = EntitlementSource.PURCHASE
        )
        entitlementRepository.grantEntitlement(subscription)

        assertTrue(entitlementRepository.hasAccessToFeature("expanded_practice_insights"))
        assertTrue(entitlementRepository.entitlementState.value is EntitlementState.Premium)

        // Advance time past expiration
        testClock.advanceMillis(10_000L)
        entitlementRepository.refreshExpirations()

        // State is now Expired
        val state = entitlementRepository.entitlementState.value
        assertTrue(state is EntitlementState.Expired)
        assertFalse(entitlementRepository.hasAccessToFeature("expanded_practice_insights"))

        // Optional feature access revoked
        assertFalse(checkFeatureAccessUseCase("expanded_practice_insights"))

        // CRITICAL INVARIANT (REQ-MON-001): Core Official Battle gameplay remains 100% accessible!
        assertTrue(checkFeatureAccessUseCase("official_battle"))
        assertTrue(checkFeatureAccessUseCase("snap"))
        assertTrue(checkFeatureAccessUseCase("shift"))
        assertTrue(checkFeatureAccessUseCase("crowd_call"))
    }

    // ============================================================================
    // 8. Unknown Entitlement Failure Handling
    // ============================================================================

    @Test
    fun `failure handling - unknown entitlement fails-closed for premium but fails-open for official battle`() = runTest {
        // Set entitlement state to Unknown (e.g. unverified, store partition, corrupted cache)
        entitlementRepository.setUnknownState("Store verification unreachable")

        val state = entitlementRepository.entitlementState.value
        assertTrue(state is EntitlementState.Unknown)
        assertEquals("Store verification unreachable", (state as EntitlementState.Unknown).message)

        // Optional premium features FAIL-CLOSED
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))
        assertFalse(checkFeatureAccessUseCase("theme_midnight"))

        // Core official battle gameplay FAILS-OPEN (unconditionally free and playable)
        assertTrue(checkFeatureAccessUseCase("official_battle"))
        assertTrue(checkFeatureAccessUseCase("snap"))
        assertTrue(checkFeatureAccessUseCase("shift"))
        assertTrue(checkFeatureAccessUseCase("crowd_call"))
        assertTrue(checkFeatureAccessUseCase("scoring"))
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
}

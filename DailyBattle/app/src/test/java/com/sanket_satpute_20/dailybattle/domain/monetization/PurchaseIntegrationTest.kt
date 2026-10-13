package com.sanket_satpute_20.dailybattle.domain.monetization

import com.sanket_satpute_20.dailybattle.data.monetization.DefaultEntitlementRepository
import com.sanket_satpute_20.dailybattle.data.monetization.SandboxBillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingConnectionState
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.DefaultPurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerificationResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementSource
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreState
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.CheckFeatureAccessUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.PurchaseProductUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.QueryAvailableProductsUseCase
import com.sanket_satpute_20.dailybattle.domain.monetization.usecase.RestorePurchasesUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseIntegrationTest {

    private lateinit var testClock: MutableTestClock
    private lateinit var entitlementRepository: DefaultEntitlementRepository
    private lateinit var sandboxBillingProvider: SandboxBillingProvider
    private lateinit var defaultPurchaseVerifier: DefaultPurchaseVerifier

    private lateinit var queryAvailableProductsUseCase: QueryAvailableProductsUseCase
    private lateinit var purchaseProductUseCase: PurchaseProductUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase
    private lateinit var checkFeatureAccessUseCase: CheckFeatureAccessUseCase

    @Before
    fun setUp() {
        testClock = MutableTestClock(currentMillis = 2_000_000L)
        entitlementRepository = DefaultEntitlementRepository(clock = testClock)
        sandboxBillingProvider = SandboxBillingProvider()
        defaultPurchaseVerifier = DefaultPurchaseVerifier()

        queryAvailableProductsUseCase = QueryAvailableProductsUseCase(sandboxBillingProvider)
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
    // 1. Catalog Querying Tests (§22, §23)
    // ============================================================================

    @Test
    fun `catalog querying - returns available products with pricing and metadata`() = runTest {
        val products = queryAvailableProductsUseCase(
            setOf("product_theme_midnight", "product_practice_insights_monthly")
        )

        assertEquals(2, products.size)
        val themeProduct = products.first { it.id == "product_theme_midnight" }
        assertEquals("Midnight Pro Theme", themeProduct.title)
        assertEquals("$1.99", themeProduct.formattedPrice)
        assertEquals("theme_midnight", themeProduct.featureId)
    }

    @Test
    fun `catalog querying - ignores non-existent product IDs gracefully`() = runTest {
        val products = queryAvailableProductsUseCase(setOf("non_existent_product"))
        assertTrue(products.isEmpty())
    }

    // ============================================================================
    // 2. Connection Lifecycle Tests
    // ============================================================================

    @Test
    fun `connection lifecycle - connect and disconnect update provider state`() = runTest {
        assertTrue(sandboxBillingProvider.isAvailable)
        assertEquals(BillingConnectionState.Connected, sandboxBillingProvider.connectionState.value)

        sandboxBillingProvider.disconnect()
        assertFalse(sandboxBillingProvider.isAvailable)
        assertEquals(BillingConnectionState.Disconnected, sandboxBillingProvider.connectionState.value)

        val connected = sandboxBillingProvider.connect()
        assertTrue(connected)
        assertTrue(sandboxBillingProvider.isAvailable)
        assertEquals(BillingConnectionState.Connected, sandboxBillingProvider.connectionState.value)
    }

    @Test
    fun `connection lifecycle - purchase auto-connects if disconnected`() = runTest {
        sandboxBillingProvider.disconnect()
        assertFalse(sandboxBillingProvider.isAvailable)

        val result = purchaseProductUseCase(
            productId = "product_theme_midnight",
            featureId = "theme_midnight"
        )

        assertTrue(result is PurchaseState.Success)
        assertTrue(sandboxBillingProvider.isAvailable)
    }

    // ============================================================================
    // 3. Purchase Integration & Token Verification Tests (§25, §37, §38)
    // ============================================================================

    @Test
    fun `purchase integration - complete successful purchase grants entitlement and acknowledges`() = runTest {
        val result = purchaseProductUseCase(
            productId = "product_theme_midnight",
            featureId = "theme_midnight"
        )

        assertTrue(result is PurchaseState.Success)
        val success = result as PurchaseState.Success
        assertEquals("product_theme_midnight", success.productId)
        assertNotNull(success.purchaseToken)
        assertTrue(success.purchaseToken!!.startsWith("sandbox_tok_"))

        // Entitlement verified and granted
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))
        assertTrue(entitlementRepository.entitlementState.value is EntitlementState.Premium)
    }

    @Test
    fun `purchase integration - token verification failure rejects purchase without granting entitlement`() = runTest {
        val rejectingVerifier = object : PurchaseVerifier {
            override suspend fun verifyPurchase(productId: String, purchaseToken: String?): PurchaseVerificationResult {
                return PurchaseVerificationResult.Invalid("Forged signature detected")
            }
        }

        val customPurchaseUseCase = PurchaseProductUseCase(
            entitlementRepository = entitlementRepository,
            billingProvider = sandboxBillingProvider,
            purchaseVerifier = rejectingVerifier
        )

        val result = customPurchaseUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.InvalidVerification)
        assertEquals("Forged signature detected", (failed.error as PurchaseError.InvalidVerification).message)
        assertFalse(failed.canRetry)

        // Entitlement must NOT be granted
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))
        assertEquals(EntitlementState.Free, entitlementRepository.entitlementState.value)
    }

    @Test
    fun `purchase integration - user cancellation transitions to Cancelled without entitlement`() = runTest {
        sandboxBillingProvider.setSimulateCancel(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Cancelled)
        assertEquals(PurchaseState.Cancelled("product_theme_midnight"), entitlementRepository.purchaseState.value)
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))
        assertEquals(EntitlementState.Free, entitlementRepository.entitlementState.value)
    }

    @Test
    fun `purchase integration - network failure sets retryable Failed state`() = runTest {
        sandboxBillingProvider.setSimulateNetworkError(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.NetworkFailure)
        assertTrue("Network errors must be retryable", failed.canRetry)
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))
    }

    @Test
    fun `purchase integration - already owned item simulation sets non-retryable Failed state`() = runTest {
        sandboxBillingProvider.setSimulateAlreadyOwned(true)

        val result = purchaseProductUseCase("product_theme_midnight", "theme_midnight")

        assertTrue(result is PurchaseState.Failed)
        val failed = result as PurchaseState.Failed
        assertTrue(failed.error is PurchaseError.ItemAlreadyOwned)
        assertFalse("Already owned item cannot be retried", failed.canRetry)
    }

    // ============================================================================
    // 4. Acknowledgment & Restoration Tests (§27)
    // ============================================================================

    @Test
    fun `acknowledgment - acknowledging unrecorded token returns false`() = runTest {
        val acknowledged = sandboxBillingProvider.acknowledgePurchase("unrecorded_token_123")
        assertFalse(acknowledged)
    }

    @Test
    fun `restoration - restores past sandbox purchases and sets source to RESTORE`() = runTest {
        // Complete a purchase first
        purchaseProductUseCase("product_theme_midnight", "theme_midnight")
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))

        // Simulate app re-install / new session by wiping repository
        entitlementRepository.clearAllEntitlements()
        assertEquals(EntitlementState.Free, entitlementRepository.entitlementState.value)
        assertFalse(entitlementRepository.hasAccessToFeature("theme_midnight"))

        // Restore purchases
        val restoreResult = restorePurchasesUseCase()

        assertTrue(restoreResult is RestoreState.Success)
        val success = restoreResult as RestoreState.Success
        assertEquals(1, success.restoredEntitlements.size)
        val restored = success.restoredEntitlements.first()
        assertEquals("theme_midnight", restored.featureId)
        assertEquals(EntitlementSource.RESTORE, restored.source)

        // Entitlement recovered
        assertTrue(entitlementRepository.hasAccessToFeature("theme_midnight"))
        assertTrue(entitlementRepository.entitlementState.value is EntitlementState.Premium)
    }

    @Test
    fun `restoration - returns NoPurchasesFound when no purchases were made`() = runTest {
        sandboxBillingProvider.clearPurchases()

        val restoreResult = restorePurchasesUseCase()

        assertTrue(restoreResult is RestoreState.Failed)
        val failed = restoreResult as RestoreState.Failed
        assertEquals(RestoreError.NoPurchasesFound, failed.error)
        assertFalse(failed.canRetry)
    }

    // ============================================================================
    // Test Helpers
    // ============================================================================

    private class MutableTestClock(private var currentMillis: Long) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentMillis)
        override fun millis(): Long = currentMillis
    }
}

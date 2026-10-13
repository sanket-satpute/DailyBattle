package com.sanket_satpute_20.dailybattle.data.monetization

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingConnectionState
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingPurchaseResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingRestoreResult
import com.sanket_satpute_20.dailybattle.domain.monetization.model.BillingProduct
import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementSource
import com.sanket_satpute_20.dailybattle.domain.monetization.model.ProductType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-ready Sandbox implementation of [BillingProvider].
 *
 * Implements Sprint 21.2 per `13_MONETIZATION.md` §49, §55:
 * - Provides a safe test/sandbox billing environment for debug/dev builds without real money.
 * - Adheres strictly to §55: Zero unauthorized third-party billing SDKs.
 * - Models real store behavior: product catalog querying, connection management, purchase token
 *   issuance, purchase acknowledgment, cancellation simulation, and purchase restoration.
 */
@Singleton
class SandboxBillingProvider @Inject constructor() : BillingProvider {

    private val mutex = Mutex()

    private val _connectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Connected)
    override val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    override val isAvailable: Boolean
        get() = _connectionState.value == BillingConnectionState.Connected

    // Catalog of available sandbox products
    private val catalog = mutableMapOf<String, BillingProduct>()

    // Record of completed sandbox purchases (purchaseToken -> PurchaseRecord)
    private val purchaseRecords = mutableMapOf<String, SandboxPurchaseRecord>()

    // Simulation overrides for testing
    private var simulateCancel: Boolean = false
    private var simulateNetworkError: Boolean = false
    private var simulateItemUnavailable: Boolean = false
    private var simulateAlreadyOwned: Boolean = false
    private var simulateBillingUnavailable: Boolean = false

    init {
        // Seed standard approved sandbox catalog
        seedDefaultCatalog()
    }

    override suspend fun connect(): Boolean = mutex.withLock {
        if (simulateBillingUnavailable || _connectionState.value is BillingConnectionState.Unavailable) {
            return false
        }
        _connectionState.value = BillingConnectionState.Connecting
        _connectionState.value = BillingConnectionState.Connected
        return true
    }

    override suspend fun disconnect() = mutex.withLock {
        _connectionState.value = BillingConnectionState.Disconnected
    }

    override suspend fun queryProductDetails(productIds: Set<String>): List<BillingProduct> = mutex.withLock {
        if (!isAvailable) return emptyList()
        productIds.mapNotNull { catalog[it] }
    }

    override suspend fun initiatePurchase(productId: String): BillingPurchaseResult = mutex.withLock {
        if (!isAvailable) {
            return BillingPurchaseResult.Error(
                productId = productId,
                message = "Billing service is not connected.",
                isNetworkError = true
            )
        }

        if (simulateNetworkError) {
            return BillingPurchaseResult.Error(
                productId = productId,
                message = "Simulated network timeout during billing flow.",
                isNetworkError = true
            )
        }

        if (simulateCancel) {
            return BillingPurchaseResult.Cancelled(productId = productId)
        }

        if (simulateAlreadyOwned) {
            return BillingPurchaseResult.Error(
                productId = productId,
                message = "Product $productId is already owned.",
                isAlreadyOwned = true
            )
        }

        val product = catalog[productId]
            ?: return BillingPurchaseResult.Error(
                productId = productId,
                message = "Product $productId not found in store catalog.",
                isItemUnavailable = true
            )

        if (simulateItemUnavailable) {
            return BillingPurchaseResult.Error(
                productId = productId,
                message = "Product $productId is currently unavailable.",
                isItemUnavailable = true
            )
        }

        // Generate verified sandbox purchase token
        val token = "sandbox_tok_${productId}_${UUID.randomUUID()}"
        val entitlement = Entitlement(
            id = "ent_${productId}_${System.currentTimeMillis()}",
            featureId = product.featureId,
            grantedAt = System.currentTimeMillis(),
            expiresAt = if (product.type == ProductType.SUBSCRIPTION) System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000 else null,
            source = EntitlementSource.PURCHASE
        )

        // Store record for acknowledgment and restoration
        purchaseRecords[token] = SandboxPurchaseRecord(
            productId = productId,
            purchaseToken = token,
            entitlement = entitlement,
            isAcknowledged = false
        )

        return BillingPurchaseResult.Success(
            productId = productId,
            entitlement = entitlement,
            purchaseToken = token
        )
    }

    override suspend fun acknowledgePurchase(purchaseToken: String): Boolean = mutex.withLock {
        val record = purchaseRecords[purchaseToken] ?: return false
        purchaseRecords[purchaseToken] = record.copy(isAcknowledged = true)
        return true
    }

    override suspend fun restorePurchases(): BillingRestoreResult = mutex.withLock {
        if (!isAvailable) {
            return BillingRestoreResult.Error(
                message = "Billing service is not connected.",
                isNetworkError = true
            )
        }

        if (simulateNetworkError) {
            return BillingRestoreResult.Error(
                message = "Network timeout while querying purchase history.",
                isNetworkError = true
            )
        }

        val restored = purchaseRecords.values.map { record ->
            record.entitlement.copy(source = EntitlementSource.RESTORE)
        }

        return BillingRestoreResult.Success(restoredEntitlements = restored)
    }

    // ============================================================================
    // Test & Sandbox Configuration Hooks
    // ============================================================================

    fun registerProduct(product: BillingProduct) {
        catalog[product.id] = product
    }

    fun clearPurchases() {
        purchaseRecords.clear()
    }

    fun setSimulateCancel(cancel: Boolean) {
        simulateCancel = cancel
    }

    fun setSimulateNetworkError(error: Boolean) {
        simulateNetworkError = error
    }

    fun setSimulateItemUnavailable(unavailable: Boolean) {
        simulateItemUnavailable = unavailable
    }

    fun setSimulateAlreadyOwned(alreadyOwned: Boolean) {
        simulateAlreadyOwned = alreadyOwned
    }

    fun setConnectionState(state: BillingConnectionState) {
        _connectionState.value = state
    }

    fun setSimulateBillingUnavailable(unavailable: Boolean) {
        simulateBillingUnavailable = unavailable
        if (unavailable) {
            _connectionState.value = BillingConnectionState.Unavailable("Simulated store unavailable")
        }
    }

    private fun seedDefaultCatalog() {
        registerProduct(
            BillingProduct(
                id = "product_theme_midnight",
                type = ProductType.IN_APP,
                title = "Midnight Pro Theme",
                description = "Unlock the sleek Midnight Pro visual aesthetic.",
                formattedPrice = "$1.99",
                currencyCode = "USD",
                priceMicros = 1990000L,
                featureId = "theme_midnight"
            )
        )
        registerProduct(
            BillingProduct(
                id = "product_practice_insights_monthly",
                type = ProductType.SUBSCRIPTION,
                title = "Practice Insights Subscription",
                description = "Monthly access to expanded practice diagnostics and history.",
                formattedPrice = "$2.99 / mo",
                currencyCode = "USD",
                priceMicros = 2990000L,
                featureId = "expanded_practice_insights"
            )
        )
    }

    private data class SandboxPurchaseRecord(
        val productId: String,
        val purchaseToken: String,
        val entitlement: Entitlement,
        val isAcknowledged: Boolean
    )
}

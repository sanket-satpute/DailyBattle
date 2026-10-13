package com.sanket_satpute_20.dailybattle.domain.monetization.billing

import com.sanket_satpute_20.dailybattle.domain.monetization.model.BillingProduct
import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for underlying platform billing providers (e.g. Google Play Billing, Sandbox Store).
 *
 * Implements Sprint 21.2 per `13_MONETIZATION.md` §22, §23, §25, §49, §55, §56:
 * - Domain layer is completely decoupled from any third-party billing SDK.
 * - Manages store connection lifecycle.
 * - Supports product catalog querying, purchase execution, acknowledgment, and restore operations.
 */
interface BillingProvider {

    /**
     * Indicates whether the billing provider is currently connected and operational.
     */
    val isAvailable: Boolean

    /**
     * Observable connection state between application and provider store.
     */
    val connectionState: StateFlow<BillingConnectionState>

    /**
     * Connects to the billing provider service. Returns true if connection succeeds.
     */
    suspend fun connect(): Boolean

    /**
     * Disconnects from the billing provider service.
     */
    suspend fun disconnect()

    /**
     * Queries product catalog details for specified [productIds].
     */
    suspend fun queryProductDetails(productIds: Set<String>): List<BillingProduct>

    /**
     * Launches a purchase transaction for [productId].
     */
    suspend fun initiatePurchase(productId: String): BillingPurchaseResult

    /**
     * Acknowledges an unacknowledged purchase transaction with the store.
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Boolean

    /**
     * Queries and restores previously purchased transactions from the store.
     */
    suspend fun restorePurchases(): BillingRestoreResult
}

/**
 * Result of a provider purchase transaction.
 */
sealed interface BillingPurchaseResult {
    data class Success(
        val productId: String,
        val entitlement: Entitlement,
        val purchaseToken: String? = null
    ) : BillingPurchaseResult

    data class Cancelled(
        val productId: String
    ) : BillingPurchaseResult

    data class Error(
        val productId: String,
        val message: String,
        val isNetworkError: Boolean = false,
        val isAlreadyOwned: Boolean = false,
        val isItemUnavailable: Boolean = false
    ) : BillingPurchaseResult
}

/**
 * Result of a provider restore operation.
 */
sealed interface BillingRestoreResult {
    data class Success(
        val restoredEntitlements: List<Entitlement>
    ) : BillingRestoreResult

    data class Error(
        val message: String,
        val isNetworkError: Boolean = false
    ) : BillingRestoreResult
}

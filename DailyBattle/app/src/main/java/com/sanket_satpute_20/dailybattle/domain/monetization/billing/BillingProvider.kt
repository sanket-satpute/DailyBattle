package com.sanket_satpute_20.dailybattle.domain.monetization.billing

import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement

/**
 * Abstraction for underlying platform billing providers (e.g. Google Play Billing).
 *
 * Implements Sprint 21.1 architecture boundaries per `13_MONETIZATION.md` §55, §56, §57:
 * - Domain layer is completely decoupled from any third-party billing SDK.
 * - Concrete integrations (Sprint 21.2) implement this contract.
 */
interface BillingProvider {

    /**
     * Indicates whether the billing provider is currently connected and operational.
     */
    val isAvailable: Boolean

    /**
     * Launches a purchase transaction for [productId].
     */
    suspend fun initiatePurchase(productId: String): BillingPurchaseResult

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

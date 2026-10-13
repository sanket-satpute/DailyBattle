package com.sanket_satpute_20.dailybattle.domain.monetization.model

/**
 * Categorized error types for purchase failures per `13_MONETIZATION.md` §37, §39.
 */
sealed interface PurchaseError {
    data class NetworkFailure(val message: String) : PurchaseError
    data class ItemAlreadyOwned(val productId: String) : PurchaseError
    data class ItemUnavailable(val productId: String) : PurchaseError
    data object UserCancelled : PurchaseError
    data class BillingUnavailable(val message: String) : PurchaseError
    data class InvalidVerification(val message: String) : PurchaseError
    data class GameplayProtectionViolation(val reason: String) : PurchaseError
    data class Unknown(val message: String, val cause: Throwable? = null) : PurchaseError
}

/**
 * Lifecycle state of a purchase transaction per `13_MONETIZATION.md` §36, §37, §38, §39:
 * - [Idle]: No purchase currently in flight.
 * - [Pending]: Purchase initiated; prevents duplicate flows (§38).
 * - [Success]: Purchase completed and verified; entitlement granted.
 * - [Failed]: Purchase failed; provides categorized error and retry capability flag.
 * - [Cancelled]: Purchase flow was dismissed or cancelled by the user.
 */
sealed interface PurchaseState {
    data object Idle : PurchaseState

    data class Pending(
        val productId: String
    ) : PurchaseState

    data class Success(
        val productId: String,
        val entitlement: Entitlement,
        val purchaseToken: String? = null
    ) : PurchaseState

    data class Failed(
        val productId: String,
        val error: PurchaseError,
        val canRetry: Boolean = true
    ) : PurchaseState

    data class Cancelled(
        val productId: String
    ) : PurchaseState
}

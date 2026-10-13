package com.sanket_satpute_20.dailybattle.domain.monetization.model

/**
 * Resolves user-facing error messages and recovery action instructions per `13_MONETIZATION.md` §37, §39:
 * - Understandable, transparent, non-deceptive feedback.
 * - Explicit indication of retry safety.
 */
object PurchaseErrorMessageResolver {

    data class ResolvedError(
        val code: String,
        val userMessage: String,
        val recoveryAction: String,
        val canRetry: Boolean
    )

    fun resolve(error: PurchaseError): ResolvedError {
        return when (error) {
            is PurchaseError.NetworkFailure -> ResolvedError(
                code = "ERR_PURCHASE_NETWORK",
                userMessage = "Network connection failed during purchase.",
                recoveryAction = "Check your connection and try again.",
                canRetry = true
            )
            is PurchaseError.ItemAlreadyOwned -> ResolvedError(
                code = "ERR_ALREADY_OWNED",
                userMessage = "You already own this item.",
                recoveryAction = "If it is not showing, tap 'Restore Purchases'.",
                canRetry = false
            )
            is PurchaseError.ItemUnavailable -> ResolvedError(
                code = "ERR_ITEM_UNAVAILABLE",
                userMessage = "This item is currently unavailable in the store.",
                recoveryAction = "Please check back later.",
                canRetry = false
            )
            is PurchaseError.UserCancelled -> ResolvedError(
                code = "USER_CANCELLED",
                userMessage = "Purchase was cancelled.",
                recoveryAction = "You can purchase this item at any time.",
                canRetry = true
            )
            is PurchaseError.BillingUnavailable -> ResolvedError(
                code = "ERR_BILLING_UNAVAILABLE",
                userMessage = "Store billing service is unreachable.",
                recoveryAction = "Ensure Google Play Store is up to date and try again.",
                canRetry = true
            )
            is PurchaseError.InvalidVerification -> ResolvedError(
                code = "ERR_INVALID_TOKEN",
                userMessage = "Purchase verification failed.",
                recoveryAction = "If your account was debited, please contact support.",
                canRetry = false
            )
            is PurchaseError.GameplayProtectionViolation -> ResolvedError(
                code = "ERR_GAMEPLAY_PROTECTION",
                userMessage = error.reason.ifBlank { "Action blocked by gameplay protection rules." },
                recoveryAction = "Commercial purchases cannot modify gameplay or interrupt battles.",
                canRetry = false
            )
            is PurchaseError.Unknown -> ResolvedError(
                code = "ERR_PURCHASE_UNKNOWN",
                userMessage = error.message.ifBlank { "An unexpected purchase error occurred." },
                recoveryAction = "Please try again later.",
                canRetry = true
            )
        }
    }

    fun resolve(error: RestoreError): ResolvedError {
        return when (error) {
            is RestoreError.NoPurchasesFound -> ResolvedError(
                code = "RESTORE_EMPTY",
                userMessage = "No previous purchases found for this account.",
                recoveryAction = "Make sure you are signed into the correct Google account.",
                canRetry = false
            )
            is RestoreError.NetworkFailure -> ResolvedError(
                code = "RESTORE_NETWORK",
                userMessage = "Network error while restoring purchases.",
                recoveryAction = "Check your network connection and retry.",
                canRetry = true
            )
            is RestoreError.BillingUnavailable -> ResolvedError(
                code = "RESTORE_UNAVAILABLE",
                userMessage = "Store billing service is unreachable.",
                recoveryAction = "Please try again in a few moments.",
                canRetry = true
            )
            is RestoreError.Unknown -> ResolvedError(
                code = "RESTORE_UNKNOWN",
                userMessage = error.message.ifBlank { "Failed to restore purchases." },
                recoveryAction = "Please try again later.",
                canRetry = true
            )
        }
    }
}

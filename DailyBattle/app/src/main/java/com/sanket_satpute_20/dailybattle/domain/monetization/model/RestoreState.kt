package com.sanket_satpute_20.dailybattle.domain.monetization.model

/**
 * Categorized error types for restore failures per `13_MONETIZATION.md` §27, §39.
 */
sealed interface RestoreError {
    data class NetworkFailure(val message: String) : RestoreError
    data object NoPurchasesFound : RestoreError
    data class BillingUnavailable(val message: String) : RestoreError
    data class Unknown(val message: String, val cause: Throwable? = null) : RestoreError
}

/**
 * Lifecycle state of an entitlement restoration operation per `13_MONETIZATION.md` §27, §39:
 * - [Idle]: No restore operation currently active.
 * - [Restoring]: Querying remote billing platform / store for existing purchases.
 * - [Success]: Entitlements reconciled successfully.
 * - [Failed]: Restore failed due to network, store unavailability, or missing records.
 */
sealed interface RestoreState {
    data object Idle : RestoreState

    data object Restoring : RestoreState

    data class Success(
        val restoredEntitlements: List<Entitlement>
    ) : RestoreState

    data class Failed(
        val error: RestoreError,
        val canRetry: Boolean = true
    ) : RestoreState
}

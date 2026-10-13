package com.sanket_satpute_20.dailybattle.domain.monetization.billing

/**
 * Lifecycle state of the connection between the application and the billing provider store.
 */
sealed interface BillingConnectionState {
    data object Disconnected : BillingConnectionState
    data object Connecting : BillingConnectionState
    data object Connected : BillingConnectionState
    data class Unavailable(val message: String) : BillingConnectionState
}

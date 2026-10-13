package com.sanket_satpute_20.dailybattle.data.monetization

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingConnectionState
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingPurchaseResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingRestoreResult
import com.sanket_satpute_20.dailybattle.domain.monetization.model.BillingProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fallback no-op [BillingProvider] used when no billing provider is active.
 */
@Singleton
class NoOpBillingProvider @Inject constructor() : BillingProvider {

    override val isAvailable: Boolean = false

    private val _connectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    override val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    override suspend fun connect(): Boolean {
        _connectionState.value = BillingConnectionState.Unavailable("Billing provider is not configured.")
        return false
    }

    override suspend fun disconnect() {
        _connectionState.value = BillingConnectionState.Disconnected
    }

    override suspend fun queryProductDetails(productIds: Set<String>): List<BillingProduct> {
        return emptyList()
    }

    override suspend fun initiatePurchase(productId: String): BillingPurchaseResult {
        return BillingPurchaseResult.Error(
            productId = productId,
            message = "Billing provider is not configured or unavailable.",
            isNetworkError = false
        )
    }

    override suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        return false
    }

    override suspend fun restorePurchases(): BillingRestoreResult {
        return BillingRestoreResult.Error(
            message = "Billing provider is not configured or unavailable.",
            isNetworkError = false
        )
    }
}

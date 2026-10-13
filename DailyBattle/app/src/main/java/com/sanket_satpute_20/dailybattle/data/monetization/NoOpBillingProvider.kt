package com.sanket_satpute_20.dailybattle.data.monetization

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingPurchaseResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingRestoreResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fallback no-op [BillingProvider] used when no third-party billing SDK is configured.
 *
 * Enforces `13_MONETIZATION.md` §55, §58, §64:
 * - Prevents unauthorized third-party billing dependencies from being added before approval.
 * - Safely returns billing unavailable for purchase operations without crashing or corrupting state.
 */
@Singleton
class NoOpBillingProvider @Inject constructor() : BillingProvider {

    override val isAvailable: Boolean = false

    override suspend fun initiatePurchase(productId: String): BillingPurchaseResult {
        return BillingPurchaseResult.Error(
            productId = productId,
            message = "Billing provider is not configured or unavailable.",
            isNetworkError = false
        )
    }

    override suspend fun restorePurchases(): BillingRestoreResult {
        return BillingRestoreResult.Error(
            message = "Billing provider is not configured or unavailable.",
            isNetworkError = false
        )
    }
}

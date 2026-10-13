package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.model.BillingProduct
import javax.inject.Inject

/**
 * Queries available purchasable products from the billing provider catalog.
 *
 * Implements Sprint 21.2 per `13_MONETIZATION.md` §22, §23:
 * - Transparent product identification, title, description, and localized pricing.
 */
class QueryAvailableProductsUseCase @Inject constructor(
    private val billingProvider: BillingProvider
) {
    suspend operator fun invoke(productIds: Set<String>): List<BillingProduct> {
        if (!billingProvider.isAvailable) {
            billingProvider.connect()
        }
        return billingProvider.queryProductDetails(productIds)
    }
}

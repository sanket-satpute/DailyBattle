package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingPurchaseResult
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import javax.inject.Inject

/**
 * Handles product purchase execution with duplicate protection and safe error handling.
 *
 * Implements Sprint 21.1 per `13_MONETIZATION.md` §37, §38, §39:
 * - Duplicate purchase protection (§38): Prevents concurrent or duplicate application-level purchase flows.
 * - Error isolation (§37): Failed or cancelled purchases never grant entitlements or corrupt user state.
 */
class PurchaseProductUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository,
    private val billingProvider: BillingProvider
) {
    suspend operator fun invoke(productId: String, featureId: String): PurchaseState {
        val currentState = entitlementRepository.purchaseState.value

        // Duplicate purchase protection (§38): Prevent starting a new flow if one is already pending
        if (currentState is PurchaseState.Pending) {
            return currentState
        }

        // Check if item is already active
        if (entitlementRepository.hasAccessToFeature(featureId)) {
            val failedState = PurchaseState.Failed(
                productId = productId,
                error = PurchaseError.ItemAlreadyOwned(productId),
                canRetry = false
            )
            entitlementRepository.updatePurchaseState(failedState)
            return failedState
        }

        // Transition to Pending
        val pendingState = PurchaseState.Pending(productId)
        entitlementRepository.updatePurchaseState(pendingState)

        // Delegate to billing provider
        val result = billingProvider.initiatePurchase(productId)

        val finalState = when (result) {
            is BillingPurchaseResult.Success -> {
                entitlementRepository.grantEntitlement(result.entitlement)
                PurchaseState.Success(
                    productId = productId,
                    entitlement = result.entitlement,
                    purchaseToken = result.purchaseToken
                )
            }
            is BillingPurchaseResult.Cancelled -> {
                PurchaseState.Cancelled(productId = productId)
            }
            is BillingPurchaseResult.Error -> {
                val error = when {
                    result.isNetworkError -> PurchaseError.NetworkFailure(result.message)
                    result.isAlreadyOwned -> PurchaseError.ItemAlreadyOwned(productId)
                    result.isItemUnavailable -> PurchaseError.ItemUnavailable(productId)
                    !billingProvider.isAvailable -> PurchaseError.BillingUnavailable(result.message)
                    else -> PurchaseError.Unknown(result.message)
                }
                PurchaseState.Failed(
                    productId = productId,
                    error = error,
                    canRetry = !result.isAlreadyOwned
                )
            }
        }

        entitlementRepository.updatePurchaseState(finalState)
        return finalState
    }
}

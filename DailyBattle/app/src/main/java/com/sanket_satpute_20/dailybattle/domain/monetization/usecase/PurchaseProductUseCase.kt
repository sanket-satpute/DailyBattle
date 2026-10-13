package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingPurchaseResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.DefaultPurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerificationResult
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Handles product purchase execution with provider integration, token verification, and acknowledgment.
 *
 * Implements Sprint 21.2 & Sprint 21.3 per `13_MONETIZATION.md` §25, §37, §38, §39:
 * - Duplicate purchase protection (§38): Atomically guards against concurrent rapid taps or duplicate requests.
 * - Token verification (§25): Enforces signature and token validation before granting entitlement.
 * - Acknowledgment pipeline: Acknowledges valid purchases with the store provider.
 * - Error isolation (§37): Failed or cancelled purchases never grant entitlements or corrupt user state.
 */
class PurchaseProductUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository,
    private val billingProvider: BillingProvider,
    private val purchaseVerifier: PurchaseVerifier = DefaultPurchaseVerifier()
) {
    private val purchaseMutex = Mutex()

    suspend operator fun invoke(productId: String, featureId: String): PurchaseState {
        // Atomic duplicate protection (§38): check state and lock entry
        val earlyReturnState: PurchaseState? = purchaseMutex.withLock {
            val currentState = entitlementRepository.purchaseState.value

            if (currentState is PurchaseState.Pending) {
                return@withLock currentState
            }

            // Check if item is already active
            if (entitlementRepository.hasAccessToFeature(featureId)) {
                val failedState = PurchaseState.Failed(
                    productId = productId,
                    error = PurchaseError.ItemAlreadyOwned(productId),
                    canRetry = false
                )
                entitlementRepository.updatePurchaseState(failedState)
                return@withLock failedState
            }

            // Set to Pending atomically
            val pendingState = PurchaseState.Pending(productId)
            entitlementRepository.updatePurchaseState(pendingState)
            null
        }

        if (earlyReturnState != null) {
            return earlyReturnState
        }

        // Ensure provider connection
        if (!billingProvider.isAvailable) {
            val connected = billingProvider.connect()
            if (!connected) {
                val unavailableState = PurchaseState.Failed(
                    productId = productId,
                    error = PurchaseError.BillingUnavailable("Failed to connect to billing provider store."),
                    canRetry = true
                )
                entitlementRepository.updatePurchaseState(unavailableState)
                return unavailableState
            }
        }

        // Delegate to billing provider
        val result = billingProvider.initiatePurchase(productId)

        val finalState = when (result) {
            is BillingPurchaseResult.Success -> {
                // 1. Verify token authenticity (§25)
                val verification = purchaseVerifier.verifyPurchase(productId, result.purchaseToken)
                if (verification is PurchaseVerificationResult.Invalid) {
                    PurchaseState.Failed(
                        productId = productId,
                        error = PurchaseError.InvalidVerification(verification.reason),
                        canRetry = false
                    )
                } else {
                    // 2. Acknowledge with store provider
                    if (!result.purchaseToken.isNullOrBlank()) {
                        billingProvider.acknowledgePurchase(result.purchaseToken)
                    }

                    // 3. Grant entitlement into repository
                    entitlementRepository.grantEntitlement(result.entitlement)

                    PurchaseState.Success(
                        productId = productId,
                        entitlement = result.entitlement,
                        purchaseToken = result.purchaseToken
                    )
                }
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
                    canRetry = !result.isAlreadyOwned && !result.isItemUnavailable
                )
            }
        }

        entitlementRepository.updatePurchaseState(finalState)
        return finalState
    }
}

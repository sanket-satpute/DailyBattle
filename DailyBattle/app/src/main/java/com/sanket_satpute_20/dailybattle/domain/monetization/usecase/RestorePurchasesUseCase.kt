package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingRestoreResult
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreError
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreState
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Handles purchase restoration per `13_MONETIZATION.md` §27, §39:
 * - Queries store for existing purchases.
 * - Reconciles restored entitlements into [EntitlementRepository].
 * - Distinguishes empty purchases, network failures, and successful restorations.
 * - Atomically prevents duplicate concurrent restore requests.
 */
class RestorePurchasesUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository,
    private val billingProvider: BillingProvider
) {
    private val restoreMutex = Mutex()

    suspend operator fun invoke(): RestoreState {
        // Prevent concurrent restores atomically
        val earlyReturnState: RestoreState? = restoreMutex.withLock {
            if (entitlementRepository.restoreState.value is RestoreState.Restoring) {
                return@withLock entitlementRepository.restoreState.value
            }
            entitlementRepository.updateRestoreState(RestoreState.Restoring)
            null
        }

        if (earlyReturnState != null) {
            return earlyReturnState
        }

        // Ensure provider connection
        if (!billingProvider.isAvailable) {
            val connected = billingProvider.connect()
            if (!connected) {
                val unavailableState = RestoreState.Failed(
                    error = RestoreError.BillingUnavailable("Billing provider store unavailable"),
                    canRetry = true
                )
                entitlementRepository.updateRestoreState(unavailableState)
                return unavailableState
            }
        }

        val result = billingProvider.restorePurchases()

        val finalState = when (result) {
            is BillingRestoreResult.Success -> {
                if (result.restoredEntitlements.isEmpty()) {
                    RestoreState.Failed(RestoreError.NoPurchasesFound, canRetry = false)
                } else {
                    for (entitlement in result.restoredEntitlements) {
                        entitlementRepository.grantEntitlement(entitlement)
                    }
                    RestoreState.Success(result.restoredEntitlements)
                }
            }
            is BillingRestoreResult.Error -> {
                val error = when {
                    result.isNetworkError -> RestoreError.NetworkFailure(result.message)
                    !billingProvider.isAvailable -> RestoreError.BillingUnavailable(result.message)
                    else -> RestoreError.Unknown(result.message)
                }
                RestoreState.Failed(error = error, canRetry = true)
            }
        }

        entitlementRepository.updateRestoreState(finalState)
        return finalState
    }
}

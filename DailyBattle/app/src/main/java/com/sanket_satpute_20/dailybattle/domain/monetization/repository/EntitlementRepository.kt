package com.sanket_satpute_20.dailybattle.domain.monetization.repository

import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreState
import kotlinx.coroutines.flow.StateFlow

/**
 * Centralized repository contract for managing entitlement, purchase, and restore lifecycles.
 *
 * Implements Sprint 21.1 per `13_MONETIZATION.md` §24, §36, §37, §38, §39:
 * - Centralizes entitlement evaluations (User -> Entitlements -> Feature access).
 * - Exposes reactive StateFlows for entitlement, purchase, and restore states.
 * - Enforces dynamic expiration checking.
 * - Isolates monetization from gameplay (REQ-MON-001 - REQ-MON-005).
 */
interface EntitlementRepository {

    /**
     * Observable stream of the user's aggregate entitlement status.
     */
    val entitlementState: StateFlow<EntitlementState>

    /**
     * Observable stream of the current purchase transaction lifecycle.
     */
    val purchaseState: StateFlow<PurchaseState>

    /**
     * Observable stream of the current restore transaction lifecycle.
     */
    val restoreState: StateFlow<RestoreState>

    /**
     * Returns all currently active entitlements (filtering out expired ones).
     */
    suspend fun getActiveEntitlements(): List<Entitlement>

    /**
     * Returns all recorded entitlements regardless of expiration.
     */
    suspend fun getAllEntitlements(): List<Entitlement>

    /**
     * Checks whether the user is entitled to access a specific feature.
     */
    suspend fun hasAccessToFeature(featureId: String): Boolean

    /**
     * Grants a new or restored entitlement, updating the aggregate [entitlementState].
     */
    suspend fun grantEntitlement(entitlement: Entitlement)

    /**
     * Revokes an entitlement by ID (e.g. refund, cancellation).
     */
    suspend fun revokeEntitlement(entitlementId: String)

    /**
     * Clears all entitlements (e.g. account deletion / right-to-erasure wipe).
     */
    suspend fun clearAllEntitlements()

    /**
     * Explicitly sets aggregate entitlement state to [EntitlementState.Unknown] (e.g. unverified store or network partition).
     */
    suspend fun setUnknownState(reason: String? = null)

    /**
     * Updates the observable [purchaseState].
     */
    suspend fun updatePurchaseState(newState: PurchaseState)

    /**
     * Resets [purchaseState] back to [PurchaseState.Idle].
     */
    suspend fun resetPurchaseState()

    /**
     * Updates the observable [restoreState].
     */
    suspend fun updateRestoreState(newState: RestoreState)

    /**
     * Resets [restoreState] back to [RestoreState.Idle].
     */
    suspend fun resetRestoreState()
}

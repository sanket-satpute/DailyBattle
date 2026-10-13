package com.sanket_satpute_20.dailybattle.domain.monetization.model

/**
 * Represents the aggregate entitlement status of the current user.
 *
 * Implements Sprint 21.1 per `13_MONETIZATION.md` §36:
 * - [Free]: Default state; no active entitlements (REQ-MON-001: core experience is 100% free).
 * - [Premium]: User possesses one or more currently active entitlements.
 * - [Expired]: User possessed entitlements in the past, but all have expired.
 * - [Unknown]: Entitlement state could not be resolved (e.g. offline with no cache, or uninitialized).
 */
sealed interface EntitlementState {
    /**
     * Default core state. No active entitlements.
     */
    data object Free : EntitlementState

    /**
     * User has one or more active entitlements.
     */
    data class Premium(
        val activeEntitlements: List<Entitlement>
    ) : EntitlementState

    /**
     * All previously granted entitlements have expired.
     */
    data class Expired(
        val expiredEntitlements: List<Entitlement>
    ) : EntitlementState

    /**
     * Entitlement state could not be determined.
     */
    data class Unknown(
        val message: String? = null
    ) : EntitlementState
}

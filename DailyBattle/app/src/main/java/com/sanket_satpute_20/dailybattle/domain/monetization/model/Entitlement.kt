package com.sanket_satpute_20.dailybattle.domain.monetization.model

/**
 * Represents a user entitlement granting access to an optional premium feature.
 *
 * Implements Sprint 21.1 per `ROADMAP.md` and `13_MONETIZATION.md` §24, §36:
 * - Distinguishes User -> Entitlements -> Feature access.
 * - Supports both non-expiring (lifetime / one-time) and expiring (subscription / promotional) models.
 * - [expiresAt] is nullable; null indicates lifetime/permanent entitlement.
 */
data class Entitlement(
    val id: String,
    val featureId: String,
    val grantedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val source: EntitlementSource = EntitlementSource.PURCHASE
) {
    /**
     * Determines whether this entitlement has expired relative to [currentTimeMillis].
     */
    fun isExpired(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return expiresAt != null && currentTimeMillis >= expiresAt
    }

    /**
     * Determines whether this entitlement is currently active relative to [currentTimeMillis].
     */
    fun isActive(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return !isExpired(currentTimeMillis)
    }
}

/**
 * The origin or acquisition channel of an entitlement.
 */
enum class EntitlementSource {
    PURCHASE,
    RESTORE,
    PROMOTION,
    DEFAULT
}

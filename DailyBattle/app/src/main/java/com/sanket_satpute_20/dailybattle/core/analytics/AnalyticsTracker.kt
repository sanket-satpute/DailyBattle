package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * High-level analytics abstraction consumed by Presentation, Domain, and Core layers.
 *
 * Per `14_ANALYTICS.md` §57 & §58:
 * - Decouples application and gameplay from concrete analytics destinations.
 * - Enforces non-blocking, observational analytics (REQ-AN-001, REQ-AN-003, REQ-AN-015).
 * - Ensures event naming, validation, and privacy filtering.
 */
interface AnalyticsTracker {

    /**
     * Records an explicit [AnalyticsEvent].
     */
    fun track(event: AnalyticsEvent)

    /**
     * Convenience method to track an event by name with optional properties.
     */
    fun track(eventName: String, properties: Map<String, Any?> = emptyMap())

    /**
     * Sets a non-sensitive user property (e.g. theme preference).
     */
    fun setUserProperty(name: String, value: String?)

    /**
     * Associates subsequent events with a non-sensitive user identifier, or null for anonymous users.
     */
    fun setUserId(userId: String?)
}

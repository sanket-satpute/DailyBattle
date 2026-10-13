package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * Contract for concrete analytics destinations/providers (e.g. In-Memory test sink,
 * Logging provider, or future third-party analytics SDKs).
 *
 * Per `14_ANALYTICS.md` §57 & §60:
 * - Providers are isolated from UI and domain logic.
 * - No third-party analytics SDK is currently locked (AN-PENDING-001).
 * - Provider failures must be safely caught by the abstraction and not propagate.
 */
interface AnalyticsProvider {

    /**
     * Unique identifier for this provider (e.g. "InMemory", "ConsoleLogger", "NoOp").
     */
    val name: String

    /**
     * Whether this provider is currently active and accepting events.
     */
    val isEnabled: Boolean

    /**
     * Dispatches an event to this provider.
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * Sets a non-sensitive user property for subsequent event attribution.
     */
    fun setUserProperty(name: String, value: String?)

    /**
     * Sets a non-sensitive user identifier, or null for anonymous sessions.
     */
    fun setUserId(userId: String?)

    /**
     * Flushes any buffered events if applicable.
     */
    fun flush() {}
}

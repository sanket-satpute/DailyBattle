package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * A safe, no-operation analytics provider.
 * Used when analytics is disabled or in unconfigured environments.
 */
class NoOpAnalyticsProvider : AnalyticsProvider {

    override val name: String = "NoOp"

    override val isEnabled: Boolean = false

    override fun logEvent(event: AnalyticsEvent) {
        // No-op
    }

    override fun setUserProperty(name: String, value: String?) {
        // No-op
    }

    override fun setUserId(userId: String?) {
        // No-op
    }
}

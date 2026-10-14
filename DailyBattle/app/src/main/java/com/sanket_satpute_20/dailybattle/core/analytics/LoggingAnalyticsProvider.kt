package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * Analytics provider that logs analytics events for local debugging.
 *
 * Per `14_ANALYTICS.md` §85:
 * "During development, analytics events should be inspectable through an approved debugging mechanism."
 */
class LoggingAnalyticsProvider(
    override val name: String = "Logging",
    override val isEnabled: Boolean = true,
    private val logOutput: (String) -> Unit = {}
) : AnalyticsProvider {

    override fun logEvent(event: AnalyticsEvent) {
        if (!isEnabled) return
        logOutput("[Analytics:$name] Event: ${event.name} -> properties=${event.properties} timestamp=${event.timestamp}")
    }

    override fun setUserProperty(name: String, value: String?) {
        if (!isEnabled) return
        logOutput("[Analytics:$name] UserProperty: $name = $value")
    }

    override fun setUserId(userId: String?) {
        if (!isEnabled) return
        logOutput("[Analytics:$name] UserId: $userId")
    }
}

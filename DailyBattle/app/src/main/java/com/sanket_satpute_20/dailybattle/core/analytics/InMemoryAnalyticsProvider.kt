package com.sanket_satpute_20.dailybattle.core.analytics

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread-safe, in-memory analytics provider for testing and diagnostic verification.
 *
 * Per `14_ANALYTICS.md` §79 & §80 (REQ-AN-010):
 * "Every analytics implementation must be testable without relying exclusively on a production dashboard."
 */
class InMemoryAnalyticsProvider(
    override val name: String = "InMemory",
    override var isEnabled: Boolean = true
) : AnalyticsProvider {

    private val _events = CopyOnWriteArrayList<AnalyticsEvent>()
    private val _userProperties = ConcurrentHashMap<String, String>()
    @Volatile
    private var _currentUserId: String? = null

    override fun logEvent(event: AnalyticsEvent) {
        if (!isEnabled) return
        _events.add(event)
    }

    override fun setUserProperty(name: String, value: String?) {
        if (!isEnabled) return
        if (value != null) {
            _userProperties[name] = value
        } else {
            _userProperties.remove(name)
        }
    }

    override fun setUserId(userId: String?) {
        if (!isEnabled) return
        _currentUserId = userId
    }

    /**
     * Returns an unmodifiable snapshot of all recorded events in insertion order.
     */
    fun getRecordedEvents(): List<AnalyticsEvent> = _events.toList()

    /**
     * Returns all recorded events matching the specified [eventName].
     */
    fun getEventsByName(eventName: String): List<AnalyticsEvent> =
        _events.filter { it.name == eventName }

    /**
     * Returns a snapshot of current user properties.
     */
    fun getUserProperties(): Map<String, String> = _userProperties.toMap()

    /**
     * Returns the current associated user ID.
     */
    fun getUserId(): String? = _currentUserId

    /**
     * Clears all recorded events, user properties, and user ID.
     */
    fun clear() {
        _events.clear()
        _userProperties.clear()
        _currentUserId = null
    }
}

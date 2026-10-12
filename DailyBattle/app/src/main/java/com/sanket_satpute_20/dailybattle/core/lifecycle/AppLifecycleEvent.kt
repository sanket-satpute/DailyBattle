package com.sanket_satpute_20.dailybattle.core.lifecycle

/**
 * Discrete lifecycle events emitted upon application state transitions.
 *
 * Conforms to:
 * - 07_TECHNICAL_ARCHITECTURE.md §28 (Lifecycle Handling)
 * - 15_ANTIGRAVITY_RULES.md Rule 43 (Lifecycle Rule)
 */
sealed interface AppLifecycleEvent {

    /**
     * Application transitioned to the foreground.
     *
     * @param timestamp Epoch milliseconds when foreground transition occurred.
     * @param backgroundDurationMs Elapsed milliseconds spent in the background prior to returning.
     */
    data class Foreground(
        val timestamp: Long,
        val backgroundDurationMs: Long,
    ) : AppLifecycleEvent

    /**
     * Application transitioned to the background.
     *
     * @param timestamp Epoch milliseconds when background transition occurred.
     * @param reason The triggering context ([BackgroundReason]).
     */
    data class Background(
        val timestamp: Long,
        val reason: BackgroundReason,
    ) : AppLifecycleEvent
}

/**
 * Listener interface for observing lifecycle events.
 */
fun interface AppLifecycleListener {
    fun onLifecycleEvent(event: AppLifecycleEvent)
}

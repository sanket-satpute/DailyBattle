package com.sanket_satpute_20.dailybattle.core.lifecycle

/**
 * Underlying catalyst for transitioning the application into the background.
 *
 * Conforms to:
 * - 02_GAMEPLAY.md §28 (Interruption causes: Home, app switcher, notification, screen lock, system dialog)
 * - 07_TECHNICAL_ARCHITECTURE.md §28 (Lifecycle Handling)
 * - ROADMAP.md Sprint 18.1 (Background, foreground, screen lock, notification interruption, app switching)
 */
enum class BackgroundReason {
    /**
     * User pressed Home or navigated away via system navigation.
     */
    USER_NAVIGATION,

    /**
     * Device screen was turned off or locked while app was active.
     */
    SCREEN_LOCK,

    /**
     * Interrupted by an incoming notification or user interacting with notification shade.
     */
    NOTIFICATION_INTERRUPT,

    /**
     * Interrupted by a system dialog, incoming call, or runtime permission prompt.
     */
    SYSTEM_INTERRUPT,

    /**
     * User invoked the Android app switcher (Recents) or switched to another task.
     */
    APP_SWITCHING,

    /**
     * Unspecified backgrounding event.
     */
    UNKNOWN,
}

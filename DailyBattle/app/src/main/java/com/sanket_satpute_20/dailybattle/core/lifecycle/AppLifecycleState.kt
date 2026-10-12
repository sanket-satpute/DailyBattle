package com.sanket_satpute_20.dailybattle.core.lifecycle

/**
 * High-level application lifecycle states.
 *
 * Conforms to:
 * - 07_TECHNICAL_ARCHITECTURE.md §28 (Lifecycle Handling)
 * - 15_ANTIGRAVITY_RULES.md Rule 43 (Lifecycle Rule)
 */
enum class AppLifecycleState {
    /**
     * Application is visible and interactive in the foreground.
     */
    FOREGROUND,

    /**
     * Application is in the background (home pressed, app switching, screen lock, or interruption).
     */
    BACKGROUND,
}

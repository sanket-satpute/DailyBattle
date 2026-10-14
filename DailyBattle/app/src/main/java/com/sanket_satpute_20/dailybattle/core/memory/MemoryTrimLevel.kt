@file:Suppress("DEPRECATION")

package com.sanket_satpute_20.dailybattle.core.memory

import android.content.ComponentCallbacks2

/**
 * Strongly-typed representation of Android OS memory trim levels.
 *
 * Conforms to:
 * - `07_TECHNICAL_ARCHITECTURE.md` §48 (Performance Requirements)
 * - `15_ANTIGRAVITY_RULES.md` Rule 98 (Memory Rule)
 * - `ROADMAP.md` Sprint 24.4 (Memory)
 */
enum class MemoryTrimLevel(val androidLevel: Int) {
    /** App is running and device is beginning to run low on memory. */
    RUNNING_MODERATE(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE),

    /** App is running and device is running much lower on memory. */
    RUNNING_LOW(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW),

    /** App is running and system is severely low on memory; kill candidate if not trimmed. */
    RUNNING_CRITICAL(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL),

    /** App's UI is no longer visible (e.g. backgrounded); prime opportunity to free UI resources. */
    UI_HIDDEN(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN),

    /** App is in background LRU cache list and system is running low on memory. */
    BACKGROUND(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND),

    /** App is in background LRU cache near the middle of the list. */
    MODERATE(ComponentCallbacks2.TRIM_MEMORY_MODERATE),

    /** App is in background LRU cache near the end of the list and likely to be killed. */
    COMPLETE(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);

    val isCritical: Boolean
        get() = this == RUNNING_CRITICAL || this == COMPLETE

    val isUiHidden: Boolean
        get() = this == UI_HIDDEN || this == BACKGROUND || this == MODERATE || this == COMPLETE

    companion object {
        fun fromAndroidLevel(level: Int): MemoryTrimLevel {
            return entries.firstOrNull { it.androidLevel == level } ?: when {
                level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> COMPLETE
                level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE -> MODERATE
                level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> BACKGROUND
                level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> UI_HIDDEN
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> RUNNING_CRITICAL
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> RUNNING_LOW
                else -> RUNNING_MODERATE
            }
        }
    }
}

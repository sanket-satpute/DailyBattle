package com.sanket_satpute_20.dailybattle.core.memory

/**
 * Listener interface for subsystems that hold caches or temporary graphical resources.
 *
 * Implements Sprint 24.4 memory trim dispatch per `docs/ROADMAP.md` and `docs/15_ANTIGRAVITY_RULES.md` Rule 98.
 */
interface MemoryTrimCallback {
    /**
     * Called when the system requests memory trimming.
     * Implementations should evict caches or release unneeded graphics buffers based on [trimLevel].
     */
    fun onTrimMemory(trimLevel: MemoryTrimLevel)

    /**
     * Called when the system is running critically low on memory.
     * Implementations must release all non-essential resources immediately.
     */
    fun onLowMemory()
}

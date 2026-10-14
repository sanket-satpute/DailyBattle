package com.sanket_satpute_20.dailybattle.core.memory

import android.content.ComponentCallbacks2

/**
 * Authoritative manager for application-wide memory governance per Sprint 24.4:
 * 1. Checks and reports memory leaks (via weak reference tracking of transient components)
 * 2. Verifies retained screens are disposed on navigation exit
 * 3. Prevents large image/resource retention via LRU caching and memory trim callbacks
 * 4. Prunes and clears unnecessary in-memory caches on system memory pressure
 *
 * Conforms to:
 * - `07_TECHNICAL_ARCHITECTURE.md` §48 (Performance Requirements)
 * - `15_ANTIGRAVITY_RULES.md` Rule 98 (Memory Rule)
 * - `12_TESTING_AND_QA.md` §57 (Memory / Resource Testing)
 */
interface AppMemoryManager : ComponentCallbacks2 {
    val currentHeapBytes: Long
    val maxHeapBytes: Long
    val heapUtilizationRatio: Double

    /** Registers a callback to receive memory trim notifications. */
    fun registerTrimCallback(callback: MemoryTrimCallback)

    /** Unregisters a memory trim callback. */
    fun unregisterTrimCallback(callback: MemoryTrimCallback)

    /** Manually triggers memory trim by level (e.g. testing or system dispatch). */
    override fun onTrimMemory(level: Int)

    /** Manually triggers low memory handling. */
    @Deprecated("Deprecated in ComponentCallbacks", ReplaceWith("onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)"))
    override fun onLowMemory()

    /**
     * Evicts unnecessary in-memory caches and clears transient data.
     * When [isCritical] is true, purges all non-authoritative cached data.
     */
    fun releaseUnnecessaryCaches(isCritical: Boolean = false)

    /**
     * Tracks a transient object (e.g. screen, ViewModel, or listener) via WeakReference
     * to monitor lifecycle disposal and detect memory leaks.
     */
    fun trackRetainedObject(key: String, target: Any)

    /**
     * Returns keys of tracked objects that are still retained in memory.
     */
    fun getRetainedObjectKeys(): List<String>

    /** Clears all tracked object references. */
    fun clearTrackedObjects()

    /** Produces a comprehensive memory diagnostic report. */
    fun getMemoryReport(): MemoryReport
}

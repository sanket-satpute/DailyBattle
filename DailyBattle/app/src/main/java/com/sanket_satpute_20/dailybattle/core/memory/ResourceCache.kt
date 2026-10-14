package com.sanket_satpute_20.dailybattle.core.memory

import java.util.Collections
import java.util.LinkedHashMap

/**
 * Bounded LRU in-memory resource cache enforcing strict memory budgets per Sprint 24.4:
 * - Evicts least-recently-used items when capacity is reached
 * - Integrates with [MemoryTrimCallback] to clear or trim cached resources on system memory pressure
 * - Prevents large image and resource retention per `docs/ROADMAP.md` and `docs/15_ANTIGRAVITY_RULES.md` Rule 98
 */
class ResourceCache<K : Any, V : Any>(
    val maxCapacity: Int = 50,
) : MemoryTrimCallback {

    init {
        require(maxCapacity > 0) { "maxCapacity must be > 0. Found: $maxCapacity" }
    }

    private val map: MutableMap<K, V> = Collections.synchronizedMap(
        object : LinkedHashMap<K, V>(maxCapacity, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
                return size > maxCapacity
            }
        }
    )

    fun get(key: K): V? = map[key]

    fun put(key: K, value: V) {
        map[key] = value
    }

    fun remove(key: K): V? = map.remove(key)

    fun clear() {
        map.clear()
    }

    fun size(): Int = map.size

    val keys: Set<K>
        get() = synchronized(map) { map.keys.toSet() }

    override fun onTrimMemory(trimLevel: MemoryTrimLevel) {
        if (trimLevel.isCritical) {
            clear()
        } else if (trimLevel.isUiHidden) {
            val targetSize = maxCapacity / 2
            synchronized(map) {
                val iterator = map.entries.iterator()
                while (map.size > targetSize && iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }
    }

    override fun onLowMemory() {
        clear()
    }
}

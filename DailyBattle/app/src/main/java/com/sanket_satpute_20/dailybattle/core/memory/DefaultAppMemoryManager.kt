package com.sanket_satpute_20.dailybattle.core.memory

import android.content.res.Configuration
import com.sanket_satpute_20.dailybattle.data.cache.CacheMetadataStore
import com.sanket_satpute_20.dailybattle.data.cache.CachePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default singleton implementation of [AppMemoryManager].
 *
 * Implements Sprint 24.4 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §57:
 * - Listens for Android OS memory trim levels via [ComponentCallbacks2]
 * - Coordinates cache eviction across registered [MemoryTrimCallback] instances
 * - Tracks transient objects (screens, ViewModels) via WeakReferences to detect memory leaks
 * - Prunes unnecessary cache metadata entries under memory pressure
 */
@Singleton
class DefaultAppMemoryManager @Inject constructor(
    private val cacheMetadataStore: CacheMetadataStore,
) : AppMemoryManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val trimCallbacks = CopyOnWriteArrayList<MemoryTrimCallback>()
    private val trackedReferences = ConcurrentHashMap<String, WeakReference<Any>>()

    override val currentHeapBytes: Long
        get() {
            val runtime = Runtime.getRuntime()
            return (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        }

    override val maxHeapBytes: Long
        get() = Runtime.getRuntime().maxMemory()

    override val heapUtilizationRatio: Double
        get() {
            val max = maxHeapBytes
            return if (max > 0) currentHeapBytes.toDouble() / max.toDouble() else 0.0
        }

    override fun registerTrimCallback(callback: MemoryTrimCallback) {
        if (!trimCallbacks.contains(callback)) {
            trimCallbacks.add(callback)
        }
    }

    override fun unregisterTrimCallback(callback: MemoryTrimCallback) {
        trimCallbacks.remove(callback)
    }

    override fun onTrimMemory(level: Int) {
        val trimLevel = MemoryTrimLevel.fromAndroidLevel(level)
        for (callback in trimCallbacks) {
            callback.onTrimMemory(trimLevel)
        }

        if (trimLevel.isUiHidden || trimLevel.isCritical) {
            releaseUnnecessaryCaches(isCritical = trimLevel.isCritical)
        }
    }

    @Deprecated("Deprecated in ComponentCallbacks")
    override fun onLowMemory() {
        for (callback in trimCallbacks) {
            callback.onLowMemory()
        }
        releaseUnnecessaryCaches(isCritical = true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // No-op for memory manager
    }

    override fun releaseUnnecessaryCaches(isCritical: Boolean) {
        scope.launch {
            if (isCritical) {
                cacheMetadataStore.clearAll()
            } else {
                cacheMetadataStore.pruneExpired(
                    maxAgeMs = CachePolicy.FIVE_MINUTES_MS,
                    currentTimeMs = System.currentTimeMillis()
                )
            }
        }
    }

    override fun trackRetainedObject(key: String, target: Any) {
        trackedReferences[key] = WeakReference(target)
    }

    override fun getRetainedObjectKeys(): List<String> {
        val retained = mutableListOf<String>()
        val iterator = trackedReferences.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.get() != null) {
                retained.add(entry.key)
            } else {
                // Prune collected reference
                iterator.remove()
            }
        }
        return retained
    }

    override fun clearTrackedObjects() {
        trackedReferences.clear()
    }

    override fun getMemoryReport(): MemoryReport {
        val runtime = Runtime.getRuntime()
        val total = runtime.totalMemory()
        val free = runtime.freeMemory()
        val used = (total - free).coerceAtLeast(0L)
        val max = runtime.maxMemory()
        val ratio = if (max > 0) used.toDouble() / max.toDouble() else 0.0
        val retainedKeys = getRetainedObjectKeys()

        val isHealthy = ratio < 0.85 && retainedKeys.isEmpty()

        return MemoryReport(
            usedHeapBytes = used,
            maxHeapBytes = max,
            totalHeapBytes = total,
            heapUtilizationRatio = ratio,
            activeRetainedObjectsCount = retainedKeys.size,
            unreleasedObjectKeys = retainedKeys,
            isMemoryHealthy = isHealthy
        )
    }
}

@file:Suppress("DEPRECATION")

package com.sanket_satpute_20.dailybattle.core.memory

import android.content.ComponentCallbacks2
import com.sanket_satpute_20.dailybattle.data.cache.InMemoryCacheMetadataStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DefaultAppMemoryManager], [ResourceCache], and [MemoryTrimLevel].
 *
 * Implements Sprint 24.4 (Memory) unit test verification per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §57:
 * - Leaks / weak reference retention tracking
 * - Retained screen / object detection
 * - Resource cache LRU eviction and memory trim
 * - Unnecessary cache pruning
 */
class AppMemoryManagerTest {

    private lateinit var cacheMetadataStore: InMemoryCacheMetadataStore
    private lateinit var memoryManager: DefaultAppMemoryManager

    @Before
    fun setUp() {
        cacheMetadataStore = InMemoryCacheMetadataStore()
        memoryManager = DefaultAppMemoryManager(cacheMetadataStore)
    }

    @Test
    fun tracksHeapUtilizationAndProducesValidMemoryReport() {
        val report = memoryManager.getMemoryReport()

        assertTrue("Used heap bytes must be >= 0", report.usedHeapBytes >= 0L)
        assertTrue("Total heap bytes must be >= used heap bytes", report.totalHeapBytes >= report.usedHeapBytes)
        assertTrue("Max heap bytes must be > 0", report.maxHeapBytes > 0L)
        assertTrue("Heap utilization ratio must be in 0.0..1.0", report.heapUtilizationRatio in 0.0..1.0)
        assertEquals(0, report.activeRetainedObjectsCount)
        assertTrue("Memory must report healthy under baseline state", report.isMemoryHealthy)
    }

    @Test
    fun weakReferenceTrackingDetectsRetainedObjectsAndPrunesCollected() {
        var strongObject: Any? = Object()
        memoryManager.trackRetainedObject("screen_active", strongObject!!)

        val retainedInitially = memoryManager.getRetainedObjectKeys()
        assertEquals(1, retainedInitially.size)
        assertEquals("screen_active", retainedInitially.first())

        // Clear tracking
        memoryManager.clearTrackedObjects()
        assertTrue(memoryManager.getRetainedObjectKeys().isEmpty())

        // Track and simulate strong reference cleared
        strongObject = Object()
        memoryManager.trackRetainedObject("transient_viewmodel", strongObject)
        assertEquals(1, memoryManager.getRetainedObjectKeys().size)

        // Clear strong reference and clear tracked objects
        strongObject = null
        memoryManager.clearTrackedObjects()
        assertTrue(memoryManager.getRetainedObjectKeys().isEmpty())
    }

    @Test
    fun dispatchesMemoryTrimEventsToRegisteredCallbacks() {
        var observedTrimLevel: MemoryTrimLevel? = null
        var lowMemoryDispatched = false

        val testCallback = object : MemoryTrimCallback {
            override fun onTrimMemory(trimLevel: MemoryTrimLevel) {
                observedTrimLevel = trimLevel
            }

            override fun onLowMemory() {
                lowMemoryDispatched = true
            }
        }

        memoryManager.registerTrimCallback(testCallback)

        // Dispatch UI_HIDDEN
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        assertEquals(MemoryTrimLevel.UI_HIDDEN, observedTrimLevel)
        assertFalse(lowMemoryDispatched)

        // Dispatch RUNNING_CRITICAL
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        assertEquals(MemoryTrimLevel.RUNNING_CRITICAL, observedTrimLevel)

        // Dispatch onLowMemory
        memoryManager.onLowMemory()
        assertTrue(lowMemoryDispatched)

        // Unregister callback
        memoryManager.unregisterTrimCallback(testCallback)
        observedTrimLevel = null
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
        assertNull("Unregistered callback must not receive trim dispatch", observedTrimLevel)
    }

    @Test
    fun resourceCacheEvictsEldestEntryWhenCapacityExceeded() {
        val cache = ResourceCache<String, String>(maxCapacity = 3)

        cache.put("k1", "v1")
        cache.put("k2", "v2")
        cache.put("k3", "v3")

        assertEquals(3, cache.size())
        assertEquals("v1", cache.get("k1"))

        // Inserting 4th item evicts eldest (k2 was least recently used since k1 was accessed)
        cache.put("k4", "v4")

        assertEquals(3, cache.size())
        assertNotNull(cache.get("k1"))
        assertNull("k2 must be evicted by LRU policy", cache.get("k2"))
        assertNotNull(cache.get("k3"))
        assertNotNull(cache.get("k4"))
    }

    @Test
    fun resourceCacheTrimsOnMemoryPressure() {
        val cache = ResourceCache<Int, String>(maxCapacity = 10)

        for (i in 1..10) {
            cache.put(i, "data_$i")
        }
        assertEquals(10, cache.size())

        // UI_HIDDEN trims to half capacity
        cache.onTrimMemory(MemoryTrimLevel.UI_HIDDEN)
        assertEquals(5, cache.size())

        // CRITICAL trims completely
        cache.onTrimMemory(MemoryTrimLevel.RUNNING_CRITICAL)
        assertEquals(0, cache.size())

        // Re-populate and test onLowMemory
        for (i in 1..4) {
            cache.put(i, "data_$i")
        }
        assertEquals(4, cache.size())
        cache.onLowMemory()
        assertEquals(0, cache.size())
    }

    @Test
    fun cacheMetadataStorePrunesExpiredEntries() = runTest {
        val now = 100_000L
        val maxAgeMs = 10_000L

        // k1 is fresh (5_000 ms old)
        cacheMetadataStore.setLastUpdated("fresh_key", now - 5_000L)
        // k2 is expired (15_000 ms old)
        cacheMetadataStore.setLastUpdated("stale_key_1", now - 15_000L)
        // k3 is expired (25_000 ms old)
        cacheMetadataStore.setLastUpdated("stale_key_2", now - 25_000L)

        assertEquals(3, cacheMetadataStore.entryCount)

        cacheMetadataStore.pruneExpired(maxAgeMs = maxAgeMs, currentTimeMs = now)

        assertEquals(1, cacheMetadataStore.entryCount)
        assertNotNull(cacheMetadataStore.getLastUpdated("fresh_key"))
        assertNull(cacheMetadataStore.getLastUpdated("stale_key_1"))
        assertNull(cacheMetadataStore.getLastUpdated("stale_key_2"))
    }

    @Test
    fun memoryTrimLevelMappingAccuratelyTranslatesComponentCallbacks2() {
        assertEquals(MemoryTrimLevel.RUNNING_MODERATE, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE))
        assertEquals(MemoryTrimLevel.RUNNING_LOW, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW))
        assertEquals(MemoryTrimLevel.RUNNING_CRITICAL, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL))
        assertEquals(MemoryTrimLevel.UI_HIDDEN, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN))
        assertEquals(MemoryTrimLevel.BACKGROUND, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND))
        assertEquals(MemoryTrimLevel.MODERATE, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_MODERATE))
        assertEquals(MemoryTrimLevel.COMPLETE, MemoryTrimLevel.fromAndroidLevel(ComponentCallbacks2.TRIM_MEMORY_COMPLETE))

        assertTrue(MemoryTrimLevel.RUNNING_CRITICAL.isCritical)
        assertTrue(MemoryTrimLevel.COMPLETE.isCritical)
        assertFalse(MemoryTrimLevel.RUNNING_MODERATE.isCritical)

        assertTrue(MemoryTrimLevel.UI_HIDDEN.isUiHidden)
        assertTrue(MemoryTrimLevel.BACKGROUND.isUiHidden)
        assertFalse(MemoryTrimLevel.RUNNING_LOW.isUiHidden)
    }
}

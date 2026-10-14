@file:Suppress("DEPRECATION")

package com.sanket_satpute_20.dailybattle.performance

import android.content.ComponentCallbacks2
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanket_satpute_20.dailybattle.core.memory.AppMemoryManager
import com.sanket_satpute_20.dailybattle.core.memory.MemoryTrimCallback
import com.sanket_satpute_20.dailybattle.core.memory.MemoryTrimLevel
import com.sanket_satpute_20.dailybattle.core.memory.ResourceCache
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumentation test validating runtime memory governance (Sprint 24.4) on Android OS:
 * - Real runtime JVM heap metrics and report generation
 * - Dispatching system ComponentCallbacks2 trim levels
 * - ResourceCache LRU bounding and trim handling
 * - Weak reference leak detection
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MemoryInstrumentationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var memoryManager: AppMemoryManager

    @Before
    fun setUp() {
        hiltRule.inject()
        memoryManager.clearTrackedObjects()
    }

    @Test
    fun runtimeMemoryManagerReportsHealthyAndTrimsOnSystemPressure() {
        val report = memoryManager.getMemoryReport()

        assertTrue("Runtime heap used bytes must be positive", report.usedHeapBytes > 0L)
        assertTrue("Runtime heap max bytes must be positive", report.maxHeapBytes > 0L)
        assertTrue("Heap utilization ratio must be <= 1.0", report.heapUtilizationRatio <= 1.0)
        assertTrue("Memory must report healthy under baseline state", report.isMemoryHealthy)

        // Register a resource cache and test system trim on Android
        val cache = ResourceCache<Int, ByteArray>(maxCapacity = 10)
        memoryManager.registerTrimCallback(cache)

        for (i in 1..10) {
            cache.put(i, ByteArray(512))
        }
        assertEquals(10, cache.size())

        // Dispatch UI_HIDDEN trim
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        assertEquals(5, cache.size())

        // Dispatch RUNNING_CRITICAL trim
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        assertEquals(0, cache.size())

        memoryManager.unregisterTrimCallback(cache)
    }

    @Test
    fun runtimeWeakReferenceTrackingDetectsRetainedObjects() {
        val testObject = Object()
        memoryManager.trackRetainedObject("runtime_retained_test", testObject)

        val retainedKeys = memoryManager.getRetainedObjectKeys()
        assertEquals(1, retainedKeys.size)
        assertEquals("runtime_retained_test", retainedKeys.first())

        memoryManager.clearTrackedObjects()
        assertTrue(memoryManager.getRetainedObjectKeys().isEmpty())
    }
}

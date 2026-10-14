package com.sanket_satpute_20.dailybattle

import android.app.Application
import android.os.SystemClock
import com.sanket_satpute_20.dailybattle.core.memory.AppMemoryManager
import com.sanket_satpute_20.dailybattle.core.performance.DefaultStartupPerformanceTracker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application-level Hilt composition root.
 *
 * Implements:
 * - Startup tracking (Sprint 24.1)
 * - Memory management & ComponentCallbacks2 trim handling (Sprint 24.4)
 */
@HiltAndroidApp
class DailyBattleApplication : Application() {

    @Inject
    lateinit var memoryManager: AppMemoryManager

    override fun onCreate() {
        super.onCreate()
        DefaultStartupPerformanceTracker.onApplicationCreate(SystemClock.elapsedRealtime())
        if (::memoryManager.isInitialized) {
            registerComponentCallbacks(memoryManager)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (::memoryManager.isInitialized) {
            memoryManager.onTrimMemory(level)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::memoryManager.isInitialized) {
            memoryManager.onLowMemory()
        }
    }
}

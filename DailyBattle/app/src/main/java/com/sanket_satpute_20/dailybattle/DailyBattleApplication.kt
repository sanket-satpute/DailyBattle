package com.sanket_satpute_20.dailybattle

import android.app.Application
import android.os.SystemClock
import com.sanket_satpute_20.dailybattle.core.performance.DefaultStartupPerformanceTracker
import dagger.hilt.android.HiltAndroidApp

/**
 * Application-level Hilt composition root. Feature bindings are added only when their contracts
 * and implementations are approved.
 */
@HiltAndroidApp
class DailyBattleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DefaultStartupPerformanceTracker.onApplicationCreate(SystemClock.elapsedRealtime())
    }
}

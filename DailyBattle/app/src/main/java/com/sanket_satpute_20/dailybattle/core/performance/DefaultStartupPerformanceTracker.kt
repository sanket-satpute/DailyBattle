package com.sanket_satpute_20.dailybattle.core.performance

import android.os.SystemClock
import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.analytics.trackAppStartup
import com.sanket_satpute_20.dailybattle.core.analytics.trackScreenLoad
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleEvent
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleListener
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Default singleton implementation of [StartupPerformanceTracker] that measures:
 * 1. Cold start latency (from process / Application creation to initial state readiness)
 * 2. Warm start latency (Activity recreation or foreground return)
 * 3. First screen rendering latency (initial composition and draw of the entry destination)
 *
 * Implements Sprint 24.1 per `docs/ROADMAP.md` and `docs/16_DEFINITION_OF_DONE.md` §53, §54.
 */
@Singleton
class DefaultStartupPerformanceTracker @Inject constructor(
    private val timeProvider: TimeProvider,
    private val analyticsTracker: AnalyticsTracker,
    private val lifecycleManager: AppLifecycleManager,
) : StartupPerformanceTracker, AppLifecycleListener {

    private val _metrics = MutableStateFlow(
        StartupMetrics(
            processStartTimeMs = initialProcessStartTimeMs ?: timeProvider.elapsedRealtime()
        )
    )
    override val metrics: StateFlow<StartupMetrics> = _metrics.asStateFlow()

    private val screenRenderBeginTimes = ConcurrentHashMap<String, Long>()
    private var warmStartBeginTimeMs: Long? = null

    init {
        lifecycleManager.addListener(this)
    }

    override fun recordProcessStart(timestampMs: Long?) {
        val now = timestampMs ?: timeProvider.elapsedRealtime()
        _metrics.update { current ->
            current.copy(
                processStartTimeMs = now,
                startupType = StartupType.COLD
            )
        }
    }

    override fun recordColdStartCompleted(timestampMs: Long?): Long? {
        val now = timestampMs ?: timeProvider.elapsedRealtime()
        val current = _metrics.value
        if (current.isColdStartCompleted) {
            return current.coldStartDurationMs
        }

        val duration = max(0L, now - current.processStartTimeMs)
        _metrics.update {
            it.copy(
                coldStartDurationMs = duration,
                isColdStartCompleted = true,
                startupType = StartupType.COLD
            )
        }

        runCatching {
            analyticsTracker.trackAppStartup(duration)
        }

        return duration
    }

    override fun recordWarmStartBegin(timestampMs: Long?) {
        val now = timestampMs ?: timeProvider.elapsedRealtime()
        warmStartBeginTimeMs = now
        _metrics.update {
            it.copy(
                startupType = StartupType.WARM
            )
        }
    }

    override fun recordWarmStartCompleted(timestampMs: Long?): Long? {
        val now = timestampMs ?: timeProvider.elapsedRealtime()
        val start = warmStartBeginTimeMs ?: (now - 10L)
        val duration = max(0L, now - start)

        _metrics.update {
            it.copy(
                warmStartDurationMs = duration,
                isWarmStartCompleted = true,
                startupType = StartupType.WARM
            )
        }
        warmStartBeginTimeMs = null
        return duration
    }

    override fun recordFirstScreenRenderBegin(screenName: String, timestampMs: Long?) {
        val now = timestampMs ?: timeProvider.elapsedRealtime()
        screenRenderBeginTimes[screenName] = now
    }

    override fun recordFirstScreenRenderCompleted(screenName: String, timestampMs: Long?): Long? {
        val now = timestampMs ?: timeProvider.elapsedRealtime()
        val current = _metrics.value
        if (current.isFirstScreenRendered) {
            return current.firstScreenRenderDurationMs
        }

        val beginTime = screenRenderBeginTimes[screenName] ?: current.processStartTimeMs
        val duration = max(0L, now - beginTime)

        _metrics.update {
            it.copy(
                firstScreenRenderDurationMs = duration,
                firstScreenName = screenName,
                isFirstScreenRendered = true
            )
        }

        runCatching {
            analyticsTracker.trackScreenLoad(screenName, duration)
        }

        return duration
    }

    override fun onLifecycleEvent(event: AppLifecycleEvent) {
        when (event) {
            is AppLifecycleEvent.Foreground -> {
                if (_metrics.value.isColdStartCompleted) {
                    val duration = if (warmStartBeginTimeMs != null) {
                        max(0L, event.timestamp - warmStartBeginTimeMs!!)
                    } else {
                        // Use transition or minimum baseline
                        50L
                    }
                    _metrics.update {
                        it.copy(
                            warmStartDurationMs = duration,
                            isWarmStartCompleted = true,
                            startupType = StartupType.WARM
                        )
                    }
                    warmStartBeginTimeMs = null
                }
            }
            is AppLifecycleEvent.Recreated -> {
                if (event.isProcessRecreation) {
                    recordWarmStartBegin(event.timestamp)
                }
            }
            is AppLifecycleEvent.Background -> {
                // Prepare for next potential warm start
                warmStartBeginTimeMs = event.timestamp
            }
        }
    }

    override fun resetForTesting() {
        screenRenderBeginTimes.clear()
        warmStartBeginTimeMs = null
        val now = timeProvider.elapsedRealtime()
        _metrics.value = StartupMetrics(processStartTimeMs = now)
    }

    companion object {
        @Volatile
        var initialProcessStartTimeMs: Long? = null
            private set

        fun onApplicationCreate(timestampMs: Long = SystemClock.elapsedRealtime()) {
            if (initialProcessStartTimeMs == null) {
                initialProcessStartTimeMs = timestampMs
            }
        }

        fun resetInitialProcessStartForTesting() {
            initialProcessStartTimeMs = null
        }
    }
}

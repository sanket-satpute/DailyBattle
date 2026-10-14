package com.sanket_satpute_20.dailybattle.core.performance

import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleEvent
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleListener
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Default singleton implementation of [SessionStabilityTracker].
 *
 * Implements Sprint 24.3 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §57, §58:
 * - Listens directly to [AppLifecycleManager] to automatically capture background/foreground cycles
 * - Monitors prolonged session duration via monotonic [TimeProvider]
 * - Samples JVM heap memory periodically and computes memory creep
 * - Tracks repeated battles, repeated navigation, repeated practice, and repeated results viewing
 */
@Singleton
class DefaultSessionStabilityTracker @Inject constructor(
    private val timeProvider: TimeProvider,
    private val lifecycleManager: AppLifecycleManager,
) : SessionStabilityTracker, AppLifecycleListener {

    private var startMonotonicMs: Long = timeProvider.elapsedRealtime()

    private val _metrics = MutableStateFlow(
        SessionStabilityMetrics(
            sessionStartTimestampMs = System.currentTimeMillis(),
            startingHeapBytes = getCurrentUsedHeapBytes(),
            currentHeapBytes = getCurrentUsedHeapBytes(),
            peakHeapBytes = getCurrentUsedHeapBytes()
        )
    )
    override val metrics: StateFlow<SessionStabilityMetrics> = _metrics.asStateFlow()

    init {
        lifecycleManager.addListener(this)
    }

    override fun startSession(startTimestampMs: Long?) {
        val currentHeap = getCurrentUsedHeapBytes()
        startMonotonicMs = timeProvider.elapsedRealtime()
        _metrics.update {
            SessionStabilityMetrics(
                sessionStartTimestampMs = startTimestampMs ?: System.currentTimeMillis(),
                sessionDurationMs = 0L,
                startingHeapBytes = currentHeap,
                currentHeapBytes = currentHeap,
                peakHeapBytes = currentHeap,
            )
        }
    }

    override fun recordBattleCycle() {
        val nowElapsed = timeProvider.elapsedRealtime()
        val currentHeap = getCurrentUsedHeapBytes()
        _metrics.update { current ->
            val duration = (nowElapsed - startMonotonicMs).coerceAtLeast(0L)
            current.copy(
                sessionDurationMs = duration,
                battleCyclesCompleted = current.battleCyclesCompleted + 1L,
                currentHeapBytes = currentHeap,
                peakHeapBytes = max(current.peakHeapBytes, currentHeap),
            )
        }
    }

    override fun recordPracticeCycle() {
        val nowElapsed = timeProvider.elapsedRealtime()
        val currentHeap = getCurrentUsedHeapBytes()
        _metrics.update { current ->
            val duration = (nowElapsed - startMonotonicMs).coerceAtLeast(0L)
            current.copy(
                sessionDurationMs = duration,
                practiceCyclesCompleted = current.practiceCyclesCompleted + 1L,
                currentHeapBytes = currentHeap,
                peakHeapBytes = max(current.peakHeapBytes, currentHeap),
            )
        }
    }

    override fun recordNavigationTransition(destinationRoute: String?) {
        val nowElapsed = timeProvider.elapsedRealtime()
        _metrics.update { current ->
            val duration = (nowElapsed - startMonotonicMs).coerceAtLeast(0L)
            current.copy(
                sessionDurationMs = duration,
                navigationTransitions = current.navigationTransitions + 1L,
            )
        }
    }

    override fun recordBackgroundForegroundCycle() {
        val nowElapsed = timeProvider.elapsedRealtime()
        val currentHeap = getCurrentUsedHeapBytes()
        _metrics.update { current ->
            val duration = (nowElapsed - startMonotonicMs).coerceAtLeast(0L)
            current.copy(
                sessionDurationMs = duration,
                backgroundForegroundCycles = current.backgroundForegroundCycles + 1L,
                currentHeapBytes = currentHeap,
                peakHeapBytes = max(current.peakHeapBytes, currentHeap),
            )
        }
    }

    override fun recordResultsView() {
        val nowElapsed = timeProvider.elapsedRealtime()
        _metrics.update { current ->
            val duration = (nowElapsed - startMonotonicMs).coerceAtLeast(0L)
            current.copy(
                sessionDurationMs = duration,
                resultsViewCount = current.resultsViewCount + 1L,
            )
        }
    }

    override fun recordHeapSample(usedBytes: Long?) {
        val sampled = usedBytes ?: getCurrentUsedHeapBytes()
        val nowElapsed = timeProvider.elapsedRealtime()
        _metrics.update { current ->
            val duration = (nowElapsed - startMonotonicMs).coerceAtLeast(0L)
            current.copy(
                sessionDurationMs = duration,
                currentHeapBytes = sampled,
                peakHeapBytes = max(current.peakHeapBytes, sampled),
            )
        }
    }

    override fun recordException(throwable: Throwable?) {
        _metrics.update { current ->
            current.copy(
                unhandledExceptionsCount = current.unhandledExceptionsCount + 1L
            )
        }
    }

    override fun getSnapshot(): SessionStabilityMetrics {
        return _metrics.value
    }

    override fun resetForTesting() {
        startMonotonicMs = timeProvider.elapsedRealtime()
        val currentHeap = getCurrentUsedHeapBytes()
        _metrics.value = SessionStabilityMetrics(
            sessionStartTimestampMs = System.currentTimeMillis(),
            startingHeapBytes = currentHeap,
            currentHeapBytes = currentHeap,
            peakHeapBytes = currentHeap
        )
    }

    override fun onLifecycleEvent(event: AppLifecycleEvent) {
        when (event) {
            is AppLifecycleEvent.Foreground -> {
                recordBackgroundForegroundCycle()
            }
            is AppLifecycleEvent.Background -> {
                recordHeapSample()
            }
            is AppLifecycleEvent.Recreated -> {
                recordHeapSample()
            }
        }
    }

    private fun getCurrentUsedHeapBytes(): Long {
        return try {
            val runtime = Runtime.getRuntime()
            (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        } catch (_: Throwable) {
            0L
        }
    }
}

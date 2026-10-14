package com.sanket_satpute_20.dailybattle.core.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op implementation of [SessionStabilityTracker] for previews and minimal tests.
 */
class NoOpSessionStabilityTracker : SessionStabilityTracker {

    private val _metrics = MutableStateFlow(SessionStabilityMetrics())
    override val metrics: StateFlow<SessionStabilityMetrics> = _metrics.asStateFlow()

    override fun startSession(startTimestampMs: Long?) {}
    override fun recordBattleCycle() {}
    override fun recordPracticeCycle() {}
    override fun recordNavigationTransition(destinationRoute: String?) {}
    override fun recordBackgroundForegroundCycle() {}
    override fun recordResultsView() {}
    override fun recordHeapSample(usedBytes: Long?) {}
    override fun recordException(throwable: Throwable?) {}
    override fun getSnapshot(): SessionStabilityMetrics = _metrics.value
    override fun resetForTesting() {
        _metrics.value = SessionStabilityMetrics()
    }
}

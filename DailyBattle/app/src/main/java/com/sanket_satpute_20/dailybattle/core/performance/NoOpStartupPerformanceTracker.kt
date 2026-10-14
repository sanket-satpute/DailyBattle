package com.sanket_satpute_20.dailybattle.core.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Safe no-op implementation of [StartupPerformanceTracker] used for testing and fallbacks.
 */
object NoOpStartupPerformanceTracker : StartupPerformanceTracker {

    private val _metrics = MutableStateFlow(StartupMetrics())
    override val metrics: StateFlow<StartupMetrics> = _metrics.asStateFlow()

    override fun recordProcessStart(timestampMs: Long?) {}

    override fun recordColdStartCompleted(timestampMs: Long?): Long? = null

    override fun recordWarmStartBegin(timestampMs: Long?) {}

    override fun recordWarmStartCompleted(timestampMs: Long?): Long? = null

    override fun recordFirstScreenRenderBegin(screenName: String, timestampMs: Long?) {}

    override fun recordFirstScreenRenderCompleted(screenName: String, timestampMs: Long?): Long? = null

    override fun resetForTesting() {
        _metrics.value = StartupMetrics()
    }
}

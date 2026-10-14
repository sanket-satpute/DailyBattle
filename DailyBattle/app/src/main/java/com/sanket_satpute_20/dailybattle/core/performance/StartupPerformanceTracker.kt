package com.sanket_satpute_20.dailybattle.core.performance

import kotlinx.coroutines.flow.StateFlow

/**
 * Authoritative contract for measuring application startup performance:
 * - Cold start latency
 * - Warm start latency
 * - First screen rendering latency
 *
 * Implements Sprint 24.1 per `docs/ROADMAP.md` and `docs/16_DEFINITION_OF_DONE.md` §53, §54.
 */
interface StartupPerformanceTracker {

    /** Current immutable startup performance metrics state. */
    val metrics: StateFlow<StartupMetrics>

    /** Records the process start timestamp. Called at Application creation or process launch. */
    fun recordProcessStart(timestampMs: Long? = null)

    /**
     * Records cold start completion once initial startup, dependency injection,
     * and background state restoration finish. Idempotent.
     *
     * @return cold start duration in milliseconds, or null if already completed.
     */
    fun recordColdStartCompleted(timestampMs: Long? = null): Long?

    /** Records the beginning of a warm start (e.g. Activity recreation or returning from background). */
    fun recordWarmStartBegin(timestampMs: Long? = null)

    /**
     * Records warm start completion once the activity is restored to foreground.
     *
     * @return warm start duration in milliseconds.
     */
    fun recordWarmStartCompleted(timestampMs: Long? = null): Long?

    /** Records the start of rendering for a screen during startup. */
    fun recordFirstScreenRenderBegin(screenName: String, timestampMs: Long? = null)

    /**
     * Records the completion of the first screen rendering. Idempotent for the first screen.
     *
     * @return duration in milliseconds to render the first screen from startup start.
     */
    fun recordFirstScreenRenderCompleted(screenName: String, timestampMs: Long? = null): Long?

    /** Clears tracked metrics for test isolation. */
    fun resetForTesting()
}

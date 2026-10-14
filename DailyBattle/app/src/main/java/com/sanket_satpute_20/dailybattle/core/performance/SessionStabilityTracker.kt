package com.sanket_satpute_20.dailybattle.core.performance

import kotlinx.coroutines.flow.StateFlow

/**
 * Authoritative interface for monitoring long session stability:
 * 1. Repeated battles without session/state leakage or memory creep
 * 2. Repeated navigation without retaining screens or leaking listeners
 * 3. Long app session runtime without state corruption or crashes
 * 4. Background/foreground lifecycle cycles across diverse states
 * 5. Repeated practice iterations isolated from official records
 * 6. Repeated results viewing without listener leaks or state corruption
 *
 * Implements Sprint 24.3 per `docs/ROADMAP.md` and `docs/12_TESTING_AND_QA.md` §57, §58.
 */
interface SessionStabilityTracker {

    /** Current observable snapshot of session stability metrics. */
    val metrics: StateFlow<SessionStabilityMetrics>

    /** Starts or re-initializes a long session stability tracking window. */
    fun startSession(startTimestampMs: Long? = null)

    /** Records completion of an official battle cycle. */
    fun recordBattleCycle()

    /** Records completion of a practice battle cycle. */
    fun recordPracticeCycle()

    /** Records a navigation transition to a destination screen. */
    fun recordNavigationTransition(destinationRoute: String? = null)

    /** Records a completed background/foreground lifecycle cycle. */
    fun recordBackgroundForegroundCycle()

    /** Records a results screen view or dismissal. */
    fun recordResultsView()

    /** Records a memory heap sample in bytes (null samples current Runtime heap). */
    fun recordHeapSample(usedBytes: Long? = null)

    /** Records an unhandled exception or critical stability failure. */
    fun recordException(throwable: Throwable? = null)

    /** Returns current immutable snapshot of metrics. */
    fun getSnapshot(): SessionStabilityMetrics

    /** Resets metrics for testing isolation. */
    fun resetForTesting()
}

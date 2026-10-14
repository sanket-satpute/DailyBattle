package com.sanket_satpute_20.dailybattle.core.performance

/**
 * Immutable snapshot of application startup performance measurements.
 *
 * Conforms to:
 * - `docs/ROADMAP.md` Sprint 24.1 (Cold start, warm start, first screen rendering)
 * - `docs/07_TECHNICAL_ARCHITECTURE.md` §48 (Performance Requirements)
 * - `docs/12_TESTING_AND_QA.md` §56 (Performance Testing)
 * - `docs/14_ANALYTICS.md` §74 (Performance Analytics)
 * - `docs/16_DEFINITION_OF_DONE.md` §53 (Performance DoD)
 */
data class StartupMetrics(
    val startupType: StartupType = StartupType.COLD,
    val processStartTimeMs: Long = 0L,
    val coldStartDurationMs: Long? = null,
    val warmStartDurationMs: Long? = null,
    val firstScreenRenderDurationMs: Long? = null,
    val firstScreenName: String? = null,
    val isColdStartCompleted: Boolean = false,
    val isWarmStartCompleted: Boolean = false,
    val isFirstScreenRendered: Boolean = false,
) {
    companion object {
        /** Baseline performance targets in milliseconds. Exact thresholds are PENDING in docs. */
        const val TARGET_COLD_START_MS: Long = 2000L
        const val TARGET_WARM_START_MS: Long = 500L
        const val TARGET_FIRST_SCREEN_RENDER_MS: Long = 1000L
    }

    fun isColdStartWithinTarget(targetMs: Long = TARGET_COLD_START_MS): Boolean =
        coldStartDurationMs?.let { it <= targetMs } ?: false

    fun isWarmStartWithinTarget(targetMs: Long = TARGET_WARM_START_MS): Boolean =
        warmStartDurationMs?.let { it <= targetMs } ?: false

    fun isFirstScreenRenderWithinTarget(targetMs: Long = TARGET_FIRST_SCREEN_RENDER_MS): Boolean =
        firstScreenRenderDurationMs?.let { it <= targetMs } ?: false
}

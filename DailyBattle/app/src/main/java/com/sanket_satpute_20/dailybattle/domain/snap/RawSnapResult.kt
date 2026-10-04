package com.sanket_satpute_20.dailybattle.domain.snap

/**
 * Captures the raw inputs of a Snap challenge round without applying
 * authoritative scoring algorithms (which remain PENDING via DEC-GAME-001).
 *
 * This allows the engine to record the user's reaction performance safely
 * while leaving the final point translation to a separate calculation layer.
 */
data class RawSnapResult(
    val targetAppeared: Boolean,
    val targetAppearanceTimeMs: Long?,
    val taps: List<TapRecord>,
    val roundDurationMs: Long
)

data class TapRecord(
    val timestampMs: Long,
    val elementId: String? // Which element was tapped, if any
)

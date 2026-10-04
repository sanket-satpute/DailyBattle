package com.sanket_satpute_20.dailybattle.domain.shift

/**
 * Captures the raw inputs of a Shift challenge round without applying
 * authoritative scoring algorithms (which remain PENDING via DEC-GAME-002).
 */
data class RawShiftResult(
    val roundsCompleted: Int,
    val selections: List<ShiftSelectionRecord>,
    val challengeDurationMs: Long
)

data class ShiftSelectionRecord(
    val cellId: String,
    val timestampMs: Long
)

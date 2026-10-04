package com.sanket_satpute_20.dailybattle.domain.shift

/**
 * Domain events that can be submitted to the ShiftEngine.
 */
sealed interface ShiftEvent {
    /**
     * Start the challenge (enter Observation state).
     */
    object Start : ShiftEvent

    /**
     * Transition from Observation to Shifting state.
     */
    object BeginShift : ShiftEvent
    
    /**
     * Transition from Shifting to Answer state.
     */
    object PresentAnswer : ShiftEvent

    /**
     * The player tapped a cell to answer.
     */
    data class TapCell(val cellId: String, val timestampMs: Long = 0L) : ShiftEvent

    /**
     * The time window for reaction ended.
     */
    object Timeout : ShiftEvent

    /**
     * Move to the next round (if applicable) or finish the challenge.
     */
    object Continue : ShiftEvent
}

package com.sanket_satpute_20.dailybattle.domain.snap

/**
 * Domain events that can be submitted to the SnapEngine.
 */
sealed interface SnapEvent {
    /**
     * Start the challenge.
     */
    object Start : SnapEvent

    /**
     * The player tapped an element.
     */
    data class Tap(val elementId: String) : SnapEvent

    /**
     * The time window for reaction ended.
     */
    object Timeout : SnapEvent

    /**
     * Move to the next round (if applicable) or finish the challenge.
     */
    object Continue : SnapEvent
}

package com.sanket_satpute_20.dailybattle.domain.shift

/**
 * High-level state representation for the Shift challenge.
 *
 * Conforms to Sprint 9.1 constraints:
 * - Provides grid state (Observation)
 * - Provides movement/change state (Shifting)
 * - Provides answer state (Answer)
 * - Exact algorithms (grid size, shift types) are PENDING (DEC-GAME-002).
 */
sealed interface ShiftState {
    /**
     * The challenge has not yet started.
     */
    object NotStarted : ShiftState

    /**
     * The grid state.
     * The player observes the initial grid arrangement.
     */
    data class Observation(
        val grid: ShiftGrid
    ) : ShiftState

    /**
     * The movement/change state.
     * Elements on the grid are hidden, moving, or changing.
     */
    data class Shifting(
        val grid: ShiftGrid
    ) : ShiftState

    /**
     * The answer state.
     * The player must select the correct cell or element based on the shift.
     */
    data class Answer(
        val grid: ShiftGrid
    ) : ShiftState

    /**
     * The reaction state providing immediate feedback.
     */
    data class ReactionFeedback(
        val isCorrect: Boolean
    ) : ShiftState

    /**
     * The challenge is finished.
     */
    data class Completed(
        val result: RawShiftResult
    ) : ShiftState
}

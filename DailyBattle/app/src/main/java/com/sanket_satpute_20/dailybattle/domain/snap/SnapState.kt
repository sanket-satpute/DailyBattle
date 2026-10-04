package com.sanket_satpute_20.dailybattle.domain.snap

/**
 * High-level state representation for the Snap challenge.
 *
 * Designed to conform to the Sprint 8.1 constraints and user instructions:
 * - Does not assume round count, target types, distractors, or timing.
 * - Provides the domain boundary for reaction and result states.
 * - Leaves exact target generation and scoring PENDING (DEC-GAME-001).
 */
sealed interface SnapState {
    /**
     * The challenge has not yet started or is preparing.
     */
    object NotStarted : SnapState

    /**
     * The challenge is active and waiting for a reaction.
     * Contains the current elements shown to the user (targets/distractors).
     */
    data class Active(
        val elements: List<SnapElement>
    ) : SnapState

    /**
     * The reaction state providing immediate feedback on an interaction.
     */
    data class ReactionFeedback(
        val isCorrect: Boolean
    ) : SnapState

    /**
     * The challenge is finished. The raw score is calculated and ready.
     * Scoring formulas are currently PENDING.
     */
    data class Completed(
        val result: RawSnapResult
    ) : SnapState
}

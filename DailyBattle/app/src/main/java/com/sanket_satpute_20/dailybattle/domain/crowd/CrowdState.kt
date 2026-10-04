package com.sanket_satpute_20.dailybattle.domain.crowd

/**
 * Represents the state machine for the Crowd Call challenge.
 */
sealed interface CrowdState {
    
    /**
     * Initial state before the challenge begins.
     */
    data object NotStarted : CrowdState

    /**
     * The question and choices are presented to the user for prediction.
     */
    data class Question(
        val question: CrowdQuestion
    ) : CrowdState

    /**
     * The result of the prediction is revealed, showing the crowd distribution.
     * The score logic is pending, so this state only contains the pure domain structure.
     */
    data class PredictionResult(
        val question: CrowdQuestion,
        val selectedChoiceId: String?,
        val distribution: CrowdDistribution,
        val isMajorityChosen: Boolean // Optional stub helper for feedback
    ) : CrowdState

    /**
     * The challenge is fully completed, emitting the raw result payload.
     */
    data class Completed(
        val result: RawCrowdResult
    ) : CrowdState
}

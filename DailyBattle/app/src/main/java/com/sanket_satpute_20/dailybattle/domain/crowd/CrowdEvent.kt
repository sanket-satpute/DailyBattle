package com.sanket_satpute_20.dailybattle.domain.crowd

/**
 * Defines all valid user interactions or lifecycle triggers for Crowd Call.
 */
sealed interface CrowdEvent {
    
    /**
     * Start the challenge (transition from NotStarted to Question).
     */
    data object Start : CrowdEvent

    /**
     * User predicts the crowd's majority choice.
     */
    data class SubmitPrediction(val choiceId: String, val timestampMs: Long) : CrowdEvent

    /**
     * Time runs out for the user to make a prediction.
     */
    data object Timeout : CrowdEvent

    /**
     * Move past the results screen to complete the challenge.
     */
    data object Continue : CrowdEvent
}

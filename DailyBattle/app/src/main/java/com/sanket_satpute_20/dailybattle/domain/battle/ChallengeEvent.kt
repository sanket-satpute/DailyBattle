package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Events that trigger transitions in the Challenge State Machine.
 */
sealed interface ChallengeEvent {
    /** Starts the challenge gameplay, moving from Ready to Active. */
    data object Start : ChallengeEvent
    
    /** Records a correct interaction, moving from Active to Correct. */
    data object SubmitCorrect : ChallengeEvent
    
    /** Records an incorrect interaction, moving from Active to Incorrect. */
    data object SubmitIncorrect : ChallengeEvent
    
    /** Continues the challenge after feedback, moving from Correct/Incorrect back to Active. */
    data object Continue : ChallengeEvent
    
    /** Finishes the challenge (due to completion or timeout), moving to Complete. */
    data object Finish : ChallengeEvent
}

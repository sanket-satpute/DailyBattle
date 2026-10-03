package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Events that trigger transitions in the Battle State Machine.
 */
sealed interface BattleEvent {
    /** Starts the battle, moving from NotStarted to Ready(1). */
    data object StartBattle : BattleEvent
    
    /** Begins the active phase of the current challenge, moving from Ready to Active. */
    data object BeginChallenge : BattleEvent
    
    /** Finishes the active phase, moving from Active to ChallengeComplete. */
    data object FinishChallenge : BattleEvent
    
    /** 
     * Continues the flow.
     * From ChallengeComplete -> NextChallenge (if more challenges) or BattleComplete (if all done).
     */
    data object Continue : BattleEvent
    
    /** Prepares the next challenge, moving from NextChallenge to Ready. */
    data object PrepareNext : BattleEvent
    
    /** Displays the final results, moving from BattleComplete to Results. */
    data object ShowResults : BattleEvent
}

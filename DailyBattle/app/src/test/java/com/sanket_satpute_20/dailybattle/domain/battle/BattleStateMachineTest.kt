package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleStateMachineTest {

    @Test
    fun `initial state is NotStarted`() {
        val machine = BattleStateMachine()
        assertEquals(BattleState.NotStarted, machine.currentState)
    }

    @Test
    fun `full successful battle flow transitions correctly`() {
        val machine = BattleStateMachine()

        // Challenge 1
        var result = machine.transition(BattleEvent.StartBattle)
        assertSuccessAndState(result, BattleState.Ready(1))

        result = machine.transition(BattleEvent.BeginChallenge)
        assertSuccessAndState(result, BattleState.Active(1))

        result = machine.transition(BattleEvent.FinishChallenge)
        assertSuccessAndState(result, BattleState.ChallengeComplete(1))

        result = machine.transition(BattleEvent.Continue)
        assertSuccessAndState(result, BattleState.NextChallenge(2))

        result = machine.transition(BattleEvent.PrepareNext)
        assertSuccessAndState(result, BattleState.Ready(2))

        // Challenge 2
        result = machine.transition(BattleEvent.BeginChallenge)
        assertSuccessAndState(result, BattleState.Active(2))

        result = machine.transition(BattleEvent.FinishChallenge)
        assertSuccessAndState(result, BattleState.ChallengeComplete(2))

        result = machine.transition(BattleEvent.Continue)
        assertSuccessAndState(result, BattleState.NextChallenge(3))

        result = machine.transition(BattleEvent.PrepareNext)
        assertSuccessAndState(result, BattleState.Ready(3))

        // Challenge 3
        result = machine.transition(BattleEvent.BeginChallenge)
        assertSuccessAndState(result, BattleState.Active(3))

        result = machine.transition(BattleEvent.FinishChallenge)
        assertSuccessAndState(result, BattleState.ChallengeComplete(3))

        // End of challenges
        result = machine.transition(BattleEvent.Continue)
        assertSuccessAndState(result, BattleState.BattleComplete)

        result = machine.transition(BattleEvent.ShowResults)
        assertSuccessAndState(result, BattleState.Results)
    }

    @Test
    fun `invalid transition returns domain failure`() {
        val machine = BattleStateMachine() // Starts in NotStarted

        // Cannot begin challenge before starting battle
        val result = machine.transition(BattleEvent.BeginChallenge)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.BattleState, (result as DomainResult.Failure).error)
        
        // State remains unchanged
        assertEquals(BattleState.NotStarted, machine.currentState)
    }

    @Test
    fun `terminal state rejects all further transitions`() {
        val machine = BattleStateMachine(initialState = BattleState.Results)
        
        // All events should fail
        assertTrue(machine.transition(BattleEvent.StartBattle) is DomainResult.Failure)
        assertTrue(machine.transition(BattleEvent.BeginChallenge) is DomainResult.Failure)
        assertTrue(machine.transition(BattleEvent.FinishChallenge) is DomainResult.Failure)
        assertTrue(machine.transition(BattleEvent.Continue) is DomainResult.Failure)
        assertTrue(machine.transition(BattleEvent.PrepareNext) is DomainResult.Failure)
        assertTrue(machine.transition(BattleEvent.ShowResults) is DomainResult.Failure)
        
        // State remains unchanged
        assertEquals(BattleState.Results, machine.currentState)
    }

    private fun assertSuccessAndState(result: DomainResult<BattleState>, expectedState: BattleState) {
        assertTrue(result is DomainResult.Success)
        assertEquals(expectedState, (result as DomainResult.Success).value)
    }
}

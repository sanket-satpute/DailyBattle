package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeStateMachineTest {

    @Test
    fun `initial state is Ready`() {
        val machine = ChallengeStateMachine()
        assertEquals(ChallengeState.Ready, machine.currentState)
    }

    @Test
    fun `successful correct interaction flow transitions correctly`() {
        val machine = ChallengeStateMachine()

        var result = machine.transition(ChallengeEvent.Start)
        assertSuccessAndState(result, ChallengeState.Active)

        result = machine.transition(ChallengeEvent.SubmitCorrect)
        assertSuccessAndState(result, ChallengeState.Correct)

        result = machine.transition(ChallengeEvent.Continue)
        assertSuccessAndState(result, ChallengeState.Active)

        result = machine.transition(ChallengeEvent.Finish)
        assertSuccessAndState(result, ChallengeState.Complete)
    }

    @Test
    fun `successful incorrect interaction flow transitions correctly`() {
        val machine = ChallengeStateMachine()

        machine.transition(ChallengeEvent.Start)

        var result = machine.transition(ChallengeEvent.SubmitIncorrect)
        assertSuccessAndState(result, ChallengeState.Incorrect)

        result = machine.transition(ChallengeEvent.Finish)
        assertSuccessAndState(result, ChallengeState.Complete)
    }

    @Test
    fun `timeout directly from active transitions to complete`() {
        val machine = ChallengeStateMachine()
        machine.transition(ChallengeEvent.Start)

        val result = machine.transition(ChallengeEvent.Finish)
        assertSuccessAndState(result, ChallengeState.Complete)
    }

    @Test
    fun `invalid transition returns domain failure`() {
        val machine = ChallengeStateMachine() // Starts in Ready

        // Cannot submit answer before starting
        val result = machine.transition(ChallengeEvent.SubmitCorrect)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
        
        // State remains unchanged
        assertEquals(ChallengeState.Ready, machine.currentState)
    }

    @Test
    fun `terminal state rejects all further transitions`() {
        val machine = ChallengeStateMachine(initialState = ChallengeState.Complete)
        
        // All events should fail
        assertTrue(machine.transition(ChallengeEvent.Start) is DomainResult.Failure)
        assertTrue(machine.transition(ChallengeEvent.SubmitCorrect) is DomainResult.Failure)
        assertTrue(machine.transition(ChallengeEvent.SubmitIncorrect) is DomainResult.Failure)
        assertTrue(machine.transition(ChallengeEvent.Continue) is DomainResult.Failure)
        assertTrue(machine.transition(ChallengeEvent.Finish) is DomainResult.Failure)
        
        // State remains unchanged
        assertEquals(ChallengeState.Complete, machine.currentState)
    }

    private fun assertSuccessAndState(result: DomainResult<ChallengeState>, expectedState: ChallengeState) {
        assertTrue(result is DomainResult.Success)
        assertEquals(expectedState, (result as DomainResult.Success).value)
    }
}

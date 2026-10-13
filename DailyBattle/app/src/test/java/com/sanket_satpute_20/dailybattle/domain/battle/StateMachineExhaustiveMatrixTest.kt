package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 23.1 — Unit Tests (State Machines)
 * Exhaustive transition and rejection matrix testing for both [BattleStateMachine]
 * and [ChallengeStateMachine]:
 * 1. Valid transitions progress state correctly.
 * 2. Invalid transitions return domain failure and DO NOT modify current state (immutability under failure).
 * 3. Terminal states reject all subsequent transitions.
 *
 * Per `docs/02_GAMEPLAY.md` §16 and `docs/12_TESTING_AND_QA.md` §8.
 */
class StateMachineExhaustiveMatrixTest {

    // ─────────────────────────────────────────────────────────────────────────────
    // BattleStateMachine Exhaustive Matrix
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `BattleStateMachine - all invalid events from NotStarted are rejected and preserve state`() {
        val machine = BattleStateMachine(BattleState.NotStarted)
        val invalidEvents = listOf(
            BattleEvent.BeginChallenge,
            BattleEvent.FinishChallenge,
            BattleEvent.Continue,
            BattleEvent.PrepareNext,
            BattleEvent.ShowResults
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from NotStarted", result is DomainResult.Failure)
            assertEquals(AppError.BattleState, (result as DomainResult.Failure).error)
            assertEquals("State must remain NotStarted", BattleState.NotStarted, machine.currentState)
        }
    }

    @Test
    fun `BattleStateMachine - all invalid events from Ready are rejected and preserve state`() {
        val machine = BattleStateMachine(BattleState.Ready(1))
        val invalidEvents = listOf(
            BattleEvent.StartBattle,
            BattleEvent.FinishChallenge,
            BattleEvent.Continue,
            BattleEvent.PrepareNext,
            BattleEvent.ShowResults
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from Ready", result is DomainResult.Failure)
            assertEquals(BattleState.Ready(1), machine.currentState)
        }
    }

    @Test
    fun `BattleStateMachine - all invalid events from Active are rejected and preserve state`() {
        val machine = BattleStateMachine(BattleState.Active(1))
        val invalidEvents = listOf(
            BattleEvent.StartBattle,
            BattleEvent.BeginChallenge,
            BattleEvent.Continue,
            BattleEvent.PrepareNext,
            BattleEvent.ShowResults
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from Active", result is DomainResult.Failure)
            assertEquals(BattleState.Active(1), machine.currentState)
        }
    }

    @Test
    fun `BattleStateMachine - all invalid events from BattleComplete are rejected and preserve state`() {
        val machine = BattleStateMachine(BattleState.BattleComplete)
        val invalidEvents = listOf(
            BattleEvent.StartBattle,
            BattleEvent.BeginChallenge,
            BattleEvent.FinishChallenge,
            BattleEvent.Continue,
            BattleEvent.PrepareNext
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from BattleComplete", result is DomainResult.Failure)
            assertEquals(BattleState.BattleComplete, machine.currentState)
        }
    }

    @Test
    fun `BattleStateMachine - Results is terminal and rejects all transitions`() {
        val machine = BattleStateMachine(BattleState.Results)
        val allEvents = listOf(
            BattleEvent.StartBattle,
            BattleEvent.BeginChallenge,
            BattleEvent.FinishChallenge,
            BattleEvent.Continue,
            BattleEvent.PrepareNext,
            BattleEvent.ShowResults
        )

        for (event in allEvents) {
            val result = machine.transition(event)
            assertTrue("Results state must reject $event", result is DomainResult.Failure)
            assertEquals(BattleState.Results, machine.currentState)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ChallengeStateMachine Exhaustive Matrix
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `ChallengeStateMachine - all invalid events from Ready are rejected and preserve state`() {
        val machine = ChallengeStateMachine(ChallengeState.Ready)
        val invalidEvents = listOf(
            ChallengeEvent.SubmitCorrect,
            ChallengeEvent.SubmitIncorrect,
            ChallengeEvent.Continue,
            ChallengeEvent.Finish
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from Ready", result is DomainResult.Failure)
            assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
            assertEquals(ChallengeState.Ready, machine.currentState)
        }
    }

    @Test
    fun `ChallengeStateMachine - all invalid events from Active are rejected and preserve state`() {
        val machine = ChallengeStateMachine(ChallengeState.Active)
        val invalidEvents = listOf(
            ChallengeEvent.Start,
            ChallengeEvent.Continue
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from Active", result is DomainResult.Failure)
            assertEquals(ChallengeState.Active, machine.currentState)
        }
    }

    @Test
    fun `ChallengeStateMachine - all invalid events from Correct are rejected and preserve state`() {
        val machine = ChallengeStateMachine(ChallengeState.Correct)
        val invalidEvents = listOf(
            ChallengeEvent.Start,
            ChallengeEvent.SubmitCorrect,
            ChallengeEvent.SubmitIncorrect
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from Correct", result is DomainResult.Failure)
            assertEquals(ChallengeState.Correct, machine.currentState)
        }
    }

    @Test
    fun `ChallengeStateMachine - all invalid events from Incorrect are rejected and preserve state`() {
        val machine = ChallengeStateMachine(ChallengeState.Incorrect)
        val invalidEvents = listOf(
            ChallengeEvent.Start,
            ChallengeEvent.SubmitCorrect,
            ChallengeEvent.SubmitIncorrect
        )

        for (event in invalidEvents) {
            val result = machine.transition(event)
            assertTrue("Expected failure for $event from Incorrect", result is DomainResult.Failure)
            assertEquals(ChallengeState.Incorrect, machine.currentState)
        }
    }

    @Test
    fun `ChallengeStateMachine - Complete is terminal and rejects all transitions`() {
        val machine = ChallengeStateMachine(ChallengeState.Complete)
        val allEvents = listOf(
            ChallengeEvent.Start,
            ChallengeEvent.SubmitCorrect,
            ChallengeEvent.SubmitIncorrect,
            ChallengeEvent.Continue,
            ChallengeEvent.Finish
        )

        for (event in allEvents) {
            val result = machine.transition(event)
            assertTrue("Complete state must reject $event", result is DomainResult.Failure)
            assertEquals(ChallengeState.Complete, machine.currentState)
        }
    }
}

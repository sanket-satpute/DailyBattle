package com.sanket_satpute_20.dailybattle.domain.shift

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

class ShiftInputCollectorTest {

    @Test
    fun `valid transition flow through states`() {
        val collector = ShiftInputCollector()
        
        // Prepare -> Observation
        val prepareResult = collector.prepare()
        assertTrue(prepareResult is DomainResult.Success)
        assertTrue(collector.currentState is ShiftState.Observation)
        
        // Start
        collector.start()
        
        // Observation -> Shifting
        val shiftResult = collector.handleInput(ShiftEvent.BeginShift)
        assertTrue(shiftResult is DomainResult.Success)
        assertTrue(collector.currentState is ShiftState.Shifting)
        
        // Shifting -> Answer
        val answerResult = collector.handleInput(ShiftEvent.PresentAnswer)
        assertTrue(answerResult is DomainResult.Success)
        assertTrue(collector.currentState is ShiftState.Answer)
        
        // Answer -> ReactionFeedback
        val tapResult = collector.handleInput(ShiftEvent.TapCell("cell_1", 1000L))
        assertTrue(tapResult is DomainResult.Success)
        assertTrue(collector.currentState is ShiftState.ReactionFeedback)
        
        // ReactionFeedback -> Complete
        val completeResult = collector.complete()
        assertTrue(completeResult is DomainResult.Success)
        assertTrue(collector.currentState is ShiftState.Completed)
        
        val finalResult = (collector.currentState as ShiftState.Completed).result
        assertEquals(1, finalResult.selections.size)
        assertEquals("cell_1", finalResult.selections.first().cellId)
    }

    @Test
    fun `invalid transition fails`() {
        val collector = ShiftInputCollector()
        collector.prepare()
        collector.start() // Observation state
        
        // Try to tap cell before Answer state
        val tapResult = collector.handleInput(ShiftEvent.TapCell("cell_1", 1000L))
        assertTrue(tapResult is DomainResult.Failure)
        
        // Still in Observation
        assertTrue(collector.currentState is ShiftState.Observation)
    }

    @Test
    fun `multiple rounds tracking`() {
        val collector = ShiftInputCollector()
        collector.prepare()
        collector.start()
        
        // Round 1
        collector.handleInput(ShiftEvent.BeginShift)
        collector.handleInput(ShiftEvent.PresentAnswer)
        collector.handleInput(ShiftEvent.TapCell("cell_1", 1000L))
        collector.handleInput(ShiftEvent.Continue) // Moves back to Observation
        
        assertTrue(collector.currentState is ShiftState.Observation)
        
        // Round 2
        collector.handleInput(ShiftEvent.BeginShift)
        collector.handleInput(ShiftEvent.PresentAnswer)
        collector.handleInput(ShiftEvent.TapCell("cell_2", 2000L))
        
        collector.complete()
        
        val finalResult = (collector.currentState as ShiftState.Completed).result
        assertEquals(1, finalResult.roundsCompleted) // Since round 2 didn't receive Continue before complete
        assertEquals(2, finalResult.selections.size)
        assertEquals("cell_1", finalResult.selections[0].cellId)
        assertEquals("cell_2", finalResult.selections[1].cellId)
    }
}

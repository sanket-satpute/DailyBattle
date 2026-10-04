package com.sanket_satpute_20.dailybattle.domain.crowd

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrowdInputCollectorTest {

    private lateinit var collector: CrowdInputCollector
    private var currentTime = 1000L

    @Before
    fun setup() {
        collector = CrowdInputCollector(timeProvider = { currentTime })
    }

    @Test
    fun `prepare transitions state from NotStarted to Question`() {
        val result = collector.prepare()

        assertTrue(result is DomainResult.Success)
        val state = collector.currentState
        assertTrue(state is CrowdState.Question)
        assertEquals("q_stub_1", (state as CrowdState.Question).question.id)
    }

    @Test
    fun `start fails if prepare is not called`() {
        val result = collector.start()

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.BattleState, (result as DomainResult.Failure).error)
    }

    @Test
    fun `valid submission flows to PredictionResult and then Completed`() {
        collector.prepare()
        collector.start()

        currentTime = 2500L
        val submissionResult = collector.handleInput(CrowdEvent.SubmitPrediction("c1", currentTime))
        
        assertTrue(submissionResult is DomainResult.Success)
        val state = collector.currentState
        assertTrue(state is CrowdState.PredictionResult)
        assertEquals("c1", (state as CrowdState.PredictionResult).selectedChoiceId)

        val continueResult = collector.handleInput(CrowdEvent.Continue)
        assertTrue(continueResult is DomainResult.Success)
        val finalState = collector.currentState
        assertTrue(finalState is CrowdState.Completed)

        val rawResult = (finalState as CrowdState.Completed).result
        assertEquals("c1", rawResult.selectedChoiceId)
        assertEquals(1500L, rawResult.timeToAnswerMs)
    }

    @Test
    fun `timeout flows to PredictionResult with null choice and then Completed`() {
        collector.prepare()
        collector.start()

        currentTime = 5000L
        val timeoutResult = collector.handleInput(CrowdEvent.Timeout)
        
        assertTrue(timeoutResult is DomainResult.Success)
        val state = collector.currentState
        assertTrue(state is CrowdState.PredictionResult)
        assertEquals(null, (state as CrowdState.PredictionResult).selectedChoiceId)

        collector.handleInput(CrowdEvent.Continue)
        val finalState = collector.currentState
        assertTrue(finalState is CrowdState.Completed)

        val rawResult = (finalState as CrowdState.Completed).result
        assertEquals(null, rawResult.selectedChoiceId)
        assertEquals(4000L, rawResult.timeToAnswerMs)
    }
}

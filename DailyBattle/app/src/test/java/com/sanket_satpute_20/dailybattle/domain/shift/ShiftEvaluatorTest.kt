package com.sanket_satpute_20.dailybattle.domain.shift

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftEvaluatorTest {

    private val evaluator = ShiftEvaluator(
        timeProvider = { 1000L },
        idGenerator = { "fixed-uuid" }
    )
    
    private val sessionId = BattleSessionId("test-session")
    private val challengeId = ChallengeId("test-challenge")

    @Test
    fun `evaluate assigns 0 score when no movement (empty selections) is provided`() {
        val rawResult = RawShiftResult(
            selections = emptyList(),
            challengeDurationMs = 5000L,
            roundsCompleted = 1
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals(0, result.score)
        assertEquals(300, result.maxScore)
        assertEquals(sessionId, result.sessionId)
        assertEquals(challengeId, result.challengeId)
    }

    @Test
    fun `evaluate assigns 0 score when multiple movements (multiple selections) are provided`() {
        val rawResult = RawShiftResult(
            selections = listOf(
                ShiftSelectionRecord("cell-1", 1000L),
                ShiftSelectionRecord("cell-2", 1500L)
            ),
            challengeDurationMs = 5000L,
            roundsCompleted = 1
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        // Multiple movement represents ambiguous/invalid input according to current rules
        assertEquals(0, result.score)
    }

    @Test
    fun `evaluate assigns full score when exactly one movement is selected (pending exact algorithm)`() {
        val rawResult = RawShiftResult(
            selections = listOf(
                ShiftSelectionRecord("cell-1", 1000L)
            ),
            challengeDurationMs = 5000L,
            roundsCompleted = 1
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        // Single valid selection yields full score pending DEC-GAME-002
        assertEquals(300, result.score)
    }
}

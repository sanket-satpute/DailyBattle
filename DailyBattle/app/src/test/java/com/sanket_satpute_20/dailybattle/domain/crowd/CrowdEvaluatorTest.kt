package com.sanket_satpute_20.dailybattle.domain.crowd

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Test

class CrowdEvaluatorTest {

    private val sessionId = BattleSessionId("session_123")
    private val challengeId = ChallengeId("crowd_challenge_1")
    
    // Controlled test dependencies
    private val testTime = 1600000000000L
    private val testId = "uuid-1234"
    private val evaluator = CrowdEvaluator(
        timeProvider = { testTime },
        idGenerator = { testId }
    )

    @Test
    fun `evaluate valid selection returns stubbed perfect score`() {
        val rawResult = RawCrowdResult(
            questionId = "q1",
            selectedChoiceId = "c2",
            timeToAnswerMs = 1500L
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals("res_uuid-1234", result.resultId.value)
        assertEquals(challengeId, result.challengeId)
        assertEquals(sessionId, result.sessionId)
        assertEquals(300, result.score)
        assertEquals(300, result.maxScore)
        assertEquals(testTime, result.completedAt)
    }

    @Test
    fun `evaluate timeout returns zero score`() {
        val rawResult = RawCrowdResult(
            questionId = "q1",
            selectedChoiceId = null,
            timeToAnswerMs = 3000L
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals("res_uuid-1234", result.resultId.value)
        assertEquals(challengeId, result.challengeId)
        assertEquals(sessionId, result.sessionId)
        assertEquals(0, result.score)
        assertEquals(300, result.maxScore)
        assertEquals(testTime, result.completedAt)
    }
}

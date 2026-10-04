package com.sanket_satpute_20.dailybattle.domain.snap

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Test

class SnapEvaluatorTest {

    private val sessionId = BattleSessionId("session_1")
    private val challengeId = ChallengeId("snap_1")
    
    private val testTime = 1600000000000L
    private val testId = "uuid-snap"
    private val evaluator = SnapEvaluator(
        timeProvider = { testTime },
        idGenerator = { testId }
    )

    @Test
    fun `evaluate valid tap returns max score`() {
        val rawResult = RawSnapResult(
            targetAppeared = true,
            targetAppearanceTimeMs = 1000L,
            taps = listOf(TapRecord(timestampMs = 1200L, elementId = "target_1")),
            roundDurationMs = 2000L
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals(300, result.score)
    }

    @Test
    fun `evaluate false start returns zero score`() {
        // Tapped before appearance
        val rawResult = RawSnapResult(
            targetAppeared = true,
            targetAppearanceTimeMs = 1000L,
            taps = listOf(TapRecord(timestampMs = 900L, elementId = null)),
            roundDurationMs = 2000L
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals(0, result.score)
    }

    @Test
    fun `evaluate tap when target never appeared returns zero score`() {
        // Tapped when target never appeared
        val rawResult = RawSnapResult(
            targetAppeared = false,
            targetAppearanceTimeMs = null,
            taps = listOf(TapRecord(timestampMs = 500L, elementId = null)),
            roundDurationMs = 2000L
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals(0, result.score)
    }

    @Test
    fun `evaluate no taps returns zero score`() {
        val rawResult = RawSnapResult(
            targetAppeared = true,
            targetAppearanceTimeMs = 1000L,
            taps = emptyList(),
            roundDurationMs = 3000L
        )

        val result = evaluator.evaluate(rawResult, sessionId, challengeId)

        assertEquals(0, result.score)
    }
}

package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeScoreValidatorTest {

    private val validator = ChallengeScoreValidator()

    private fun makeResult(score: Int, maxScore: Int = 300): ChallengeResult {
        return ChallengeResult(
            resultId = ChallengeResultId("res_test"),
            challengeId = ChallengeId("challenge_1"),
            sessionId = BattleSessionId("session_1"),
            score = score,
            maxScore = maxScore,
            completedAt = 1600000000000L,
        )
    }

    // ---------------------------------------------------------------
    // 0 score
    // ---------------------------------------------------------------
    @Test
    fun `validate zero score passes unchanged`() {
        val result = makeResult(score = 0)
        val validated = validator.validate(result, ChallengeType.SNAP)
        assertEquals(0, validated.score)
        assertEquals(300, validated.maxScore)
    }

    // ---------------------------------------------------------------
    // Maximum score
    // ---------------------------------------------------------------
    @Test
    fun `validate maximum score passes unchanged`() {
        val result = makeResult(score = 300)
        val validated = validator.validate(result, ChallengeType.SNAP)
        assertEquals(300, validated.score)
    }

    // ---------------------------------------------------------------
    // Negative input
    // ---------------------------------------------------------------
    @Test
    fun `validate negative score clamps to zero`() {
        val result = makeResult(score = -50)
        val validated = validator.validate(result, ChallengeType.SNAP)
        assertEquals(0, validated.score)
    }

    // ---------------------------------------------------------------
    // Score overflow
    // ---------------------------------------------------------------
    @Test
    fun `validate score overflow clamps to max`() {
        val result = makeResult(score = 999)
        val validated = validator.validate(result, ChallengeType.SNAP)
        assertEquals(300, validated.score)
    }

    // ---------------------------------------------------------------
    // Invalid maxScore correction
    // ---------------------------------------------------------------
    @Test
    fun `validate corrects wrong maxScore to approved value`() {
        val result = makeResult(score = 200, maxScore = 500)
        val validated = validator.validate(result, ChallengeType.SHIFT)
        assertEquals(200, validated.score)
        assertEquals(300, validated.maxScore)
    }

    // ---------------------------------------------------------------
    // Per-challenge-type max scores
    // ---------------------------------------------------------------
    @Test
    fun `validate snap max score is 300`() {
        val result = makeResult(score = 300, maxScore = 300)
        val validated = validator.validate(result, ChallengeType.SNAP)
        assertEquals(300, validated.maxScore)
    }

    @Test
    fun `validate shift max score is 300`() {
        val result = makeResult(score = 300, maxScore = 300)
        val validated = validator.validate(result, ChallengeType.SHIFT)
        assertEquals(300, validated.maxScore)
    }

    @Test
    fun `validate crowd call max score is 300`() {
        val result = makeResult(score = 300, maxScore = 300)
        val validated = validator.validate(result, ChallengeType.CROWD_CALL)
        assertEquals(300, validated.maxScore)
    }

    // ---------------------------------------------------------------
    // Valid result passes through unchanged (referential equality)
    // ---------------------------------------------------------------
    @Test
    fun `validate returns same instance when already valid`() {
        val result = makeResult(score = 150, maxScore = 300)
        val validated = validator.validate(result, ChallengeType.SNAP)
        assert(result === validated) { "Expected same instance for already-valid result" }
    }
}

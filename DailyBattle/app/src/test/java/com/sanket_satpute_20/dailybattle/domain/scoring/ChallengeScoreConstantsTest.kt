package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeScoreConstantsTest {

    @Test
    fun `snap max score is 300`() {
        assertEquals(300, ChallengeScoreConstants.maxScoreFor(ChallengeType.SNAP))
    }

    @Test
    fun `shift max score is 300`() {
        assertEquals(300, ChallengeScoreConstants.maxScoreFor(ChallengeType.SHIFT))
    }

    @Test
    fun `crowd call max score is 300`() {
        assertEquals(300, ChallengeScoreConstants.maxScoreFor(ChallengeType.CROWD_CALL))
    }

    @Test
    fun `max consistency score is 100`() {
        assertEquals(100, ChallengeScoreConstants.MAX_CONSISTENCY_SCORE)
    }

    @Test
    fun `max total score is 1000`() {
        assertEquals(1000, ChallengeScoreConstants.MAX_TOTAL_SCORE)
    }

    @Test
    fun `min score is 0`() {
        assertEquals(0, ChallengeScoreConstants.MIN_SCORE)
    }

    @Test
    fun `all challenge types have entries`() {
        ChallengeType.entries.forEach { type ->
            val maxScore = ChallengeScoreConstants.maxScoreFor(type)
            assert(maxScore > 0) { "Max score for $type must be positive" }
        }
    }
}

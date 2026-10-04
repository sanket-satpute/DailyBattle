package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult

/**
 * Aggregates individual challenge scores and computes consistency to produce the final total score.
 * 
 * Enforces the structure from `02_GAMEPLAY.md` §12:
 * Total = Snap + Shift + Crowd Call + Consistency
 */
class BattleScoreAggregator(
    private val consistencyEvaluator: ConsistencyEvaluator = ConsistencyEvaluator(),
    private val validator: ChallengeScoreValidator = ChallengeScoreValidator()
) {
    /**
     * Aggregates challenge results into an [AggregatedBattleScore].
     * 
     * @param mode The battle mode (OFFICIAL or PRACTICE).
     * @param snapResult Result of the Snap challenge.
     * @param shiftResult Result of the Shift challenge.
     * @param crowdCallResult Result of the Crowd Call challenge.
     * @return The aggregated score components. Missing challenge scores are treated as 0.
     */
    fun aggregate(
        mode: BattleMode,
        snapResult: ChallengeResult?,
        shiftResult: ChallengeResult?,
        crowdCallResult: ChallengeResult?
    ): AggregatedBattleScore {
        
        // Sanitize the inputs first to clamp scores to 0-max
        val validSnap = snapResult?.let { validator.validate(it, com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType.SNAP) }
        val validShift = shiftResult?.let { validator.validate(it, com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType.SHIFT) }
        val validCrowdCall = crowdCallResult?.let { validator.validate(it, com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType.CROWD_CALL) }
        
        // Missing challenge results are treated as 0 score
        val snapScore = validSnap?.score ?: 0
        val shiftScore = validShift?.score ?: 0
        val crowdCallScore = validCrowdCall?.score ?: 0
        
        // Consistency Evaluator returns null if any result is missing or if practice mode
        val consistencyScore = consistencyEvaluator.evaluate(
            mode, 
            validSnap, 
            validShift, 
            validCrowdCall
        ) ?: 0

        val totalScore = snapScore + shiftScore + crowdCallScore + consistencyScore

        return AggregatedBattleScore(
            snapScore = snapScore,
            shiftScore = shiftScore,
            crowdCallScore = crowdCallScore,
            consistencyScore = consistencyScore,
            totalScore = totalScore.coerceIn(0, ChallengeScoreConstants.MAX_TOTAL_SCORE)
        )
    }
}

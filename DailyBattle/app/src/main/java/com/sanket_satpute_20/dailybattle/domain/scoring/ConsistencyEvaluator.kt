package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Calculates the Consistency Score according to the approved Sprint 11.3 formula.
 *
 * Consistency measures how evenly the player performed across Snap, Shift,
 * and Crowd Call. It is a measure of performance balance, not an independent
 * measure of accuracy, reaction time, memory, or social prediction.
 *
 * Properties enforced:
 * - Deterministic, no AI, no randomness.
 * - Official scores only. Practice scores return null.
 * - Missing/incomplete challenge scores return null.
 * - Range is clamped to 0-100 and rounded to the nearest integer.
 */
class ConsistencyEvaluator {

    /**
     * Evaluates the Consistency Score given the battle mode and individual challenge results.
     *
     * @param mode The BattleMode (OFFICIAL or PRACTICE).
     * @param snapResult Final official Snap result.
     * @param shiftResult Final official Shift result.
     * @param crowdCallResult Final official Crowd Call result.
     * @return The calculated Consistency Score (0-100), or null if the inputs are incomplete or practice.
     */
    fun evaluate(
        mode: BattleMode,
        snapResult: ChallengeResult?,
        shiftResult: ChallengeResult?,
        crowdCallResult: ChallengeResult?
    ): Int? {
        if (mode == BattleMode.PRACTICE) {
            return null
        }

        if (snapResult == null || shiftResult == null || crowdCallResult == null) {
            return null
        }

        val snapScore = snapResult.score.toDouble()
        val shiftScore = shiftResult.score.toDouble()
        val crowdCallScore = crowdCallResult.score.toDouble()

        // Normalize
        val s = snapScore / 300.0
        val h = shiftScore / 300.0
        val c = crowdCallScore / 300.0

        // Mean
        val mean = (s + h + c) / 3.0

        // Population standard deviation
        val variance = ((s - mean).pow(2) + (h - mean).pow(2) + (c - mean).pow(2)) / 3.0
        val stdDev = sqrt(variance)

        // Maximum possible standard deviation for 3 values constrained to [0,1]
        val maxStdDev = sqrt(2.0) / 3.0

        // Balance
        val balance = 1.0 - (stdDev / maxStdDev)

        // Consistency
        val consistencyRaw = 100.0 * balance

        // Final Score (clamp then round)
        return consistencyRaw.coerceIn(0.0, 100.0).roundToInt()
    }
}

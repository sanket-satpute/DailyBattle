package com.sanket_satpute_20.dailybattle.domain.shift

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import java.util.UUID

/**
 * Evaluates the raw input from the Shift challenge to produce an official ChallengeResult.
 *
 * Scoring algorithm is PENDING (DEC-GAME-002). This provides a stubbed scoring layer
 * to satisfy the architectural contract while explicitly handling edge cases like
 * no movement, multiple movement, and ambiguous movement.
 */
class ShiftEvaluator(
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val idGenerator: () -> String = { UUID.randomUUID().toString() }
) {
    fun evaluate(
        rawResult: RawShiftResult,
        sessionId: BattleSessionId,
        challengeId: ChallengeId
    ): ChallengeResult {
        
        // STUBBED SCORING: Pending DEC-GAME-002
        // We enforce edge-case penalties based on the raw selection size.
        
        // No movement (0 selections) -> score 0
        // Multiple/Ambiguous movement (>1 selections) -> score 0
        // Exactly 1 selection -> 300 (stubbed perfect score)
        
        val score = when {
            rawResult.selections.isEmpty() -> 0
            rawResult.selections.size > 1 -> 0
            else -> 300
        }

        return ChallengeResult(
            resultId = ChallengeResultId("res_${idGenerator()}"),
            challengeId = challengeId,
            sessionId = sessionId,
            score = score,
            maxScore = 300,
            completedAt = timeProvider()
        )
    }
}

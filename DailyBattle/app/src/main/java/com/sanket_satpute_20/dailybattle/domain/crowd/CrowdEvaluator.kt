package com.sanket_satpute_20.dailybattle.domain.crowd

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import java.util.UUID

/**
 * Evaluates the raw input from the Crowd Call challenge to produce an official ChallengeResult.
 *
 * Scoring algorithm is PENDING (DEC-GAME-003). This provides a stubbed scoring layer
 * to satisfy the architectural contract while explicitly handling edge cases like
 * timeout / no answer selected.
 */
class CrowdEvaluator(
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val idGenerator: () -> String = { UUID.randomUUID().toString() }
) {
    fun evaluate(
        rawResult: RawCrowdResult,
        sessionId: BattleSessionId,
        challengeId: ChallengeId
    ): ChallengeResult {
        
        // STUBBED SCORING: Pending DEC-GAME-003
        // We enforce edge-case penalties based on the raw selection.
        
        // No selection (Timeout) -> score 0
        // Has selection -> 300 (stubbed score, since actual majority calculation requires backend distribution)
        
        val score = if (rawResult.selectedChoiceId == null) {
            0
        } else {
            300
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

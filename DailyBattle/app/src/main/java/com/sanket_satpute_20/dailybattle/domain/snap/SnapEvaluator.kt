package com.sanket_satpute_20.dailybattle.domain.snap

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import java.util.UUID

/**
 * Evaluates the raw input from the Snap challenge to produce an official ChallengeResult.
 *
 * Scoring algorithm is PENDING (DEC-GAME-001). This provides a stubbed scoring layer
 * to satisfy the architectural contract while explicitly handling edge cases like
 * false starts (negative input) and timeouts (0 score).
 */
class SnapEvaluator(
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val idGenerator: () -> String = { UUID.randomUUID().toString() }
) {
    fun evaluate(
        rawResult: RawSnapResult,
        sessionId: BattleSessionId,
        challengeId: ChallengeId
    ): ChallengeResult {
        
        // STUBBED SCORING: Pending DEC-GAME-001
        
        var score = 0
        if (rawResult.taps.isNotEmpty()) {
            val firstTap = rawResult.taps.first()
            
            // Check false start (tapped before target appeared, or target never appeared but user tapped)
            val isFalseStart = !rawResult.targetAppeared || 
                (rawResult.targetAppearanceTimeMs != null && firstTap.timestampMs < rawResult.targetAppearanceTimeMs)
            
            if (!isFalseStart) {
                // Legitimate reaction, stub max score
                score = 300
            }
        }

        return ChallengeResult(
            resultId = ChallengeResultId("res_${idGenerator()}"),
            challengeId = challengeId,
            sessionId = sessionId,
            score = score,
            maxScore = 300,
            completedAt = timeProvider(),
            metadata = null
        )
    }
}

package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType

/**
 * Validates and enforces the approved score constraints on a [ChallengeResult].
 *
 * Per `02_GAMEPLAY.md` §13 (Score Integrity):
 * - Gameplay scores must be generated from gameplay events.
 * - The client must not allow arbitrary modification of the final score.
 *
 * This validator enforces:
 * - Score is within [0, maxScore] for the given challenge type.
 * - Negative scores are clamped to 0.
 * - Scores exceeding maxScore are clamped to maxScore.
 * - maxScore matches the approved constant for the challenge type.
 *
 * The actual scoring algorithms remain PENDING (DEC-GAME-001/002/003).
 * This validator sits downstream of whatever scoring logic produces the result,
 * acting as a safety net that enforces the documented structural bounds.
 */
class ChallengeScoreValidator {

    /**
     * Validates a [ChallengeResult] against the approved scoring constraints
     * for the given [challengeType].
     *
     * Returns a new [ChallengeResult] with clamped score values if they
     * fall outside the approved range. The original result is returned
     * unchanged if it is already valid.
     */
    fun validate(
        result: ChallengeResult,
        challengeType: ChallengeType
    ): ChallengeResult {
        val approvedMax = ChallengeScoreConstants.maxScoreFor(challengeType)

        val clampedScore = result.score.coerceIn(
            ChallengeScoreConstants.MIN_SCORE,
            approvedMax
        )

        val correctedMaxScore = approvedMax

        return if (clampedScore == result.score && correctedMaxScore == result.maxScore) {
            result
        } else {
            result.copy(
                score = clampedScore,
                maxScore = correctedMaxScore
            )
        }
    }
}

package com.sanket_satpute_20.dailybattle.domain.scoring

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType

/**
 * Defines the approved maximum scores for each challenge type and the
 * scoring structure for an official Battle.
 *
 * Per `02_GAMEPLAY.md` §12 and `08_DATA_AND_API.md` §24/§26:
 * - Snap:        0–300
 * - Shift:       0–300
 * - Crowd Call:  0–300
 * - Consistency: 0–100 (PENDING — not implemented here)
 * - Total:       0–1000
 *
 * The exact scoring algorithms producing each component score remain PENDING.
 * This object enforces only the approved structural contracts.
 */
object ChallengeScoreConstants {

    /** Maximum score for each challenge type. */
    val MAX_SCORES: Map<ChallengeType, Int> = mapOf(
        ChallengeType.SNAP to 300,
        ChallengeType.SHIFT to 300,
        ChallengeType.CROWD_CALL to 300,
    )

    /** Maximum consistency score (PENDING formula — DEC not yet defined). */
    const val MAX_CONSISTENCY_SCORE: Int = 100

    /** Maximum total battle score. */
    const val MAX_TOTAL_SCORE: Int = 1000

    /** Minimum allowed score for any component. */
    const val MIN_SCORE: Int = 0

    /**
     * Returns the approved maximum score for the given [challengeType].
     * @throws IllegalArgumentException if the type is not recognized.
     */
    fun maxScoreFor(challengeType: ChallengeType): Int {
        return MAX_SCORES[challengeType]
            ?: throw IllegalArgumentException("Unknown challenge type: $challengeType")
    }
}

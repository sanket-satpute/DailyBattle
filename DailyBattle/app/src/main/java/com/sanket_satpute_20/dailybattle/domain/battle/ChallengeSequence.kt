package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Authoritative definition of the official challenge sequence.
 *
 * Per `02_GAMEPLAY.md` §7 (REQ-GAME-002) and `08_DATA_AND_API.md` Invariant 7:
 *
 * Official challenge order is:
 *   1. SNAP
 *   2. SHIFT
 *   3. CROWD_CALL
 *
 * This object is the single source of truth for challenge ordering.
 * No other code may independently define or reorder the official sequence.
 */
object ChallengeSequence {

    /**
     * The official challenge sequence, indexed by challenge order (1-based).
     * Index 0 is unused; indices 1–3 map to the official types.
     */
    private val officialSequence: List<ChallengeType> = listOf(
        ChallengeType.SNAP,
        ChallengeType.SHIFT,
        ChallengeType.CROWD_CALL,
    )

    /** Total number of challenges in an official Battle. */
    const val TOTAL_CHALLENGES: Int = 3

    /**
     * Returns the [ChallengeType] for the given 1-based [challengeIndex].
     *
     * @param challengeIndex 1-based position (1 = Snap, 2 = Shift, 3 = Crowd Call)
     * @return the corresponding [ChallengeType]
     * @throws IllegalArgumentException if the index is out of the valid range [1..3]
     */
    fun typeForIndex(challengeIndex: Int): ChallengeType {
        require(challengeIndex in 1..TOTAL_CHALLENGES) {
            "Challenge index must be in 1..$TOTAL_CHALLENGES, was $challengeIndex"
        }
        return officialSequence[challengeIndex - 1]
    }

    /**
     * Returns the 1-based official index for the given [ChallengeType].
     *
     * @return the official position (1 = Snap, 2 = Shift, 3 = Crowd Call)
     */
    fun indexForType(type: ChallengeType): Int {
        return officialSequence.indexOf(type) + 1
    }

    /**
     * Validates that a [Battle]'s challenge list conforms to the official sequence.
     *
     * Per `08_DATA_AND_API.md` §9 and Invariant 7:
     * - The list must contain exactly [TOTAL_CHALLENGES] entries.
     * - Each challenge's [Challenge.order] must match its position (1, 2, 3).
     * - Each challenge's [Challenge.type] must match the official type for that position.
     *
     * @return `true` if the battle's challenges are valid, `false` otherwise.
     */
    fun isValidSequence(challenges: List<Challenge>): Boolean {
        if (challenges.size != TOTAL_CHALLENGES) return false

        return challenges.all { challenge ->
            challenge.order in 1..TOTAL_CHALLENGES &&
                challenge.type == officialSequence[challenge.order - 1]
        } && challenges.map { it.order }.toSet() == setOf(1, 2, 3)
    }
}

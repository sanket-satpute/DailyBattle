package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Domain enumeration of the persitable states for a Challenge Session.
 *
 * Per `08_DATA_AND_API.md` §22, these conceptual values map directly
 * to the gameplay state model:
 * - READY
 * - ACTIVE
 * - CORRECT
 * - INCORRECT
 * - COMPLETE
 */
enum class ChallengeSessionStatus {
    READY,
    ACTIVE,
    CORRECT,
    INCORRECT,
    COMPLETE,
}

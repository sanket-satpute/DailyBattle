package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * The challenge type within an official Battle.
 *
 * Per `08_DATA_AND_API.md` §11:
 * - [SNAP]       — first challenge (reaction-based)
 * - [SHIFT]      — second challenge (memory/identification)
 * - [CROWD_CALL] — third challenge (prediction-based)
 *
 * The official order is SNAP → SHIFT → CROWD_CALL.
 * No additional challenge type may be added without an approved product requirement.
 */
enum class ChallengeType {
    SNAP,
    SHIFT,
    CROWD_CALL,
}

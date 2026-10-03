package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Availability/lifecycle status of an official daily Battle.
 *
 * Per `08_DATA_AND_API.md` §8, conceptual values:
 * - [NOT_AVAILABLE] — Battle is not yet available to users
 * - [AVAILABLE]     — Battle is open and can be started
 * - [ACTIVE]        — Battle is currently ongoing (within its active window)
 * - [COMPLETED]     — Battle window has closed
 *
 * The exact backend status model is PENDING (`08_DATA_AND_API.md` §8 line 173).
 * Client must not invent additional product states.
 */
enum class BattleStatus {
    NOT_AVAILABLE,
    AVAILABLE,
    ACTIVE,
    COMPLETED,
}

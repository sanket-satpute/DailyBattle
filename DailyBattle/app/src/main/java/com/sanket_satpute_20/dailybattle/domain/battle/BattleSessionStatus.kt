package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Lifecycle status of a Battle Session.
 *
 * Per `08_DATA_AND_API.md` §20, conceptual values:
 * - [NOT_STARTED] — session created but not yet begun
 * - [IN_PROGRESS] — session is actively underway
 * - [COMPLETED]   — session has been fully completed
 *
 * Additional internal technical states may exist (§20 note) but must not
 * change product behavior without approval.
 */
enum class BattleSessionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}

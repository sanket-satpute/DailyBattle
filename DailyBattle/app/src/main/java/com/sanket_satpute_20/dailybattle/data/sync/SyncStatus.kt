package com.sanket_satpute_20.dailybattle.data.sync

/**
 * Status lifecycle of a queued synchronization operation.
 *
 * Conforms to 09_OFFLINE_AND_ERROR_HANDLING.md §48:
 * - [PENDING]: Queued locally, awaiting network synchronization.
 * - [SYNCING]: Currently executing network synchronization request.
 * - [SUCCESS]: Successfully acknowledged and synchronized with remote authority.
 * - [FAILED]: Network or execution failure; retained for recovery per §50.
 * - [CANCELLED]: Operation superseded or explicitly discarded.
 */
enum class SyncStatus {
    PENDING,
    SYNCING,
    SUCCESS,
    FAILED,
    CANCELLED
}

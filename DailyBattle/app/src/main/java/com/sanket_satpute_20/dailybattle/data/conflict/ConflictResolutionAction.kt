package com.sanket_satpute_20.dailybattle.data.conflict

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession

/**
 * Resolution actions output by the conflict resolution engine.
 *
 * Conforms to:
 * - 08_DATA_AND_API.md §74 (Backend is authoritative for conflict resolution)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §24 (Reconciliation of server-completed battles)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicate actions/mutations)
 */
sealed interface ConflictResolutionAction<out T> {

    /**
     * The authoritative remote state takes precedence.
     */
    data class AcceptRemote<T>(
        val remoteData: T,
        val reason: String,
    ) : ConflictResolutionAction<T>

    /**
     * The local state is retained (e.g. server returned stale data or local state is newer).
     */
    data class RetainLocal<T>(
        val localData: T,
        val reason: String,
    ) : ConflictResolutionAction<T>

    /**
     * A duplicate or conflicting pending mutation is discarded/cancelled.
     */
    data class DiscardMutation(
        val operationId: String,
        val reason: String,
    ) : ConflictResolutionAction<Nothing>

    /**
     * An active local battle session is reconciled with remote authoritative progress.
     */
    data class ReconcileSession(
        val session: BattleSession,
        val reason: String,
    ) : ConflictResolutionAction<BattleSession>
}

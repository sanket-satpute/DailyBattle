package com.sanket_satpute_20.dailybattle.data.sync

/**
 * Observable runtime synchronization state.
 */
sealed interface SyncState {
    data object Idle : SyncState
    data class Syncing(val activeOperationId: String) : SyncState
    data class Success(val completedOperationsCount: Int) : SyncState
    data class Error(val failedOperationId: String, val error: String) : SyncState
    data object Offline : SyncState
}

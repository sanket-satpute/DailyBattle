package com.sanket_satpute_20.dailybattle.data.sync

/**
 * Result of a synchronization batch execution.
 */
sealed interface SyncResult {
    data class Success(val syncedCount: Int) : SyncResult
    data class PartialFailure(val syncedCount: Int, val failedOperationId: String, val error: String) : SyncResult
    data class Failed(val failedOperationId: String, val error: String) : SyncResult
    data object Offline : SyncResult
    data object NoPendingOperations : SyncResult
}

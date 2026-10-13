package com.sanket_satpute_20.dailybattle.data.sync

import kotlinx.serialization.Serializable

/**
 * Conceptual queued synchronization unit implementing 09_OFFLINE_AND_ERROR_HANDLING.md §48:
 *
 * PendingOperation
 * ├── operationId
 * ├── operationType
 * ├── payload
 * ├── createdAt
 * ├── retryCount
 * ├── status
 * └── lastError
 */
@Serializable
data class PendingOperation(
    val operationId: String,
    val operationType: SyncOperationType,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val status: SyncStatus = SyncStatus.PENDING,
    val lastError: String? = null,
) {
    val isPending: Boolean get() = status == SyncStatus.PENDING
    val isSyncing: Boolean get() = status == SyncStatus.SYNCING
    val isSuccess: Boolean get() = status == SyncStatus.SUCCESS
    val isFailed: Boolean get() = status == SyncStatus.FAILED
    val isCancelled: Boolean get() = status == SyncStatus.CANCELLED
    val isUnknown: Boolean get() = status == SyncStatus.UNKNOWN
}

package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates synchronization of offline mutations with authoritative remote endpoints.
 *
 * Implements the lifecycle defined in Phase 17, Sprint 17.3:
 * Local change -> Pending operation -> Network available -> Sync -> Success.
 *
 * Conforms to:
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §48 (Synchronization Queue)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §49 (Sync Ordering)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §50 (Synchronization Failure — local state preserved, operation recoverable)
 */
interface Synchronizer {
    val syncState: StateFlow<SyncState>

    /**
     * Enqueues a local change mutation as a [PendingOperation].
     */
    suspend fun enqueue(
        operationType: SyncOperationType,
        payload: String,
        operationId: String = UUID.randomUUID().toString(),
    ): DomainResult<PendingOperation>

    /**
     * Executes pending operations when network is available.
     */
    suspend fun sync(): SyncResult

    /**
     * Observes network availability and automatically triggers synchronization when online.
     */
    fun startAutoSync(coroutineScope: CoroutineScope): Job
}

@Singleton
class DefaultSynchronizer @Inject constructor(
    private val queueStore: SyncQueueStore,
    private val networkMonitor: NetworkMonitor,
    private val syncExecutor: SyncExecutor,
    private val clock: Clock,
) : Synchronizer {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val syncMutex = Mutex()

    override suspend fun enqueue(
        operationType: SyncOperationType,
        payload: String,
        operationId: String,
    ): DomainResult<PendingOperation> {
        val operation = PendingOperation(
            operationId = operationId,
            operationType = operationType,
            payload = payload,
            createdAt = clock.millis(),
            retryCount = 0,
            status = SyncStatus.PENDING,
            lastError = null,
        )

        return when (val result = queueStore.enqueue(operation)) {
            is DomainResult.Success -> DomainResult.Success(operation)
            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun sync(): SyncResult = syncMutex.withLock {
        if (!networkMonitor.isCurrentlyOnline) {
            _syncState.value = SyncState.Offline
            return@withLock SyncResult.Offline
        }

        val allOperations = queueStore.getPendingOperations()
        val pending = allOperations.filter { it.status == SyncStatus.PENDING || it.status == SyncStatus.FAILED }

        if (pending.isEmpty()) {
            _syncState.value = SyncState.Idle
            return@withLock SyncResult.NoPendingOperations
        }

        // Preserve logical and chronological ordering (§49)
        val orderedPending = pending.sortedBy { it.createdAt }
        var syncedCount = 0

        for (operation in orderedPending) {
            _syncState.value = SyncState.Syncing(operation.operationId)
            val syncingOp = operation.copy(status = SyncStatus.SYNCING)
            queueStore.updateOperation(syncingOp)

            when (val result = syncExecutor.execute(syncingOp)) {
                is DomainResult.Success -> {
                    queueStore.removeOperation(operation.operationId)
                    syncedCount++
                }
                is DomainResult.Failure -> {
                    val errorDescription = result.error::class.simpleName ?: "SyncError"
                    val failedOp = syncingOp.copy(
                        status = SyncStatus.FAILED,
                        retryCount = syncingOp.retryCount + 1,
                        lastError = errorDescription,
                    )
                    queueStore.updateOperation(failedOp)
                    _syncState.value = SyncState.Error(operation.operationId, errorDescription)

                    // Dependent operations must halt to avoid out-of-order execution (§49)
                    return@withLock if (syncedCount > 0) {
                        SyncResult.PartialFailure(syncedCount, operation.operationId, errorDescription)
                    } else {
                        SyncResult.Failed(operation.operationId, errorDescription)
                    }
                }
            }
        }

        _syncState.value = SyncState.Success(syncedCount)
        return@withLock SyncResult.Success(syncedCount)
    }

    override fun startAutoSync(coroutineScope: CoroutineScope): Job {
        return coroutineScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                if (isOnline) {
                    sync()
                } else {
                    _syncState.value = SyncState.Offline
                }
            }
        }
    }
}

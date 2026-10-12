package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage contract for managing queued [PendingOperation] instances.
 */
interface SyncQueueStore {
    suspend fun enqueue(operation: PendingOperation): DomainResult<Unit>
    suspend fun getPendingOperations(): List<PendingOperation>
    suspend fun getOperation(operationId: String): PendingOperation?
    suspend fun updateOperation(operation: PendingOperation): DomainResult<Unit>
    suspend fun removeOperation(operationId: String): DomainResult<Unit>
    suspend fun clearQueue(): DomainResult<Unit>
}

/**
 * Thread-safe in-memory queue store preserving logical and chronological order.
 */
@Singleton
class InMemorySyncQueueStore @Inject constructor() : SyncQueueStore {

    private val queue = ConcurrentHashMap<String, PendingOperation>()

    override suspend fun enqueue(operation: PendingOperation): DomainResult<Unit> {
        queue[operation.operationId] = operation
        return DomainResult.Success(Unit)
    }

    override suspend fun getPendingOperations(): List<PendingOperation> {
        // Return sorted by createdAt to preserve logical sequence (§49)
        return queue.values
            .sortedBy { it.createdAt }
    }

    override suspend fun getOperation(operationId: String): PendingOperation? {
        return queue[operationId]
    }

    override suspend fun updateOperation(operation: PendingOperation): DomainResult<Unit> {
        queue[operation.operationId] = operation
        return DomainResult.Success(Unit)
    }

    override suspend fun removeOperation(operationId: String): DomainResult<Unit> {
        queue.remove(operationId)
        return DomainResult.Success(Unit)
    }

    override suspend fun clearQueue(): DomainResult<Unit> {
        queue.clear()
        return DomainResult.Success(Unit)
    }
}

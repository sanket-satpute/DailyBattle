package com.sanket_satpute_20.dailybattle.data.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncQueueStoreTest {

    private lateinit var store: InMemorySyncQueueStore

    @Before
    fun setup() {
        store = InMemorySyncQueueStore()
    }

    @Test
    fun enqueueAddsOperationAndGetOperationReturnsIt() = runTest {
        val op = PendingOperation(
            operationId = "op_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            createdAt = 1000L,
        )

        store.enqueue(op)

        val retrieved = store.getOperation("op_1")
        assertNotNull(retrieved)
        assertEquals("op_1", retrieved?.operationId)
        assertEquals(SyncOperationType.START_BATTLE, retrieved?.operationType)
    }

    @Test
    fun getPendingOperationsReturnsOperationsSortedByCreatedAtAscending() = runTest {
        val op2 = PendingOperation(
            operationId = "op_2",
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = "{}",
            createdAt = 2000L,
        )
        val op1 = PendingOperation(
            operationId = "op_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            createdAt = 1000L,
        )
        val op3 = PendingOperation(
            operationId = "op_3",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = "{}",
            createdAt = 3000L,
        )

        // Enqueue out of order
        store.enqueue(op2)
        store.enqueue(op3)
        store.enqueue(op1)

        val operations = store.getPendingOperations()
        assertEquals(3, operations.size)
        // Verify FIFO / chronological sequence (§49)
        assertEquals("op_1", operations[0].operationId)
        assertEquals("op_2", operations[1].operationId)
        assertEquals("op_3", operations[2].operationId)
    }

    @Test
    fun updateOperationModifiesStatusAndError() = runTest {
        val op = PendingOperation(
            operationId = "op_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            createdAt = 1000L,
            status = SyncStatus.PENDING,
        )
        store.enqueue(op)

        val updated = op.copy(
            status = SyncStatus.FAILED,
            retryCount = 1,
            lastError = "Connection timeout",
        )
        store.updateOperation(updated)

        val retrieved = store.getOperation("op_1")
        assertNotNull(retrieved)
        assertEquals(SyncStatus.FAILED, retrieved?.status)
        assertEquals(1, retrieved?.retryCount)
        assertEquals("Connection timeout", retrieved?.lastError)
    }

    @Test
    fun removeOperationDeletesEntry() = runTest {
        val op = PendingOperation(
            operationId = "op_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            createdAt = 1000L,
        )
        store.enqueue(op)
        assertNotNull(store.getOperation("op_1"))

        store.removeOperation("op_1")
        assertNull(store.getOperation("op_1"))
        assertTrue(store.getPendingOperations().isEmpty())
    }

    @Test
    fun clearQueueRemovesAllEntries() = runTest {
        store.enqueue(
            PendingOperation(
                operationId = "op_1",
                operationType = SyncOperationType.START_BATTLE,
                payload = "{}",
                createdAt = 1000L,
            )
        )
        store.enqueue(
            PendingOperation(
                operationId = "op_2",
                operationType = SyncOperationType.COMPLETE_BATTLE,
                payload = "{}",
                createdAt = 2000L,
            )
        )

        assertEquals(2, store.getPendingOperations().size)

        store.clearQueue()
        assertTrue(store.getPendingOperations().isEmpty())
    }
}

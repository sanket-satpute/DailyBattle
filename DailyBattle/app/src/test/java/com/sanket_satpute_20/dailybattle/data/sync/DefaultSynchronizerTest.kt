package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSynchronizerTest {

    private lateinit var fakeQueueStore: InMemorySyncQueueStore
    private lateinit var fakeNetworkMonitor: FakeNetworkMonitor
    private lateinit var fakeSyncExecutor: FakeSyncExecutor
    private var currentTime = 1_000_000L
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(currentTime)
    }

    private lateinit var synchronizer: DefaultSynchronizer

    @Before
    fun setup() {
        fakeQueueStore = InMemorySyncQueueStore()
        fakeNetworkMonitor = FakeNetworkMonitor()
        fakeSyncExecutor = FakeSyncExecutor()
        synchronizer = DefaultSynchronizer(
            queueStore = fakeQueueStore,
            networkMonitor = fakeNetworkMonitor,
            syncExecutor = fakeSyncExecutor,
            clock = clock,
        )
    }

    @Test
    fun enqueueCreatesPendingOperationInQueueStore() = runTest {
        val result = synchronizer.enqueue(
            operationType = SyncOperationType.START_BATTLE,
            payload = "{\"battleId\":\"b_1\"}",
            operationId = "op_test_1",
        )

        assertTrue(result is DomainResult.Success)
        val op = (result as DomainResult.Success).value
        assertEquals("op_test_1", op.operationId)
        assertEquals(SyncOperationType.START_BATTLE, op.operationType)
        assertEquals(SyncStatus.PENDING, op.status)
        assertEquals(1_000_000L, op.createdAt)

        val inStore = fakeQueueStore.getOperation("op_test_1")
        assertNotNull(inStore)
        assertEquals(SyncStatus.PENDING, inStore?.status)
    }

    @Test
    fun syncWhenNetworkOfflineReturnsOfflineAndPreservesPendingOperations() = runTest {
        fakeNetworkMonitor.setOnline(false)

        synchronizer.enqueue(
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            operationId = "op_offline",
        )

        val result = synchronizer.sync()
        assertEquals(SyncResult.Offline, result)
        assertEquals(SyncState.Offline, synchronizer.syncState.value)

        // Local state intact, operation remains recoverable (§50)
        val op = fakeQueueStore.getOperation("op_offline")
        assertNotNull(op)
        assertEquals(SyncStatus.PENDING, op?.status)
        assertEquals(0, fakeSyncExecutor.executedOperations.size)
    }

    @Test
    fun syncWhenNoPendingOperationsReturnsNoPendingOperations() = runTest {
        fakeNetworkMonitor.setOnline(true)

        val result = synchronizer.sync()
        assertEquals(SyncResult.NoPendingOperations, result)
        assertEquals(SyncState.Idle, synchronizer.syncState.value)
    }

    @Test
    fun syncWhenNetworkAvailableExecutesOperationsInFifoOrderAndSucceeds() = runTest {
        fakeNetworkMonitor.setOnline(true)
        fakeSyncExecutor.defaultResult = DomainResult.Success(Unit)

        // Enqueue three dependent operations with distinct timestamps
        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{\"id\":\"1\"}", "op_1")
        currentTime = 2000L
        synchronizer.enqueue(SyncOperationType.SUBMIT_CHALLENGE_RESULT, "{\"id\":\"2\"}", "op_2")
        currentTime = 3000L
        synchronizer.enqueue(SyncOperationType.COMPLETE_BATTLE, "{\"id\":\"3\"}", "op_3")

        val result = synchronizer.sync()
        assertTrue(result is SyncResult.Success)
        assertEquals(3, (result as SyncResult.Success).syncedCount)
        assertEquals(SyncState.Success(3), synchronizer.syncState.value)

        // Verify operations executed in strict chronological order (§49)
        assertEquals(3, fakeSyncExecutor.executedOperations.size)
        assertEquals("op_1", fakeSyncExecutor.executedOperations[0].operationId)
        assertEquals("op_2", fakeSyncExecutor.executedOperations[1].operationId)
        assertEquals("op_3", fakeSyncExecutor.executedOperations[2].operationId)

        // Queue should now be empty after successful sync
        assertTrue(fakeQueueStore.getPendingOperations().isEmpty())
    }

    @Test
    fun syncFailurePreservesOperationAsFailedAndHaltsDependentOperations() = runTest {
        fakeNetworkMonitor.setOnline(true)

        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_1")
        currentTime = 2000L
        synchronizer.enqueue(SyncOperationType.COMPLETE_BATTLE, "{}", "op_2")

        // First operation fails with network error
        fakeSyncExecutor.setResultFor("op_1", DomainResult.Failure(AppError.Network))

        val result = synchronizer.sync()
        assertTrue(result is SyncResult.Failed)
        assertEquals("op_1", (result as SyncResult.Failed).failedOperationId)

        // Operation 1 remains in queue marked FAILED (§50)
        val op1 = fakeQueueStore.getOperation("op_1")
        assertNotNull(op1)
        assertEquals(SyncStatus.FAILED, op1?.status)
        assertEquals(1, op1?.retryCount)
        assertNotNull(op1?.lastError)

        // Operation 2 was NOT executed to preserve ordering (§49)
        val op2 = fakeQueueStore.getOperation("op_2")
        assertNotNull(op2)
        assertEquals(SyncStatus.PENDING, op2?.status)
        assertEquals(1, fakeSyncExecutor.executedOperations.size)
    }

    @Test
    fun partialSyncSuccessWhenMiddleOperationFails() = runTest {
        fakeNetworkMonitor.setOnline(true)

        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_1")
        currentTime = 2000L
        synchronizer.enqueue(SyncOperationType.SUBMIT_CHALLENGE_RESULT, "{}", "op_2")

        fakeSyncExecutor.setResultFor("op_1", DomainResult.Success(Unit))
        fakeSyncExecutor.setResultFor("op_2", DomainResult.Failure(AppError.Network))

        val result = synchronizer.sync()
        assertTrue(result is SyncResult.PartialFailure)
        val partial = result as SyncResult.PartialFailure
        assertEquals(1, partial.syncedCount)
        assertEquals("op_2", partial.failedOperationId)

        // Op 1 was removed on success
        assertTrue(fakeQueueStore.getOperation("op_1") == null)
        // Op 2 remains FAILED
        assertEquals(SyncStatus.FAILED, fakeQueueStore.getOperation("op_2")?.status)
    }

    @Test
    fun autoSyncTriggersWhenNetworkBecomesAvailable() = runTest {
        fakeNetworkMonitor.setOnline(false)
        fakeSyncExecutor.defaultResult = DomainResult.Success(Unit)

        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_auto")

        val job = synchronizer.startAutoSync(this)
        advanceUntilIdle()

        // Still offline, queue intact
        assertEquals(1, fakeQueueStore.getPendingOperations().size)

        // Transition to online: Network available -> Sync -> Success
        fakeNetworkMonitor.setOnline(true)
        advanceUntilIdle()

        // Sync should have triggered and cleared the queue
        assertTrue(fakeQueueStore.getPendingOperations().isEmpty())
        assertEquals(1, fakeSyncExecutor.executedOperations.size)
        assertEquals("op_auto", fakeSyncExecutor.executedOperations[0].operationId)

        job.cancel()
    }

    private class FakeNetworkMonitor : NetworkMonitor {
        private val _isOnline = MutableStateFlow(true)
        override val isOnline: Flow<Boolean> = _isOnline
        override val isCurrentlyOnline: Boolean get() = _isOnline.value

        fun setOnline(online: Boolean) {
            _isOnline.value = online
        }
    }

    private class FakeSyncExecutor : SyncExecutor {
        var defaultResult: DomainResult<Unit> = DomainResult.Success(Unit)
        val customResults = mutableMapOf<String, DomainResult<Unit>>()
        val executedOperations = mutableListOf<PendingOperation>()

        fun setResultFor(operationId: String, result: DomainResult<Unit>) {
            customResults[operationId] = result
        }

        override suspend fun execute(operation: PendingOperation): DomainResult<Unit> {
            executedOperations.add(operation)
            return customResults[operation.operationId] ?: defaultResult
        }
    }
}

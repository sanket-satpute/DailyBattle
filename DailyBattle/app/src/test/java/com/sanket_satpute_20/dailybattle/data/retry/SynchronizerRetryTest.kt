package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.sync.DefaultSynchronizer
import com.sanket_satpute_20.dailybattle.data.sync.InMemorySyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.NetworkMonitor
import com.sanket_satpute_20.dailybattle.data.sync.PendingOperation
import com.sanket_satpute_20.dailybattle.data.sync.SyncExecutor
import com.sanket_satpute_20.dailybattle.data.sync.SyncOperationType
import com.sanket_satpute_20.dailybattle.data.sync.SyncResult
import com.sanket_satpute_20.dailybattle.data.sync.SyncStatus
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SynchronizerRetryTest {

    private lateinit var queueStore: InMemorySyncQueueStore
    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var syncExecutor: FakeSyncExecutor
    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(1_000_000L)
    }

    private val retryPolicy = DefaultRetryPolicy(
        maxRetries = 3,
        initialDelayMillis = 1000L,
        maxDelayMillis = 10_000L,
        backoffMultiplier = 2.0,
    )

    private lateinit var synchronizer: DefaultSynchronizer

    @Before
    fun setup() {
        queueStore = InMemorySyncQueueStore()
        networkMonitor = FakeNetworkMonitor()
        syncExecutor = FakeSyncExecutor()
        synchronizer = DefaultSynchronizer(
            queueStore = queueStore,
            networkMonitor = networkMonitor,
            syncExecutor = syncExecutor,
            clock = clock,
            retryPolicy = retryPolicy,
        )
    }

    @Test
    fun syncExhaustsMaxRetriesAndStopsAutomaticallyPreventingInfiniteLoop() = runTest {
        networkMonitor.setOnline(true)
        syncExecutor.defaultResult = DomainResult.Failure(AppError.Network)

        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_loop_test")

        // First attempt (retryCount becomes 1)
        val res1 = synchronizer.sync()
        assertTrue(res1 is SyncResult.Failed)
        assertEquals(1, queueStore.getOperation("op_loop_test")?.retryCount)

        // Second attempt (retryCount becomes 2)
        val res2 = synchronizer.sync()
        assertTrue(res2 is SyncResult.Failed)
        assertEquals(2, queueStore.getOperation("op_loop_test")?.retryCount)

        // Third attempt (retryCount becomes 3 == maxRetries)
        val res3 = synchronizer.sync()
        assertTrue(res3 is SyncResult.Failed)
        assertEquals(3, queueStore.getOperation("op_loop_test")?.retryCount)

        // Fourth attempt: retry budget exhausted (§133)
        // Must NOT attempt to sync again, returns NoPendingOperations
        val res4 = synchronizer.sync()
        assertEquals(SyncResult.NoPendingOperations, res4)

        // Operation remains preserved as FAILED (§50), not deleted or looped forever
        val op = queueStore.getOperation("op_loop_test")
        assertNotNull(op)
        assertEquals(SyncStatus.FAILED, op?.status)
        assertEquals(3, op?.retryCount)
        assertEquals("Network", op?.lastError)
        // Total executions capped at 3
        assertEquals(3, syncExecutor.executedOperations.size)
    }

    @Test
    fun nonRetryableErrorIsNotAutomaticallyRetriedOnNextSync() = runTest {
        networkMonitor.setOnline(true)
        syncExecutor.defaultResult = DomainResult.Failure(AppError.Validation)

        synchronizer.enqueue(SyncOperationType.ADD_FRIEND, "{}", "op_validation_fail")

        // First attempt fails with non-retryable Validation error
        val res1 = synchronizer.sync()
        assertTrue(res1 is SyncResult.Failed)
        assertEquals(1, queueStore.getOperation("op_validation_fail")?.retryCount)
        assertEquals("Validation", queueStore.getOperation("op_validation_fail")?.lastError)

        // Next automatic sync: Validation error is not retryable, so it's skipped immediately (§133)
        val res2 = synchronizer.sync()
        assertEquals(SyncResult.NoPendingOperations, res2)
        assertEquals(1, syncExecutor.executedOperations.size)
    }

    @Test
    fun retryOperationResetsRetryCountAndAllowsManualRetry() = runTest {
        networkMonitor.setOnline(true)
        syncExecutor.defaultResult = DomainResult.Failure(AppError.Network)

        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_manual_retry")

        // Fail 3 times until exhausted
        repeat(3) { synchronizer.sync() }
        assertEquals(3, queueStore.getOperation("op_manual_retry")?.retryCount)
        assertEquals(SyncResult.NoPendingOperations, synchronizer.sync())

        // User manually taps Retry
        val resetResult = synchronizer.retryOperation("op_manual_retry")
        assertTrue(resetResult is DomainResult.Success)

        val resetOp = queueStore.getOperation("op_manual_retry")
        assertNotNull(resetOp)
        assertEquals(SyncStatus.PENDING, resetOp?.status)
        assertEquals(0, resetOp?.retryCount)
        assertNull(resetOp?.lastError)

        // Now sync executor recovers on next attempt
        syncExecutor.defaultResult = DomainResult.Success(Unit)
        val resAfterReset = synchronizer.sync()
        assertTrue(resAfterReset is SyncResult.Success)
        assertEquals(1, (resAfterReset as SyncResult.Success).syncedCount)
        assertNull(queueStore.getOperation("op_manual_retry"))
    }

    @Test
    fun cancelOperationSetsStatusToCancelledAndIsNeverRetried() = runTest {
        networkMonitor.setOnline(true)
        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_to_cancel")

        val cancelResult = synchronizer.cancelOperation("op_to_cancel")
        assertTrue(cancelResult is DomainResult.Success)

        val cancelledOp = queueStore.getOperation("op_to_cancel")
        assertNotNull(cancelledOp)
        assertEquals(SyncStatus.CANCELLED, cancelledOp?.status)

        // Sync does not execute CANCELLED operations
        val syncRes = synchronizer.sync()
        assertEquals(SyncResult.NoPendingOperations, syncRes)
        assertEquals(0, syncExecutor.executedOperations.size)
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
        val executedOperations = mutableListOf<PendingOperation>()

        override suspend fun execute(operation: PendingOperation): DomainResult<Unit> {
            executedOperations.add(operation)
            return defaultResult
        }
    }
}

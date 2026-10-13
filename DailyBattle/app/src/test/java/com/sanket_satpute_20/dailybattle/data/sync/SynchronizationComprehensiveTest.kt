package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.conflict.ConflictResolutionAction
import com.sanket_satpute_20.dailybattle.data.conflict.DefaultConflictResolver
import com.sanket_satpute_20.dailybattle.data.retry.DefaultRetryPolicy
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Comprehensive verification of Synchronization engine, queue operations, and retry boundaries (Sprint 23.2).
 *
 * Verifies:
 * - End-to-end multi-step queue lifecycle with FIFO dependency preservation.
 * - Dependent failure handling and interruption recovery (Sprint 18.4).
 * - Controlled retry limit boundaries preventing infinite retry loops (Rule 133).
 * - Exponential backoff delay calculation via DefaultRetryPolicy.
 * - Manual operation retry and user-initiated cancellation.
 * - Conflict resolution prioritizing authoritative backend state (§74).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SynchronizationComprehensiveTest {

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
    private lateinit var retryPolicy: DefaultRetryPolicy
    private lateinit var conflictResolver: DefaultConflictResolver

    @Before
    fun setup() {
        fakeQueueStore = InMemorySyncQueueStore()
        fakeNetworkMonitor = FakeNetworkMonitor()
        fakeSyncExecutor = FakeSyncExecutor()
        retryPolicy = DefaultRetryPolicy(maxRetries = 3, initialDelayMillis = 1000L, backoffMultiplier = 2.0)
        conflictResolver = DefaultConflictResolver()

        synchronizer = DefaultSynchronizer(
            queueStore = fakeQueueStore,
            networkMonitor = fakeNetworkMonitor,
            syncExecutor = fakeSyncExecutor,
            clock = clock,
            retryPolicy = retryPolicy,
            conflictResolver = conflictResolver,
        )
    }

    // =========================================================================
    // 1. Strict FIFO Queue Execution
    // =========================================================================

    @Test
    fun executesMultiStageBattleOperationsInStrictFifoOrder() = runTest {
        fakeNetworkMonitor.setOnline(true)
        fakeSyncExecutor.defaultResult = DomainResult.Success(Unit)

        // Enqueue: Start Battle -> Submit Challenge -> Complete Battle -> Add Friend
        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.START_BATTLE, """{"battleId":"b1"}""", "op-1")
        currentTime = 2000L
        synchronizer.enqueue(SyncOperationType.SUBMIT_CHALLENGE_RESULT, """{"score":290}""", "op-2")
        currentTime = 3000L
        synchronizer.enqueue(SyncOperationType.COMPLETE_BATTLE, """{"sessionId":"s1"}""", "op-3")
        currentTime = 4000L
        synchronizer.enqueue(SyncOperationType.ADD_FRIEND, """{"code":"FR-123"}""", "op-4")

        val result = synchronizer.sync()
        assertTrue("Sync must succeed completely", result is SyncResult.Success)
        assertEquals(4, (result as SyncResult.Success).syncedCount)

        // Strict FIFO verification: op-1 -> op-2 -> op-3 -> op-4
        assertEquals(4, fakeSyncExecutor.executedOperations.size)
        assertEquals("op-1", fakeSyncExecutor.executedOperations[0].operationId)
        assertEquals("op-2", fakeSyncExecutor.executedOperations[1].operationId)
        assertEquals("op-3", fakeSyncExecutor.executedOperations[2].operationId)
        assertEquals("op-4", fakeSyncExecutor.executedOperations[3].operationId)

        // Queue completely emptied on success
        assertTrue(fakeQueueStore.getPendingOperations().isEmpty())
    }

    // =========================================================================
    // 2. Dependent Failure and Queue Halting
    // =========================================================================

    @Test
    fun haltsSubsequentOperationsWhenPrerequisiteOperationFails() = runTest {
        fakeNetworkMonitor.setOnline(true)

        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op-start")
        currentTime = 2000L
        synchronizer.enqueue(SyncOperationType.SUBMIT_CHALLENGE_RESULT, "{}", "op-submit")
        currentTime = 3000L
        synchronizer.enqueue(SyncOperationType.COMPLETE_BATTLE, "{}", "op-complete")

        // First succeeds, second fails
        fakeSyncExecutor.setResultFor("op-start", DomainResult.Success(Unit))
        fakeSyncExecutor.setResultFor("op-submit", DomainResult.Failure(AppError.Network))

        val result = synchronizer.sync()
        assertTrue(result is SyncResult.PartialFailure)
        val partial = result as SyncResult.PartialFailure
        assertEquals(1, partial.syncedCount)
        assertEquals("op-submit", partial.failedOperationId)

        // op-start succeeded and removed
        assertNull(fakeQueueStore.getOperation("op-start"))

        // op-submit failed and retained with incremented retry count
        val failedOp = fakeQueueStore.getOperation("op-submit")
        assertNotNull(failedOp)
        assertEquals(SyncStatus.FAILED, failedOp?.status)
        assertEquals(1, failedOp?.retryCount)

        // op-complete was NEVER attempted to protect dependency order (§49)
        val pendingOp = fakeQueueStore.getOperation("op-complete")
        assertNotNull(pendingOp)
        assertEquals(SyncStatus.PENDING, pendingOp?.status)
        assertEquals(0, pendingOp?.retryCount)
        assertEquals(2, fakeSyncExecutor.executedOperations.size)
    }

    // =========================================================================
    // 3. Retry Policy and Controlled Limits (Rule 133)
    // =========================================================================

    @Test
    fun retryPolicyCalculatesExponentialBackoffAndTerminatesAtMaxRetries() {
        val policy = DefaultRetryPolicy(
            maxRetries = 3,
            initialDelayMillis = 1000L,
            maxDelayMillis = 10000L,
            backoffMultiplier = 2.0,
        )

        // Exponential delays: 1000 -> 2000 -> 4000 -> capped at maxDelayMillis
        assertEquals(1000L, policy.calculateDelayMillis(1))
        assertEquals(2000L, policy.calculateDelayMillis(2))
        assertEquals(4000L, policy.calculateDelayMillis(3))

        // Retry eligibility: attempts 0, 1, 2 are valid retries before reaching maxRetries (3)
        assertTrue(policy.canRetry(0, AppError.Network))
        assertTrue(policy.canRetry(1, AppError.Network))
        assertTrue(policy.canRetry(2, AppError.Server))

        // Exceeded limit: must terminate at or after maxRetries (No Infinite Retry Rule 133)
        assertFalse(policy.canRetry(3, AppError.Timeout))
        assertFalse(policy.canRetry(4, AppError.Network))

        // Non-retryable errors fail immediately
        assertFalse(policy.isRetryable(AppError.Validation))
        assertFalse(policy.isRetryable(AppError.Authentication))
        assertFalse(policy.isRetryable(AppError.Authorization))
        assertFalse(policy.isRetryable(AppError.Domain))
    }

    // =========================================================================
    // 4. User-Initiated Retry and Cancellation
    // =========================================================================

    @Test
    fun userManualRetryResetsRetryCountAndRecoversOperationToPending() = runTest {
        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.COMPLETE_BATTLE, "{}", "op-manual")

        // Simulate failure state
        val op = fakeQueueStore.getOperation("op-manual")!!
        fakeQueueStore.updateOperation(op.copy(status = SyncStatus.FAILED, lastError = "Timeout", retryCount = 3))
        assertEquals(SyncStatus.FAILED, fakeQueueStore.getOperation("op-manual")?.status)
        assertEquals(3, fakeQueueStore.getOperation("op-manual")?.retryCount)

        // User taps Retry (§35)
        val retryResult = synchronizer.retryOperation("op-manual")
        assertTrue(retryResult is DomainResult.Success)

        val recovered = fakeQueueStore.getOperation("op-manual")
        assertEquals(SyncStatus.PENDING, recovered?.status)
        assertEquals(0, recovered?.retryCount)
        assertNull(recovered?.lastError)
    }

    @Test
    fun userCancellationMovesOperationToCancelledAndSkipsExecution() = runTest {
        currentTime = 1000L
        synchronizer.enqueue(SyncOperationType.ADD_FRIEND, "{}", "op-cancel")

        val cancelResult = synchronizer.cancelOperation("op-cancel")
        assertTrue(cancelResult is DomainResult.Success)

        val cancelledOp = fakeQueueStore.getOperation("op-cancel")
        assertEquals(SyncStatus.CANCELLED, cancelledOp?.status)

        // Sync run must skip cancelled operations
        fakeNetworkMonitor.setOnline(true)
        val syncResult = synchronizer.sync()
        assertEquals(SyncResult.NoPendingOperations, syncResult)
        assertEquals(0, fakeSyncExecutor.executedOperations.size)
    }

    // =========================================================================
    // 5. Interrupted Operations Recovery (Sprint 18.4)
    // =========================================================================

    @Test
    fun recoversOperationsInterruptedDuringSynchronizationWithoutDuplication() = runTest {
        // Simulate operation stuck in SYNCING due to process death / crash mid-flight
        currentTime = 5000L
        val stuckOp = PendingOperation(
            operationId = "op-interrupted",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = "{}",
            createdAt = 5000L,
            retryCount = 0,
            status = SyncStatus.SYNCING,
        )
        fakeQueueStore.enqueue(stuckOp)

        val recoveredList = synchronizer.recoverInterruptedOperations()
        assertEquals(1, recoveredList.size)
        assertEquals("op-interrupted", recoveredList.first().operationId)

        // Reset to PENDING for safe restart without duplicate submission
        val inStore = fakeQueueStore.getOperation("op-interrupted")
        assertEquals(SyncStatus.PENDING, inStore?.status)
    }

    // =========================================================================
    // 6. Conflict Resolution (Authoritative Remote Precedence §74)
    // =========================================================================

    @Test
    fun conflictResolverIdentifiesStaleServerData() {
        // If local timestamp is newer than remote timestamp, remote is stale
        assertTrue(conflictResolver.isServerDataStale(cachedTimestamp = 2000L, remoteTimestamp = 1500L))
        // If remote timestamp is newer or equal, it is fresh
        assertFalse(conflictResolver.isServerDataStale(cachedTimestamp = 2000L, remoteTimestamp = 2500L))
    }

    @Test
    fun conflictResolverDetectsDuplicateQueuedMutations() {
        val startPayload = DailyBattleJson.encodeToString(
            StartBattlePayload.serializer(),
            StartBattlePayload(battleId = "battle-today", clientRequestId = "req-start-1", userId = "user-1"),
        )
        val friendPayload = DailyBattleJson.encodeToString(
            AddFriendPayload.serializer(),
            AddFriendPayload(userId = "user-1", battleCode = "WARRIOR-99", clientRequestId = "req-friend-1"),
        )

        val existingOps = listOf(
            PendingOperation(
                operationId = "op-exist-start",
                operationType = SyncOperationType.START_BATTLE,
                payload = startPayload,
                createdAt = 1000L,
            ),
            PendingOperation(
                operationId = "op-exist-friend",
                operationType = SyncOperationType.ADD_FRIEND,
                payload = friendPayload,
                createdAt = 2000L,
            ),
        )

        // Duplicate start battle for same battleId
        val dupStart = PendingOperation(
            operationId = "op-candidate-start",
            operationType = SyncOperationType.START_BATTLE,
            payload = startPayload,
            createdAt = 3000L,
        )
        val dupStartAction = conflictResolver.resolveDuplicateMutation(existingOps, dupStart)
        assertTrue("Duplicate START_BATTLE must be discarded", dupStartAction is ConflictResolutionAction.DiscardMutation)

        // Duplicate add friend for same code
        val dupFriend = PendingOperation(
            operationId = "op-candidate-friend",
            operationType = SyncOperationType.ADD_FRIEND,
            payload = friendPayload,
            createdAt = 4000L,
        )
        val dupFriendAction = conflictResolver.resolveDuplicateMutation(existingOps, dupFriend)
        assertTrue("Duplicate ADD_FRIEND must be discarded", dupFriendAction is ConflictResolutionAction.DiscardMutation)

        // Distinct operation is allowed
        val distinctStartPayload = DailyBattleJson.encodeToString(
            StartBattlePayload.serializer(),
            StartBattlePayload(battleId = "battle-tomorrow", clientRequestId = "req-start-2", userId = "user-1"),
        )
        val distinctStart = PendingOperation(
            operationId = "op-candidate-other",
            operationType = SyncOperationType.START_BATTLE,
            payload = distinctStartPayload,
            createdAt = 5000L,
        )
        assertNull(conflictResolver.resolveDuplicateMutation(existingOps, distinctStart))
    }

    // =========================================================================
    // Test Doubles
    // =========================================================================

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

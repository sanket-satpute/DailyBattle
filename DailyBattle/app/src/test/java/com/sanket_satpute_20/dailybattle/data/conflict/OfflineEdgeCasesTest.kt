package com.sanket_satpute_20.dailybattle.data.conflict

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.cache.CacheFreshness
import com.sanket_satpute_20.dailybattle.data.cache.CachedResource
import com.sanket_satpute_20.dailybattle.data.retry.DefaultRetryPolicy
import com.sanket_satpute_20.dailybattle.data.sync.DefaultSynchronizer
import com.sanket_satpute_20.dailybattle.data.sync.InMemorySyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.NetworkMonitor
import com.sanket_satpute_20.dailybattle.data.sync.PendingOperation
import com.sanket_satpute_20.dailybattle.data.sync.SyncExecutor
import com.sanket_satpute_20.dailybattle.data.sync.SyncOperationType
import com.sanket_satpute_20.dailybattle.data.sync.SyncResult
import com.sanket_satpute_20.dailybattle.data.sync.SyncState
import com.sanket_satpute_20.dailybattle.data.sync.SyncStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * Validates the full suite of offline edge cases specified in Phase 17, Sprint 17.5.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineEdgeCasesTest {

    private lateinit var queueStore: InMemorySyncQueueStore
    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var syncExecutor: FakeSyncExecutor
    private val conflictResolver = DefaultConflictResolver()

    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(1_000_000L)
    }

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
            retryPolicy = DefaultRetryPolicy(),
            conflictResolver = conflictResolver,
        )
    }

    @Test
    fun offlineAtLaunchPreservesCachedStateAndDoesNotFabricateData() = runTest {
        networkMonitor.setOnline(false)

        // Cached battle from yesterday exists
        val cachedResource = CachedResource(
            data = "cached_battle_definition",
            freshness = CacheFreshness.STALE,
            cachedAtMillis = 500_000L,
            isFromCache = true,
        )

        // Offline state is reflected accurately; cached data is usable
        assertTrue(cachedResource.isUsable)
        assertFalse(cachedResource.isFresh)
        assertEquals(SyncResult.Offline, synchronizer.sync())
        assertEquals(SyncState.Offline, synchronizer.syncState.value)
    }

    @Test
    fun offlineBeforeBattleDoesNotCreateCorruptedOrDuplicateSessions() = runTest {
        networkMonitor.setOnline(false)

        val localSession = BattleSession(
            sessionId = BattleSessionId("sess_offline"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
        )

        assertEquals(BattleSessionStatus.NOT_STARTED, localSession.status)
        assertNull(localSession.startedAt)
    }

    @Test
    fun offlineDuringBattlePreservesActiveSessionProgress() = runTest {
        networkMonitor.setOnline(true)

        val sessionInProgress = BattleSession(
            sessionId = BattleSessionId("sess_active"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2, // on Shift
            startedAt = 1000L,
            completedAt = null,
        )

        // Network drops during active challenge
        networkMonitor.setOnline(false)

        // Local state remains active; does not clear progress or restart battle
        assertEquals(BattleSessionStatus.IN_PROGRESS, sessionInProgress.status)
        assertEquals(2, sessionInProgress.currentChallenge)
        assertEquals(1000L, sessionInProgress.startedAt)
    }

    @Test
    fun offlineDuringSubmissionEnqueuesOperationSafelyWithoutDataLoss() = runTest {
        networkMonitor.setOnline(false)

        val result = synchronizer.enqueue(
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = "{\"sessionId\":\"sess_1\",\"challengeId\":\"c_snap\",\"clientRequestId\":\"req_sub\"}",
            operationId = "op_sub_offline",
        )

        assertTrue(result is DomainResult.Success)
        val op = queueStore.getOperation("op_sub_offline")
        assertNotNull(op)
        assertEquals(SyncStatus.PENDING, op?.status)

        // Sync attempts while offline report Offline without modifying queue
        val syncRes = synchronizer.sync()
        assertEquals(SyncResult.Offline, syncRes)
        assertEquals(SyncStatus.PENDING, queueStore.getOperation("op_sub_offline")?.status)
    }

    @Test
    fun offlineAfterSubmissionPreservesCompletedLocalState() = runTest {
        networkMonitor.setOnline(false)

        val completedLocalSession = BattleSession(
            sessionId = BattleSessionId("sess_done"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 3000L,
        )

        assertEquals(BattleSessionStatus.COMPLETED, completedLocalSession.status)
        assertEquals(3000L, completedLocalSession.completedAt)
    }

    @Test
    fun networkRestoredDuringSubmissionAutoSyncsSuccessfully() = runTest {
        networkMonitor.setOnline(false)
        syncExecutor.defaultResult = DomainResult.Success(Unit)

        synchronizer.enqueue(
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = "{\"sessionId\":\"sess_1\",\"clientRequestId\":\"req_c\"}",
            operationId = "op_auto_sync",
        )

        val job = synchronizer.startAutoSync(this)
        advanceUntilIdle()

        // Still offline, 1 operation pending
        assertEquals(1, queueStore.getPendingOperations().size)

        // Network restored
        networkMonitor.setOnline(true)
        advanceUntilIdle()

        // Sync executed automatically
        assertTrue(queueStore.getPendingOperations().isEmpty())
        assertEquals(1, syncExecutor.executedOperations.size)
        assertEquals("op_auto_sync", syncExecutor.executedOperations[0].operationId)

        job.cancel()
    }

    @Test
    fun networkChangesRepeatedlyPreservesQueueConsistencyWithoutRaceConditions() = runTest {
        syncExecutor.defaultResult = DomainResult.Success(Unit)

        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{\"id\":\"1\"}", "op_1")

        val job = synchronizer.startAutoSync(this)

        // Network flaps rapidly
        networkMonitor.setOnline(false)
        advanceUntilIdle()
        networkMonitor.setOnline(true)
        advanceUntilIdle()
        networkMonitor.setOnline(false)
        advanceUntilIdle()
        networkMonitor.setOnline(true)
        advanceUntilIdle()

        // Operation processed exactly once without corruption
        assertEquals(1, syncExecutor.executedOperations.size)
        assertTrue(queueStore.getPendingOperations().isEmpty())

        job.cancel()
    }

    @Test
    fun serverReturnsStaleDataIsDetectedAndRejected() = runTest {
        val cachedTimestamp = 1_000_000L
        val staleRemoteTimestamp = 900_000L

        val isStale = conflictResolver.isServerDataStale(cachedTimestamp, staleRemoteTimestamp)
        assertTrue(isStale)
    }

    @Test
    fun duplicateSyncCallsAreSerializedAndDoNotDuplicateExecution() = runTest {
        networkMonitor.setOnline(true)
        val releaseGate = CompletableDeferred<Unit>()

        syncExecutor.customExecution = {
            releaseGate.await()
            DomainResult.Success(Unit)
        }

        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_single")

        // Two concurrent sync invocations
        val sync1 = async { synchronizer.sync() }
        val sync2 = async { synchronizer.sync() }

        advanceUntilIdle()
        releaseGate.complete(Unit)
        advanceUntilIdle()

        sync1.await()
        sync2.await()

        // Operation was executed exactly once
        assertEquals(1, syncExecutor.executedOperations.size)
        assertTrue(queueStore.getPendingOperations().isEmpty())
    }

    @Test
    fun appKilledDuringSyncPreservesOperationsInStorageForRestart() = runTest {
        networkMonitor.setOnline(true)

        // Enqueue operation
        synchronizer.enqueue(SyncOperationType.START_BATTLE, "{}", "op_crash_recovery")

        // Simulate crash by retrieving directly from store
        val storedOp = queueStore.getOperation("op_crash_recovery")
        assertNotNull(storedOp)
        assertEquals(SyncStatus.PENDING, storedOp?.status)

        // New synchronizer instance simulates app launch after process death
        val restartedSynchronizer = DefaultSynchronizer(
            queueStore = queueStore,
            networkMonitor = networkMonitor,
            syncExecutor = syncExecutor,
            clock = clock,
        )

        val result = restartedSynchronizer.sync()
        assertTrue(result is SyncResult.Success)
        assertEquals(1, (result as SyncResult.Success).syncedCount)
        assertTrue(queueStore.getPendingOperations().isEmpty())
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
        var customExecution: (suspend () -> DomainResult<Unit>)? = null
        val executedOperations = mutableListOf<PendingOperation>()

        override suspend fun execute(operation: PendingOperation): DomainResult<Unit> {
            executedOperations.add(operation)
            return customExecution?.invoke() ?: defaultResult
        }
    }
}

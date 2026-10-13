package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.conflict.ConflictResolutionAction
import com.sanket_satpute_20.dailybattle.data.conflict.DefaultConflictResolver
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.data.sync.CompleteBattlePayload
import com.sanket_satpute_20.dailybattle.data.sync.DefaultSynchronizer
import com.sanket_satpute_20.dailybattle.data.sync.InMemorySyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.NetworkMonitor
import com.sanket_satpute_20.dailybattle.data.sync.PendingOperation
import com.sanket_satpute_20.dailybattle.data.sync.SubmitChallengeResultPayload
import com.sanket_satpute_20.dailybattle.data.sync.SyncExecutor
import com.sanket_satpute_20.dailybattle.data.sync.SyncOperationType
import com.sanket_satpute_20.dailybattle.data.sync.SyncResult
import com.sanket_satpute_20.dailybattle.data.sync.SyncStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

/**
 * Verification test suite for Sprint 18.4 (Interrupted Submission):
 * Submit -> Process killed -> Unknown submission status -> without duplicating the mutation.
 *
 * Conforms to:
 * - 08_DATA_AND_API.md §57 (Request Idempotency)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §21 (Challenge Submission Failure)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §22 (Duplicate Challenge Submission Prevention)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §23 (Challenge Result Unknown State)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §24 (Battle Completion Failure & Reconciliation)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicate requests or attempts)
 * - 15_ANTIGRAVITY_RULES.md Rule 46 (Idempotency Rule)
 */
class InterruptedSubmissionRecoveryTest {

    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var fakeResultRepository: FakeResultRepository
    private lateinit var completeBattleUseCase: CompleteBattleUseCase
    private lateinit var fakeQueueStore: InMemorySyncQueueStore
    private lateinit var fakeNetworkMonitor: FakeNetworkMonitor
    private lateinit var fakeSyncExecutor: FakeSyncExecutor
    private lateinit var synchronizer: DefaultSynchronizer
    private lateinit var conflictResolver: DefaultConflictResolver
    private lateinit var recoverer: DefaultInterruptedSubmissionRecoverer

    private val testUserId = UserId("user_sprint18_4")
    private val testBattleId = BattleId("battle_sprint18_4")
    private val testSessionId = BattleSessionId("session_sprint18_4")

    private val fixedClock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(2_000_000L)
    }

    @Before
    fun setup() {
        fakeSessionRepository = FakeSessionRepository()
        fakeResultRepository = FakeResultRepository()
        completeBattleUseCase = CompleteBattleUseCase(fakeResultRepository, fakeSessionRepository)
        fakeQueueStore = InMemorySyncQueueStore()
        fakeNetworkMonitor = FakeNetworkMonitor()
        fakeSyncExecutor = FakeSyncExecutor()
        conflictResolver = DefaultConflictResolver()

        synchronizer = DefaultSynchronizer(
            queueStore = fakeQueueStore,
            networkMonitor = fakeNetworkMonitor,
            syncExecutor = fakeSyncExecutor,
            clock = fixedClock,
            conflictResolver = conflictResolver,
        )

        recoverer = DefaultInterruptedSubmissionRecoverer(
            sessionRepository = fakeSessionRepository,
            resultRepository = fakeResultRepository,
            completeBattleUseCase = completeBattleUseCase,
            queueStore = fakeQueueStore,
        )
    }

    private fun createUnacknowledgedSession(): BattleSession {
        return BattleSession(
            sessionId = testSessionId,
            userId = testUserId,
            battleId = testBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("snap_1"), testSessionId, ChallengeSessionStatus.COMPLETE, 1000L, 2000L),
                ChallengeSession(ChallengeId("shift_1"), testSessionId, ChallengeSessionStatus.COMPLETE, 2000L, 3000L),
                ChallengeSession(ChallengeId("crowd_1"), testSessionId, ChallengeSessionStatus.COMPLETE, 3000L, 4000L),
            ),
        )
    }

    @Test
    fun `interrupted battle completion recovers existing result without creating duplicate result`() = runTest {
        // Given: The user completed Crowd Call, submitted battle, but process died.
        // The server already processed the result before process died, but client status was unknown.
        val session = createUnacknowledgedSession()
        fakeSessionRepository.saveSession(session)

        val existingResult = BattleResult(
            resultId = BattleResultId("res_already_saved"),
            userId = testUserId,
            battleId = testBattleId,
            sessionId = testSessionId,
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 99.5,
            completedAt = 4500L,
        )
        fakeResultRepository.saveExistingResult(existingResult)

        // When: App re-opens and recovers interrupted submissions
        val result = recoverer.recoverInterruptedSubmissions(testUserId)

        // Then:
        assertTrue(result is DomainResult.Success)
        val report = (result as DomainResult.Success).value
        assertEquals(1, report.recoveredSessions.size)
        assertEquals(testSessionId, report.recoveredSessions.first())
        assertEquals(1, report.reconciledResults.size)
        assertEquals("res_already_saved", report.reconciledResults.first().resultId.value)

        // Verifies no duplicate mutation: completeBattle was NOT called again
        assertEquals(0, fakeResultRepository.completeCallCount)

        // Session in repository is updated to COMPLETED locally
        val updatedSession = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertNotNull(updatedSession)
        assertEquals(BattleSessionStatus.COMPLETED, updatedSession?.status)
    }

    @Test
    fun `interrupted battle completion with lost request retries with deterministic idempotency key`() = runTest {
        // Given: Request was in flight and lost before server received it. No result exists.
        val session = createUnacknowledgedSession()
        fakeSessionRepository.saveSession(session)

        // When: Recovery executes
        val result = recoverer.recoverInterruptedSubmissions(testUserId)

        // Then:
        assertTrue(result is DomainResult.Success)
        val report = (result as DomainResult.Success).value
        assertEquals(1, report.recoveredSessions.size)
        assertEquals(testSessionId, report.recoveredSessions.first())

        // completeBattle was safely called exactly once using deterministic idempotencyKey
        assertEquals(1, fakeResultRepository.completeCallCount)
        assertEquals(testSessionId.value, fakeResultRepository.lastClientRequestId)

        val updatedSession = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)
        assertEquals(BattleSessionStatus.COMPLETED, updatedSession?.status)
    }

    @Test
    fun `repeated completion after recovery returns existing result and does not duplicate`() = runTest {
        val session = createUnacknowledgedSession()
        fakeSessionRepository.saveSession(session)

        // First completion succeeds
        val firstResult = completeBattleUseCase.execute(session)
        assertTrue(firstResult is DomainResult.Success)
        val initialCallCount = fakeResultRepository.completeCallCount

        // Simulated process restart / repeated execution
        val updatedSession = fakeSessionRepository.getOfficialSession(testUserId, testBattleId)!!
        val repeatedResult = completeBattleUseCase.execute(updatedSession)

        assertTrue(repeatedResult is DomainResult.Success)
        assertEquals((firstResult as DomainResult.Success).value.resultId, (repeatedResult as DomainResult.Success).value.resultId)
        // No additional completeBattle call made to remote service
        assertEquals(initialCallCount, fakeResultRepository.completeCallCount)
    }

    @Test
    fun `interrupted queued operations in SYNCING state transition to UNKNOWN and recover to PENDING with preserved clientRequestId`() = runTest {
        // Given: A queued challenge result submission was in SYNCING state when process was killed
        val challengePayload = SubmitChallengeResultPayload(
            sessionId = testSessionId.value,
            challengeId = "snap_1",
            clientRequestId = "req_deterministic_123",
            score = 300,
            maxScore = 300,
            completedAt = 1500L,
        )
        val interruptedOp = PendingOperation(
            operationId = "op_interrupted_1",
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = DailyBattleJson.encodeToString(challengePayload),
            createdAt = 1000L,
            retryCount = 0,
            status = SyncStatus.SYNCING, // Was syncing when process died
            lastError = null,
        )
        fakeQueueStore.enqueue(interruptedOp)

        // When: Recovery is triggered for interrupted operations
        val recoveredOps = synchronizer.recoverInterruptedOperations()

        // Then:
        assertEquals(1, recoveredOps.size)
        val recovered = recoveredOps.first()
        assertEquals("op_interrupted_1", recovered.operationId)
        assertEquals(SyncStatus.PENDING, recovered.status)
        assertEquals("RecoveredFromInterruptedSubmission", recovered.lastError)

        // Crucial: Payload and clientRequestId are perfectly preserved (no new random UUID)
        val decoded = DailyBattleJson.decodeFromString<SubmitChallengeResultPayload>(recovered.payload)
        assertEquals("req_deterministic_123", decoded.clientRequestId)
        assertEquals("snap_1", decoded.challengeId)
    }

    @Test
    fun `duplicate candidate mutation is discarded while interrupted operation is in queue`() = runTest {
        // Given: An interrupted operation is currently in the queue with UNKNOWN or PENDING status
        val challengePayload = SubmitChallengeResultPayload(
            sessionId = testSessionId.value,
            challengeId = "snap_1",
            clientRequestId = "req_deterministic_123",
            score = 300,
            maxScore = 300,
            completedAt = 1500L,
        )
        val existingOp = PendingOperation(
            operationId = "op_interrupted_1",
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = DailyBattleJson.encodeToString(challengePayload),
            createdAt = 1000L,
            retryCount = 0,
            status = SyncStatus.UNKNOWN,
        )
        fakeQueueStore.enqueue(existingOp)

        // When: An attempt is made to enqueue a duplicate candidate for the same challenge
        val candidatePayload = SubmitChallengeResultPayload(
            sessionId = testSessionId.value,
            challengeId = "snap_1",
            clientRequestId = "req_deterministic_123",
            score = 300,
            maxScore = 300,
            completedAt = 1600L,
        )
        val candidateOp = PendingOperation(
            operationId = "op_duplicate_candidate",
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = DailyBattleJson.encodeToString(candidatePayload),
            createdAt = 1100L,
            status = SyncStatus.PENDING,
        )

        val conflict = conflictResolver.resolveDuplicateMutation(listOf(existingOp), candidateOp)

        // Then: Rule 42 prevents duplicate mutation
        assertNotNull(conflict)
        assertTrue(conflict is ConflictResolutionAction.DiscardMutation)
        val discard = conflict as ConflictResolutionAction.DiscardMutation
        assertEquals("op_duplicate_candidate", discard.operationId)
        assertTrue(discard.reason.contains("Rule 42"))
    }

    @Test
    fun `sync automatically recovers orphaned SYNCING operations on cold start`() = runTest {
        // Given: Operation was left in SYNCING state when process was killed
        val completePayload = CompleteBattlePayload(
            sessionId = testSessionId.value,
            clientRequestId = "client_req_complete_999",
        )
        val orphanedOp = PendingOperation(
            operationId = "op_complete_orphaned",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = DailyBattleJson.encodeToString(completePayload),
            createdAt = 1000L,
            status = SyncStatus.SYNCING,
        )
        fakeQueueStore.enqueue(orphanedOp)
        fakeNetworkMonitor.setOnline(true)

        // When: sync() runs
        val syncResult = synchronizer.sync()

        // Then:
        assertTrue(syncResult is SyncResult.Success)
        assertEquals(1, (syncResult as SyncResult.Success).syncedCount)
        assertEquals(1, fakeSyncExecutor.executedOperations.size)
        assertEquals("op_complete_orphaned", fakeSyncExecutor.executedOperations.first().operationId)

        // The operation was executed with the preserved clientRequestId
        val executedPayload = DailyBattleJson.decodeFromString<CompleteBattlePayload>(
            fakeSyncExecutor.executedOperations.first().payload
        )
        assertEquals("client_req_complete_999", executedPayload.clientRequestId)
    }
}

// Test Doubles

private class FakeSessionRepository : BattleSessionRepository {
    private val sessions = mutableMapOf<Pair<String, String>, BattleSession>()

    override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
        return sessions[Pair(userId.value, battleId.value)]
    }

    override fun saveSession(session: BattleSession) {
        sessions[Pair(session.userId.value, session.battleId.value)] = session
    }

    override fun getUnacknowledgedSessions(): List<BattleSession> {
        return sessions.values.filter {
            it.mode == BattleMode.OFFICIAL &&
            it.status == BattleSessionStatus.IN_PROGRESS &&
            it.challengeSessions.size == 3 &&
            it.challengeSessions.all { c -> c.status == ChallengeSessionStatus.COMPLETE }
        }
    }

    override fun getActiveOfficialSession(userId: UserId): BattleSession? {
        return sessions.values.firstOrNull { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }
    }

    override fun getSessionById(sessionId: BattleSessionId): BattleSession? {
        return sessions.values.firstOrNull { it.sessionId == sessionId }
    }
}

private class FakeResultRepository : BattleResultRepository {
    var completeCallCount = 0
    var lastClientRequestId: String? = null
    private val results = mutableMapOf<Pair<String, String>, BattleResult>()
    private val sessionResults = mutableMapOf<String, BattleResult>()

    fun saveExistingResult(result: BattleResult) {
        results[Pair(result.userId.value, result.battleId.value)] = result
        sessionResults[result.sessionId.value] = result
    }

    override suspend fun completeBattle(
        sessionId: BattleSessionId,
        clientRequestId: String
    ): DomainResult<BattleResult> {
        completeCallCount++
        lastClientRequestId = clientRequestId

        val existing = sessionResults[sessionId.value]
        if (existing != null) {
            return DomainResult.Success(existing)
        }

        val newResult = BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = UserId("user_sprint18_4"),
            battleId = BattleId("battle_sprint18_4"),
            sessionId = sessionId,
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            percentile = 99.9,
            completedAt = 5000L,
        )
        saveExistingResult(newResult)
        return DomainResult.Success(newResult)
    }

    override suspend fun getBattleResult(
        userId: UserId,
        battleId: BattleId
    ): DomainResult<BattleResult> {
        val result = results[Pair(userId.value, battleId.value)]
        return if (result != null) DomainResult.Success(result) else DomainResult.Failure(AppError.Domain)
    }

    override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
        return DomainResult.Success(results.values.filter { it.userId == userId })
    }

    override suspend fun getBattleResultBySessionId(sessionId: BattleSessionId): DomainResult<BattleResult> {
        val result = sessionResults[sessionId.value]
        return if (result != null) DomainResult.Success(result) else DomainResult.Failure(AppError.Domain)
    }
}

private class FakeNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    override val isCurrentlyOnline: Boolean get() = _isOnline.value

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}

private class FakeSyncExecutor : SyncExecutor {
    val executedOperations = mutableListOf<PendingOperation>()

    override suspend fun execute(operation: PendingOperation): DomainResult<Unit> {
        executedOperations.add(operation)
        return DomainResult.Success(Unit)
    }
}

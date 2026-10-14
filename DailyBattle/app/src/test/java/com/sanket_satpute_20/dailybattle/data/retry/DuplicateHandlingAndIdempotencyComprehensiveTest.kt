package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.sync.InMemorySyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.PendingOperation
import com.sanket_satpute_20.dailybattle.data.sync.SyncOperationType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Comprehensive verification of Duplicate Handling and Idempotency boundaries (Sprint 23.2).
 *
 * Verifies:
 * - Rule 42: No duplicate requests, sessions, submissions, or friend actions.
 * - Idempotency in BattleResultRepository (caching by clientRequestId, database session checks, user/battle checks).
 * - In-flight concurrent request deduplication via IdempotentRetryHandler.
 * - Duplicate friend request rejection in FriendRepository.
 * - ConflictResolver duplicate mutation detection and queue idempotency.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DuplicateHandlingAndIdempotencyComprehensiveTest {

    // =========================================================================
    // 1. BattleResult Repository Idempotency
    // =========================================================================

    @Test
    fun completeBattleReusesResultForIdenticalClientRequestIdWithoutDuplicateWrites() = runTest {
        val resultDao = FakeBattleResultDao()
        val historyDao = FakeBattleHistoryDao()
        val sessionDao = FakeBattleSessionDao()
        val repo = RoomBattleResultRepository(resultDao, historyDao, sessionDao)

        sessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "sess-idemp-1",
                userId = "u-idemp",
                battleId = "b-idemp",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        // First completion call
        val firstResult = repo.completeBattle(BattleSessionId("sess-idemp-1"), "req-idempotent-xyz")
        assertTrue(firstResult is DomainResult.Success)
        val res1 = (firstResult as DomainResult.Success).value

        // Immediate second call with identical clientRequestId (rapid double-tap simulation)
        val secondResult = repo.completeBattle(BattleSessionId("sess-idemp-1"), "req-idempotent-xyz")
        assertTrue(secondResult is DomainResult.Success)
        val res2 = (secondResult as DomainResult.Success).value

        // Must return identical result ID and score
        assertEquals(res1.resultId, res2.resultId)
        assertEquals(res1.totalScore, res2.totalScore)

        // Exactly one database row was created (no duplicate row)
        assertEquals(1, resultDao.results.size)
        assertEquals(1, historyDao.historyItems.size)
    }

    @Test
    fun completeBattleReturnsExistingDatabaseResultIfSessionAlreadyCompleted() = runTest {
        val resultDao = FakeBattleResultDao()
        val historyDao = FakeBattleHistoryDao()
        val sessionDao = FakeBattleSessionDao()

        // Pre-populate database with an already completed session result
        resultDao.insertResult(
            BattleResultEntity(
                resultId = "existing-result-99",
                userId = "u-1",
                battleId = "b-1",
                sessionId = "sess-pre-existing",
                snapScore = 300,
                shiftScore = 300,
                crowdCallScore = 300,
                consistencyScore = 100,
                totalScore = 1000,
                percentile = 99.0,
                completedAt = 1000L,
            )
        )

        val repo = RoomBattleResultRepository(resultDao, historyDao, sessionDao)

        // Attempting to complete using a NEW clientRequestId
        val call = repo.completeBattle(BattleSessionId("sess-pre-existing"), "req-new-id")
        assertTrue(call is DomainResult.Success)
        val result = (call as DomainResult.Success).value

        assertEquals("existing-result-99", result.resultId.value)
        assertEquals(1000, result.totalScore)
        // No second row inserted
        assertEquals(1, resultDao.results.size)
    }

    // =========================================================================
    // 2. In-Flight Concurrent Request Deduplication
    // =========================================================================

    @Test
    fun idempotentRetryHandlerExecutesConcurrentBlockExactlyOnce() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val handler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val executionCounter = AtomicInteger(0)
        val releaseGate = CompletableDeferred<Unit>()

        // 5 concurrent coroutines calling with identical key
        val deferredList = (1..5).map {
            async {
                handler.executeIdempotent("shared-idempotency-key") {
                    executionCounter.incrementAndGet()
                    releaseGate.await()
                    DomainResult.Success("unified-result")
                }
            }
        }

        advanceUntilIdle()

        // Verify the block was entered only once
        assertEquals(1, executionCounter.get())
        assertTrue(handler.isInFlight("shared-idempotency-key"))

        // Release gate
        releaseGate.complete(Unit)
        advanceUntilIdle()

        // All 5 callers received identical Success value
        for (deferred in deferredList) {
            val result = deferred.await()
            assertTrue(result is DomainResult.Success)
            assertEquals("unified-result", (result as DomainResult.Success).value)
        }

        assertFalse(handler.isInFlight("shared-idempotency-key"))
        assertEquals(1, executionCounter.get())
    }

    @Test
    fun idempotentRetryHandlerCleansUpOnFailureAllowingSubsequentAttempts() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val handler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        // Call 1 fails
        val res1 = handler.executeIdempotent("failing-key") {
            DomainResult.Failure(AppError.Network)
        }
        assertTrue(res1 is DomainResult.Failure)
        assertFalse(handler.isInFlight("failing-key"))

        // Subsequent call 2 can execute cleanly
        val res2 = handler.executeIdempotent("failing-key") {
            DomainResult.Success("recovered")
        }
        assertTrue(res2 is DomainResult.Success)
        assertEquals("recovered", (res2 as DomainResult.Success).value)
    }

    // =========================================================================
    // 3. Friend Repository Duplicate Request Rejection
    // =========================================================================

    @Test
    fun friendRepositoryRejectsDuplicateFriendRequests() = runTest {
        val friendDao = FakeFriendDao()
        val repo = RoomFriendRepository(friendDao)

        val u1 = UserId("user-me")
        val u2 = UserId("user-friend")

        val req1 = repo.sendFriendRequest(u1, u2)
        assertTrue("Initial request succeeds", req1 is DomainResult.Success)

        // Rapid duplicate request
        val req2 = repo.sendFriendRequest(u1, u2)
        assertTrue("Duplicate request must fail", req2 is DomainResult.Failure)
        assertEquals(AppError.Domain, (req2 as DomainResult.Failure).error)

        // Exactly 1 friend record exists
        assertEquals(1, friendDao.friends.size)
    }

    // =========================================================================
    // 4. Sync Queue Store Deduplication
    // =========================================================================

    @Test
    fun syncQueueStoreUpdatesExistingOperationWithoutDuplicatingRows() = runTest {
        val store = InMemorySyncQueueStore()

        val op1 = PendingOperation(
            operationId = "op-idemp-1",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = """{"score":1000}""",
            createdAt = 1000L,
        )
        store.enqueue(op1)
        assertEquals(1, store.getPendingOperations().size)

        // Save updated state with same operation ID
        val opUpdated = op1.copy(retryCount = 2, status = com.sanket_satpute_20.dailybattle.data.sync.SyncStatus.FAILED)
        store.updateOperation(opUpdated)

        assertEquals(1, store.getPendingOperations().size)
        assertEquals(2, store.getOperation("op-idemp-1")?.retryCount)
    }

    // =========================================================================
    // Test Doubles
    // =========================================================================

    private class FakeBattleResultDao : BattleResultDao {
        val results = mutableMapOf<String, BattleResultEntity>()

        override suspend fun insertResult(result: BattleResultEntity) {
            results[result.resultId] = result
        }

        override suspend fun getResultById(resultId: String): BattleResultEntity? = results[resultId]

        override suspend fun getResultBySessionId(sessionId: String): BattleResultEntity? =
            results.values.find { it.sessionId == sessionId }

        override suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity? =
            results.values.find { it.userId == userId && it.battleId == battleId }

        override suspend fun getAllResultsForUser(userId: String): List<BattleResultEntity> =
            results.values.filter { it.userId == userId }

        override fun observeAllResultsForUser(userId: String): Flow<List<BattleResultEntity>> =
            flowOf(results.values.filter { it.userId == userId })

        override suspend fun deleteResultById(resultId: String): Int =
            if (results.remove(resultId) != null) 1 else 0
    }

    private class FakeBattleHistoryDao : BattleHistoryDao {
        val historyItems = mutableListOf<BattleHistoryItemEntity>()

        override suspend fun insertHistoryItems(items: List<BattleHistoryItemEntity>) {
            historyItems.addAll(items)
        }

        override suspend fun getHistoryForUser(userId: String): List<BattleHistoryItemEntity> =
            historyItems.filter { it.userId == userId }

        override suspend fun clearHistoryForUser(userId: String): Int {
            val count = historyItems.count { it.userId == userId }
            historyItems.removeAll { it.userId == userId }
            return count
        }
    }

    private class FakeBattleSessionDao : BattleSessionDao {
        val sessions = mutableMapOf<String, BattleSessionEntity>()

        override suspend fun insertSession(session: BattleSessionEntity) {
            sessions[session.sessionId] = session
        }

        override suspend fun getSessionById(sessionId: String): BattleSessionEntity? = sessions[sessionId]

        override suspend fun getSession(userId: String, battleId: String, mode: String): BattleSessionEntity? =
            sessions.values.find { it.userId == userId && it.battleId == battleId && it.mode == mode }

        override suspend fun getActiveSession(userId: String, mode: String): BattleSessionEntity? =
            sessions.values.find { it.userId == userId && it.mode == mode && it.status == "IN_PROGRESS" }

        override suspend fun getSessionsByModeAndStatus(mode: String, status: String): List<BattleSessionEntity> =
            sessions.values.filter { it.mode == mode && it.status == status }

        override suspend fun deleteSessionById(sessionId: String): Int =
            if (sessions.remove(sessionId) != null) 1 else 0
    }

    private class FakeFriendDao : FriendDao {
        val friends = mutableMapOf<String, FriendEntity>()

        override suspend fun insertFriends(friends: List<FriendEntity>) {
            friends.forEach { insertFriend(it) }
        }

        override suspend fun insertFriend(friend: FriendEntity) {
            friends["${friend.userId}_${friend.friendUserId}"] = friend
        }

        override suspend fun getFriendsForUser(userId: String): List<FriendEntity> =
            friends.values.filter { it.userId == userId }

        override fun observeFriendsForUser(userId: String): Flow<List<FriendEntity>> =
            flowOf(friends.values.filter { it.userId == userId })

        override suspend fun getFriend(userId: String, friendUserId: String): FriendEntity? =
            friends["${userId}_$friendUserId"]

        override suspend fun getFriendById(friendshipId: String): FriendEntity? =
            friends.values.find { it.friendshipId == friendshipId }

        override suspend fun deleteFriendById(friendshipId: String): Int {
            val key = friends.entries.find { it.value.friendshipId == friendshipId }?.key
            return if (key != null && friends.remove(key) != null) 1 else 0
        }

        override suspend fun clearFriendsForUser(userId: String): Int {
            val count = friends.count { it.value.userId == userId }
            friends.entries.removeIf { it.value.userId == userId }
            return count
        }
    }
}

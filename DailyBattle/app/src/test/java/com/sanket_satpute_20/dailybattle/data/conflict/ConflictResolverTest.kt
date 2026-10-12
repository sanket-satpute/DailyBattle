package com.sanket_satpute_20.dailybattle.data.conflict

import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.data.sync.AddFriendPayload
import com.sanket_satpute_20.dailybattle.data.sync.CompleteBattlePayload
import com.sanket_satpute_20.dailybattle.data.sync.PendingOperation
import com.sanket_satpute_20.dailybattle.data.sync.StartBattlePayload
import com.sanket_satpute_20.dailybattle.data.sync.SubmitChallengeResultPayload
import com.sanket_satpute_20.dailybattle.data.sync.SyncOperationType
import com.sanket_satpute_20.dailybattle.data.sync.SyncStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConflictResolverTest {

    private val resolver = DefaultConflictResolver()

    @Test
    fun isServerDataStaleDetectsStaleRemoteTimestamp() {
        assertTrue(resolver.isServerDataStale(cachedTimestamp = 10_000L, remoteTimestamp = 9_000L))
        assertFalse(resolver.isServerDataStale(cachedTimestamp = 10_000L, remoteTimestamp = 11_000L))
        assertFalse(resolver.isServerDataStale(cachedTimestamp = 10_000L, remoteTimestamp = 10_000L))
    }

    @Test
    fun resolveSessionConflictWhenServerCompletedReconcilesToCompleted() {
        val localSession = BattleSession(
            sessionId = BattleSessionId("sess_1"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
        )

        val action = resolver.resolveSessionConflict(
            localSession = localSession,
            remoteSessionStatus = BattleSessionStatus.COMPLETED,
            remoteCompletedAt = 5000L,
        )

        assertTrue(action is ConflictResolutionAction.ReconcileSession)
        val reconciled = (action as ConflictResolutionAction.ReconcileSession).session
        assertEquals(BattleSessionStatus.COMPLETED, reconciled.status)
        assertEquals(5000L, reconciled.completedAt)
        assertNull(reconciled.currentChallenge)
    }

    @Test
    fun resolveSessionConflictWhenLocalCompletedAndRemoteInProgressRetainsLocal() {
        val localSession = BattleSession(
            sessionId = BattleSessionId("sess_1"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 4000L,
        )

        val action = resolver.resolveSessionConflict(
            localSession = localSession,
            remoteSessionStatus = BattleSessionStatus.IN_PROGRESS,
            remoteCompletedAt = null,
        )

        assertTrue(action is ConflictResolutionAction.RetainLocal)
        val retained = (action as ConflictResolutionAction.RetainLocal).localData
        assertEquals(BattleSessionStatus.COMPLETED, retained.status)
    }

    @Test
    fun resolveDuplicateMutationDetectsDirectOperationIdCollision() {
        val existing = listOf(
            PendingOperation(
                operationId = "op_100",
                operationType = SyncOperationType.START_BATTLE,
                payload = "{}",
                createdAt = 1000L,
            )
        )
        val candidate = PendingOperation(
            operationId = "op_100",
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            createdAt = 2000L,
        )

        val action = resolver.resolveDuplicateMutation(existing, candidate)
        assertTrue(action is ConflictResolutionAction.DiscardMutation)
        assertEquals("op_100", (action as ConflictResolutionAction.DiscardMutation).operationId)
    }

    @Test
    fun resolveDuplicateMutationDetectsDuplicateStartBattle() {
        val payload1 = DailyBattleJson.encodeToString(
            StartBattlePayload(battleId = "b_today", clientRequestId = "req_1", userId = "u_1")
        )
        val payload2 = DailyBattleJson.encodeToString(
            StartBattlePayload(battleId = "b_today", clientRequestId = "req_2", userId = "u_1")
        )

        val existing = listOf(
            PendingOperation(
                operationId = "op_start_1",
                operationType = SyncOperationType.START_BATTLE,
                payload = payload1,
                createdAt = 1000L,
            )
        )
        val candidate = PendingOperation(
            operationId = "op_start_2",
            operationType = SyncOperationType.START_BATTLE,
            payload = payload2,
            createdAt = 2000L,
        )

        val action = resolver.resolveDuplicateMutation(existing, candidate)
        assertTrue(action is ConflictResolutionAction.DiscardMutation)
    }

    @Test
    fun resolveDuplicateMutationDetectsDuplicateChallengeSubmission() {
        val payload1 = DailyBattleJson.encodeToString(
            SubmitChallengeResultPayload(
                sessionId = "s_1",
                challengeId = "c_snap",
                clientRequestId = "req_a",
                score = 800,
                maxScore = 1000,
                completedAt = 1000L,
            )
        )
        val payload2 = DailyBattleJson.encodeToString(
            SubmitChallengeResultPayload(
                sessionId = "s_1",
                challengeId = "c_snap",
                clientRequestId = "req_b",
                score = 850,
                maxScore = 1000,
                completedAt = 1500L,
            )
        )

        val existing = listOf(
            PendingOperation(
                operationId = "op_sub_1",
                operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
                payload = payload1,
                createdAt = 1000L,
            )
        )
        val candidate = PendingOperation(
            operationId = "op_sub_2",
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = payload2,
            createdAt = 2000L,
        )

        val action = resolver.resolveDuplicateMutation(existing, candidate)
        assertTrue(action is ConflictResolutionAction.DiscardMutation)
    }

    @Test
    fun resolveDuplicateMutationDetectsDuplicateCompleteBattle() {
        val payload = DailyBattleJson.encodeToString(
            CompleteBattlePayload(sessionId = "sess_active", clientRequestId = "req_c")
        )

        val existing = listOf(
            PendingOperation(
                operationId = "op_comp_1",
                operationType = SyncOperationType.COMPLETE_BATTLE,
                payload = payload,
                createdAt = 1000L,
            )
        )
        val candidate = PendingOperation(
            operationId = "op_comp_2",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = payload,
            createdAt = 2000L,
        )

        val action = resolver.resolveDuplicateMutation(existing, candidate)
        assertTrue(action is ConflictResolutionAction.DiscardMutation)
    }

    @Test
    fun resolveDuplicateMutationDetectsDuplicateAddFriend() {
        val payload1 = DailyBattleJson.encodeToString(
            AddFriendPayload(userId = "user_me", battleCode = "CODE123", clientRequestId = "req_f1")
        )
        val payload2 = DailyBattleJson.encodeToString(
            AddFriendPayload(userId = "user_me", battleCode = "CODE123", clientRequestId = "req_f2")
        )

        val existing = listOf(
            PendingOperation(
                operationId = "op_friend_1",
                operationType = SyncOperationType.ADD_FRIEND,
                payload = payload1,
                createdAt = 1000L,
            )
        )
        val candidate = PendingOperation(
            operationId = "op_friend_2",
            operationType = SyncOperationType.ADD_FRIEND,
            payload = payload2,
            createdAt = 2000L,
        )

        val action = resolver.resolveDuplicateMutation(existing, candidate)
        assertTrue(action is ConflictResolutionAction.DiscardMutation)
    }

    @Test
    fun resolveDuplicateMutationAllowsDistinctMutations() {
        val payload1 = DailyBattleJson.encodeToString(
            StartBattlePayload(battleId = "b_day_1", clientRequestId = "req_1", userId = "u_1")
        )
        val payload2 = DailyBattleJson.encodeToString(
            StartBattlePayload(battleId = "b_day_2", clientRequestId = "req_2", userId = "u_1")
        )

        val existing = listOf(
            PendingOperation(
                operationId = "op_1",
                operationType = SyncOperationType.START_BATTLE,
                payload = payload1,
                createdAt = 1000L,
            )
        )
        val candidate = PendingOperation(
            operationId = "op_2",
            operationType = SyncOperationType.START_BATTLE,
            payload = payload2,
            createdAt = 2000L,
        )

        val action = resolver.resolveDuplicateMutation(existing, candidate)
        assertNull(action)
    }
}

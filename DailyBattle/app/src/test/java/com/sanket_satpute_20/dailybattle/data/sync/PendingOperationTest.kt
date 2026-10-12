package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingOperationTest {

    @Test
    fun pendingOperationStatusFlagsReflectStatusCorrectly() {
        val pendingOp = PendingOperation(
            operationId = "op_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = "{}",
            createdAt = 1000L,
            status = SyncStatus.PENDING,
        )
        assertTrue(pendingOp.isPending)
        assertFalse(pendingOp.isSyncing)
        assertFalse(pendingOp.isSuccess)
        assertFalse(pendingOp.isFailed)
        assertFalse(pendingOp.isCancelled)

        val syncingOp = pendingOp.copy(status = SyncStatus.SYNCING)
        assertTrue(syncingOp.isSyncing)
        assertFalse(syncingOp.isPending)

        val successOp = pendingOp.copy(status = SyncStatus.SUCCESS)
        assertTrue(successOp.isSuccess)

        val failedOp = pendingOp.copy(status = SyncStatus.FAILED, lastError = "Network error")
        assertTrue(failedOp.isFailed)
        assertEquals("Network error", failedOp.lastError)

        val cancelledOp = pendingOp.copy(status = SyncStatus.CANCELLED)
        assertTrue(cancelledOp.isCancelled)
    }

    @Test
    fun pendingOperationSerializesAndDeserializesCorrectly() {
        val payload = StartBattlePayload(
            battleId = "battle_20261012",
            clientRequestId = "req_123",
            userId = "user_456",
        )
        val payloadJson = DailyBattleJson.encodeToString(payload)

        val op = PendingOperation(
            operationId = "op_start_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = payloadJson,
            createdAt = 123456789L,
            retryCount = 2,
            status = SyncStatus.PENDING,
            lastError = null,
        )

        val serialized = DailyBattleJson.encodeToString(op)
        val deserialized = DailyBattleJson.decodeFromString<PendingOperation>(serialized)

        assertEquals(op.operationId, deserialized.operationId)
        assertEquals(op.operationType, deserialized.operationType)
        assertEquals(op.payload, deserialized.payload)
        assertEquals(op.createdAt, deserialized.createdAt)
        assertEquals(op.retryCount, deserialized.retryCount)
        assertEquals(op.status, deserialized.status)

        val decodedPayload = DailyBattleJson.decodeFromString<StartBattlePayload>(deserialized.payload)
        assertEquals("battle_20261012", decodedPayload.battleId)
        assertEquals("req_123", decodedPayload.clientRequestId)
        assertEquals("user_456", decodedPayload.userId)
    }

    @Test
    fun allPayloadTypesSerializeAndDeserializeCorrectly() {
        val submitPayload = SubmitChallengeResultPayload(
            sessionId = "sess_1",
            challengeId = "ch_snap",
            clientRequestId = "req_submit",
            score = 300,
            maxScore = 300,
            completedAt = 5000L,
        )
        val submitJson = DailyBattleJson.encodeToString(submitPayload)
        val decodedSubmit = DailyBattleJson.decodeFromString<SubmitChallengeResultPayload>(submitJson)
        assertEquals("sess_1", decodedSubmit.sessionId)
        assertEquals(300, decodedSubmit.score)

        val completePayload = CompleteBattlePayload(
            sessionId = "sess_1",
            clientRequestId = "req_complete",
        )
        val completeJson = DailyBattleJson.encodeToString(completePayload)
        val decodedComplete = DailyBattleJson.decodeFromString<CompleteBattlePayload>(completeJson)
        assertEquals("sess_1", decodedComplete.sessionId)

        val addFriendPayload = AddFriendPayload(
            userId = "user_1",
            battleCode = "CODE123",
            clientRequestId = "req_friend",
        )
        val addFriendJson = DailyBattleJson.encodeToString(addFriendPayload)
        val decodedAddFriend = DailyBattleJson.decodeFromString<AddFriendPayload>(addFriendJson)
        assertEquals("CODE123", decodedAddFriend.battleCode)

        val refreshProfilePayload = RefreshProfilePayload(userId = "user_1")
        val refreshProfileJson = DailyBattleJson.encodeToString(refreshProfilePayload)
        val decodedRefreshProfile = DailyBattleJson.decodeFromString<RefreshProfilePayload>(refreshProfileJson)
        assertEquals("user_1", decodedRefreshProfile.userId)
    }
}

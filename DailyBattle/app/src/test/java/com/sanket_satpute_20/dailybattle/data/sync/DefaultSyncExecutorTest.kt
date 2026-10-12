package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteBattleDataSource
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteProfileDataSource
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteSocialDataSource
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSyncExecutorTest {

    private lateinit var fakeBattleDataSource: FakeRemoteBattleDataSource
    private lateinit var fakeSocialDataSource: FakeRemoteSocialDataSource
    private lateinit var fakeProfileDataSource: FakeRemoteProfileDataSource
    private lateinit var executor: DefaultSyncExecutor

    @Before
    fun setup() {
        fakeBattleDataSource = FakeRemoteBattleDataSource()
        fakeSocialDataSource = FakeRemoteSocialDataSource()
        fakeProfileDataSource = FakeRemoteProfileDataSource()
        executor = DefaultSyncExecutor(
            remoteBattleDataSource = fakeBattleDataSource,
            remoteSocialDataSource = fakeSocialDataSource,
            remoteProfileDataSource = fakeProfileDataSource,
            json = DailyBattleJson,
        )
    }

    @Test
    fun executeStartBattleSuccess() = runTest {
        val payload = StartBattlePayload(
            battleId = "b_1",
            clientRequestId = "req_1",
            userId = "u_1",
        )
        fakeBattleDataSource.startBattleResult = DomainResult.Success(
            BattleSession(
                sessionId = BattleSessionId("sess_1"),
                userId = UserId("u_1"),
                battleId = BattleId("b_1"),
                mode = com.sanket_satpute_20.dailybattle.domain.battle.BattleMode.OFFICIAL,
                status = BattleSessionStatus.IN_PROGRESS,
                currentChallenge = 1,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        val op = PendingOperation(
            operationId = "op_1",
            operationType = SyncOperationType.START_BATTLE,
            payload = DailyBattleJson.encodeToString(payload),
            createdAt = 1000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Success)
        assertEquals(1, fakeBattleDataSource.startBattleCallCount)
        assertEquals("b_1", fakeBattleDataSource.lastStartBattleId)
    }

    @Test
    fun executeSubmitChallengeResultSuccess() = runTest {
        val payload = SubmitChallengeResultPayload(
            sessionId = "sess_1",
            challengeId = "ch_1",
            clientRequestId = "req_submit",
            score = 300,
            maxScore = 300,
            completedAt = 2000L,
        )
        fakeBattleDataSource.submitResult = DomainResult.Success(
            ChallengeResult(
                resultId = com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId("cr_1"),
                challengeId = ChallengeId("ch_1"),
                sessionId = BattleSessionId("sess_1"),
                score = 300,
                maxScore = 300,
                completedAt = 2000L,
            )
        )

        val op = PendingOperation(
            operationId = "op_2",
            operationType = SyncOperationType.SUBMIT_CHALLENGE_RESULT,
            payload = DailyBattleJson.encodeToString(payload),
            createdAt = 2000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Success)
        assertEquals(1, fakeBattleDataSource.submitCallCount)
    }

    @Test
    fun executeCompleteBattleSuccess() = runTest {
        val payload = CompleteBattlePayload(
            sessionId = "sess_1",
            clientRequestId = "req_comp",
        )
        fakeBattleDataSource.completeResult = DomainResult.Success(
            BattleResult(
                resultId = BattleResultId("res_1"),
                userId = UserId("u_1"),
                battleId = BattleId("b_1"),
                sessionId = BattleSessionId("sess_1"),
                snapScore = 300,
                shiftScore = 200,
                crowdCallScore = 300,
                consistencyScore = 800,
                totalScore = 800,
                percentile = 90.0,
                completedAt = 3000L,
            )
        )

        val op = PendingOperation(
            operationId = "op_3",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = DailyBattleJson.encodeToString(payload),
            createdAt = 3000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Success)
        assertEquals(1, fakeBattleDataSource.completeCallCount)
    }

    @Test
    fun executeAddFriendSuccess() = runTest {
        val payload = AddFriendPayload(
            userId = "u_1",
            battleCode = "CODE123",
            clientRequestId = "req_friend",
        )
        fakeSocialDataSource.addFriendResult = DomainResult.Success(
            Friend(
                friendshipId = FriendshipId("fs_1"),
                userId = UserId("u_1"),
                friendUserId = UserId("f_1"),
                friendName = "Bob",
                status = FriendStatus.ACCEPTED,
                todayScore = null,
                createdAt = 1000L,
                updatedAt = 1000L,
            )
        )

        val op = PendingOperation(
            operationId = "op_4",
            operationType = SyncOperationType.ADD_FRIEND,
            payload = DailyBattleJson.encodeToString(payload),
            createdAt = 4000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Success)
        assertEquals(1, fakeSocialDataSource.addFriendCallCount)
    }

    @Test
    fun executeRefreshProfileSuccess() = runTest {
        val payload = RefreshProfilePayload(userId = "u_1")
        fakeProfileDataSource.profileResult = DomainResult.Success(
            Profile(
                userId = UserId("u_1"),
                battleName = "Gladiator",
                momentum = 2,
                bestScore = 900,
                averageScore = 750,
                battleDNA = BattleDNA(speed = 80, memory = 80, people = 80),
                records = PersonalRecords(bestScore = 900, bestMomentum = 5, battlesPlayed = 10),
            )
        )

        val op = PendingOperation(
            operationId = "op_5",
            operationType = SyncOperationType.REFRESH_PROFILE,
            payload = DailyBattleJson.encodeToString(payload),
            createdAt = 5000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Success)
        assertEquals(1, fakeProfileDataSource.getProfileCallCount)
    }

    @Test
    fun executeRemoteFailurePropagatesError() = runTest {
        val payload = CompleteBattlePayload(sessionId = "sess_1", clientRequestId = "req_comp")
        fakeBattleDataSource.completeResult = DomainResult.Failure(AppError.Network)

        val op = PendingOperation(
            operationId = "op_fail",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = DailyBattleJson.encodeToString(payload),
            createdAt = 1000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun executeCorruptedPayloadReturnsSynchronizationError() = runTest {
        val op = PendingOperation(
            operationId = "op_corrupt",
            operationType = SyncOperationType.COMPLETE_BATTLE,
            payload = "INVALID_JSON_PAYLOAD",
            createdAt = 1000L,
        )

        val result = executor.execute(op)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Synchronization, (result as DomainResult.Failure).error)
    }

    private class FakeRemoteBattleDataSource : RemoteBattleDataSource {
        var startBattleResult: DomainResult<BattleSession> = DomainResult.Failure(AppError.Network)
        var submitResult: DomainResult<ChallengeResult> = DomainResult.Failure(AppError.Network)
        var completeResult: DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)

        var startBattleCallCount = 0
        var submitCallCount = 0
        var completeCallCount = 0
        var lastStartBattleId: String? = null

        override suspend fun getTodayBattle(): DomainResult<Battle> = DomainResult.Failure(AppError.Network)

        override suspend fun startBattle(
            battleId: BattleId,
            clientRequestId: String,
            userId: UserId,
        ): DomainResult<BattleSession> {
            startBattleCallCount++
            lastStartBattleId = battleId.value
            return startBattleResult
        }

        override suspend fun submitChallengeResult(
            sessionId: BattleSessionId,
            challengeId: ChallengeId,
            clientRequestId: String,
            score: Int,
            maxScore: Int,
            completedAt: Long,
        ): DomainResult<ChallengeResult> {
            submitCallCount++
            return submitResult
        }

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String,
        ): DomainResult<BattleResult> {
            completeCallCount++
            return completeResult
        }

        override suspend fun getBattleResult(
            battleId: BattleId,
            userId: UserId,
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)
    }

    private class FakeRemoteSocialDataSource : RemoteSocialDataSource {
        var addFriendResult: DomainResult<Friend> = DomainResult.Failure(AppError.Network)
        var addFriendCallCount = 0

        override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> =
            DomainResult.Failure(AppError.Network)

        override suspend fun addFriendByBattleCode(
            userId: UserId,
            battleCode: String,
            clientRequestId: String,
        ): DomainResult<Friend> {
            addFriendCallCount++
            return addFriendResult
        }

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> =
            DomainResult.Failure(AppError.Network)

        override suspend fun getRivalComparison(
            userId: UserId,
            battleId: BattleId,
        ): DomainResult<RivalScoreComparison> = DomainResult.Failure(AppError.Network)
    }

    private class FakeRemoteProfileDataSource : RemoteProfileDataSource {
        var profileResult: DomainResult<Profile> = DomainResult.Failure(AppError.Network)
        var getProfileCallCount = 0

        override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
            getProfileCallCount++
            return profileResult
        }

        override suspend fun getBattleHistory(userId: UserId): DomainResult<List<BattleResult>> =
            DomainResult.Failure(AppError.Network)

        override suspend fun getBattleHistoryItem(
            userId: UserId,
            battleId: BattleId,
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Network)
    }
}

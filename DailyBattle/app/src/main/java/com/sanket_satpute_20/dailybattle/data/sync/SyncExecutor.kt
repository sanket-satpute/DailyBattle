package com.sanket_satpute_20.dailybattle.data.sync

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteBattleDataSource
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteProfileDataSource
import com.sanket_satpute_20.dailybattle.data.remote.datasource.RemoteSocialDataSource
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dispatches a pending operation to the authoritative remote service.
 */
interface SyncExecutor {
    suspend fun execute(operation: PendingOperation): DomainResult<Unit>
}

@Singleton
class DefaultSyncExecutor @Inject constructor(
    private val remoteBattleDataSource: RemoteBattleDataSource,
    private val remoteSocialDataSource: RemoteSocialDataSource,
    private val remoteProfileDataSource: RemoteProfileDataSource,
    private val json: Json = DailyBattleJson,
) : SyncExecutor {

    override suspend fun execute(operation: PendingOperation): DomainResult<Unit> {
        return try {
            when (operation.operationType) {
                SyncOperationType.START_BATTLE -> {
                    val payload = json.decodeFromString<StartBattlePayload>(operation.payload)
                    when (val res = remoteBattleDataSource.startBattle(
                        battleId = BattleId(payload.battleId),
                        clientRequestId = payload.clientRequestId,
                        userId = UserId(payload.userId),
                    )) {
                        is DomainResult.Success -> DomainResult.Success(Unit)
                        is DomainResult.Failure -> DomainResult.Failure(res.error)
                    }
                }
                SyncOperationType.SUBMIT_CHALLENGE_RESULT -> {
                    val payload = json.decodeFromString<SubmitChallengeResultPayload>(operation.payload)
                    when (val res = remoteBattleDataSource.submitChallengeResult(
                        sessionId = BattleSessionId(payload.sessionId),
                        challengeId = ChallengeId(payload.challengeId),
                        clientRequestId = payload.clientRequestId,
                        score = payload.score,
                        maxScore = payload.maxScore,
                        completedAt = payload.completedAt,
                    )) {
                        is DomainResult.Success -> DomainResult.Success(Unit)
                        is DomainResult.Failure -> DomainResult.Failure(res.error)
                    }
                }
                SyncOperationType.COMPLETE_BATTLE -> {
                    val payload = json.decodeFromString<CompleteBattlePayload>(operation.payload)
                    when (val res = remoteBattleDataSource.completeBattle(
                        sessionId = BattleSessionId(payload.sessionId),
                        clientRequestId = payload.clientRequestId,
                    )) {
                        is DomainResult.Success -> DomainResult.Success(Unit)
                        is DomainResult.Failure -> DomainResult.Failure(res.error)
                    }
                }
                SyncOperationType.ADD_FRIEND -> {
                    val payload = json.decodeFromString<AddFriendPayload>(operation.payload)
                    when (val res = remoteSocialDataSource.addFriendByBattleCode(
                        userId = UserId(payload.userId),
                        battleCode = payload.battleCode,
                        clientRequestId = payload.clientRequestId,
                    )) {
                        is DomainResult.Success -> DomainResult.Success(Unit)
                        is DomainResult.Failure -> DomainResult.Failure(res.error)
                    }
                }
                SyncOperationType.REFRESH_PROFILE -> {
                    val payload = json.decodeFromString<RefreshProfilePayload>(operation.payload)
                    when (val res = remoteProfileDataSource.getProfile(UserId(payload.userId))) {
                        is DomainResult.Success -> DomainResult.Success(Unit)
                        is DomainResult.Failure -> DomainResult.Failure(res.error)
                    }
                }
            }
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Synchronization)
        }
    }
}

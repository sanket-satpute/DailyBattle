package com.sanket_satpute_20.dailybattle.data.remote.datasource

import com.sanket_satpute_20.dailybattle.data.remote.api.BattleApiService
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.CompleteBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.StartBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Contract for remote Battle network operations.
 */
interface RemoteBattleDataSource {
    suspend fun getTodayBattle(): DomainResult<Battle>
    suspend fun startBattle(battleId: BattleId, clientRequestId: String, userId: UserId): DomainResult<BattleSession>
    suspend fun submitChallengeResult(
        sessionId: BattleSessionId,
        challengeId: ChallengeId,
        clientRequestId: String,
        score: Int,
        maxScore: Int,
        completedAt: Long,
    ): DomainResult<ChallengeResult>
    suspend fun completeBattle(sessionId: BattleSessionId, clientRequestId: String): DomainResult<BattleResult>
    suspend fun getBattleResult(battleId: BattleId, userId: UserId): DomainResult<BattleResult>
}

/**
 * Implementation of [RemoteBattleDataSource] backed by [BattleApiService].
 */
class RetrofitBattleDataSource @Inject constructor(
    private val apiService: BattleApiService,
) : RemoteBattleDataSource {

    override suspend fun getTodayBattle(): DomainResult<Battle> =
        SafeApiCall.execute { apiService.getTodayBattle().toDomain() }

    override suspend fun startBattle(
        battleId: BattleId,
        clientRequestId: String,
        userId: UserId,
    ): DomainResult<BattleSession> =
        SafeApiCall.execute {
            apiService.startBattle(
                battleId = battleId.value,
                request = StartBattleRequestDto(clientRequestId = clientRequestId),
            ).toDomain(userId)
        }

    override suspend fun submitChallengeResult(
        sessionId: BattleSessionId,
        challengeId: ChallengeId,
        clientRequestId: String,
        score: Int,
        maxScore: Int,
        completedAt: Long,
    ): DomainResult<ChallengeResult> =
        SafeApiCall.execute {
            apiService.submitChallengeResult(
                sessionId = sessionId.value,
                challengeId = challengeId.value,
                request = SubmitChallengeResultRequestDto(
                    clientRequestId = clientRequestId,
                    score = score,
                    maxScore = maxScore,
                    completedAt = completedAt,
                ),
            ).challengeResult.toDomain()
        }

    override suspend fun completeBattle(
        sessionId: BattleSessionId,
        clientRequestId: String,
    ): DomainResult<BattleResult> =
        SafeApiCall.execute {
            apiService.completeBattle(
                sessionId = sessionId.value,
                request = CompleteBattleRequestDto(clientRequestId = clientRequestId),
            ).toDomain()
        }

    override suspend fun getBattleResult(
        battleId: BattleId,
        userId: UserId,
    ): DomainResult<BattleResult> =
        SafeApiCall.execute {
            apiService.getBattleResult(
                battleId = battleId.value,
                userId = userId.value,
            ).toDomain()
        }
}

package com.sanket_satpute_20.dailybattle.data.remote.api

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleSessionResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.CompleteBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.StartBattleRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.SubmitChallengeResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.TodayBattleResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API definition for Battle operations.
 *
 * Implements approved technical decision DATA-PENDING-002 for Sprint 16.2.
 */
interface BattleApiService {

    /**
     * Retrieves today's official Battle definition (§47).
     */
    @GET("/api/v1/battles/today")
    suspend fun getTodayBattle(): TodayBattleResponseDto

    /**
     * Starts an official Battle session (§48).
     * Mutation request protected with idempotency key [StartBattleRequestDto.clientRequestId] (§57).
     */
    @POST("/api/v1/battles/{battleId}/start")
    suspend fun startBattle(
        @Path("battleId") battleId: String,
        @Body request: StartBattleRequestDto,
    ): BattleSessionResponseDto

    /**
     * Submits a completed challenge outcome within an active session (§49).
     * Mutation request protected with idempotency key [SubmitChallengeResultRequestDto.clientRequestId] (§57).
     */
    @POST("/api/v1/battles/sessions/{sessionId}/challenges/{challengeId}/result")
    suspend fun submitChallengeResult(
        @Path("sessionId") sessionId: String,
        @Path("challengeId") challengeId: String,
        @Body request: SubmitChallengeResultRequestDto,
    ): SubmitChallengeResultResponseDto

    /**
     * Finalizes and completes an official Battle session (§50).
     * Mutation request protected with idempotency key [CompleteBattleRequestDto.clientRequestId] (§57).
     */
    @POST("/api/v1/battles/sessions/{sessionId}/complete")
    suspend fun completeBattle(
        @Path("sessionId") sessionId: String,
        @Body request: CompleteBattleRequestDto,
    ): BattleResultResponseDto

    /**
     * Retrieves the authoritative result for a user's Battle (§51).
     */
    @GET("/api/v1/battles/{battleId}/results/{userId}")
    suspend fun getBattleResult(
        @Path("battleId") battleId: String,
        @Path("userId") userId: String,
    ): BattleResultResponseDto
}

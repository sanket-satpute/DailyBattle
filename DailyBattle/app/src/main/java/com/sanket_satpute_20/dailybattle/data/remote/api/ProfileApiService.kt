package com.sanket_satpute_20.dailybattle.data.remote.api

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.history.HistoryResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API definition for Profile and History operations.
 *
 * Implements approved technical decision for Sprint 16.4.
 */
interface ProfileApiService {

    /**
     * Retrieves the user's server-authoritative profile data (§38, §55).
     */
    @GET("/api/v1/profile/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String,
    ): ProfileResponseDto

    /**
     * Retrieves the user's past official battle history results (§40, §56).
     */
    @GET("/api/v1/history/{userId}")
    suspend fun getBattleHistory(
        @Path("userId") userId: String,
    ): HistoryResponseDto

    /**
     * Retrieves an authoritative historical battle result (§56).
     */
    @GET("/api/v1/history/{userId}/battles/{battleId}")
    suspend fun getBattleHistoryItem(
        @Path("userId") userId: String,
        @Path("battleId") battleId: String,
    ): BattleResultResponseDto
}

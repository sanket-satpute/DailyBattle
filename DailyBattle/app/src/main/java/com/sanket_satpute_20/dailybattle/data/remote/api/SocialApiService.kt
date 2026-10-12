package com.sanket_satpute_20.dailybattle.data.remote.api

import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendsResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API definition for Social operations.
 *
 * Implements approved technical decision for Sprint 16.3.
 */
interface SocialApiService {

    /**
     * Retrieves friends and their available battle scores for a user (§52).
     */
    @GET("/api/v1/friends/{userId}")
    suspend fun getFriends(
        @Path("userId") userId: String,
    ): FriendsResponseDto

    /**
     * Adds a friend by Battle Code (§53).
     * Mutation request protected with idempotency key [AddFriendRequestDto.clientRequestId] (§57).
     */
    @POST("/api/v1/friends/add")
    suspend fun addFriend(
        @Body request: AddFriendRequestDto,
    ): AddFriendResponseDto

    /**
     * Retrieves the current server-selected rival for a user (§54).
     */
    @GET("/api/v1/rivals/{userId}")
    suspend fun getCurrentRival(
        @Path("userId") userId: String,
    ): RivalResponseDto

    /**
     * Retrieves the rival score comparison for a specific battle (§54).
     */
    @GET("/api/v1/rivals/{userId}/comparison/{battleId}")
    suspend fun getRivalComparison(
        @Path("userId") userId: String,
        @Path("battleId") battleId: String,
    ): RivalScoreComparisonResponseDto
}

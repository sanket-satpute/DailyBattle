package com.sanket_satpute_20.dailybattle.data.remote.dto.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO representing the response from POST /api/v1/friends/add (§53).
 *
 * Source: 08_DATA_AND_API.md section 53: output contains friendship and status.
 */
@Serializable
data class AddFriendResponseDto(
    @SerialName("friendship") val friendship: FriendDto,
    @SerialName("status") val status: String,
)

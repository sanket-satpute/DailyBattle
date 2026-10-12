package com.sanket_satpute_20.dailybattle.data.remote.dto.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO representing the response from GET /api/v1/friends/{userId} (§52).
 */
@Serializable
data class FriendsResponseDto(
    @SerialName("friends") val friends: List<FriendDto> = emptyList(),
)

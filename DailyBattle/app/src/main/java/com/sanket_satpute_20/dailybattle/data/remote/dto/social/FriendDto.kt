package com.sanket_satpute_20.dailybattle.data.remote.dto.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for a Friend entity (§33).
 *
 * Source: 08_DATA_AND_API.md section 33, matching domain model [com.sanket_satpute_20.dailybattle.domain.friend.Friend].
 */
@Serializable
data class FriendDto(
    @SerialName("friendshipId") val friendshipId: String,
    @SerialName("userId") val userId: String,
    @SerialName("friendUserId") val friendUserId: String,
    @SerialName("friendName") val friendName: String,
    @SerialName("status") val status: String,
    @SerialName("todayScore") val todayScore: Int? = null,
    @SerialName("createdAt") val createdAt: Long,
    @SerialName("updatedAt") val updatedAt: Long,
)

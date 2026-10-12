package com.sanket_satpute_20.dailybattle.data.remote.dto.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for adding a friend by Battle Code (§53).
 *
 * Source: 08_DATA_AND_API.md section 53, 57 (idempotency key).
 * Battle Code is treated as an opaque validated string per DATA-PENDING-014.
 */
@Serializable
data class AddFriendRequestDto(
    @SerialName("userId") val userId: String,
    @SerialName("battleCode") val battleCode: String,
    @SerialName("clientRequestId") val clientRequestId: String,
)

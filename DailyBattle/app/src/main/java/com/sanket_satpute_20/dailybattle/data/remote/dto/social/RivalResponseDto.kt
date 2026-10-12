package com.sanket_satpute_20.dailybattle.data.remote.dto.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for the current server-selected Rival (§36, §54).
 *
 * Source: 08_DATA_AND_API.md section 36, matching domain model [com.sanket_satpute_20.dailybattle.domain.rival.Rival].
 */
@Serializable
data class RivalResponseDto(
    @SerialName("userId") val userId: String,
    @SerialName("rivalUserId") val rivalUserId: String,
    @SerialName("rivalName") val rivalName: String,
    @SerialName("selectedAt") val selectedAt: Long,
    @SerialName("status") val status: String,
)

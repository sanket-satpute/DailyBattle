package com.sanket_satpute_20.dailybattle.data.remote.dto.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for user Profile response (§38, §55).
 *
 * Source: 08_DATA_AND_API.md section 38, matching domain model [com.sanket_satpute_20.dailybattle.domain.profile.Profile].
 */
@Serializable
data class ProfileResponseDto(
    @SerialName("userId") val userId: String,
    @SerialName("battleName") val battleName: String,
    @SerialName("momentum") val momentum: Int,
    @SerialName("bestScore") val bestScore: Int,
    @SerialName("averageScore") val averageScore: Int,
    @SerialName("battleDNA") val battleDNA: BattleDnaDto,
    @SerialName("records") val records: PersonalRecordsDto,
)

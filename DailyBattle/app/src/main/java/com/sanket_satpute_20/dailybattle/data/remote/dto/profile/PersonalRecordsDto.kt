package com.sanket_satpute_20.dailybattle.data.remote.dto.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for personal records in the user profile (§38).
 *
 * Source: 08_DATA_AND_API.md section 38, matching domain model [com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords].
 */
@Serializable
data class PersonalRecordsDto(
    @SerialName("bestScore") val bestScore: Int,
    @SerialName("bestMomentum") val bestMomentum: Int,
    @SerialName("battlesPlayed") val battlesPlayed: Int,
)

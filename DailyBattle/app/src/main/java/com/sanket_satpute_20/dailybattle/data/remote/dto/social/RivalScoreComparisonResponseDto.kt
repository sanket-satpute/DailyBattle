package com.sanket_satpute_20.dailybattle.data.remote.dto.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for Rival score comparison (§37, §54).
 *
 * Source: 08_DATA_AND_API.md section 37, matching domain model [com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison].
 */
@Serializable
data class RivalScoreComparisonResponseDto(
    @SerialName("userScore") val userScore: Int,
    @SerialName("rivalScore") val rivalScore: Int,
    @SerialName("scoreGap") val scoreGap: Int,
    @SerialName("battleId") val battleId: String,
    @SerialName("direction") val direction: String,
)

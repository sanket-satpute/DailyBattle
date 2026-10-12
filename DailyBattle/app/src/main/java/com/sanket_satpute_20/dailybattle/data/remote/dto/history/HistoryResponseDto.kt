package com.sanket_satpute_20.dailybattle.data.remote.dto.history

import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO representing history list response from GET /api/v1/history/{userId} (§40, §56).
 */
@Serializable
data class HistoryResponseDto(
    @SerialName("history") val history: List<BattleResultResponseDto> = emptyList(),
)

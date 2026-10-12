package com.sanket_satpute_20.dailybattle.data.remote.dto.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote transport DTO for Battle DNA performance dimensions (§39).
 *
 * Source: 08_DATA_AND_API.md section 39. Initial state may be null when dimensions are unavailable.
 */
@Serializable
data class BattleDnaDto(
    @SerialName("speed") val speed: Int? = null,
    @SerialName("memory") val memory: Int? = null,
    @SerialName("people") val people: Int? = null,
)

package com.sanket_satpute_20.dailybattle.data.remote.dto.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured remote error response as defined in 08_DATA_AND_API.md §58.
 *
 * Example:
 * ```json
 * {
 *   "code": "BATTLE_NOT_AVAILABLE",
 *   "message": "The battle is not currently available.",
 *   "details": "Session expired or battle window closed",
 *   "requestId": "req-12345"
 * }
 * ```
 */
@Serializable
data class ApiErrorResponseDto(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
    @SerialName("details") val details: String? = null,
    @SerialName("requestId") val requestId: String? = null,
)

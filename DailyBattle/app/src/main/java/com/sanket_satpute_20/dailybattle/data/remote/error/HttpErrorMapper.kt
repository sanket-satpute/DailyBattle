package com.sanket_satpute_20.dailybattle.data.remote.error

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.dto.error.ApiErrorResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import retrofit2.HttpException

/**
 * Maps HTTP response status codes and exceptions into domain [AppError] categories.
 *
 * Adheres strictly to the specification in 09_OFFLINE_AND_ERROR_HANDLING.md and 08_DATA_AND_API.md §58.
 */
object HttpErrorMapper {

    fun mapStatusCode(statusCode: Int): AppError = when (statusCode) {
        401 -> AppError.Authentication
        403 -> AppError.Authorization
        400, 422 -> AppError.Validation
        in 500..599 -> AppError.Server
        else -> AppError.Unknown
    }

    fun mapHttpException(exception: HttpException): AppError =
        mapStatusCode(exception.code())

    /**
     * Parses structured error response payload as defined in 08_DATA_AND_API.md §58.
     * Returns null if the body is null, blank, or does not conform to the error schema.
     */
    fun parseErrorResponse(errorBodyJson: String?): ApiErrorResponseDto? {
        if (errorBodyJson.isNullOrBlank()) return null
        return try {
            DailyBattleJson.decodeFromString(ApiErrorResponseDto.serializer(), errorBodyJson)
        } catch (_: Exception) {
            null
        }
    }
}


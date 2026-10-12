package com.sanket_satpute_20.dailybattle.data.remote.error

import com.sanket_satpute_20.dailybattle.core.error.AppError
import retrofit2.HttpException

/**
 * Maps HTTP response status codes and exceptions into domain [AppError] categories.
 *
 * Adheres strictly to the specification in 09_OFFLINE_AND_ERROR_HANDLING.md.
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
}

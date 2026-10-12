package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.error.HttpErrorMapper
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class HttpErrorMapperTest {

    @Test
    fun mapsAuthenticationStatusCodeCorrectly() {
        assertEquals(AppError.Authentication, HttpErrorMapper.mapStatusCode(401))

        val httpException = HttpException(Response.error<Unit>(401, "".toResponseBody()))
        assertEquals(AppError.Authentication, HttpErrorMapper.mapHttpException(httpException))
    }

    @Test
    fun mapsAuthorizationStatusCodeCorrectly() {
        assertEquals(AppError.Authorization, HttpErrorMapper.mapStatusCode(403))

        val httpException = HttpException(Response.error<Unit>(403, "".toResponseBody()))
        assertEquals(AppError.Authorization, HttpErrorMapper.mapHttpException(httpException))
    }

    @Test
    fun mapsValidationStatusCodesCorrectly() {
        assertEquals(AppError.Validation, HttpErrorMapper.mapStatusCode(400))
        assertEquals(AppError.Validation, HttpErrorMapper.mapStatusCode(422))
    }

    @Test
    fun mapsServerStatusCodesCorrectly() {
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(500))
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(502))
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(503))
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(504))
    }

    @Test
    fun mapsUnclassifiedStatusCodesToUnknown() {
        assertEquals(AppError.Unknown, HttpErrorMapper.mapStatusCode(404))
        assertEquals(AppError.Unknown, HttpErrorMapper.mapStatusCode(409))
        assertEquals(AppError.Unknown, HttpErrorMapper.mapStatusCode(418))
    }
}

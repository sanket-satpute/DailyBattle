package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthenticationRequiredException
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SafeApiCallTest {

    @Test
    fun returnsSuccessWhenBlockCompletesNormally() = runBlocking {
        val result = SafeApiCall.execute { "success_payload" }

        assertTrue(result is DomainResult.Success)
        assertEquals("success_payload", (result as DomainResult.Success).value)
    }

    @Test(expected = CancellationException::class)
    fun rethrowsCancellationExceptionPromptlyWithoutSwallowing() = runBlocking {
        SafeApiCall.execute<String> {
            throw CancellationException("Coroutine was cancelled")
        }
        Unit
    }

    @Test
    fun mapsSocketTimeoutExceptionToTimeoutError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw SocketTimeoutException("Read timed out")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }

    @Test
    fun mapsUnknownHostExceptionToNetworkError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw UnknownHostException("Unable to resolve host")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun mapsConnectExceptionToNetworkError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw ConnectException("Failed to connect to host")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun mapsSerializationExceptionToValidationError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw SerializationException("Missing required JSON field")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun mapsAuthenticationRequiredExceptionToAuthenticationError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw AuthenticationRequiredException("No token configured")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authentication, (result as DomainResult.Failure).error)
    }

    @Test
    fun mapsHttpExceptionThroughHttpErrorMapper() = runBlocking {
        val http500 = HttpException(Response.error<Unit>(500, "".toResponseBody()))
        val serverResult = SafeApiCall.execute<String> { throw http500 }
        assertTrue(serverResult is DomainResult.Failure)
        assertEquals(AppError.Server, (serverResult as DomainResult.Failure).error)

        val http401 = HttpException(Response.error<Unit>(401, "".toResponseBody()))
        val authResult = SafeApiCall.execute<String> { throw http401 }
        assertTrue(authResult is DomainResult.Failure)
        assertEquals(AppError.Authentication, (authResult as DomainResult.Failure).error)
    }

    @Test
    fun mapsGenericIoExceptionToNetworkError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw IOException("Socket broken")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Network, (result as DomainResult.Failure).error)
    }

    @Test
    fun mapsUnhandledExceptionToUnknownError() = runBlocking {
        val result = SafeApiCall.execute<String> {
            throw IllegalStateException("Unexpected crash")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Unknown, (result as DomainResult.Failure).error)
    }
}

package com.sanket_satpute_20.dailybattle.data.remote.error

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthenticationRequiredException
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Safely executes a network call, mapping network/HTTP/serialization errors into structured [DomainResult].
 *
 * Guarantees that:
 * 1. Coroutine [CancellationException] is never swallowed and is promptly rethrown.
 * 2. Timeouts, connection failures, validation/serialization issues, and auth failures map to distinct [AppError] variants.
 * 3. Raw technical error details or sensitive data are not exposed directly to UI layers.
 */
object SafeApiCall {

    suspend fun <T> execute(block: suspend () -> T): DomainResult<T> {
        return try {
            DomainResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            DomainResult.Failure(HttpErrorMapper.mapHttpException(e))
        } catch (e: SocketTimeoutException) {
            DomainResult.Failure(AppError.Timeout)
        } catch (e: UnknownHostException) {
            DomainResult.Failure(AppError.Network)
        } catch (e: ConnectException) {
            DomainResult.Failure(AppError.Network)
        } catch (e: SerializationException) {
            DomainResult.Failure(AppError.Validation)
        } catch (e: AuthenticationRequiredException) {
            DomainResult.Failure(AppError.Authentication)
        } catch (e: IOException) {
            DomainResult.Failure(AppError.Network)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Unknown)
        }
    }
}

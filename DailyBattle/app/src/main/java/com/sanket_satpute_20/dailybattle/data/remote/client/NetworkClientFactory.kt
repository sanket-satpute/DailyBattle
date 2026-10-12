package com.sanket_satpute_20.dailybattle.data.remote.client

import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthInterceptor
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.interceptor.HeaderInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory for creating configured OkHttpClient and Retrofit instances.
 *
 * Enforces centralized timeout configuration (15s connect, 15s read, 15s write),
 * standard headers, and kotlinx.serialization converter.
 */
object NetworkClientFactory {

    fun createOkHttpClient(
        config: NetworkConfig,
        authTokenProvider: AuthTokenProvider,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(AuthInterceptor(authTokenProvider))
            .build()
    }

    fun createRetrofitBuilder(
        okHttpClient: OkHttpClient,
        json: Json,
        config: NetworkConfig? = null,
    ): Retrofit.Builder {
        val mediaType = NetworkConfig.CONTENT_TYPE_JSON.toMediaType()
        val builder = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(mediaType))

        val baseUrl = config?.baseUrl
        if (!baseUrl.isNullOrBlank()) {
            builder.baseUrl(baseUrl)
        }

        return builder
    }
}

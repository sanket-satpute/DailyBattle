package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.auth.UnconfiguredAuthTokenProvider
import com.sanket_satpute_20.dailybattle.data.remote.client.NetworkClientFactory
import com.sanket_satpute_20.dailybattle.data.remote.config.NetworkConfig
import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module providing networking infrastructure dependencies.
 *
 * Implements approved technical decision TECH-PENDING-009.
 * Preserves unresolved backend decisions TECH-PENDING-008 and DATA-PENDING-001 by leaving base URL
 * unconfigured until authoritative server contracts are finalized.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = DailyBattleJson

    @Provides
    @Singleton
    fun provideNetworkConfig(
        environmentProvider: EnvironmentProvider,
    ): NetworkConfig = NetworkConfig(
        environment = environmentProvider.current,
        baseUrl = null, // Explicitly unconfigured per TECH-PENDING-008
    )

    @Provides
    @Singleton
    fun provideAuthTokenProvider(): AuthTokenProvider = UnconfiguredAuthTokenProvider()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        config: NetworkConfig,
        authTokenProvider: AuthTokenProvider,
        payloadSizeInterceptor: com.sanket_satpute_20.dailybattle.core.network.PayloadSizeInterceptor? = null,
    ): OkHttpClient = NetworkClientFactory.createOkHttpClient(
        config = config,
        authTokenProvider = authTokenProvider,
        additionalInterceptors = listOfNotNull(payloadSizeInterceptor),
    )

    @Provides
    @Singleton
    fun provideRetrofitBuilder(
        okHttpClient: OkHttpClient,
        json: Json,
        config: NetworkConfig,
    ): Retrofit.Builder = NetworkClientFactory.createRetrofitBuilder(okHttpClient, json, config)
}

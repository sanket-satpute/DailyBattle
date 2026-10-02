package com.sanket_satpute_20.dailybattle.core.config.di

import com.sanket_satpute_20.dailybattle.BuildConfig
import com.sanket_satpute_20.dailybattle.core.config.BuildTypeEnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Singleton
    fun provideEnvironmentProvider(): EnvironmentProvider =
        BuildTypeEnvironmentProvider(isDebugBuild = BuildConfig.DEBUG)
}

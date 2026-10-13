package com.sanket_satpute_20.dailybattle.core.analytics.di

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.analytics.DefaultAnalyticsTracker
import com.sanket_satpute_20.dailybattle.core.analytics.InMemoryAnalyticsProvider
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideDefaultAnalyticsTracker(
        environmentProvider: EnvironmentProvider
    ): DefaultAnalyticsTracker {
        // Safe in-memory provider registered by default (AN-PENDING-001: no external SDK locked)
        val inMemoryProvider = InMemoryAnalyticsProvider()
        return DefaultAnalyticsTracker(
            environmentProvider = environmentProvider,
            initialProviders = listOf(inMemoryProvider)
        )
    }

    @Provides
    @Singleton
    fun provideAnalyticsTracker(
        defaultTracker: DefaultAnalyticsTracker
    ): AnalyticsTracker = defaultTracker
}

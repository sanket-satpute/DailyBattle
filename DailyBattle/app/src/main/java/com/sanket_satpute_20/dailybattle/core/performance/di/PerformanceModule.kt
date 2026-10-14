package com.sanket_satpute_20.dailybattle.core.performance.di

import com.sanket_satpute_20.dailybattle.core.performance.DefaultGameplayPerformanceTracker
import com.sanket_satpute_20.dailybattle.core.performance.DefaultSessionStabilityTracker
import com.sanket_satpute_20.dailybattle.core.performance.DefaultStartupPerformanceTracker
import com.sanket_satpute_20.dailybattle.core.performance.GameplayPerformanceTracker
import com.sanket_satpute_20.dailybattle.core.performance.SessionStabilityTracker
import com.sanket_satpute_20.dailybattle.core.performance.StartupPerformanceTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding application performance measurement dependencies.
 *
 * Implements Sprint 24.1 (Startup), Sprint 24.2 (Gameplay) & Sprint 24.3 (Stability) per `docs/ROADMAP.md`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PerformanceModule {

    @Binds
    @Singleton
    abstract fun bindStartupPerformanceTracker(
        impl: DefaultStartupPerformanceTracker
    ): StartupPerformanceTracker

    @Binds
    @Singleton
    abstract fun bindGameplayPerformanceTracker(
        impl: DefaultGameplayPerformanceTracker
    ): GameplayPerformanceTracker

    @Binds
    @Singleton
    abstract fun bindSessionStabilityTracker(
        impl: DefaultSessionStabilityTracker
    ): SessionStabilityTracker
}

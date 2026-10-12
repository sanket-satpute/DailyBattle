package com.sanket_satpute_20.dailybattle.domain.di

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Provides
    @Singleton
    fun provideGetOrStartOfficialBattleUseCase(
        repository: BattleSessionRepository
    ): GetOrStartOfficialBattleUseCase {
        return GetOrStartOfficialBattleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideActiveBattleSessionManager(
        repository: BattleSessionRepository
    ): com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager {
        return com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager(repository)
    }

    @Provides
    @Singleton
    fun provideDomainAuthorityRegistry(
        registry: com.sanket_satpute_20.dailybattle.domain.authority.DefaultDomainAuthorityRegistry
    ): com.sanket_satpute_20.dailybattle.domain.authority.DomainAuthorityRegistry {
        return registry
    }
}


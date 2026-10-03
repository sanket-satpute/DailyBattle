package com.sanket_satpute_20.dailybattle.domain.di

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideGetOrStartOfficialBattleUseCase(
        repository: BattleSessionRepository
    ): GetOrStartOfficialBattleUseCase {
        return GetOrStartOfficialBattleUseCase(repository)
    }
}

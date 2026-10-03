package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.user.InMemoryUserRepository
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        inMemoryUserRepository: InMemoryUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindBattleSessionRepository(
        inMemoryBattleSessionRepository: com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
    ): com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
}

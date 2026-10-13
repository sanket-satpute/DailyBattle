package com.sanket_satpute_20.dailybattle.core.lifecycle.di

import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.lifecycle.LifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.GameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultProcessDeathRecoverer
import com.sanket_satpute_20.dailybattle.core.lifecycle.ProcessDeathRecoverer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding application lifecycle management dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LifecycleModule {

    @Binds
    @Singleton
    abstract fun bindAppLifecycleManager(
        impl: DefaultAppLifecycleManager,
    ): AppLifecycleManager

    @Binds
    @Singleton
    abstract fun bindLifecycleBattlePreserver(
        impl: DefaultLifecycleBattlePreserver,
    ): LifecycleBattlePreserver

    @Binds
    @Singleton
    abstract fun bindGameplayStateRestorer(
        impl: DefaultGameplayStateRestorer,
    ): GameplayStateRestorer

    @Binds
    @Singleton
    abstract fun bindProcessDeathRecoverer(
        impl: DefaultProcessDeathRecoverer,
    ): ProcessDeathRecoverer
}

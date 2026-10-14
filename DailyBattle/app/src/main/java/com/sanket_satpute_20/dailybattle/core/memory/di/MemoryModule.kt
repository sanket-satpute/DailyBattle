package com.sanket_satpute_20.dailybattle.core.memory.di

import com.sanket_satpute_20.dailybattle.core.memory.AppMemoryManager
import com.sanket_satpute_20.dailybattle.core.memory.DefaultAppMemoryManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding application memory management dependencies.
 *
 * Implements Sprint 24.4 (Memory) per `docs/ROADMAP.md`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MemoryModule {

    @Binds
    @Singleton
    abstract fun bindAppMemoryManager(
        impl: DefaultAppMemoryManager
    ): AppMemoryManager
}

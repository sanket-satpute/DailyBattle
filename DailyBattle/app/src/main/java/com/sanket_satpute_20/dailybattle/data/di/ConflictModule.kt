package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.conflict.ConflictResolver
import com.sanket_satpute_20.dailybattle.data.conflict.DefaultConflictResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding conflict resolution dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ConflictModule {

    @Binds
    @Singleton
    abstract fun bindConflictResolver(
        impl: DefaultConflictResolver,
    ): ConflictResolver
}

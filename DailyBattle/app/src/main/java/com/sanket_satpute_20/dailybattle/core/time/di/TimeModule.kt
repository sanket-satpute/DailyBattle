package com.sanket_satpute_20.dailybattle.core.time.di

import com.sanket_satpute_20.dailybattle.core.time.DefaultTimeProvider
import com.sanket_satpute_20.dailybattle.core.time.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    abstract fun bindTimeProvider(
        defaultTimeProvider: DefaultTimeProvider
    ): TimeProvider
}

package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.retry.DefaultIdempotentRetryHandler
import com.sanket_satpute_20.dailybattle.data.retry.DefaultRetryExecutor
import com.sanket_satpute_20.dailybattle.data.retry.DefaultRetryPolicy
import com.sanket_satpute_20.dailybattle.data.retry.IdempotentRetryHandler
import com.sanket_satpute_20.dailybattle.data.retry.RetryExecutor
import com.sanket_satpute_20.dailybattle.data.retry.RetryPolicy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding retry infrastructure dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RetryModule {

    @Binds
    @Singleton
    abstract fun bindRetryExecutor(
        impl: DefaultRetryExecutor,
    ): RetryExecutor

    @Binds
    @Singleton
    abstract fun bindIdempotentRetryHandler(
        impl: DefaultIdempotentRetryHandler,
    ): IdempotentRetryHandler

    companion object {
        @Provides
        @Singleton
        fun provideRetryPolicy(): RetryPolicy = DefaultRetryPolicy()
    }
}

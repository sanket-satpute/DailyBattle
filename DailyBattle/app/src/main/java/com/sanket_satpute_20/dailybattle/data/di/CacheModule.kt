package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.cache.CacheMetadataStore
import com.sanket_satpute_20.dailybattle.data.cache.InMemoryCacheMetadataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding cache infrastructure dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    @Binds
    @Singleton
    abstract fun bindCacheMetadataStore(
        impl: InMemoryCacheMetadataStore,
    ): CacheMetadataStore
}

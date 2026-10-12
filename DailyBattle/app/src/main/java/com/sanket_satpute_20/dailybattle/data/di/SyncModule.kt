package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.sync.ConnectivityNetworkMonitor
import com.sanket_satpute_20.dailybattle.data.sync.DefaultSyncExecutor
import com.sanket_satpute_20.dailybattle.data.sync.DefaultSynchronizer
import com.sanket_satpute_20.dailybattle.data.sync.InMemorySyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.NetworkMonitor
import com.sanket_satpute_20.dailybattle.data.sync.SyncExecutor
import com.sanket_satpute_20.dailybattle.data.sync.SyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.Synchronizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding synchronization infrastructure dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncQueueStore(
        impl: InMemorySyncQueueStore,
    ): SyncQueueStore

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: ConnectivityNetworkMonitor,
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindSyncExecutor(
        impl: DefaultSyncExecutor,
    ): SyncExecutor

    @Binds
    @Singleton
    abstract fun bindSynchronizer(
        impl: DefaultSynchronizer,
    ): Synchronizer
}

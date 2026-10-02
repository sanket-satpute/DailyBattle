package com.sanket_satpute_20.dailybattle.core.config

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Test-only accessor for the production Hilt graph's [EnvironmentProvider] binding. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface EnvironmentProviderEntryPoint {
    fun environmentProvider(): EnvironmentProvider
}

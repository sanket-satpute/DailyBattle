package com.sanket_satpute_20.dailybattle.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.sanket_satpute_20.dailybattle.data.local.preferences.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(SettingsDataStore.PREFERENCES_NAME) },
    )

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        dataStore: DataStore<Preferences>,
    ): SettingsDataStore = SettingsDataStore(dataStore)
}

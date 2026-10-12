package com.sanket_satpute_20.dailybattle.data.settings

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.preferences.SettingsDataStore
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.settings.SettingsRepository
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore Preferences backed implementation of [SettingsRepository].
 *
 * Implements Sprint 15.3 repository integration connecting the settings
 * domain contract to the local DataStore Preferences storage layer.
 */
@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val settingsFlow: StateFlow<UserSettings> = settingsDataStore.settingsFlow
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettings(),
        )

    override suspend fun getSettings(): DomainResult<UserSettings> {
        return runCatching {
            settingsDataStore.getSettings()
        }.fold(
            onSuccess = { DomainResult.Success(it) },
            onFailure = { DomainResult.Failure(AppError.Persistence) },
        )
    }

    override suspend fun updateSound(enabled: Boolean): DomainResult<Unit> {
        return runCatching {
            settingsDataStore.updateSoundEnabled(enabled)
        }.fold(
            onSuccess = { DomainResult.Success(Unit) },
            onFailure = { DomainResult.Failure(AppError.Persistence) },
        )
    }

    override suspend fun updateHaptics(enabled: Boolean): DomainResult<Unit> {
        return runCatching {
            settingsDataStore.updateHapticsEnabled(enabled)
        }.fold(
            onSuccess = { DomainResult.Success(Unit) },
            onFailure = { DomainResult.Failure(AppError.Persistence) },
        )
    }

    override suspend fun updateNotifications(enabled: Boolean): DomainResult<Unit> {
        return runCatching {
            settingsDataStore.updateNotificationsEnabled(enabled)
        }.fold(
            onSuccess = { DomainResult.Success(Unit) },
            onFailure = { DomainResult.Failure(AppError.Persistence) },
        )
    }

    override suspend fun deleteAccount(): DomainResult<Unit> {
        return runCatching {
            settingsDataStore.clear()
        }.fold(
            onSuccess = { DomainResult.Success(Unit) },
            onFailure = { DomainResult.Failure(AppError.Persistence) },
        )
    }
}

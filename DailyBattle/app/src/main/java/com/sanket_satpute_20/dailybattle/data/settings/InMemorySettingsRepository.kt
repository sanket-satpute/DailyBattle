package com.sanket_satpute_20.dailybattle.data.settings

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.settings.SettingsRepository
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySettingsRepository @Inject constructor() : SettingsRepository {

    private val _settingsFlow = MutableStateFlow(UserSettings())
    override val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    var shouldFailUpdate: Boolean = false
    var shouldFailDeletion: Boolean = false

    override suspend fun getSettings(): DomainResult<UserSettings> {
        return DomainResult.Success(_settingsFlow.value)
    }

    override suspend fun updateSound(enabled: Boolean): DomainResult<Unit> {
        if (shouldFailUpdate) {
            return DomainResult.Failure(AppError.Persistence)
        }
        _settingsFlow.update { it.copy(soundEnabled = enabled) }
        return DomainResult.Success(Unit)
    }

    override suspend fun updateHaptics(enabled: Boolean): DomainResult<Unit> {
        if (shouldFailUpdate) {
            return DomainResult.Failure(AppError.Persistence)
        }
        _settingsFlow.update { it.copy(hapticsEnabled = enabled) }
        return DomainResult.Success(Unit)
    }

    override suspend fun updateNotifications(enabled: Boolean): DomainResult<Unit> {
        if (shouldFailUpdate) {
            return DomainResult.Failure(AppError.Persistence)
        }
        _settingsFlow.update { it.copy(notificationsEnabled = enabled) }
        return DomainResult.Success(Unit)
    }

    override suspend fun deleteAccount(): DomainResult<Unit> {
        if (shouldFailDeletion) {
            return DomainResult.Failure(AppError.Server)
        }
        _settingsFlow.value = UserSettings()
        return DomainResult.Success(Unit)
    }
}

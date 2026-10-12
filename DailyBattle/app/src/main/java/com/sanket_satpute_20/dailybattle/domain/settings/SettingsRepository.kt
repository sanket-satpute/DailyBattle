package com.sanket_satpute_20.dailybattle.domain.settings

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for managing user preferences and settings.
 *
 * Per `07_TECHNICAL_ARCHITECTURE.md` §22 and `08_DATA_AND_API.md` §41,
 * settings are primarily local preferences:
 * - soundEnabled
 * - hapticsEnabled
 * - notificationsEnabled
 *
 * Also manages account deletion from the settings context.
 */
interface SettingsRepository {
    val settingsFlow: StateFlow<UserSettings>

    suspend fun getSettings(): DomainResult<UserSettings>

    suspend fun updateSound(enabled: Boolean): DomainResult<Unit>

    suspend fun updateHaptics(enabled: Boolean): DomainResult<Unit>

    suspend fun updateNotifications(enabled: Boolean): DomainResult<Unit>

    suspend fun deleteAccount(): DomainResult<Unit>
}

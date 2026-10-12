package com.sanket_satpute_20.dailybattle.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore Preferences implementation for user settings storage.
 *
 * Implements approved technical decision TECH-PENDING-007.
 */
class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val settingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                soundEnabled = preferences[KEY_SOUND_ENABLED] ?: DEFAULT_SOUND_ENABLED,
                hapticsEnabled = preferences[KEY_HAPTICS_ENABLED] ?: DEFAULT_HAPTICS_ENABLED,
                notificationsEnabled = preferences[KEY_NOTIFICATIONS_ENABLED] ?: DEFAULT_NOTIFICATIONS_ENABLED,
            )
        }

    suspend fun getSettings(): UserSettings = settingsFlow.first()

    suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = settings.soundEnabled
            preferences[KEY_HAPTICS_ENABLED] = settings.hapticsEnabled
            preferences[KEY_NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        const val PREFERENCES_NAME = "user_settings_preferences"
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        const val DEFAULT_SOUND_ENABLED = true
        const val DEFAULT_HAPTICS_ENABLED = true
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
    }
}

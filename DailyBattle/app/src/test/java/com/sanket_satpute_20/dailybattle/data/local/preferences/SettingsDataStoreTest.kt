package com.sanket_satpute_20.dailybattle.data.local.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsDataStoreTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var settingsDataStore: SettingsDataStore

    @Before
    fun setup() {
        val testFile = tmpFolder.newFile("test_user_settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        settingsDataStore = SettingsDataStore(dataStore)
    }

    @Test
    fun defaultSettingsReturnAllTrue() = testScope.runTest {
        val settings = settingsDataStore.getSettings()
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun updateSoundEnabledModifiesSoundOnly() = testScope.runTest {
        settingsDataStore.updateSoundEnabled(false)

        val settings = settingsDataStore.getSettings()
        assertFalse(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun updateHapticsEnabledModifiesHapticsOnly() = testScope.runTest {
        settingsDataStore.updateHapticsEnabled(false)

        val settings = settingsDataStore.getSettings()
        assertTrue(settings.soundEnabled)
        assertFalse(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun updateNotificationsEnabledModifiesNotificationsOnly() = testScope.runTest {
        settingsDataStore.updateNotificationsEnabled(false)

        val settings = settingsDataStore.getSettings()
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertFalse(settings.notificationsEnabled)
    }

    @Test
    fun updateSettingsUpdatesAllPreferences() = testScope.runTest {
        val newSettings = UserSettings(
            soundEnabled = false,
            hapticsEnabled = false,
            notificationsEnabled = false,
        )
        settingsDataStore.updateSettings(newSettings)

        val retrieved = settingsDataStore.getSettings()
        assertEquals(newSettings, retrieved)
    }

    @Test
    fun clearResetsToDefaults() = testScope.runTest {
        settingsDataStore.updateSettings(
            UserSettings(
                soundEnabled = false,
                hapticsEnabled = false,
                notificationsEnabled = false,
            )
        )
        settingsDataStore.clear()

        val retrieved = settingsDataStore.getSettings()
        assertTrue(retrieved.soundEnabled)
        assertTrue(retrieved.hapticsEnabled)
        assertTrue(retrieved.notificationsEnabled)
    }
}

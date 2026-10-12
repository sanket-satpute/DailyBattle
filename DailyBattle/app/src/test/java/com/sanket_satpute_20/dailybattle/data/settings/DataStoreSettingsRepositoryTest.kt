package com.sanket_satpute_20.dailybattle.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sanket_satpute_20.dailybattle.data.local.preferences.SettingsDataStore
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: DataStoreSettingsRepository

    @Before
    fun setup() {
        val testFile = tmpFolder.newFile("test_settings_repo.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        settingsDataStore = SettingsDataStore(dataStore)
        repository = DataStoreSettingsRepository(settingsDataStore)
    }

    @Test
    fun getSettingsReturnsInitialDefaults() = testScope.runTest {
        val result = repository.getSettings()
        assertTrue(result is DomainResult.Success)
        val settings = (result as DomainResult.Success).value
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun updateSoundUpdatesSoundPreference() = testScope.runTest {
        val updateResult = repository.updateSound(false)
        assertTrue(updateResult is DomainResult.Success)

        val settingsResult = repository.getSettings()
        assertTrue(settingsResult is DomainResult.Success)
        val settings = (settingsResult as DomainResult.Success).value
        assertFalse(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun updateHapticsUpdatesHapticsPreference() = testScope.runTest {
        val updateResult = repository.updateHaptics(false)
        assertTrue(updateResult is DomainResult.Success)

        val settings = (repository.getSettings() as DomainResult.Success).value
        assertTrue(settings.soundEnabled)
        assertFalse(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun updateNotificationsUpdatesNotificationsPreference() = testScope.runTest {
        val updateResult = repository.updateNotifications(false)
        assertTrue(updateResult is DomainResult.Success)

        val settings = (repository.getSettings() as DomainResult.Success).value
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertFalse(settings.notificationsEnabled)
    }

    @Test
    fun deleteAccountClearsAllPreferences() = testScope.runTest {
        repository.updateSound(false)
        repository.updateHaptics(false)

        val deleteResult = repository.deleteAccount()
        assertTrue(deleteResult is DomainResult.Success)

        val settings = (repository.getSettings() as DomainResult.Success).value
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }
}

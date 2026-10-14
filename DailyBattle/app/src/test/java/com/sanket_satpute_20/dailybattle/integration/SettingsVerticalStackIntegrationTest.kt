package com.sanket_satpute_20.dailybattle.integration

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.data.local.preferences.SettingsDataStore
import com.sanket_satpute_20.dailybattle.data.settings.DataStoreSettingsRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.DeleteAccountUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.GetSettingsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateHapticsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateNotificationsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateSoundUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UpdateBattleNameUseCase
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryFriendDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryUserDao
import com.sanket_satpute_20.dailybattle.presentation.settings.SettingsDetailDialog
import com.sanket_satpute_20.dailybattle.presentation.settings.SettingsViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> SettingsViewModel <-> Use Cases <-> Repositories <-> DataStore & Room DAOs
 *
 * Verifies Sprint 23.3:
 * - Preferences loaded from DataStore and battle name from Room UserDao flow up to SettingsUiState
 * - Toggling sound, haptics, and notifications persists to DataStore preferences
 * - Editing battle name through dialog validates, updates Room UserDao, and updates UI state
 * - Account deletion clears DataStore preferences and updates account status
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsVerticalStackIntegrationTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var settingsRepository: DataStoreSettingsRepository
    private lateinit var userDao: InMemoryUserDao
    private lateinit var userRepository: RoomUserRepository
    private lateinit var friendDao: InMemoryFriendDao
    private lateinit var friendRepository: RoomFriendRepository

    private lateinit var getSettingsUseCase: GetSettingsUseCase
    private lateinit var updateSoundUseCase: UpdateSoundUseCase
    private lateinit var updateHapticsUseCase: UpdateHapticsUseCase
    private lateinit var updateNotificationsUseCase: UpdateNotificationsUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var updateBattleNameUseCase: UpdateBattleNameUseCase
    private lateinit var getUserBattleCodeUseCase: GetUserBattleCodeUseCase

    @Before
    fun setup() {
        val testFile = tmpFolder.newFile("settings_vertical_stack_test.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        val settingsDataStore = SettingsDataStore(dataStore)
        settingsRepository = DataStoreSettingsRepository(settingsDataStore)

        userDao = InMemoryUserDao()
        userRepository = RoomUserRepository(userDao)

        friendDao = InMemoryFriendDao()
        friendRepository = RoomFriendRepository(friendDao)

        getSettingsUseCase = GetSettingsUseCase(settingsRepository)
        updateSoundUseCase = UpdateSoundUseCase(settingsRepository)
        updateHapticsUseCase = UpdateHapticsUseCase(settingsRepository)
        updateNotificationsUseCase = UpdateNotificationsUseCase(settingsRepository)
        deleteAccountUseCase = DeleteAccountUseCase(settingsRepository)
        updateBattleNameUseCase = UpdateBattleNameUseCase(userRepository)
        getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepository)
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            getSettingsUseCase = getSettingsUseCase,
            updateSoundUseCase = updateSoundUseCase,
            updateHapticsUseCase = updateHapticsUseCase,
            updateNotificationsUseCase = updateNotificationsUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            userRepository = userRepository,
            updateBattleNameUseCase = updateBattleNameUseCase,
            getUserBattleCodeUseCase = getUserBattleCodeUseCase
        )
    }

    @Test
    fun loadSettingsCombinesDataStorePreferencesAndUserDao() = testScope.runTest {
        // Pre-populate UserDao with user battle name
        userDao.insertUser(
            UserEntity(
                userId = RoomUserRepository.LOCAL_USER_ID,
                battleName = "Centurion",
                createdAt = 1000L,
                updatedAt = 1000L,
                accountStatus = "ACTIVE"
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.soundEnabled)
        assertTrue(state.hapticsEnabled)
        assertTrue(state.notificationsEnabled)
        assertEquals("Centurion", state.battleName)
        assertEquals("K4X8M9", state.battleCode)
        assertNull(state.error)
    }

    @Test
    fun toggleSoundMutatesDataStoreAndUpdatesUiState() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.soundEnabled)

        // Toggle sound OFF
        viewModel.toggleSound()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.soundEnabled)
        // Verify persisted directly in SettingsRepository DataStore
        val repoSettings = settingsRepository.getSettings()
        assertFalse((repoSettings as com.sanket_satpute_20.dailybattle.domain.result.DomainResult.Success).value.soundEnabled)

        // Toggle sound ON
        viewModel.toggleSound()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.soundEnabled)
    }

    @Test
    fun toggleHapticsMutatesDataStoreAndUpdatesUiState() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hapticsEnabled)

        viewModel.toggleHaptics()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hapticsEnabled)
        val repoSettings = settingsRepository.getSettings()
        assertFalse((repoSettings as com.sanket_satpute_20.dailybattle.domain.result.DomainResult.Success).value.hapticsEnabled)
    }

    @Test
    fun toggleNotificationsMutatesDataStoreAndUpdatesUiState() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Notification permission available by default in state
        assertTrue(viewModel.uiState.value.notificationsEnabled)

        viewModel.toggleNotifications()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.notificationsEnabled)
        val repoSettings = settingsRepository.getSettings()
        assertFalse((repoSettings as com.sanket_satpute_20.dailybattle.domain.result.DomainResult.Success).value.notificationsEnabled)
    }

    @Test
    fun editBattleNameDialogFlowUpdatesUserDaoAndPresentationState() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Open edit dialog
        viewModel.openDetailDialog(SettingsDetailDialog.EDIT_BATTLE_NAME)
        assertEquals(SettingsDetailDialog.EDIT_BATTLE_NAME, viewModel.uiState.value.activeDetailDialog)

        // Enter new battle name and save
        viewModel.onEditBattleNameChange("UpdatedVanguard")
        viewModel.saveBattleName()
        advanceUntilIdle()

        // UI state reflects new name and closes dialog
        assertNull(viewModel.uiState.value.activeDetailDialog)
        assertEquals("UpdatedVanguard", viewModel.uiState.value.battleName)

        // Database entity in UserDao is updated
        val persistedUser = userDao.getUserById(RoomUserRepository.LOCAL_USER_ID)
        assertEquals("UpdatedVanguard", persistedUser?.battleName)
    }

    @Test
    fun deleteAccountResetsDataStoreAndUpdatesAccountStatus() = testScope.runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showDeleteAccountConfirmation()
        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)

        viewModel.confirmDeleteAccount()
        advanceUntilIdle()

        // UI state updated to account deleted
        assertTrue(viewModel.uiState.value.isAccountDeleted)
        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        assertEquals("", viewModel.uiState.value.battleName)

        // DataStore reset
        val repoSettings = settingsRepository.getSettings()
        assertTrue((repoSettings as com.sanket_satpute_20.dailybattle.domain.result.DomainResult.Success).value.soundEnabled)
    }
}

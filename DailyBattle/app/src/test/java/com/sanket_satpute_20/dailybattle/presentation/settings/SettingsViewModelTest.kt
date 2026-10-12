package com.sanket_satpute_20.dailybattle.presentation.settings

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.settings.InMemorySettingsRepository
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.settings.DeleteAccountUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.GetSettingsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateHapticsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateNotificationsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateSoundUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UpdateBattleNameUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserRepository : UserRepository {
        var savedName: String? = "PlayerOne"
        override suspend fun saveBattleName(name: String) {
            savedName = name
        }
        override suspend fun getBattleName(): String? = savedName
    }

    private class FakeFriendRepository : FriendRepository {
        var returnCode: String = "ABC123"
        override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> = DomainResult.Failure(AppError.Domain)
        override suspend fun getFriend(userId: UserId, friendUserId: UserId): DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        override suspend fun sendFriendRequest(userId: UserId, friendUserId: UserId): DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        override suspend fun addFriendByBattleCode(userId: UserId, battleCode: String): DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        override suspend fun acceptFriendRequest(friendshipId: FriendshipId): DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        override suspend fun rejectFriendRequest(friendshipId: FriendshipId): DomainResult<Unit> = DomainResult.Failure(AppError.Domain)
        override suspend fun blockFriend(friendshipId: FriendshipId): DomainResult<Friend> = DomainResult.Failure(AppError.Domain)
        override suspend fun getBattleCode(userId: UserId): DomainResult<String> = DomainResult.Success(returnCode)
    }

    private lateinit var settingsRepository: InMemorySettingsRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var friendRepository: FakeFriendRepository

    private lateinit var getSettingsUseCase: GetSettingsUseCase
    private lateinit var updateSoundUseCase: UpdateSoundUseCase
    private lateinit var updateHapticsUseCase: UpdateHapticsUseCase
    private lateinit var updateNotificationsUseCase: UpdateNotificationsUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var updateBattleNameUseCase: UpdateBattleNameUseCase
    private lateinit var getUserBattleCodeUseCase: GetUserBattleCodeUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = InMemorySettingsRepository()
        userRepository = FakeUserRepository()
        friendRepository = FakeFriendRepository()

        getSettingsUseCase = GetSettingsUseCase(settingsRepository)
        updateSoundUseCase = UpdateSoundUseCase(settingsRepository)
        updateHapticsUseCase = UpdateHapticsUseCase(settingsRepository)
        updateNotificationsUseCase = UpdateNotificationsUseCase(settingsRepository)
        deleteAccountUseCase = DeleteAccountUseCase(settingsRepository)
        updateBattleNameUseCase = UpdateBattleNameUseCase(userRepository)
        getUserBattleCodeUseCase = GetUserBattleCodeUseCase(friendRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
    fun `initial load sets settings, battle name, and battle code`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.soundEnabled)
        assertTrue(state.hapticsEnabled)
        assertTrue(state.notificationsEnabled)
        assertEquals("PlayerOne", state.battleName)
        assertEquals("ABC123", state.battleCode)
        assertNull(state.error)
    }

    @Test
    fun `toggleSound updates sound setting`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleSound()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.soundEnabled)
        assertFalse(settingsRepository.settingsFlow.value.soundEnabled)

        viewModel.toggleSound()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.soundEnabled)
        assertTrue(settingsRepository.settingsFlow.value.soundEnabled)
    }

    @Test
    fun `toggleSound with persistence error surfaces error`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        settingsRepository.shouldFailUpdate = true
        viewModel.toggleSound()
        advanceUntilIdle()

        assertEquals("Failed to update sound setting.", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.soundEnabled)
    }

    @Test
    fun `toggleHaptics updates haptics setting`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleHaptics()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hapticsEnabled)
        assertFalse(settingsRepository.settingsFlow.value.hapticsEnabled)
    }

    @Test
    fun `toggleHaptics with persistence error surfaces error`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        settingsRepository.shouldFailUpdate = true
        viewModel.toggleHaptics()
        advanceUntilIdle()

        assertEquals("Failed to update haptics setting.", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.hapticsEnabled)
    }

    @Test
    fun `toggleNotifications updates notifications setting`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleNotifications()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.notificationsEnabled)
        assertFalse(settingsRepository.settingsFlow.value.notificationsEnabled)
    }

    @Test
    fun `toggleNotifications when permission unavailable surfaces error and does not enable`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setNotificationPermissionAvailable(false)
        assertFalse(viewModel.uiState.value.notificationsEnabled)

        viewModel.toggleNotifications()
        advanceUntilIdle()

        assertEquals("Notification permission is unavailable on this device.", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.notificationsEnabled)
    }

    @Test
    fun `editing battle name with valid input updates battle name`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.openDetailDialog(SettingsDetailDialog.EDIT_BATTLE_NAME)
        assertEquals(SettingsDetailDialog.EDIT_BATTLE_NAME, viewModel.uiState.value.activeDetailDialog)

        viewModel.onEditBattleNameChange("NewChampion")
        viewModel.saveBattleName()
        advanceUntilIdle()

        assertEquals("NewChampion", viewModel.uiState.value.battleName)
        assertEquals("NewChampion", userRepository.savedName)
        assertNull(viewModel.uiState.value.activeDetailDialog)
        assertNull(viewModel.uiState.value.editBattleNameError)
    }

    @Test
    fun `editing battle name with invalid input sets validation error`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.openDetailDialog(SettingsDetailDialog.EDIT_BATTLE_NAME)
        viewModel.onEditBattleNameChange("   ")
        viewModel.saveBattleName()
        advanceUntilIdle()

        assertEquals("PlayerOne", viewModel.uiState.value.battleName)
        assertEquals("Battle Name cannot be empty.", viewModel.uiState.value.editBattleNameError)
    }

    @Test
    fun `showing and dismissing delete confirmation dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)

        viewModel.showDeleteAccountConfirmation()
        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)

        viewModel.dismissDeleteAccountConfirmation()
        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun `confirming account deletion succeeds and marks isAccountDeleted`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showDeleteAccountConfirmation()
        viewModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAccountDeleted)
        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        assertFalse(viewModel.uiState.value.isDeletingAccount)
    }

    @Test
    fun `confirming account deletion with failure shows error allowing retry`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        settingsRepository.shouldFailDeletion = true
        viewModel.showDeleteAccountConfirmation()
        viewModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAccountDeleted)
        assertEquals("Account deletion failed. Tap to retry.", viewModel.uiState.value.error)

        // Retry after fixing condition
        settingsRepository.shouldFailDeletion = false
        viewModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAccountDeleted)
    }

    @Test
    fun `detail dialog navigation works properly`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.openDetailDialog(SettingsDetailDialog.PRIVACY_POLICY)
        assertEquals(SettingsDetailDialog.PRIVACY_POLICY, viewModel.uiState.value.activeDetailDialog)

        viewModel.closeDetailDialog()
        assertNull(viewModel.uiState.value.activeDetailDialog)

        viewModel.openDetailDialog(SettingsDetailDialog.TERMS)
        assertEquals(SettingsDetailDialog.TERMS, viewModel.uiState.value.activeDetailDialog)

        viewModel.closeDetailDialog()
        assertNull(viewModel.uiState.value.activeDetailDialog)

        viewModel.openDetailDialog(SettingsDetailDialog.ABOUT)
        assertEquals(SettingsDetailDialog.ABOUT, viewModel.uiState.value.activeDetailDialog)

        viewModel.closeDetailDialog()
        assertNull(viewModel.uiState.value.activeDetailDialog)
    }

    @Test
    fun `clearError clears error`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        settingsRepository.shouldFailUpdate = true
        viewModel.toggleSound()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error != null)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}

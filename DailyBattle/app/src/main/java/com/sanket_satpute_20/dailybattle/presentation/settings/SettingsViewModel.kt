package com.sanket_satpute_20.dailybattle.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.settings.DeleteAccountUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.GetSettingsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateHapticsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateNotificationsUseCase
import com.sanket_satpute_20.dailybattle.domain.settings.UpdateSoundUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UpdateBattleNameUseCase
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import com.sanket_satpute_20.dailybattle.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSoundUseCase: UpdateSoundUseCase,
    private val updateHapticsUseCase: UpdateHapticsUseCase,
    private val updateNotificationsUseCase: UpdateNotificationsUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val userRepository: UserRepository,
    private val updateBattleNameUseCase: UpdateBattleNameUseCase,
    private val getUserBattleCodeUseCase: GetUserBattleCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val settingsResult = getSettingsUseCase()
            val savedBattleName = userRepository.getBattleName() ?: "Player"
            val battleCodeResult = getUserBattleCodeUseCase.execute(UserId("current-user"))
            val battleCode = when (battleCodeResult) {
                is DomainResult.Success -> battleCodeResult.value
                is DomainResult.Failure -> "CODE12"
            }

            when (settingsResult) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            soundEnabled = settingsResult.value.soundEnabled,
                            hapticsEnabled = settingsResult.value.hapticsEnabled,
                            notificationsEnabled = settingsResult.value.notificationsEnabled,
                            battleName = savedBattleName,
                            battleCode = battleCode,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            battleName = savedBattleName,
                            battleCode = battleCode,
                            isLoading = false,
                            error = "Failed to load settings."
                        )
                    }
                }
            }
        }
    }

    fun toggleSound() {
        val current = _uiState.value.soundEnabled
        val newTarget = !current
        viewModelScope.launch {
            val result = updateSoundUseCase(newTarget)
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(soundEnabled = newTarget, error = null) }
                }
                is DomainResult.Failure -> {
                    _uiState.update { it.copy(error = "Failed to update sound setting.") }
                }
            }
        }
    }

    fun toggleHaptics() {
        val current = _uiState.value.hapticsEnabled
        val newTarget = !current
        viewModelScope.launch {
            val result = updateHapticsUseCase(newTarget)
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(hapticsEnabled = newTarget, error = null) }
                }
                is DomainResult.Failure -> {
                    _uiState.update { it.copy(error = "Failed to update haptics setting.") }
                }
            }
        }
    }

    fun toggleNotifications() {
        if (!_uiState.value.isNotificationPermissionAvailable) {
            _uiState.update { it.copy(error = "Notification permission is unavailable on this device.") }
            return
        }
        val current = _uiState.value.notificationsEnabled
        val newTarget = !current
        viewModelScope.launch {
            val result = updateNotificationsUseCase(newTarget)
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(notificationsEnabled = newTarget, error = null) }
                }
                is DomainResult.Failure -> {
                    _uiState.update { it.copy(error = "Failed to update notification setting.") }
                }
            }
        }
    }

    fun setNotificationPermissionAvailable(isAvailable: Boolean) {
        _uiState.update {
            it.copy(
                isNotificationPermissionAvailable = isAvailable,
                notificationsEnabled = if (!isAvailable) false else it.notificationsEnabled
            )
        }
    }

    fun openDetailDialog(dialog: SettingsDetailDialog) {
        _uiState.update {
            it.copy(
                activeDetailDialog = dialog,
                editBattleNameInput = if (dialog == SettingsDetailDialog.EDIT_BATTLE_NAME) it.battleName else "",
                editBattleNameError = null
            )
        }
    }

    fun closeDetailDialog() {
        _uiState.update {
            it.copy(
                activeDetailDialog = null,
                editBattleNameInput = "",
                editBattleNameError = null
            )
        }
    }

    fun onEditBattleNameChange(input: String) {
        _uiState.update {
            it.copy(
                editBattleNameInput = input,
                editBattleNameError = null
            )
        }
    }

    fun saveBattleName() {
        val input = _uiState.value.editBattleNameInput
        when (val validation = InputValidator.validateBattleName(input)) {
            is ValidationResult.Invalid -> {
                _uiState.update { it.copy(editBattleNameError = validation.reason) }
            }
            is ValidationResult.Valid -> {
                viewModelScope.launch {
                    val result = updateBattleNameUseCase(input)
                    when (result) {
                        is DomainResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    battleName = input.trim(),
                                    activeDetailDialog = null,
                                    editBattleNameError = null
                                )
                            }
                        }
                        is DomainResult.Failure -> {
                            _uiState.update { it.copy(editBattleNameError = "Failed to update Battle Name.") }
                        }
                    }
                }
            }
        }
    }

    fun showDeleteAccountConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, error = null) }
    }

    fun dismissDeleteAccountConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun confirmDeleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            val result = deleteAccountUseCase()
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            showDeleteConfirmDialog = false,
                            isAccountDeleted = true,
                            battleName = "",
                            battleCode = ""
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            error = "Account deletion failed. Tap to retry."
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

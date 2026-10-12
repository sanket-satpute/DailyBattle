package com.sanket_satpute_20.dailybattle.presentation.settings

enum class SettingsDetailDialog {
    EDIT_BATTLE_NAME,
    VIEW_BATTLE_CODE,
    PRIVACY_POLICY,
    TERMS,
    ABOUT
}

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isNotificationPermissionAvailable: Boolean = true,
    val battleName: String = "",
    val battleCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val activeDetailDialog: SettingsDetailDialog? = null,
    val editBattleNameInput: String = "",
    val editBattleNameError: String? = null
)

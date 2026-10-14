package com.sanket_satpute_20.dailybattle.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.components.DBFeedback
import com.sanket_satpute_20.dailybattle.design.components.DBFeedbackType
import com.sanket_satpute_20.dailybattle.design.components.DBTopBar
import com.sanket_satpute_20.dailybattle.design.radius.DBRadius
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onAccountDeleted()
        }
    }

    SettingsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onToggleSound = viewModel::toggleSound,
        onToggleHaptics = viewModel::toggleHaptics,
        onToggleNotifications = viewModel::toggleNotifications,
        onOpenDetailDialog = viewModel::openDetailDialog,
        onCloseDetailDialog = viewModel::closeDetailDialog,
        onEditBattleNameChange = viewModel::onEditBattleNameChange,
        onSaveBattleName = viewModel::saveBattleName,
        onShowDeleteConfirmation = viewModel::showDeleteAccountConfirmation,
        onDismissDeleteConfirmation = viewModel::dismissDeleteAccountConfirmation,
        onConfirmDeleteAccount = viewModel::confirmDeleteAccount,
        onClearError = viewModel::clearError
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleNotifications: () -> Unit,
    onOpenDetailDialog: (SettingsDetailDialog) -> Unit,
    onCloseDetailDialog: () -> Unit,
    onEditBattleNameChange: (String) -> Unit,
    onSaveBattleName: () -> Unit,
    onShowDeleteConfirmation: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DBColor.BackgroundPrimary)
    ) {
        DBTopBar(
            title = "SETTINGS",
            onBackClick = onNavigateBack
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DBSpacing.MD),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DBColor.BrandPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = DBSpacing.MD, vertical = DBSpacing.SM)
            ) {
                if (uiState.error != null) {
                    DBFeedback(
                        message = uiState.error,
                        type = DBFeedbackType.Error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearError() }
                            .padding(bottom = DBSpacing.MD)
                    )
                }

                // --- GROUP 1: GAME ---
                SettingsGroupHeader(title = "GAME")
                SettingsGroupCard {
                    SettingsToggleRow(
                        label = "Sound",
                        enabled = uiState.soundEnabled,
                        onToggle = onToggleSound
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        label = "Haptics",
                        enabled = uiState.hapticsEnabled,
                        onToggle = onToggleHaptics
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        label = "Notifications",
                        enabled = uiState.notificationsEnabled,
                        onToggle = onToggleNotifications,
                        subtitle = if (!uiState.isNotificationPermissionAvailable) "Permission unavailable" else null
                    )
                }

                Spacer(modifier = Modifier.height(DBSpacing.LG))

                // --- GROUP 2: ACCOUNT ---
                SettingsGroupHeader(title = "ACCOUNT")
                SettingsGroupCard {
                    SettingsNavigationRow(
                        label = "Battle Name",
                        value = uiState.battleName,
                        onClick = { onOpenDetailDialog(SettingsDetailDialog.EDIT_BATTLE_NAME) }
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        label = "Battle Code",
                        value = uiState.battleCode,
                        onClick = { onOpenDetailDialog(SettingsDetailDialog.VIEW_BATTLE_CODE) }
                    )
                }

                Spacer(modifier = Modifier.height(DBSpacing.LG))

                // --- GROUP 3: PRIVACY ---
                SettingsGroupHeader(title = "PRIVACY")
                SettingsGroupCard {
                    SettingsNavigationRow(
                        label = "Privacy Policy",
                        onClick = { onOpenDetailDialog(SettingsDetailDialog.PRIVACY_POLICY) }
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        label = "Delete Account",
                        isDestructive = true,
                        onClick = onShowDeleteConfirmation
                    )
                }

                Spacer(modifier = Modifier.height(DBSpacing.LG))

                // --- GROUP 4: ABOUT ---
                SettingsGroupHeader(title = "ABOUT")
                SettingsGroupCard {
                    SettingsNavigationRow(
                        label = "Terms",
                        onClick = { onOpenDetailDialog(SettingsDetailDialog.TERMS) }
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        label = "About Daily Battle",
                        onClick = { onOpenDetailDialog(SettingsDetailDialog.ABOUT) }
                    )
                }

                Spacer(modifier = Modifier.height(DBSpacing.XL))
            }
        }
    }

    // --- DIALOGS ---

    // 1. Delete Account Confirmation Dialog
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirmation,
            title = {
                Text(
                    text = "DELETE ACCOUNT",
                    style = DBTypography.H2,
                    color = DBColor.Error,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Text(
                    text = "This will permanently delete your account, your Battle Name, and all gameplay history. This action cannot be undone.",
                    style = DBTypography.Body,
                    color = DBColor.TextPrimary
                )
            },
            confirmButton = {
                DBButton(
                    text = if (uiState.isDeletingAccount) "DELETING..." else "CONFIRM DELETE",
                    onClick = onConfirmDeleteAccount,
                    enabled = !uiState.isDeletingAccount,
                    isLoading = uiState.isDeletingAccount,
                    type = DBButtonType.Primary
                )
            },
            dismissButton = {
                DBButton(
                    text = "CANCEL",
                    onClick = onDismissDeleteConfirmation,
                    enabled = !uiState.isDeletingAccount,
                    type = DBButtonType.Ghost
                )
            },
            containerColor = DBColor.Surface1,
            shape = RoundedCornerShape(DBRadius.Large)
        )
    }

    // 2. Edit Battle Name Dialog
    if (uiState.activeDetailDialog == SettingsDetailDialog.EDIT_BATTLE_NAME) {
        AlertDialog(
            onDismissRequest = onCloseDetailDialog,
            title = {
                Text(
                    text = "EDIT BATTLE NAME",
                    style = DBTypography.H2,
                    color = DBColor.TextPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Column {
                    Text(
                        text = "1–20 characters. This identity is visible to rivals and friends.",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(DBSpacing.MD))
                    OutlinedTextField(
                        value = uiState.editBattleNameInput,
                        onValueChange = onEditBattleNameChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSaveBattleName() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DBColor.BrandPrimary,
                            unfocusedBorderColor = DBColor.Border,
                            focusedTextColor = DBColor.TextPrimary,
                            unfocusedTextColor = DBColor.TextPrimary,
                            cursorColor = DBColor.BrandPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.editBattleNameError != null) {
                        Spacer(modifier = Modifier.height(DBSpacing.XS))
                        Text(
                            text = uiState.editBattleNameError,
                            style = DBTypography.Caption,
                            color = DBColor.Error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSaveBattleName) {
                    Text(
                        text = "SAVE",
                        style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
                        color = DBColor.BrandPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseDetailDialog) {
                    Text(
                        text = "CANCEL",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                }
            },
            containerColor = DBColor.Surface1,
            shape = RoundedCornerShape(DBRadius.Large)
        )
    }

    // 3. View Battle Code Dialog
    if (uiState.activeDetailDialog == SettingsDetailDialog.VIEW_BATTLE_CODE) {
        AlertDialog(
            onDismissRequest = onCloseDetailDialog,
            title = {
                Text(
                    text = "BATTLE CODE",
                    style = DBTypography.H2,
                    color = DBColor.TextPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Share this 6-character code with friends to compete.",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(DBSpacing.LG))
                    Box(
                        modifier = Modifier
                            .background(DBColor.Surface2, RoundedCornerShape(DBRadius.Medium))
                            .padding(horizontal = DBSpacing.LG, vertical = DBSpacing.MD)
                    ) {
                        Text(
                            text = uiState.battleCode,
                            style = DBTypography.H1.copy(fontWeight = FontWeight.Bold),
                            color = DBColor.BrandPrimary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Battle Code", uiState.battleCode)
                        clipboard.setPrimaryClip(clip)
                        onCloseDetailDialog()
                    }
                ) {
                    Text(
                        text = "COPY CODE",
                        style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
                        color = DBColor.BrandPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseDetailDialog) {
                    Text(
                        text = "CLOSE",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                }
            },
            containerColor = DBColor.Surface1,
            shape = RoundedCornerShape(DBRadius.Large)
        )
    }

    // 4. Privacy Policy Dialog
    if (uiState.activeDetailDialog == SettingsDetailDialog.PRIVACY_POLICY) {
        AlertDialog(
            onDismissRequest = onCloseDetailDialog,
            title = {
                Text(
                    text = "PRIVACY POLICY",
                    style = DBTypography.H2,
                    color = DBColor.TextPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Daily Battle is built on a local-first privacy foundation.\n\n" +
                                "• No ad trackers or marketing SDKs.\n" +
                                "• No contact harvesting or address book access.\n" +
                                "• No location tracking.\n" +
                                "• Your preferences and historical battle metrics remain strictly under your control.\n\n" +
                                "Account deletion immediately purges your local and synchronized profile data.",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onCloseDetailDialog) {
                    Text(
                        text = "CLOSE",
                        style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
                        color = DBColor.BrandPrimary
                    )
                }
            },
            containerColor = DBColor.Surface1,
            shape = RoundedCornerShape(DBRadius.Large)
        )
    }

    // 5. Terms Dialog
    if (uiState.activeDetailDialog == SettingsDetailDialog.TERMS) {
        AlertDialog(
            onDismissRequest = onCloseDetailDialog,
            title = {
                Text(
                    text = "TERMS OF SERVICE",
                    style = DBTypography.H2,
                    color = DBColor.TextPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Daily Battle Rules of Play:\n\n" +
                                "• One Official Battle per calendar day.\n" +
                                "• Equal challenge sequence for all competing players.\n" +
                                "• Client anti-cheat and score integrity protections apply.\n" +
                                "• Practice battles are non-authoritative and do not affect official standing.\n\n" +
                                "Fair play and mutual competitive respect are required.",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onCloseDetailDialog) {
                    Text(
                        text = "CLOSE",
                        style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
                        color = DBColor.BrandPrimary
                    )
                }
            },
            containerColor = DBColor.Surface1,
            shape = RoundedCornerShape(DBRadius.Large)
        )
    }

    // 6. About Dialog
    if (uiState.activeDetailDialog == SettingsDetailDialog.ABOUT) {
        AlertDialog(
            onDismissRequest = onCloseDetailDialog,
            title = {
                Text(
                    text = "ABOUT DAILY BATTLE",
                    style = DBTypography.H2,
                    color = DBColor.TextPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = {
                Column {
                    Text(
                        text = "Daily Battle v1.0",
                        style = DBTypography.H3,
                        color = DBColor.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(DBSpacing.SM))
                    Text(
                        text = "The focused, 3-minute daily competitive ritual.\n" +
                                "Speed (Snap), Memory (Shift), and People (Crowd Call).\n\n" +
                                "Engineered with precision, restrained aesthetics, and authoritative scoring.",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onCloseDetailDialog) {
                    Text(
                        text = "CLOSE",
                        style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
                        color = DBColor.BrandPrimary
                    )
                }
            },
            containerColor = DBColor.Surface1,
            shape = RoundedCornerShape(DBRadius.Large)
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = DBTypography.Caption.copy(fontWeight = FontWeight.Bold),
        color = DBColor.TextSecondary,
        modifier = Modifier.padding(start = DBSpacing.XS, bottom = DBSpacing.XS)
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DBColor.Surface1, RoundedCornerShape(DBRadius.Medium))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    subtitle: String? = null
) {
    val stateText = if (enabled) "ON" else "OFF"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = DBSpacing.MD, vertical = DBSpacing.SM)
            .semantics {
                contentDescription = "$label, $stateText, toggle"
                role = Role.Switch
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = DBTypography.Body,
                color = DBColor.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = DBTypography.Caption,
                    color = DBColor.Error
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stateText,
                style = DBTypography.Caption.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) DBColor.BrandPrimary else DBColor.TextSecondary,
                modifier = Modifier.padding(end = DBSpacing.SM)
            )
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DBColor.BrandPrimary,
                    checkedTrackColor = DBColor.BrandPrimary.copy(alpha = 0.5f),
                    uncheckedThumbColor = DBColor.TextSecondary,
                    uncheckedTrackColor = DBColor.Surface2
                )
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    label: String,
    value: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = DBSpacing.MD, vertical = DBSpacing.MD)
            .semantics {
                val valuePart = if (value != null) ", $value" else ""
                contentDescription = "$label$valuePart, navigate"
                role = Role.Button
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = DBTypography.Body,
            color = if (isDestructive) DBColor.Error else DBColor.TextPrimary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    style = DBTypography.Body,
                    color = DBColor.TextSecondary,
                    modifier = Modifier.padding(end = DBSpacing.SM)
                )
            }
            Text(
                text = ">",
                style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
                color = if (isDestructive) DBColor.Error else DBColor.TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = DBColor.Border.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = DBSpacing.MD)
    )
}

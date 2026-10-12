package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.UserSettingsEntity
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings

/**
 * Bidirectional mapper between [UserSettingsEntity] and [UserSettings].
 */
fun UserSettingsEntity.toDomain(): UserSettings = UserSettings(
    soundEnabled = soundEnabled,
    hapticsEnabled = hapticsEnabled,
    notificationsEnabled = notificationsEnabled,
)

fun UserSettings.toEntity(
    userId: String = "local_user",
    updatedAt: Long = 0L,
): UserSettingsEntity = UserSettingsEntity(
    userId = userId,
    soundEnabled = soundEnabled,
    hapticsEnabled = hapticsEnabled,
    notificationsEnabled = notificationsEnabled,
    updatedAt = updatedAt,
)

package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing user preferences.
 *
 * Implements the local persistence representation for user settings
 * per `08_DATA_AND_API.md` §41 and `10_ANIMATION_HAPTICS.md` §52, §56.
 *
 * Fields:
 * - [userId]               — associated user ID (defaults to "local_user")
 * - [soundEnabled]         — whether sound playback is enabled (default true)
 * - [hapticsEnabled]       — whether haptic feedback is enabled (default true)
 * - [notificationsEnabled] — whether daily reminder notifications are enabled (default true)
 * - [updatedAt]            — last update timestamp (epoch ms)
 */
data class UserSettingsEntity(
    val userId: String = "local_user",
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val updatedAt: Long = 0L,
)

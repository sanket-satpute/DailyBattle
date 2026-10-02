package com.sanket_satpute_20.dailybattle.domain.user

/**
 * Domain representation of user-controlled application preferences.
 *
 * Per `08_DATA_AND_API.md` §41, settings are primarily local preferences:
 * - soundEnabled
 * - hapticsEnabled
 * - notificationsEnabled
 *
 * Per `10_ANIMATION_HAPTICS.md` §52 / §56, the user may disable haptics and
 * sound through Settings. These flags must be respected by the entire app.
 *
 * The exact full settings list follows the approved Settings specification
 * (Sprint 14.5). This model contains only the fields explicitly defined in
 * `08_DATA_AND_API.md` §41. Do not add theme switching in MVP unless the
 * product specification approves it.
 */
data class UserSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
)

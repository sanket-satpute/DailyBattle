package com.sanket_satpute_20.dailybattle.domain.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class UserSettingsTest {

    @Test
    fun `default settings have all preferences enabled`() {
        val settings = UserSettings()
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun `settings can be created with all disabled`() {
        val settings = UserSettings(
            soundEnabled = false,
            hapticsEnabled = false,
            notificationsEnabled = false,
        )
        assertFalse(settings.soundEnabled)
        assertFalse(settings.hapticsEnabled)
        assertFalse(settings.notificationsEnabled)
    }

    @Test
    fun `settings copy preserves unchanged fields`() {
        val original = UserSettings()
        val updated = original.copy(soundEnabled = false)
        assertFalse(updated.soundEnabled)
        assertTrue(updated.hapticsEnabled)
        assertTrue(updated.notificationsEnabled)
    }

    @Test
    fun `settings with different values are not equal`() {
        val a = UserSettings(soundEnabled = true)
        val b = UserSettings(soundEnabled = false)
        assertNotEquals(a, b)
    }

    @Test
    fun `settings equality is structural`() {
        val a = UserSettings()
        val b = UserSettings()
        assertEquals(a, b)
    }

    @Test
    fun `each preference can be independently toggled`() {
        val base = UserSettings()

        val soundOff = base.copy(soundEnabled = false)
        assertFalse(soundOff.soundEnabled)
        assertTrue(soundOff.hapticsEnabled)
        assertTrue(soundOff.notificationsEnabled)

        val hapticsOff = base.copy(hapticsEnabled = false)
        assertTrue(hapticsOff.soundEnabled)
        assertFalse(hapticsOff.hapticsEnabled)
        assertTrue(hapticsOff.notificationsEnabled)

        val notificationsOff = base.copy(notificationsEnabled = false)
        assertTrue(notificationsOff.soundEnabled)
        assertTrue(notificationsOff.hapticsEnabled)
        assertFalse(notificationsOff.notificationsEnabled)
    }
}

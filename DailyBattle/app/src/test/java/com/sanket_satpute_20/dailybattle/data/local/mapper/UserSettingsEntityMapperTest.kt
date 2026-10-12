package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.UserSettingsEntity
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSettingsEntityMapperTest {

    @Test
    fun `toDomain maps all UserSettingsEntity preferences accurately`() {
        val entity = UserSettingsEntity(
            userId = "user_me",
            soundEnabled = false,
            hapticsEnabled = true,
            notificationsEnabled = false,
            updatedAt = 1700000000000L,
        )

        val domain = entity.toDomain()

        assertFalse(domain.soundEnabled)
        assertTrue(domain.hapticsEnabled)
        assertFalse(domain.notificationsEnabled)
    }

    @Test
    fun `toEntity maps all UserSettings domain fields accurately with defaults`() {
        val domain = UserSettings(
            soundEnabled = true,
            hapticsEnabled = false,
            notificationsEnabled = true,
        )

        val entity = domain.toEntity(userId = "user_custom", updatedAt = 1700000050000L)

        assertEquals("user_custom", entity.userId)
        assertTrue(entity.soundEnabled)
        assertFalse(entity.hapticsEnabled)
        assertTrue(entity.notificationsEnabled)
        assertEquals(1700000050000L, entity.updatedAt)
    }

    @Test
    fun `roundtrip preserves UserSettings preferences`() {
        val original = UserSettings(
            soundEnabled = false,
            hapticsEnabled = false,
            notificationsEnabled = false,
        )

        val roundtrip = original.toEntity().toDomain()

        assertEquals(original, roundtrip)
    }
}

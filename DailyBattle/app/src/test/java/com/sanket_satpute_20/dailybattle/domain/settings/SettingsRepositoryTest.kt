package com.sanket_satpute_20.dailybattle.domain.settings

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.settings.InMemorySettingsRepository
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var repository: InMemorySettingsRepository

    @Before
    fun setup() {
        repository = InMemorySettingsRepository()
    }

    @Test
    fun `default settings are sound true, haptics true, notifications true`() = runTest {
        val result = repository.getSettings()
        assertTrue(result is DomainResult.Success)
        val settings = (result as DomainResult.Success).value
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun `updateSound updates state successfully`() = runTest {
        val result = repository.updateSound(false)
        assertTrue(result is DomainResult.Success)
        assertFalse(repository.settingsFlow.value.soundEnabled)
    }

    @Test
    fun `updateHaptics updates state successfully`() = runTest {
        val result = repository.updateHaptics(false)
        assertTrue(result is DomainResult.Success)
        assertFalse(repository.settingsFlow.value.hapticsEnabled)
    }

    @Test
    fun `updateNotifications updates state successfully`() = runTest {
        val result = repository.updateNotifications(false)
        assertTrue(result is DomainResult.Success)
        assertFalse(repository.settingsFlow.value.notificationsEnabled)
    }

    @Test
    fun `update failure returns Persistence error and does not change state`() = runTest {
        repository.shouldFailUpdate = true
        val result = repository.updateSound(false)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Persistence, (result as DomainResult.Failure).error)
        assertTrue(repository.settingsFlow.value.soundEnabled)
    }

    @Test
    fun `deleteAccount resets settings and succeeds`() = runTest {
        repository.updateSound(false)
        assertFalse(repository.settingsFlow.value.soundEnabled)

        val result = repository.deleteAccount()
        assertTrue(result is DomainResult.Success)
        assertTrue(repository.settingsFlow.value.soundEnabled)
    }

    @Test
    fun `deleteAccount failure returns Server error`() = runTest {
        repository.shouldFailDeletion = true
        val result = repository.deleteAccount()
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Server, (result as DomainResult.Failure).error)
    }
}

package com.sanket_satpute_20.dailybattle.integration

import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryUserDao
import com.sanket_satpute_20.dailybattle.presentation.home.HomeViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> HomeViewModel <-> RoomUserRepository <-> UserDao
 *
 * Verifies Sprint 23.3:
 * - Stored user in Room UserDao flows up through repository to populate HomeState.userName
 * - Absence of user in database safely falls back to default "Player"
 * - Offline and Error states correctly manipulate UI presentation flows
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userDao: InMemoryUserDao
    private lateinit var userRepository: RoomUserRepository

    @Before
    fun setup() {
        userDao = InMemoryUserDao()
        userRepository = RoomUserRepository(userDao)
    }

    @Test
    fun loadHomeDataWithUserInDatabasePopulatesUserNameAndReadyCard() = runTest {
        // Pre-populate UserDao with user record
        userDao.insertUser(
            UserEntity(
                userId = RoomUserRepository.LOCAL_USER_ID,
                battleName = "GladiatorPrime",
                accountStatus = "ACTIVE",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        val viewModel = HomeViewModel(userRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals("GladiatorPrime", state.userName)
        assertEquals(DBBattleCardState.Ready, state.battleState)
        assertEquals(3, state.challengesCount)
        assertEquals(3, state.estimatedMinutes)
        assertTrue(state.hasRival)
        assertEquals("Rahul", state.rivalName)
    }

    @Test
    fun loadHomeDataWithEmptyDatabaseFallsBackToDefaultPlayerName() = runTest {
        // Empty UserDao
        val viewModel = HomeViewModel(userRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Player", state.userName)
        assertEquals(DBBattleCardState.Ready, state.battleState)
    }

    @Test
    fun togglingOfflineAndErrorStatesUpdatesPresentationStateFlow() = runTest {
        val viewModel = HomeViewModel(userRepository)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isOffline)
        assertFalse(viewModel.state.value.isError)

        // User goes offline
        viewModel.setOffline(true)
        assertTrue(viewModel.state.value.isOffline)

        // Error occurs
        viewModel.setError(true, "Network timeout")
        assertTrue(viewModel.state.value.isError)
        assertEquals("Network timeout", viewModel.state.value.errorMessage)

        // Recovery
        viewModel.setError(false, null)
        assertFalse(viewModel.state.value.isError)
    }
}

package com.sanket_satpute_20.dailybattle.presentation.home

import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeUserRepository : UserRepository {
    var storedName: String? = null

    override suspend fun saveBattleName(name: String) {
        storedName = name
    }

    override suspend fun getBattleName(): String? = storedName
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: FakeUserRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = FakeUserRepository()
        userRepository.storedName = "Sanket"
        viewModel = HomeViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        val state = viewModel.state.value
        assertTrue(state.isLoading)
        assertEquals(DBBattleCardState.Loading, state.battleState)
    }

    @Test
    fun `loadHomeData populates state correctly`() = runTest {
        viewModel.loadHomeData()
        
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals("Sanket", state.userName)
        assertEquals(DBBattleCardState.Ready, state.battleState)
        assertEquals(3, state.challengesCount)
        assertEquals(3, state.estimatedMinutes)
        assertTrue(state.hasRival)
        assertEquals("Rahul", state.rivalName)
    }
}

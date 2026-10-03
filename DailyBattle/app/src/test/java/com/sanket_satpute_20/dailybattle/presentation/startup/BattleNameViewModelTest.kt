package com.sanket_satpute_20.dailybattle.presentation.startup

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
class BattleNameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: FakeUserRepository
    private lateinit var viewModel: BattleNameViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = FakeUserRepository()
        viewModel = BattleNameViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and invalid`() {
        val state = viewModel.state.value
        assertEquals("", state.name)
        assertFalse(state.isValid)
        assertFalse(state.isError)
    }

    @Test
    fun `valid name updates state correctly`() {
        viewModel.onNameChanged("Sanket")
        val state = viewModel.state.value
        assertEquals("Sanket", state.name)
        assertTrue(state.isValid)
        assertFalse(state.isError)
    }

    @Test
    fun `invalid name updates state correctly`() {
        viewModel.onNameChanged("ThisNameIsWayTooLongForTheSystem")
        val state = viewModel.state.value
        assertEquals("ThisNameIsWayTooLongForTheSystem", state.name)
        assertFalse(state.isValid)
        assertFalse(state.isError) // Typing doesn't trigger error state immediately
    }

    @Test
    fun `submitting valid name persists and sets success`() = runTest {
        viewModel.onNameChanged("Hero")
        viewModel.onSubmit()
        
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertFalse(state.isSubmitting)
        assertEquals("Hero", userRepository.storedName)
    }

    @Test
    fun `submitting empty name shows error`() = runTest {
        viewModel.onNameChanged("   ")
        viewModel.onSubmit()
        
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertTrue(state.isError)
        assertEquals("Battle Name cannot be empty.", state.errorMessage)
        assertFalse(state.isSuccess)
    }
}

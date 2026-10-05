package com.sanket_satpute_20.dailybattle.presentation.profile

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.GetProfileUseCase
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.profile.ProfileRepository
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProfileRepository : ProfileRepository {
        var shouldFail = false

        override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
            if (shouldFail) return DomainResult.Failure(AppError.Domain)
            return DomainResult.Success(
                Profile(
                    userId = userId,
                    battleName = "TestUser",
                    momentum = 5,
                    bestScore = 100,
                    averageScore = 50,
                    battleDNA = BattleDNA(90, 80, 70),
                    records = PersonalRecords(100, 5, 10)
                )
            )
        }
    }

    private lateinit var repo: FakeProfileRepository
    private lateinit var useCase: GetProfileUseCase
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeProfileRepository()
        useCase = GetProfileUseCase(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Success on successful load`() = runTest {
        viewModel = ProfileViewModel(useCase)
        
        // Before coroutines run, state is Loading
        assertEquals(ProfileUiState.Loading, viewModel.uiState.value)
        
        advanceUntilIdle()
        
        // After load, state should be Success
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals("TestUser", (state as ProfileUiState.Success).profile.battleName)
    }

    @Test
    fun `initial state is Loading and then Error on failed load`() = runTest {
        repo.shouldFail = true
        viewModel = ProfileViewModel(useCase)
        
        assertEquals(ProfileUiState.Loading, viewModel.uiState.value)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("Failed to load profile.", (state as ProfileUiState.Error).message)
    }
}

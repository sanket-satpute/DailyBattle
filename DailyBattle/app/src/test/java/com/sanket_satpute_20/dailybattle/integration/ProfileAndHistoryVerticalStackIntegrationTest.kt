package com.sanket_satpute_20.dailybattle.integration

import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.data.profile.RoomProfileRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.history.GetHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.GetProfileUseCase
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleResultDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryProfileDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryUserDao
import com.sanket_satpute_20.dailybattle.presentation.history.HistoryUiState
import com.sanket_satpute_20.dailybattle.presentation.history.HistoryViewModel
import com.sanket_satpute_20.dailybattle.presentation.profile.ProfileUiState
import com.sanket_satpute_20.dailybattle.presentation.profile.ProfileViewModel
import com.sanket_satpute_20.dailybattle.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * End-to-end Vertical Architecture Stack Integration Test:
 * UI Events / UI State <-> ProfileViewModel & HistoryViewModel <-> Use Cases <-> Repositories <-> Room DAOs
 *
 * Verifies Sprint 23.3:
 * - Profile entity in ProfileDao flows through RoomProfileRepository to ProfileUiState.Success
 * - Completed battles in BattleResultDao aggregate momentum, average, and best score in HistoryUiState.Success
 * - Zero completed battles cleanly transitions to HistoryUiState.Empty
 * - Missing profile returns domain failure transitioning to ProfileUiState.Error
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileAndHistoryVerticalStackIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var profileDao: InMemoryProfileDao
    private lateinit var profileRepository: RoomProfileRepository
    private lateinit var getProfileUseCase: GetProfileUseCase

    private lateinit var battleResultDao: InMemoryBattleResultDao
    private lateinit var battleHistoryDao: InMemoryBattleHistoryDao
    private lateinit var battleSessionDao: InMemoryBattleSessionDao
    private lateinit var battleResultRepository: RoomBattleResultRepository
    private lateinit var userDao: InMemoryUserDao
    private lateinit var userRepository: RoomUserRepository
    private lateinit var getHistoryUseCase: GetHistoryUseCase
    private lateinit var testClock: Clock

    @Before
    fun setup() {
        profileDao = InMemoryProfileDao()
        profileRepository = RoomProfileRepository(profileDao)

        battleResultDao = InMemoryBattleResultDao()
        battleHistoryDao = InMemoryBattleHistoryDao()
        battleSessionDao = InMemoryBattleSessionDao()
        battleResultRepository = RoomBattleResultRepository(
            battleResultDao = battleResultDao,
            battleHistoryDao = battleHistoryDao,
            battleSessionDao = battleSessionDao,
            database = null
        )

        getProfileUseCase = GetProfileUseCase(profileRepository, battleResultRepository)

        userDao = InMemoryUserDao()
        userRepository = RoomUserRepository(userDao)

        // Fixed clock for deterministic history & momentum calculations
        testClock = Clock.fixed(Instant.parse("2026-10-13T12:00:00Z"), ZoneId.of("UTC"))
        getHistoryUseCase = GetHistoryUseCase(battleResultRepository, testClock)
    }

    @Test
    fun profileVerticalStackLoadsFromProfileDaoIntoUiState() = runTest {
        // Pre-insert Profile into ProfileDao
        profileRepository.saveProfile(
            Profile(
                userId = UserId("current-user"),
                battleName = "Spartan",
                momentum = 9,
                bestScore = 985,
                averageScore = 920,
                battleDNA = BattleDNA(speed = 94, memory = 90, people = 88),
                records = PersonalRecords(bestScore = 985, bestMomentum = 14, battlesPlayed = 45)
            )
        )
        // Pre-insert a completed battle result so GetProfileUseCase computes live BattleDNA
        battleResultDao.insertResult(
            BattleResultEntity(
                resultId = "dna-res-1",
                sessionId = "dna-sess-1",
                userId = "current-user",
                battleId = "b-dna-1",
                snapScore = 94,
                shiftScore = 90,
                crowdCallScore = 88,
                consistencyScore = 100,
                totalScore = 372,
                percentile = 90.0,
                completedAt = 1000L
            )
        )

        val viewModel = ProfileViewModel(getProfileUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State must be Success", state is ProfileUiState.Success)
        val profile = (state as ProfileUiState.Success).profile
        assertEquals("Spartan", profile.battleName)
        assertEquals(9, profile.momentum)
        assertEquals(985, profile.bestScore)
        assertEquals(920, profile.averageScore)
        assertEquals(94, profile.battleDNA.speed)
        assertEquals(14, profile.records.bestMomentum)
    }

    @Test
    fun profileVerticalStackReturnsErrorWhenProfileNotInDatabase() = runTest {
        // Empty ProfileDao
        val viewModel = ProfileViewModel(getProfileUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State must be Error when profile missing", state is ProfileUiState.Error)
        assertEquals("Failed to load profile.", (state as ProfileUiState.Error).message)
    }

    @Test
    fun historyVerticalStackAggregatesFromBattleResultDaoIntoUiState() = runTest {
        val userId = "current-user"
        userDao.insertUser(
            UserEntity(
                userId = RoomUserRepository.LOCAL_USER_ID,
                battleName = userId,
                createdAt = 1000L,
                updatedAt = 1000L,
                accountStatus = "ACTIVE"
            )
        )

        val todayEpoch = Instant.parse("2026-10-13T10:00:00Z").toEpochMilli()
        val yesterdayEpoch = Instant.parse("2026-10-12T10:00:00Z").toEpochMilli()

        // Insert two completed battles on consecutive days
        battleResultDao.insertResult(
            BattleResultEntity(
                resultId = "res-1",
                sessionId = "sess-1",
                userId = userId,
                battleId = "b-1",
                snapScore = 300,
                shiftScore = 300,
                crowdCallScore = 300,
                consistencyScore = 100,
                totalScore = 1000,
                percentile = 99.0,
                completedAt = todayEpoch
            )
        )
        battleResultDao.insertResult(
            BattleResultEntity(
                resultId = "res-2",
                sessionId = "sess-2",
                userId = userId,
                battleId = "b-2",
                snapScore = 280,
                shiftScore = 280,
                crowdCallScore = 260,
                consistencyScore = 80,
                totalScore = 900,
                percentile = 85.0,
                completedAt = yesterdayEpoch
            )
        )

        val viewModel = HistoryViewModel(getHistoryUseCase, userRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State must be Success", state is HistoryUiState.Success)
        val data = (state as HistoryUiState.Success).data
        assertEquals(1000, data.best)
        assertEquals(950, data.average) // (1000 + 900) / 2
        assertEquals(2, data.momentum) // 2 consecutive days
        assertEquals(2, data.recentBattles.size)
    }

    @Test
    fun historyVerticalStackEmitsEmptyStateWhenNoBattlesExistInDatabase() = runTest {
        val userId = "current-user"
        userDao.insertUser(
            UserEntity(
                userId = RoomUserRepository.LOCAL_USER_ID,
                battleName = userId,
                createdAt = 1000L,
                updatedAt = 1000L,
                accountStatus = "ACTIVE"
            )
        )

        // Empty BattleResultDao
        val viewModel = HistoryViewModel(getHistoryUseCase, userRepository)
        advanceUntilIdle()

        assertEquals(HistoryUiState.Empty, viewModel.uiState.value)
    }
}

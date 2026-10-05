package com.sanket_satpute_20.dailybattle.domain.profile

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetProfileUseCaseTest {

    private class FakeProfileRepository : ProfileRepository {
        var profileToReturn: Profile? = null

        override suspend fun getProfile(userId: UserId): DomainResult<Profile> {
            return profileToReturn?.let { DomainResult.Success(it) }
                ?: DomainResult.Failure(AppError.Domain)
        }
    }

    private class FakeBattleResultRepository : BattleResultRepository {
        var resultsToReturn: List<BattleResult> = emptyList()

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Domain)

        override suspend fun getBattleResult(
            userId: UserId,
            battleId: BattleId
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Domain)

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            return DomainResult.Success(resultsToReturn)
        }
    }

    private lateinit var profileRepo: FakeProfileRepository
    private lateinit var battleRepo: FakeBattleResultRepository
    private lateinit var useCase: GetProfileUseCase

    private val testUserId = UserId("user-123")

    @Before
    fun setup() {
        profileRepo = FakeProfileRepository()
        battleRepo = FakeBattleResultRepository()
        useCase = GetProfileUseCase(profileRepo, battleRepo)

        profileRepo.profileToReturn = Profile(
            userId = testUserId,
            battleName = "Test",
            momentum = 0,
            bestScore = 0,
            averageScore = 0,
            battleDNA = BattleDNA(0, 0, 0), // Will be overwritten
            records = PersonalRecords(0, 0, 0)
        )
    }

    private fun createResult(snap: Int, shift: Int, crowd: Int): BattleResult {
        return BattleResult(
            resultId = BattleResultId("res-1"),
            userId = testUserId,
            battleId = BattleId("bat-1"),
            sessionId = BattleSessionId("ses-1"),
            snapScore = snap,
            shiftScore = shift,
            crowdCallScore = crowd,
            consistencyScore = 100,
            totalScore = snap + shift + crowd + 100,
            percentile = 50.0,
            completedAt = 12345L
        )
    }

    @Test
    fun `no completed battles results in unavailable DNA dimensions`() = runTest {
        battleRepo.resultsToReturn = emptyList()

        val result = useCase.execute(testUserId)
        assertTrue(result is DomainResult.Success)
        val dna = (result as DomainResult.Success).value.battleDNA

        assertEquals(null, dna.speed)
        assertEquals(null, dna.memory)
        assertEquals(null, dna.people)
    }

    @Test
    fun `one completed battle DNA equals that battles scores`() = runTest {
        battleRepo.resultsToReturn = listOf(
            createResult(snap = 250, shift = 200, crowd = 150)
        )

        val result = useCase.execute(testUserId)
        val dna = (result as DomainResult.Success).value.battleDNA

        assertEquals(250, dna.speed)
        assertEquals(200, dna.memory)
        assertEquals(150, dna.people)
    }

    @Test
    fun `multiple completed battles results in arithmetic mean`() = runTest {
        battleRepo.resultsToReturn = listOf(
            createResult(snap = 300, shift = 100, crowd = 150),
            createResult(snap = 200, shift = 300, crowd = 50)
        )

        val result = useCase.execute(testUserId)
        val dna = (result as DomainResult.Success).value.battleDNA

        // Speed = (300 + 200) / 2 = 250
        // Memory = (100 + 300) / 2 = 200
        // People = (150 + 50) / 2 = 100
        assertEquals(250, dna.speed)
        assertEquals(200, dna.memory)
        assertEquals(100, dna.people)
    }

    @Test
    fun `zero valued authoritative score remains a valid score`() = runTest {
        battleRepo.resultsToReturn = listOf(
            createResult(snap = 0, shift = 0, crowd = 0),
            createResult(snap = 100, shift = 100, crowd = 100)
        )

        val result = useCase.execute(testUserId)
        val dna = (result as DomainResult.Success).value.battleDNA

        assertEquals(50, dna.speed)
        assertEquals(50, dna.memory)
        assertEquals(50, dna.people)
    }

    @Test
    fun `dimensions are calculated independently without contamination`() = runTest {
        battleRepo.resultsToReturn = listOf(
            createResult(snap = 300, shift = 0, crowd = 0),
            createResult(snap = 300, shift = 0, crowd = 0)
        )

        val result = useCase.execute(testUserId)
        val dna = (result as DomainResult.Success).value.battleDNA

        assertEquals(300, dna.speed)
        assertEquals(0, dna.memory)
        assertEquals(0, dna.people)
    }
}

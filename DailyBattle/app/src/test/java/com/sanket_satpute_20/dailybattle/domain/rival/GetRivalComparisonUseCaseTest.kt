package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.core.error.AppError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetRivalComparisonUseCaseTest {

    private class FakeRivalRepository : RivalRepository {
        var comparisonToReturn: DomainResult<RivalScoreComparison> = DomainResult.Failure(AppError.Domain)

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
            return DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison> {
            return comparisonToReturn
        }

        override suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>> {
            return DomainResult.Failure(AppError.Domain)
        }
    }

    private val fakeRepository = FakeRivalRepository()
    private val useCase = GetRivalComparisonUseCase(fakeRepository)

    @Test
    fun `returns comparison when found`() = runTest {
        val comparison = RivalScoreComparison(
            userScore = 901,
            rivalScore = 914,
            scoreGap = 13,
            battleId = BattleId("b-1"),
            direction = ScoreDirection.BEHIND
        )
        fakeRepository.comparisonToReturn = DomainResult.Success(comparison)

        val result = useCase.execute(UserId("u-1"), UserId("u-2"), BattleId("b-1"))

        assertEquals(DomainResult.Success(comparison), result)
    }

    @Test
    fun `returns failure when not found`() = runTest {
        fakeRepository.comparisonToReturn = DomainResult.Failure(AppError.Domain)

        val result = useCase.execute(UserId("u-1"), UserId("u-2"), BattleId("b-1"))

        assertEquals(DomainResult.Failure(AppError.Domain), result)
    }
}

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
class GetRivalMatchHistoryUseCaseTest {

    private class FakeRivalRepository : RivalRepository {
        var historyToReturn: List<RivalMatchHistoryItem> = emptyList()

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
            return DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison> {
            return DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>> {
            return DomainResult.Success(historyToReturn)
        }
    }

    private val fakeRepository = FakeRivalRepository()
    private val useCase = GetRivalMatchHistoryUseCase(fakeRepository)

    @Test
    fun `returns history`() = runTest {
        val history = listOf(
            RivalMatchHistoryItem(
                battleId = BattleId("b-1"),
                date = 1000L,
                userScore = 901,
                rivalScore = 914,
                outcome = MatchOutcome.LOSS
            )
        )
        fakeRepository.historyToReturn = history

        val result = useCase.execute(UserId("u-1"), UserId("u-2"))

        assertEquals(DomainResult.Success(history), result)
    }

    @Test
    fun `returns empty list when no history exists`() = runTest {
        fakeRepository.historyToReturn = emptyList()

        val result = useCase.execute(UserId("u-1"), UserId("u-2"))

        assertEquals(DomainResult.Success(emptyList<RivalMatchHistoryItem>()), result)
    }
}

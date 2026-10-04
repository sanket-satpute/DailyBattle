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
class GetCurrentRivalUseCaseTest {

    private class FakeRivalRepository : RivalRepository {
        var rivalToReturn: Rival? = null

        override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
            return DomainResult.Success(rivalToReturn)
        }

        override suspend fun getRivalComparison(userId: UserId, rivalId: UserId, battleId: BattleId): DomainResult<RivalScoreComparison> {
            return DomainResult.Failure(AppError.Domain)
        }

        override suspend fun getRivalMatchHistory(userId: UserId, rivalId: UserId): DomainResult<List<RivalMatchHistoryItem>> {
            return DomainResult.Failure(AppError.Domain)
        }
    }

    private val fakeRepository = FakeRivalRepository()
    private val useCase = GetCurrentRivalUseCase(fakeRepository)

    @Test
    fun `returns rival when exists`() = runTest {
        val rival = Rival(
            userId = UserId("u-1"),
            rivalUserId = UserId("u-2"),
            selectedAt = 1000L,
            status = RivalStatus.ACTIVE
        )
        fakeRepository.rivalToReturn = rival

        val result = useCase.execute(UserId("u-1"))

        assertEquals(DomainResult.Success(rival), result)
    }

    @Test
    fun `returns null when no rival exists`() = runTest {
        fakeRepository.rivalToReturn = null

        val result = useCase.execute(UserId("u-1"))

        assertEquals(DomainResult.Success(null), result)
    }
}

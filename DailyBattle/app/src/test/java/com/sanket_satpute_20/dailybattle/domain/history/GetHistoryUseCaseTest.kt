package com.sanket_satpute_20.dailybattle.domain.history

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
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class GetHistoryUseCaseTest {

    private class FakeBattleResultRepository : BattleResultRepository {
        var results = mutableListOf<BattleResult>()
        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Domain)

        override suspend fun getBattleResult(
            userId: UserId,
            battleId: BattleId
        ): DomainResult<BattleResult> = DomainResult.Failure(AppError.Domain)

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
            return DomainResult.Success(results)
        }
    }

    private val fixedClock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneId.of("UTC"))
    private val repository = FakeBattleResultRepository()
    private val useCase = GetHistoryUseCase(repository, fixedClock)
    private val testUserId = UserId("user1")

    private fun buildResult(completedAtStr: String): BattleResult {
        return BattleResult(
            resultId = BattleResultId(UUID.randomUUID().toString()),
            userId = testUserId,
            battleId = BattleId(UUID.randomUUID().toString()),
            sessionId = BattleSessionId(UUID.randomUUID().toString()),
            snapScore = 100,
            shiftScore = 100,
            crowdCallScore = 100,
            consistencyScore = 100,
            totalScore = 400,
            percentile = 50.0,
            completedAt = Instant.parse(completedAtStr).toEpochMilli()
        )
    }

    @Test
    fun `1 - No completed official battle today yields 0 momentum`() = runTest {
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(0, result.value.momentum)
    }

    @Test
    fun `2 - First official battle today yields 1 momentum`() = runTest {
        repository.results.add(buildResult("2026-10-05T09:00:00Z")) // Today
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(1, result.value.momentum)
    }

    @Test
    fun `3 - Today + yesterday yields 2 momentum`() = runTest {
        repository.results.add(buildResult("2026-10-05T09:00:00Z")) // Today
        repository.results.add(buildResult("2026-10-04T09:00:00Z")) // Yesterday
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(2, result.value.momentum)
    }

    @Test
    fun `4 - Five consecutive qualifying days yields 5 momentum`() = runTest {
        repository.results.add(buildResult("2026-10-05T09:00:00Z"))
        repository.results.add(buildResult("2026-10-04T09:00:00Z"))
        repository.results.add(buildResult("2026-10-03T09:00:00Z"))
        repository.results.add(buildResult("2026-10-02T09:00:00Z"))
        repository.results.add(buildResult("2026-10-01T09:00:00Z"))
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(5, result.value.momentum)
    }

    @Test
    fun `5 - Missed day breaks streak`() = runTest {
        repository.results.add(buildResult("2026-10-05T09:00:00Z"))
        repository.results.add(buildResult("2026-10-04T09:00:00Z"))
        // Missing 2026-10-03
        repository.results.add(buildResult("2026-10-02T09:00:00Z"))
        repository.results.add(buildResult("2026-10-01T09:00:00Z"))
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(2, result.value.momentum)
    }

    @Test
    fun `6 - Today completed after an earlier historical streak yields 1 momentum`() = runTest {
        repository.results.add(buildResult("2026-10-05T09:00:00Z")) // Today
        // Missing yesterday and day before
        repository.results.add(buildResult("2026-10-01T09:00:00Z")) // earlier streak
        repository.results.add(buildResult("2026-09-30T09:00:00Z"))
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(1, result.value.momentum)
    }

    @Test
    fun `7 - Yesterday completed but today not completed yields 0 momentum`() = runTest {
        repository.results.add(buildResult("2026-10-04T09:00:00Z")) // Yesterday
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(0, result.value.momentum)
    }

    @Test
    fun `8 - Multiple completions on one day count as one qualifying day`() = runTest {
        repository.results.add(buildResult("2026-10-05T11:00:00Z")) // Today 1
        repository.results.add(buildResult("2026-10-05T09:00:00Z")) // Today 2
        repository.results.add(buildResult("2026-10-04T09:00:00Z")) // Yesterday
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(2, result.value.momentum)
    }

    @Test
    fun `13 - Deterministic repeated calculation`() = runTest {
        repository.results.add(buildResult("2026-10-05T09:00:00Z"))
        repository.results.add(buildResult("2026-10-04T09:00:00Z"))
        
        val result1 = useCase(testUserId) as DomainResult.Success
        val result2 = useCase(testUserId) as DomainResult.Success
        
        assertEquals(2, result1.value.momentum)
        assertEquals(2, result2.value.momentum)
        assertEquals(result1.value, result2.value)
    }

    @Test
    fun `14 - Calendar-day boundary behavior`() = runTest {
        // Test different timezone
        val tokyoClock = Clock.fixed(Instant.parse("2026-10-05T01:00:00Z"), ZoneId.of("Asia/Tokyo")) // It's 10:00 AM on Oct 5 in Tokyo
        val tokyoUseCase = GetHistoryUseCase(repository, tokyoClock)
        
        // A battle completed at 2026-10-04T16:00:00Z is Oct 5 01:00 AM in Tokyo
        repository.results.add(buildResult("2026-10-04T16:00:00Z")) 
        
        val result = tokyoUseCase(testUserId) as DomainResult.Success
        assertEquals(1, result.value.momentum) // Counts as today (Oct 5)
    }

    @Test
    fun `best and average are calculated correctly`() = runTest {
        val r1 = buildResult("2026-10-05T09:00:00Z").copy(totalScore = 600)
        val r2 = buildResult("2026-10-04T09:00:00Z").copy(totalScore = 800)
        val r3 = buildResult("2026-10-03T09:00:00Z").copy(totalScore = 700)
        repository.results.addAll(listOf(r1, r2, r3))
        
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(800, result.value.best)
        assertEquals(700, result.value.average)
    }

    @Test
    fun `graph data is chronological`() = runTest {
        val r1 = buildResult("2026-10-05T09:00:00Z") // newest
        val r2 = buildResult("2026-10-03T09:00:00Z") // oldest
        val r3 = buildResult("2026-10-04T09:00:00Z") // middle
        
        repository.results.addAll(listOf(r1, r2, r3))
        
        val result = useCase(testUserId) as DomainResult.Success
        assertEquals(r2.completedAt, result.value.graphData[0].completedAt)
        assertEquals(r3.completedAt, result.value.graphData[1].completedAt)
        assertEquals(r1.completedAt, result.value.graphData[2].completedAt)
    }
}

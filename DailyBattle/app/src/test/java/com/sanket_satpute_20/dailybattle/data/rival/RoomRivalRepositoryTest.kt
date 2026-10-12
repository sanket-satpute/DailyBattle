package com.sanket_satpute_20.dailybattle.data.rival

import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomRivalRepositoryTest {

    private lateinit var fakeRivalDao: FakeRivalDao
    private lateinit var fakeHistoryDao: FakeRivalMatchHistoryDao
    private lateinit var repository: RoomRivalRepository

    @Before
    fun setup() {
        fakeRivalDao = FakeRivalDao()
        fakeHistoryDao = FakeRivalMatchHistoryDao()
        repository = RoomRivalRepository(fakeRivalDao, fakeHistoryDao)
    }

    @Test
    fun getCurrentRivalReturnsNullWhenNotSet() = runTest {
        val result = repository.getCurrentRival(UserId("u1"))
        assertTrue(result is DomainResult.Success)
        assertNull((result as DomainResult.Success).value)
    }

    @Test
    fun saveRivalAndGetCurrentRival() = runTest {
        val rival = Rival(
            userId = UserId("u1"),
            rivalUserId = UserId("r1"),
            rivalName = "SpeedDemon",
            selectedAt = 1000L,
            status = RivalStatus.ACTIVE,
        )
        repository.saveRival(rival)

        val result = repository.getCurrentRival(UserId("u1"))
        assertTrue(result is DomainResult.Success)
        val retrieved = (result as DomainResult.Success).value
        assertNotNull(retrieved)
        assertEquals("SpeedDemon", retrieved?.rivalName)
    }

    @Test
    fun getRivalComparisonReturnsCorrectScores() = runTest {
        val rival = Rival(
            userId = UserId("u1"),
            rivalUserId = UserId("r1"),
            rivalName = "SpeedDemon",
            selectedAt = 1000L,
            status = RivalStatus.ACTIVE,
        )
        repository.saveRival(rival)

        val historyItem = RivalMatchHistoryItem(
            battleId = BattleId("b1"),
            date = 1000L,
            userScore = 850,
            rivalScore = 820,
            outcome = MatchOutcome.WIN,
        )
        repository.saveRivalMatchHistory(UserId("u1"), listOf(historyItem))

        val result = repository.getRivalComparison(UserId("u1"), UserId("r1"), BattleId("b1"))
        assertTrue(result is DomainResult.Success)
        val comparison = (result as DomainResult.Success).value
        assertEquals(850, comparison.userScore)
        assertEquals(820, comparison.rivalScore)
        assertEquals(30, comparison.scoreGap)
        assertEquals(ScoreDirection.AHEAD, comparison.direction)
        assertEquals(BattleId("b1"), comparison.battleId)
    }

    @Test
    fun getRivalMatchHistoryReturnsHistoryList() = runTest {
        val items = listOf(
            RivalMatchHistoryItem(BattleId("b1"), 1000L, 850, 820, MatchOutcome.WIN),
            RivalMatchHistoryItem(BattleId("b2"), 2000L, 700, 750, MatchOutcome.LOSS),
        )
        repository.saveRivalMatchHistory(UserId("u1"), items)

        val result = repository.getRivalMatchHistory(UserId("u1"), UserId("r1"))
        assertTrue(result is DomainResult.Success)
        assertEquals(2, (result as DomainResult.Success).value.size)
    }

    private class FakeRivalDao : RivalDao {
        val rivals = mutableMapOf<String, RivalEntity>()

        override suspend fun insertRival(rival: RivalEntity) {
            rivals[rival.userId] = rival
        }

        override suspend fun getRivalForUser(userId: String): RivalEntity? = rivals[userId]

        override fun observeRivalForUser(userId: String): Flow<RivalEntity?> = flowOf(rivals[userId])

        override suspend fun deleteRivalForUser(userId: String): Int {
            return if (rivals.remove(userId) != null) 1 else 0
        }
    }

    private class FakeRivalMatchHistoryDao : RivalMatchHistoryDao {
        val history = mutableListOf<RivalMatchHistoryEntity>()

        override suspend fun insertMatchHistory(items: List<RivalMatchHistoryEntity>) {
            history.addAll(items)
        }

        override suspend fun getMatchHistoryForUser(userId: String): List<RivalMatchHistoryEntity> = history

        override suspend fun clearMatchHistoryForUser(userId: String): Int {
            val count = history.size
            history.clear()
            return count
        }
    }
}

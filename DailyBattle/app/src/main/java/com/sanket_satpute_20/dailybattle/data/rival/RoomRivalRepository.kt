package com.sanket_satpute_20.dailybattle.data.rival

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.RivalRepository
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Local Room-backed implementation of [RivalRepository].
 *
 * Implements Sprint 15.3 repository integration connecting the rival
 * repository domain contract to the local Room storage layer.
 */
@Singleton
class RoomRivalRepository @Inject constructor(
    private val rivalDao: RivalDao,
    private val rivalMatchHistoryDao: RivalMatchHistoryDao,
) : RivalRepository {

    override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> {
        val entity = rivalDao.getRivalForUser(userId.value)
        return DomainResult.Success(entity?.toDomain())
    }

    override suspend fun getRivalComparison(
        userId: UserId,
        rivalId: UserId,
        battleId: BattleId,
    ): DomainResult<RivalScoreComparison> {
        val historyEntities = rivalMatchHistoryDao.getMatchHistoryForUser(userId.value)
        val matchItem = historyEntities.find { it.battleId == battleId.value }

        return if (matchItem != null) {
            val direction = when {
                matchItem.userScore > matchItem.rivalScore -> ScoreDirection.AHEAD
                matchItem.userScore < matchItem.rivalScore -> ScoreDirection.BEHIND
                else -> ScoreDirection.TIED
            }
            val comparison = RivalScoreComparison(
                userScore = matchItem.userScore,
                rivalScore = matchItem.rivalScore,
                scoreGap = abs(matchItem.userScore - matchItem.rivalScore),
                battleId = battleId,
                direction = direction,
            )
            DomainResult.Success(comparison)
        } else {
            DomainResult.Failure(AppError.Domain)
        }
    }

    override suspend fun getRivalMatchHistory(
        userId: UserId,
        rivalId: UserId,
    ): DomainResult<List<RivalMatchHistoryItem>> {
        val entities = rivalMatchHistoryDao.getMatchHistoryForUser(userId.value)
        return DomainResult.Success(entities.map { it.toDomain() })
    }

    suspend fun saveRival(rival: Rival) {
        rivalDao.insertRival(rival.toEntity())
    }

    suspend fun saveRivalMatchHistory(userId: UserId, items: List<RivalMatchHistoryItem>) {
        rivalMatchHistoryDao.insertMatchHistory(items.map { it.toEntity(userId.value) })
    }
}

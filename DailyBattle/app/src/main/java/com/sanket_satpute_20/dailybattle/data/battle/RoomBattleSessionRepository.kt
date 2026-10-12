package com.sanket_satpute_20.dailybattle.data.battle

import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.data.local.mapper.toEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local Room-backed implementation of [BattleSessionRepository].
 *
 * Implements Sprint 15.3 repository integration connecting the battle session
 * repository domain contract to the local Room storage layer.
 */
@Singleton
class RoomBattleSessionRepository @Inject constructor(
    private val battleSessionDao: BattleSessionDao,
    private val challengeSessionDao: ChallengeSessionDao,
) : BattleSessionRepository {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? = runBlocking(ioDispatcher) {
        val sessionEntity = battleSessionDao.getSession(
            userId = userId.value,
            battleId = battleId.value,
            mode = BattleMode.OFFICIAL.name,
        ) ?: return@runBlocking null

        val challengeEntities = challengeSessionDao.getChallengeSessionsForSession(sessionEntity.sessionId)
        sessionEntity.toDomain(challengeEntities.map { it.toDomain() })
    }

    override fun saveSession(session: BattleSession) = runBlocking(ioDispatcher) {
        battleSessionDao.insertSession(session.toEntity())
        if (session.challengeSessions.isNotEmpty()) {
            challengeSessionDao.insertChallengeSessions(session.challengeSessions.map { it.toEntity() })
        }
    }

    override fun getUnacknowledgedSessions(): List<BattleSession> = runBlocking(ioDispatcher) {
        val candidates = battleSessionDao.getSessionsByModeAndStatus(
            mode = BattleMode.OFFICIAL.name,
            status = BattleSessionStatus.IN_PROGRESS.name,
        )
        candidates.mapNotNull { sessionEntity ->
            val challengeEntities = challengeSessionDao.getChallengeSessionsForSession(sessionEntity.sessionId)
            val challenges = challengeEntities.map { it.toDomain() }
            if (challenges.size == 3 && challenges.all { it.status == ChallengeSessionStatus.COMPLETE }) {
                sessionEntity.toDomain(challenges)
            } else {
                null
            }
        }
    }
}

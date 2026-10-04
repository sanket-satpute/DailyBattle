package com.sanket_satpute_20.dailybattle.data.battle

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import javax.inject.Inject

class InMemoryBattleSessionRepository @Inject constructor() : BattleSessionRepository {
    private val sessions = mutableMapOf<Pair<String, String>, BattleSession>()

    override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
        return sessions[Pair(userId.value, battleId.value)]
    }

    override fun saveSession(session: BattleSession) {
        sessions[Pair(session.userId.value, session.battleId.value)] = session
    }

    override fun getUnacknowledgedSessions(): List<BattleSession> {
        return sessions.values.filter { session ->
            session.mode == com.sanket_satpute_20.dailybattle.domain.battle.BattleMode.OFFICIAL &&
            session.status == com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus.IN_PROGRESS &&
            session.challengeSessions.size == 3 &&
            session.challengeSessions.all { it.status == com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus.COMPLETE }
        }
    }
}

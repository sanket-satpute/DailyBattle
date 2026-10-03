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
}

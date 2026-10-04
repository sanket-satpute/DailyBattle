package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Interface for persisting and retrieving Battle Sessions.
 *
 * Enforces the integrity requirement (§18) where the storage layer
 * must support querying by (userId, battleId) to prevent duplicate
 * official attempts.
 */
interface BattleSessionRepository {
    /**
     * Retrieves an official BattleSession for the given user and battle, if one exists.
     * Used to enforce uniqueness constraints.
     */
    fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession?

    /**
     * Saves or updates a BattleSession in persistent storage.
     */
    fun saveSession(session: BattleSession)

    /**
     * Retrieves all official BattleSessions that have finished all challenges locally
     * but have not yet been successfully acknowledged by the server (i.e. status != COMPLETED).
     */
    fun getUnacknowledgedSessions(): List<BattleSession>
}

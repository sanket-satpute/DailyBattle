package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Interface for completing battles and retrieving official battle results.
 * 
 * Enforces the integrity requirement where the final BattleResult is generated
 * and returned by the authoritative backend, preventing the client from modifying
 * official scores (§27).
 */
interface BattleResultRepository {

    /**
     * Completes an official Battle Session on the backend.
     * 
     * @param sessionId The session being completed.
     * @param clientRequestId Idempotency key to handle duplicate requests safely.
     * @return The authoritative [BattleResult] on success, or a [DomainResult.Failure].
     */
    suspend fun completeBattle(
        sessionId: BattleSessionId, 
        clientRequestId: String
    ): DomainResult<BattleResult>

    /**
     * Retrieves an existing BattleResult.
     */
    suspend fun getBattleResult(
        userId: UserId, 
        battleId: BattleId
    ): DomainResult<BattleResult>

    /**
     * Retrieves all official BattleResults for a user.
     */
    suspend fun getUserBattleResults(
        userId: UserId
    ): DomainResult<List<BattleResult>>
}

package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to finalize a battle and receive the authoritative BattleResult.
 *
 * Requirements (Sprint 7.5):
 * - Aggregate results and finalize battle.
 * - Handles duplicate submission safely via idempotency key.
 * - Modifies session state upon success.
 */
class CompleteBattleUseCase @Inject constructor(
    private val resultRepository: BattleResultRepository,
    private val sessionRepository: BattleSessionRepository
) {
    suspend fun execute(session: BattleSession): DomainResult<BattleResult> {
        // Must be in a valid state to complete
        if (session.status == BattleSessionStatus.NOT_STARTED) {
            return DomainResult.Failure(AppError.Validation)
        }

        // Check if already completed locally
        if (session.status == BattleSessionStatus.COMPLETED) {
            // Already finalized, try to get existing result
            return resultRepository.getBattleResult(session.userId, session.battleId)
        }

        // Finalize battle
        // Ensure the idempotency key is derived from the session to safely recover without
        // creating duplicate official results on the backend if submission was interrupted.
        val idempotencyKey = session.sessionId.value
        val result = resultRepository.completeBattle(session.sessionId, idempotencyKey)

        if (result is DomainResult.Success) {
            // Update the session to COMPLETED locally
            val updatedSession = session.copy(
                status = BattleSessionStatus.COMPLETED,
                completedAt = result.value.completedAt,
                currentChallenge = null
            )
            sessionRepository.saveSession(updatedSession)
        }

        return result
    }
}

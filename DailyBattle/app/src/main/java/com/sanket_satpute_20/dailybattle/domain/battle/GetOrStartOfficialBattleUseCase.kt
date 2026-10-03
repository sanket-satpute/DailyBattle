package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.UUID

/**
 * Use case that enforces the "duplicate official attempts" integrity rule.
 * 
 * Verifies if an official attempt already exists for this (user, battle) pair.
 * If it does, and it's IN_PROGRESS, it returns the engine to resume.
 * If it does, and it's COMPLETED, it returns a domain failure (attempt integrity violation).
 * If it doesn't exist, it creates a new session and persists it.
 */
class GetOrStartOfficialBattleUseCase(
    private val repository: BattleSessionRepository
) {
    fun execute(userId: UserId, battleId: BattleId): DomainResult<BattleEngine> {
        val existingSession = repository.getOfficialSession(userId, battleId)

        if (existingSession != null) {
            if (existingSession.status == BattleSessionStatus.COMPLETED) {
                // Duplicate official attempt violation
                return DomainResult.Failure(AppError.Domain) 
            }
            // Resume existing in-progress session
            return DomainResult.Success(BattleEngine(existingSession))
        }

        // Create new session
        val newSession = BattleSession(
            sessionId = BattleSessionId(UUID.randomUUID().toString()),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList()
        )

        repository.saveSession(newSession)
        
        return DomainResult.Success(BattleEngine(newSession))
    }
}

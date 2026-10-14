package com.sanket_satpute_20.dailybattle.core.security.ownership

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Validates battle and session ownership rules per:
 * - `08_DATA_AND_API.md` §18 (Official Session Rules) & §19 (Practice Session Rules)
 * - `ROADMAP.md` Sprint 24.6 (Security — Battle Ownership)
 *
 * Invariants:
 * 1. Conceptual uniqueness constraint: (userId, battleId, OFFICIAL) must identify one official attempt (§18).
 * 2. Cross-user isolation: A user cannot perform actions on or complete another user's session.
 * 3. Mode integrity: Practice sessions cannot mutate or masquerade as official sessions.
 * 4. Result consistency: A battle result must match the user ID and battle ID of its generating session.
 */
class BattleOwnershipValidator {

    /**
     * Validates that the active caller owns the targeted [BattleSession].
     */
    fun validateSessionOwnership(
        session: BattleSession,
        callerUserId: UserId
    ): DomainResult<Unit> {
        return if (session.userId == callerUserId) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(AppError.Authorization)
        }
    }

    /**
     * Validates that a [BattleResult] belongs to the specified user and battle.
     */
    fun validateResultOwnership(
        result: BattleResult,
        expectedUserId: UserId,
        expectedBattleId: BattleId
    ): DomainResult<Unit> {
        if (result.userId != expectedUserId) {
            return DomainResult.Failure(AppError.Authorization)
        }
        if (result.battleId != expectedBattleId) {
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }

    /**
     * Validates that an official battle attempt is not duplicated.
     *
     * Per `08_DATA_AND_API.md` §18:
     * (userId, battleId, OFFICIAL) must identify exactly one official attempt.
     *
     * @param existingOfficialSessions Existing sessions for this user and battle.
     * @param targetMode The mode of the requested new session.
     */
    fun validateAttemptUniqueness(
        userId: UserId,
        battleId: BattleId,
        targetMode: BattleMode,
        existingOfficialSessions: List<BattleSession>
    ): DomainResult<Unit> {
        if (targetMode != BattleMode.OFFICIAL) {
            // Practice sessions may be repeated per approved rules
            return DomainResult.Success(Unit)
        }

        val hasExistingOfficial = existingOfficialSessions.any { session ->
            session.userId == userId &&
                    session.battleId == battleId &&
                    session.mode == BattleMode.OFFICIAL
        }

        return if (hasExistingOfficial) {
            DomainResult.Failure(AppError.BattleState)
        } else {
            DomainResult.Success(Unit)
        }
    }

    /**
     * Validates that a completed session matches the result metadata before finalizing.
     */
    fun validateSessionResultAlignment(
        session: BattleSession,
        result: BattleResult
    ): DomainResult<Unit> {
        if (session.sessionId != result.sessionId) {
            return DomainResult.Failure(AppError.Validation)
        }
        if (session.userId != result.userId) {
            return DomainResult.Failure(AppError.Authorization)
        }
        if (session.battleId != result.battleId) {
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }
}

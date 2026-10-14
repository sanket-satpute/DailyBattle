package com.sanket_satpute_20.dailybattle.core.security.score

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreConstants

/**
 * Enforces score integrity and authoritative source-of-truth rules per:
 * - `ROADMAP.md` line 1888: "Never trust the Android client as the sole authority for competitive score integrity."
 * - `08_DATA_AND_API.md` §27 (Score Integrity), §28 (Result Versioning), §66 (Score Range Validation), §67 (Anti-Cheat Boundary)
 * - `02_GAMEPLAY.md` §13 (Score Integrity)
 */
class ScoreAuthorityEnforcer {

    /**
     * Validates mathematical and range integrity of a [BattleResult].
     *
     * Invariants:
     * - Snap score in 0..300
     * - Shift score in 0..300
     * - Crowd Call score in 0..300
     * - Consistency score in 0..100
     * - Total score in 0..1000
     * - totalScore must equal snapScore + shiftScore + crowdCallScore + consistencyScore
     * - Percentile in 0.0..100.0
     */
    fun validateScoreIntegrity(result: BattleResult): DomainResult<Unit> {
        if (result.snapScore !in 0..300) return DomainResult.Failure(AppError.Validation)
        if (result.shiftScore !in 0..300) return DomainResult.Failure(AppError.Validation)
        if (result.crowdCallScore !in 0..300) return DomainResult.Failure(AppError.Validation)
        if (result.consistencyScore !in 0..ChallengeScoreConstants.MAX_CONSISTENCY_SCORE) {
            return DomainResult.Failure(AppError.Validation)
        }

        val expectedTotal = result.snapScore + result.shiftScore + result.crowdCallScore + result.consistencyScore
        if (result.totalScore != expectedTotal) {
            return DomainResult.Failure(AppError.Validation)
        }

        if (result.totalScore !in 0..ChallengeScoreConstants.MAX_TOTAL_SCORE) {
            return DomainResult.Failure(AppError.Validation)
        }

        if (result.percentile !in 0.0..100.0) {
            return DomainResult.Failure(AppError.Validation)
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Asserts whether a client-side mutation of an existing [BattleResult] is permissible.
     *
     * Per `08_DATA_AND_API.md` §27:
     * "The client must not allow arbitrary modification of totalScore, percentile, consistencyScore after official completion."
     *
     * If the existing result is [ScoreAuthorityState.SERVER_CONFIRMED], client modification of score
     * fields is strictly prohibited.
     */
    fun assertMutationAllowed(
        existingResult: BattleResult,
        existingAuthority: ScoreAuthorityState,
        proposedUpdate: BattleResult,
        isUpdateFromServer: Boolean = false
    ): DomainResult<Unit> {
        // If updating directly from an authoritative server response, allow server reconciliation
        if (isUpdateFromServer) {
            // Check result versioning / timestamps (§28): client must not overwrite newer data with stale data
            return if (proposedUpdate.completedAt >= existingResult.completedAt) {
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(AppError.Validation)
            }
        }

        // Client is attempting to modify the score locally
        if (existingAuthority == ScoreAuthorityState.SERVER_CONFIRMED) {
            val scoreModified = existingResult.totalScore != proposedUpdate.totalScore ||
                    existingResult.percentile != proposedUpdate.percentile ||
                    existingResult.consistencyScore != proposedUpdate.consistencyScore ||
                    existingResult.snapScore != proposedUpdate.snapScore ||
                    existingResult.shiftScore != proposedUpdate.shiftScore ||
                    existingResult.crowdCallScore != proposedUpdate.crowdCallScore

            if (scoreModified) {
                // Client cannot arbitrarily overwrite server-confirmed competitive scores
                return DomainResult.Failure(AppError.Authorization)
            }
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Resolves a conflict between a local cached result and a server-confirmed result.
     *
     * Principle: Backend is the sole arbiter of competitive scores.
     * Always returns the server-confirmed result if valid.
     */
    fun resolveAuthoritativeResult(
        localResult: BattleResult?,
        serverResult: BattleResult
    ): DomainResult<BattleResult> {
        val serverValidity = validateScoreIntegrity(serverResult)
        if (serverValidity is DomainResult.Failure) {
            return DomainResult.Failure(AppError.Validation)
        }

        // Server-confirmed result is authoritative over local candidate
        return DomainResult.Success(serverResult)
    }

    /**
     * Enforces the Practice Isolation Rule (Rule 26 & §19):
     * Practice data must NEVER be treated as an authoritative official result.
     */
    fun validateModeAuthority(mode: BattleMode): DomainResult<Unit> {
        return if (mode == BattleMode.OFFICIAL) {
            DomainResult.Success(Unit)
        } else {
            // Practice results cannot be officially confirmed or submitted as competitive scores
            DomainResult.Failure(AppError.Domain)
        }
    }
}

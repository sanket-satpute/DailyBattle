package com.sanket_satpute_20.dailybattle.core.security.validation

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreConstants

/**
 * Validates network payloads and parameters at the API security boundary per:
 * - `08_DATA_AND_API.md` §57 (Idempotency), §63 (Data Validation), §64 (Battle Validation),
 *   §65 (Result Validation), §66 (Score Range Validation)
 * - `09_OFFLINE_AND_ERROR_HANDLING.md` §39 (Validation Failure) & §65 (Validation Rule)
 *
 * Enforces that:
 * 1. Identifier parameters contain no path traversal, illegal control characters, or blank values.
 * 2. Mutation requests include a valid, non-blank `clientRequestId`.
 * 3. Challenge progression follows the mandatory sequence: Snap (1) -> Shift (2) -> Crowd Call (3).
 * 4. Component and total scores fall strictly within approved bounds (Snap 0-300, Shift 0-300,
 *    Crowd Call 0-300, Consistency 0-100, Total 0-1000).
 * 5. Corrupted, truncated, or impossible server responses are caught and rejected as [AppError.Validation].
 */
class ApiSecurityValidator {

    private val ILLEGAL_IDENTIFIER_CHARS = listOf("/", "\\", "..", "\u0000", "\n", "\r", "%")

    /**
     * Validates that an identifier (battleId, sessionId, userId, challengeId) is safe for URL embedding.
     */
    fun validateIdentifier(identifier: String?, fieldName: String = "id"): DomainResult<Unit> {
        if (identifier.isNullOrBlank()) {
            return DomainResult.Failure(AppError.Validation)
        }

        if (ILLEGAL_IDENTIFIER_CHARS.any { identifier.contains(it) }) {
            return DomainResult.Failure(AppError.Validation)
        }

        // Limit maximum reasonable ID length to prevent memory attacks
        if (identifier.length > MAX_IDENTIFIER_LENGTH) {
            return DomainResult.Failure(AppError.Validation)
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Validates that a mutation request possesses a valid idempotency key.
     */
    fun validateIdempotencyKey(clientRequestId: String?): DomainResult<Unit> {
        if (clientRequestId.isNullOrBlank()) {
            return DomainResult.Failure(AppError.Validation)
        }
        if (clientRequestId.length > MAX_IDEMPOTENCY_KEY_LENGTH) {
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }

    /**
     * Validates the official challenge sequence progression.
     * Order must be strictly: Snap (1) -> Shift (2) -> Crowd Call (3).
     */
    fun validateChallengeSequence(
        currentChallengeIndex: Int,
        targetChallengeType: ChallengeType
    ): DomainResult<Unit> {
        val expectedType = when (currentChallengeIndex) {
            1 -> ChallengeType.SNAP
            2 -> ChallengeType.SHIFT
            3 -> ChallengeType.CROWD_CALL
            else -> return DomainResult.Failure(AppError.Validation)
        }

        return if (targetChallengeType == expectedType) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(AppError.Validation)
        }
    }

    /**
     * Validates that a challenge score conforms to approved boundaries for its type.
     */
    fun validateChallengeScore(
        score: Int,
        challengeType: ChallengeType
    ): DomainResult<Unit> {
        val maxScore = ChallengeScoreConstants.maxScoreFor(challengeType)
        return if (score in ChallengeScoreConstants.MIN_SCORE..maxScore) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(AppError.Validation)
        }
    }

    /**
     * Validates an entire battle score breakdown against the approved mathematical bounds:
     * - Snap: 0..300
     * - Shift: 0..300
     * - Crowd Call: 0..300
     * - Consistency: 0..100
     * - Total: 0..1000
     * - Total must equal snap + shift + crowdCall + consistency
     */
    fun validateBattleScoreBreakdown(
        snapScore: Int,
        shiftScore: Int,
        crowdCallScore: Int,
        consistencyScore: Int,
        totalScore: Int,
        mode: BattleMode = BattleMode.OFFICIAL
    ): DomainResult<Unit> {
        if (snapScore !in 0..300) return DomainResult.Failure(AppError.Validation)
        if (shiftScore !in 0..300) return DomainResult.Failure(AppError.Validation)
        if (crowdCallScore !in 0..300) return DomainResult.Failure(AppError.Validation)

        val maxConsistency = if (mode == BattleMode.OFFICIAL) 100 else 0
        if (consistencyScore !in 0..maxConsistency) return DomainResult.Failure(AppError.Validation)

        val computedSum = snapScore + shiftScore + crowdCallScore + consistencyScore
        if (totalScore != computedSum) {
            return DomainResult.Failure(AppError.Validation)
        }

        if (totalScore !in 0..ChallengeScoreConstants.MAX_TOTAL_SCORE) {
            return DomainResult.Failure(AppError.Validation)
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Validates percentile rank received from the server.
     * Percentile is derived from population data and must be within [0.0, 100.0].
     */
    fun validatePercentile(percentile: Double): DomainResult<Unit> {
        return if (percentile in 0.0..100.0) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(AppError.Validation)
        }
    }

    companion object {
        const val MAX_IDENTIFIER_LENGTH = 128
        const val MAX_IDEMPOTENCY_KEY_LENGTH = 128
    }
}

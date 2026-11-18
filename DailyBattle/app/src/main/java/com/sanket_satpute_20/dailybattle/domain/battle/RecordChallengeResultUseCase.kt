package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeResultDao
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDeterministicEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreValidator
import javax.inject.Inject

/**
 * Reusable persistence use case for recording official challenge results into local database.
 *
 * Implements SPRINT-CORE-PERSISTENCE requirements:
 * 1. Maps [ChallengeResult] into [ChallengeResultEntity].
 * 2. Uses deterministic primary key: `resultId = "${sessionId}_${challengeId}"`.
 * 3. Preserves sessionId, challengeId, score, maxScore, timeSpentMs (via metadata), and completedAt.
 * 4. Enforces score validity and bounds [0, 300] using [ChallengeScoreValidator].
 * 5. Persists safely through [ChallengeResultDao].
 * 6. Guarantees idempotency for repeated submissions for the same session and challenge.
 */
class RecordChallengeResultUseCase(
    private val challengeResultDao: ChallengeResultDao,
    private val validator: ChallengeScoreValidator,
) {

    @Inject
    constructor(challengeResultDao: ChallengeResultDao) : this(
        challengeResultDao = challengeResultDao,
        validator = ChallengeScoreValidator(),
    )

    /**
     * Executes the challenge result validation and persistence flow.
     *
     * @param result The unvalidated/evaluated [ChallengeResult] from the challenge evaluator.
     * @param challengeType Optional explicit [ChallengeType] override; inferred from challengeId if null.
     * @param timeSpentMs Optional time spent in ms to preserve in result metadata.
     * @return [DomainResult.Success] containing the validated, persisted result, or [DomainResult.Failure].
     */
    suspend fun execute(
        result: ChallengeResult,
        challengeType: ChallengeType? = null,
        timeSpentMs: Long? = null,
    ): DomainResult<ChallengeResult> {
        if (result.sessionId.value.isBlank() || result.challengeId.value.isBlank()) {
            return DomainResult.Failure(AppError.Validation)
        }

        val resolvedType = challengeType ?: inferChallengeType(result.challengeId.value)
        val validatedResult = validator.validate(result, resolvedType)

        val updatedMetadata = if (timeSpentMs != null) {
            val existing = validatedResult.metadata ?: emptyMap()
            existing + ("timeSpentMs" to timeSpentMs.toString())
        } else {
            validatedResult.metadata
        }

        val deterministicResultId = "${validatedResult.sessionId.value}_${validatedResult.challengeId.value}"
        val finalResult = validatedResult.copy(
            resultId = ChallengeResultId(deterministicResultId),
            metadata = updatedMetadata,
        )

        return try {
            val entity = finalResult.toDeterministicEntity(deterministicResultId)
            challengeResultDao.insertResult(entity)
            DomainResult.Success(finalResult)
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Persistence)
        }
    }

    suspend operator fun invoke(
        result: ChallengeResult,
        challengeType: ChallengeType? = null,
        timeSpentMs: Long? = null,
    ): DomainResult<ChallengeResult> = execute(result, challengeType, timeSpentMs)

    private fun inferChallengeType(challengeId: String): ChallengeType {
        val lower = challengeId.lowercase()
        return when {
            lower.contains("snap") || lower == "c-1" || lower == "1" -> ChallengeType.SNAP
            lower.contains("shift") || lower == "c-2" || lower == "2" -> ChallengeType.SHIFT
            lower.contains("crowd") || lower == "c-3" || lower == "3" -> ChallengeType.CROWD_CALL
            else -> ChallengeType.SNAP
        }
    }
}

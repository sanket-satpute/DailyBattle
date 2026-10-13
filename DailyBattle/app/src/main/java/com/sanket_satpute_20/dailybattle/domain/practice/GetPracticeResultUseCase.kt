package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to retrieve an isolated PracticeResult by its result ID or session ID.
 *
 * Requirements:
 * - Sprint 19.3 — Practice Results: Practice results remain isolated.
 * - Handles the "No practice data" edge case cleanly with DomainResult.Failure(AppError.NotFound).
 */
class GetPracticeResultUseCase @Inject constructor(
    private val repository: PracticeResultRepository
) {
    fun execute(sessionId: PracticeSessionId): DomainResult<PracticeResult> {
        return repository.getPracticeResultBySessionId(sessionId)
    }

    fun execute(resultId: PracticeResultId): DomainResult<PracticeResult> {
        return repository.getPracticeResult(resultId)
    }
}

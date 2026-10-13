package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to retrieve the most recent PracticeResult for a given user.
 *
 * Requirements:
 * - Sprint 19.3 — Practice Results: Practice results remain isolated.
 * - Handles the "No practice data" edge case cleanly with DomainResult.Failure(AppError.NotFound).
 */
class GetLatestPracticeResultUseCase @Inject constructor(
    private val repository: PracticeResultRepository
) {
    fun execute(userId: UserId): DomainResult<PracticeResult> {
        return repository.getLatestPracticeResult(userId)
    }
}

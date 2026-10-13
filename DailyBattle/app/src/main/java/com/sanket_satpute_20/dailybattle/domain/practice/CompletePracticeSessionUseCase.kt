package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to finalize a practice session.
 *
 * Requirements:
 * - Handles the "Practice completed" edge case.
 * - Enforces Practice Isolation Rule (Rule 26, `08_DATA_AND_API.md` §19):
 *   Completing practice NEVER communicates with the official BattleResultRepository,
 *   never submits to the server, and never affects streaks, percentiles, or official history.
 */
class CompletePracticeSessionUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        return activePracticeManager.completePractice(now)
    }

    /**
     * Finalizes the practice session and returns the validated [PracticeResult].
     */
    fun completeAndGetResult(now: Long = System.currentTimeMillis()): DomainResult<PracticeResult> {
        val sessionResult = activePracticeManager.completePractice(now)
        if (sessionResult is DomainResult.Failure) {
            return sessionResult
        }
        val latest = activePracticeManager.getLatestPracticeResult()
        if (latest != null) {
            return DomainResult.Success(latest)
        }
        val session = (sessionResult as DomainResult.Success).value
        return session.toPracticeResult(completedAt = now)
    }
}

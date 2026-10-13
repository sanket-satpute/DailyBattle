package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to interrupt or abandon an in-progress practice session.
 *
 * Requirements:
 * - Handles the "Practice interrupted" edge case.
 * - Leaves official battle sessions completely unaffected.
 */
class InterruptPracticeSessionUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(
        reason: String? = null,
        now: Long = System.currentTimeMillis()
    ): DomainResult<PracticeSession> {
        return activePracticeManager.interruptPractice(reason, now)
    }
}

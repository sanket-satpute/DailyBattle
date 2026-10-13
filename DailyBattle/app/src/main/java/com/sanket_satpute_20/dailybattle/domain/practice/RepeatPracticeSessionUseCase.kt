package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to repeat a practice session.
 *
 * Requirements:
 * - Handles the "Practice repeated" edge case.
 * - Unlike official battles (which are strictly limited to one attempt per day),
 *   practice sessions can be repeated without limit.
 * - Increments repeatCount, resets challenge progress, preserves isolation.
 */
class RepeatPracticeSessionUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        return activePracticeManager.repeatPractice(now)
    }
}

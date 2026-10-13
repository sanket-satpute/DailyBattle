package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to start a new practice session.
 *
 * Requirements:
 * - Validates practice configuration (handling "Invalid practice configuration" edge case).
 * - Creates, initializes, and persists the separate [PracticeSession].
 * - Binds the session to [ActivePracticeSessionManager].
 * - Invariant: Does not alter official battle attempts or results.
 */
class StartPracticeSessionUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(
        userId: UserId,
        configuration: PracticeConfiguration = PracticeConfiguration.STANDARD,
        now: Long = System.currentTimeMillis()
    ): DomainResult<PracticeSession> {
        return activePracticeManager.startPractice(userId, configuration, now)
    }
}

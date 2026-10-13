package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import javax.inject.Inject

/**
 * Use case to retrieve the active practice session for a user.
 */
class GetActivePracticeSessionUseCase @Inject constructor(
    private val repository: PracticeSessionRepository,
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(userId: UserId): PracticeSession? {
        val managerSession = activePracticeManager.activeSession.value
        if (managerSession != null && managerSession.userId == userId) {
            return managerSession
        }
        return repository.getActivePracticeSession(userId)
    }
}

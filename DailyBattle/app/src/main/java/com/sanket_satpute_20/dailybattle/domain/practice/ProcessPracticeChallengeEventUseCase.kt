package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeEvent
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to dispatch an individual [ChallengeEvent] to the active practice session.
 *
 * Reuses the authoritative challenge infrastructure safely:
 * - Start -> Active
 * - SubmitCorrect -> Correct (immediate feedback per REQ-GAME-016)
 * - SubmitIncorrect -> Incorrect (immediate feedback per REQ-GAME-016)
 * - Continue -> Active
 * - Finish -> Complete
 */
class ProcessPracticeChallengeEventUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(event: ChallengeEvent): DomainResult<PracticeSession> {
        return activePracticeManager.processChallengeEvent(event)
    }
}

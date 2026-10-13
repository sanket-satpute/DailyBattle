package com.sanket_satpute_20.dailybattle.domain.practice

import javax.inject.Inject

/**
 * Use case to retrieve the current practice gameplay state.
 */
class GetPracticeGameplayStateUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(): PracticeGameplayState {
        val session = activePracticeManager.activeSession.value
        val totalChallenges = session?.configuration?.challengeTypes?.size ?: 3
        return PracticeGameplayState(
            session = session,
            currentChallengeIndex = session?.currentChallengeIndex,
            totalChallenges = totalChallenges,
            currentChallengeType = activePracticeManager.currentChallengeType.value,
            challengeState = activePracticeManager.currentChallengeState.value,
            challengeResults = activePracticeManager.challengeResults.value,
            aggregatedScore = activePracticeManager.aggregatedScore.value,
            isPracticeComplete = session?.isCompleted ?: false,
            isInterrupted = session?.isInterrupted ?: false
        )
    }
}

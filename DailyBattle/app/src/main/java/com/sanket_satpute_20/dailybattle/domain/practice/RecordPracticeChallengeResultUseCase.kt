package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Use case to validate and record a challenge score for a completed practice challenge.
 *
 * Reuses [com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreValidator] safely.
 * Invariant: Never writes to the official [com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository].
 */
class RecordPracticeChallengeResultUseCase @Inject constructor(
    private val activePracticeManager: ActivePracticeSessionManager,
) {
    fun execute(
        challengeType: ChallengeType,
        score: Int,
        metadata: Map<String, String>? = null
    ): DomainResult<ChallengeResult> {
        return activePracticeManager.recordPracticeChallengeScore(challengeType, score, metadata)
    }
}

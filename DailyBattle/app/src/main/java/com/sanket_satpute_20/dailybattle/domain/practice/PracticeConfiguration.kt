package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Configuration defining the parameters of a practice session.
 *
 * Supports edge case handling for "Invalid practice configuration" (ROADMAP Phase 19).
 */
data class PracticeConfiguration(
    val challengeTypes: List<ChallengeType> = listOf(
        ChallengeType.SNAP,
        ChallengeType.SHIFT,
        ChallengeType.CROWD_CALL
    ),
    val targetBattleId: BattleId? = null,
    val difficulty: PracticeDifficulty = PracticeDifficulty.STANDARD,
) {
    /**
     * Validates that the configuration contains at least one challenge, no unsupported parameters,
     * and uses only approved adaptive behavior per Sprint 19.4 — Adaptive Practice.
     *
     * Per `02_GAMEPLAY.md` §36: adaptive personalization is STATUS: PENDING.
     * [AdaptivePracticePolicy] enforces this guard.
     */
    fun isValid(): Boolean {
        if (challengeTypes.isEmpty()) return false
        if (challengeTypes.size > 3) return false
        if (!AdaptivePracticePolicy.isApproved(difficulty)) return false
        return true
    }

    fun validate(): DomainResult<Unit> {
        if (challengeTypes.isEmpty() || challengeTypes.size > 3) {
            return DomainResult.Failure(AppError.Validation)
        }
        val adaptiveCheck = AdaptivePracticePolicy.validateConfiguration(this)
        if (adaptiveCheck is DomainResult.Failure) {
            return adaptiveCheck
        }
        return DomainResult.Success(Unit)
    }


    companion object {
        val STANDARD = PracticeConfiguration()

        fun standard(targetBattleId: BattleId? = null): PracticeConfiguration {
            return PracticeConfiguration(targetBattleId = targetBattleId)
        }
    }
}

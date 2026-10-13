package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Domain policy enforcing the adaptive practice guard rails.
 *
 * Per `02_GAMEPLAY.md` §36 (PRACTICE PERSONALIZATION):
 *   "The product direction allows practice to become adaptive."
 *   "However, the exact personalization system is: STATUS: PENDING"
 *   "No implementation should introduce adaptive Practice until the relevant rules are specified."
 *
 * Per `02_GAMEPLAY.md` §40 (CHALLENGE DIFFICULTY):
 *   "practice-only adaptive difficulty" is a possible future concept.
 *   Status: DECISION_REQUIRED
 *
 * Per `15_ANTIGRAVITY_RULES.md` Rule 102:
 *   "Do not add: arbitrary AI personalization, unless explicitly approved."
 *
 * Per `ROADMAP.md` Sprint 19.4:
 *   "Only implement approved adaptive/AI behavior."
 *   "Critical invariant: Practice must not mutate official battle results."
 *
 * This policy acts as the single enforcement boundary for adaptive practice behavior.
 * All adaptive/AI-driven modifications to practice must pass through this policy.
 * Until the adaptive personalization rules are fully specified and approved, this policy
 * rejects all adaptive configurations.
 */
object AdaptivePracticePolicy {

    /**
     * The set of difficulty levels that are currently approved for use.
     *
     * Only [PracticeDifficulty.STANDARD] is approved. [PracticeDifficulty.ADAPTIVE_PENDING]
     * exists as a placeholder for future approved adaptive behavior and must be rejected
     * until the product decision is made.
     */
    val approvedDifficulties: Set<PracticeDifficulty> = setOf(PracticeDifficulty.STANDARD)

    /**
     * Returns `true` if the given difficulty is currently approved for use.
     */
    fun isApproved(difficulty: PracticeDifficulty): Boolean =
        difficulty in approvedDifficulties

    /**
     * Validates that a [PracticeConfiguration] uses only approved adaptive behavior.
     *
     * Returns [DomainResult.Failure] with [AppError.Validation] if the configuration
     * uses unapproved adaptive difficulty, enforcing the "STATUS: PENDING" guard.
     */
    fun validateConfiguration(configuration: PracticeConfiguration): DomainResult<Unit> {
        if (!isApproved(configuration.difficulty)) {
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }

    /**
     * Validates that a [PracticeDifficulty] is approved.
     * Returns [DomainResult.Failure] with [AppError.Validation] if not approved.
     */
    fun validateDifficulty(difficulty: PracticeDifficulty): DomainResult<Unit> {
        if (!isApproved(difficulty)) {
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }

    /**
     * Returns a human-readable status message for the adaptive practice feature.
     * This exists for diagnostic/logging purposes and does not affect behavior.
     */
    fun adaptiveStatus(): AdaptiveFeatureStatus = AdaptiveFeatureStatus.PENDING
}

/**
 * Represents the current status of the adaptive practice feature.
 *
 * Per `02_GAMEPLAY.md` §36: STATUS: PENDING
 * Per `02_GAMEPLAY.md` §40: DECISION_REQUIRED
 */
enum class AdaptiveFeatureStatus {
    /** Adaptive personalization rules have not yet been specified. */
    PENDING,
    /** Adaptive personalization is fully approved and rules are defined. */
    APPROVED,
    /** Adaptive personalization has been explicitly rejected. */
    REJECTED,
}

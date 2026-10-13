package com.sanket_satpute_20.dailybattle.domain.monetization.protection

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionLifecycle
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseError
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain authority enforcing gameplay protection rules per Sprint 21.4:
 *
 * Requirements:
 * - REQ-MON-001: Core Official Battle remains accessible without payment.
 * - REQ-MON-002: Paid functionality must not directly increase Official Battle score.
 * - REQ-MON-004: Monetization must not interrupt active gameplay; no pay-to-win mechanics.
 * - REQ-MON-005: Premium entitlements must be separated from gameplay scoring logic;
 *   monetization must not compromise Official Battle integrity.
 * - Rule 127 (15_ANTIGRAVITY_RULES.md): Billing failure/operations must not corrupt core gameplay.
 * - 13_MONETIZATION.md §4, §5, §6, §11, §47, §51.
 */
@Singleton
class GameplayProtectionPolicy @Inject constructor() {

    companion object {
        /**
         * Features that are strictly prohibited as pay-to-win, score-altering,
         * attempt-manipulating, or competitive advantage mechanics.
         */
        val PROHIBITED_FEATURE_PATTERNS = setOf(
            "score_boost",
            "add_score",
            "score_multiplier",
            "point_multiplier",
            "boost_score",
            "perfect_score",
            "bonus_points",
            "+100_score",
            "extra_attempt",
            "bonus_attempt",
            "battle_retry",
            "official_retry",
            "second_chance",
            "replay_battle",
            "replace_score",
            "easier_challenge",
            "skip_challenge",
            "auto_perfect",
            "snap_advantage",
            "shift_advantage",
            "crowd_advantage",
            "hint_answer",
            "defeat_rival",
            "rival_handicap",
            "percentile_boost",
            "ranking_boost",
            "leaderboard_boost"
        )

        /**
         * Core features that are unconditionally free and can never be monetized or gated.
         */
        val CORE_FREE_FEATURES = setOf(
            "official_battle",
            "snap",
            "shift",
            "crowd_call",
            "scoring",
            "results",
            "rival_comparison",
            "basic_practice",
            "history_basic"
        )
    }

    /**
     * Determines whether a requested feature ID represents a prohibited pay-to-win,
     * score-altering, or competitive integrity violation.
     */
    fun isProhibitedFeature(featureId: String): Boolean {
        val normalized = featureId.trim().lowercase()
        if (normalized.isBlank()) return false

        return PROHIBITED_FEATURE_PATTERNS.any { pattern ->
            normalized == pattern || normalized.contains(pattern)
        }
    }

    /**
     * Determines whether a requested feature ID represents a core free feature that
     * must never be sold or gated behind an entitlement.
     */
    fun isCoreFreeFeature(featureId: String): Boolean {
        val normalized = featureId.trim().lowercase()
        return normalized in CORE_FREE_FEATURES
    }

    /**
     * Determines whether active gameplay is currently in progress.
     *
     * Per `13_MONETIZATION.md` §11 and §47:
     * Monetization flows and ads must NEVER interrupt an active official challenge
     * (Ready, Started, ChallengeActive, ChallengeComplete, BattleComplete).
     */
    fun isGameplayActive(lifecycle: BattleSessionLifecycle?): Boolean {
        return when (lifecycle) {
            is BattleSessionLifecycle.Ready,
            is BattleSessionLifecycle.Started,
            is BattleSessionLifecycle.ChallengeActive,
            is BattleSessionLifecycle.ChallengeComplete,
            is BattleSessionLifecycle.BattleComplete -> true
            is BattleSessionLifecycle.Created,
            is BattleSessionLifecycle.Submitted,
            null -> false
        }
    }

    /**
     * Verifies whether monetization or purchase flows can be initiated given the current lifecycle.
     */
    fun canInterruptWithMonetization(lifecycle: BattleSessionLifecycle?): Boolean {
        return !isGameplayActive(lifecycle)
    }

    /**
     * Evaluates purchase eligibility under gameplay protection rules.
     *
     * @param featureId The feature being purchased.
     * @param currentBattleLifecycle Current lifecycle of the battle session, if any.
     * @return [PurchaseError.GameplayProtectionViolation] if blocked, or `null` if permitted.
     */
    fun validatePurchaseEligibility(
        featureId: String,
        currentBattleLifecycle: BattleSessionLifecycle?
    ): PurchaseError? {
        if (isProhibitedFeature(featureId)) {
            return PurchaseError.GameplayProtectionViolation(
                "Prohibited pay-to-win feature: '$featureId'. Official scoring and competitive advantages cannot be purchased."
            )
        }

        if (isCoreFreeFeature(featureId)) {
            return PurchaseError.GameplayProtectionViolation(
                "Core feature '$featureId' is free for all players and cannot be purchased."
            )
        }

        if (isGameplayActive(currentBattleLifecycle)) {
            return PurchaseError.GameplayProtectionViolation(
                "Purchases and commercial prompts cannot interrupt active gameplay."
            )
        }

        return null
    }
}

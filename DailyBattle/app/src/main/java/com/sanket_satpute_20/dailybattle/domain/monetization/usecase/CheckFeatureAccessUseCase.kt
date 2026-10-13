package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import javax.inject.Inject

/**
 * Checks whether access is granted for a requested feature.
 *
 * Implements strict product invariants per `13_MONETIZATION.md` (REQ-MON-001 - REQ-MON-005):
 * - Core Official Battle gameplay is ALWAYS free and fair. Official challenges, scoring, results,
 *   and rival comparisons never require paid entitlements.
 * - Non-core / optional premium feature keys are checked against [EntitlementRepository].
 */
class CheckFeatureAccessUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository
) {
    companion object {
        /**
         * Core features that are unconditionally free and can never be gated by entitlements.
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

    suspend operator fun invoke(featureId: String): Boolean {
        // Enforce REQ-MON-001 & REQ-MON-005: Core Official Battle is unconditionally accessible
        if (featureId.lowercase() in CORE_FREE_FEATURES) {
            return true
        }

        return entitlementRepository.hasAccessToFeature(featureId)
    }
}

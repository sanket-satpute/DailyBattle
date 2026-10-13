package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.protection.GameplayProtectionPolicy
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import javax.inject.Inject

/**
 * Checks whether access is granted for a requested feature.
 *
 * Implements strict product invariants per `13_MONETIZATION.md` (REQ-MON-001 - REQ-MON-005):
 * - Core Official Battle gameplay is ALWAYS free and fair. Official challenges, scoring, results,
 *   and rival comparisons never require paid entitlements.
 * - Prohibited pay-to-win features always return false (fail-closed).
 * - Non-core / optional premium feature keys are checked against [EntitlementRepository].
 */
class CheckFeatureAccessUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository,
    private val gameplayProtectionPolicy: GameplayProtectionPolicy = GameplayProtectionPolicy()
) {
    companion object {
        /**
         * Core features that are unconditionally free and can never be gated by entitlements.
         */
        val CORE_FREE_FEATURES = GameplayProtectionPolicy.CORE_FREE_FEATURES
    }

    suspend operator fun invoke(featureId: String): Boolean {
        // Enforce REQ-MON-002 & REQ-MON-004: Prohibited pay-to-win features can never be granted
        if (gameplayProtectionPolicy.isProhibitedFeature(featureId)) {
            return false
        }

        // Enforce REQ-MON-001 & REQ-MON-005: Core Official Battle is unconditionally accessible
        if (gameplayProtectionPolicy.isCoreFreeFeature(featureId)) {
            return true
        }

        return entitlementRepository.hasAccessToFeature(featureId)
    }
}

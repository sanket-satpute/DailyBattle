package com.sanket_satpute_20.dailybattle.domain.monetization.usecase

import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Observes the aggregate entitlement state of the user.
 */
class GetEntitlementStateUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository
) {
    operator fun invoke(): StateFlow<EntitlementState> {
        return entitlementRepository.entitlementState
    }
}

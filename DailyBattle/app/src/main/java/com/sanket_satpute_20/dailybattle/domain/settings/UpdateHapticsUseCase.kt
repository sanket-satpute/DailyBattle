package com.sanket_satpute_20.dailybattle.domain.settings

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class UpdateHapticsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean): DomainResult<Unit> {
        return repository.updateHaptics(enabled)
    }
}

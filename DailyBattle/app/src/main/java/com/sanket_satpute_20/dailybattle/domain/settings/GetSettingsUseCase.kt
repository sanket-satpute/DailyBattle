package com.sanket_satpute_20.dailybattle.domain.settings

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): DomainResult<UserSettings> {
        return repository.getSettings()
    }
}

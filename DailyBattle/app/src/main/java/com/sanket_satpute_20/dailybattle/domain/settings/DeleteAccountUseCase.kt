package com.sanket_satpute_20.dailybattle.domain.settings

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return repository.deleteAccount()
    }
}

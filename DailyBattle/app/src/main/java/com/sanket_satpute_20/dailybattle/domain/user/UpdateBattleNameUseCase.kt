package com.sanket_satpute_20.dailybattle.domain.user

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import com.sanket_satpute_20.dailybattle.domain.validation.ValidationResult
import javax.inject.Inject

class UpdateBattleNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): DomainResult<Unit> {
        val trimmed = name.trim()
        return when (val validation = InputValidator.validateBattleName(trimmed)) {
            is ValidationResult.Valid -> {
                userRepository.saveBattleName(trimmed)
                DomainResult.Success(Unit)
            }
            is ValidationResult.Invalid -> {
                DomainResult.Failure(AppError.Validation)
            }
        }
    }
}

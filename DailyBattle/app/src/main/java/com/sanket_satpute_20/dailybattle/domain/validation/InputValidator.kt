package com.sanket_satpute_20.dailybattle.domain.validation

/**
 * Result of a validation operation, allowing UI or use cases to determine
 * exact error states without guessing.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

/**
 * Canonical validation rules for Battle Code and Battle Name as defined in Sprint 3.5.
 * These rules are deterministic and must be shared wherever these inputs are used.
 */
object InputValidator {

    private val BATTLE_CODE_REGEX = Regex("^[A-Z0-9]{6}$")

    /**
     * Validates a Battle Code.
     * 
     * Rules:
     * - Exactly 6 characters
     * - Allowed characters: A–Z and 0–9 only
     * - No spaces, no special characters
     * - Must be uppercase (normalization should happen before or during validation, 
     *   but this validation enforces the final canonical format `^[A-Z0-9]{6}$`).
     */
    fun validateBattleCode(code: String): ValidationResult {
        // Enforce the canonical regex strictly. 
        // Note: The UI is responsible for automatically normalizing input to uppercase,
        // but this core validator ensures only true uppercase alphanumerics pass.
        return if (BATTLE_CODE_REGEX.matches(code)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Battle Code must be exactly 6 uppercase letters and numbers.")
        }
    }

    /**
     * Normalizes a Battle Code string to its canonical form (uppercase, no trailing spaces).
     * If the user types "abc123 ", it normalizes to "ABC123" which can then be validated.
     */
    fun normalizeBattleCode(code: String): String {
        return code.trim().uppercase()
    }

    /**
     * Validates a Battle Name.
     * 
     * Rules:
     * - Minimum 1 character, Maximum 20 characters
     * - Empty/whitespace-only input -> invalid
     * - Values exceeding 20 characters -> invalid
     * - Leading/trailing whitespace should be trimmed before validation
     */
    fun validateBattleName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Battle Name cannot be empty.")
        }
        if (trimmed.length > 20) {
            return ValidationResult.Invalid("Battle Name cannot exceed 20 characters.")
        }
        return ValidationResult.Valid
    }
}

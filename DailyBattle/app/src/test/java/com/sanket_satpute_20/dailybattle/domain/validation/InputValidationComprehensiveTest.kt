package com.sanket_satpute_20.dailybattle.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 23.1 — Unit Tests (Validation)
 * Comprehensive testing of input validation, normalization, and bounds:
 * 1. Battle Name length limits (1 to 20 characters), trimming, empty/blank rejection.
 * 2. Battle Code format (6 uppercase alphanumeric characters), regex matching.
 * 3. Normalization behavior for Battle Code (trimming, casing).
 * 4. Duplicate action / debounce protection logic.
 *
 * Per `docs/01_PRODUCT.md`, `docs/decisions/SPRINT-3.5_VALIDATION_RULES.md`,
 * and `docs/12_TESTING_AND_QA.md` §8.
 */
class InputValidationComprehensiveTest {

    // ─────────────────────────────────────────────────────────────────────────────
    // Battle Name Validation
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `valid battle names pass validation`() {
        val validNames = listOf(
            "A",                       // Min length 1
            "Commander",               // Standard
            "DailyWarrior123",         // Alphanumeric
            "12345678901234567890",    // Exactly 20 chars
            "  PaddedName  "           // Padded with spaces, trims to length 10
        )

        for (name in validNames) {
            val result = InputValidator.validateBattleName(name)
            assertTrue("Expected Valid for '$name'", result is ValidationResult.Valid)
        }
    }

    @Test
    fun `empty or blank battle names fail validation`() {
        val emptyNames = listOf(
            "",
            " ",
            "   ",
            "\t\n"
        )

        for (name in emptyNames) {
            val result = InputValidator.validateBattleName(name)
            assertTrue("Expected Invalid for blank name '$name'", result is ValidationResult.Invalid)
            val reason = (result as ValidationResult.Invalid).reason
            assertEquals("Battle Name cannot be empty.", reason)
        }
    }

    @Test
    fun `battle names exceeding 20 characters fail validation`() {
        val overLengthName = "123456789012345678901" // 21 chars
        val result = InputValidator.validateBattleName(overLengthName)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Battle Name cannot exceed 20 characters.", (result as ValidationResult.Invalid).reason)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Battle Code Validation & Normalization
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `valid battle codes pass validation`() {
        val validCodes = listOf(
            "ABC123",
            "000000",
            "ZZZZZZ",
            "B4TTL3"
        )

        for (code in validCodes) {
            val result = InputValidator.validateBattleCode(code)
            assertTrue("Expected Valid for code '$code'", result is ValidationResult.Valid)
        }
    }

    @Test
    fun `invalid battle codes fail validation`() {
        val invalidCodes = listOf(
            "ABC12",        // 5 chars (too short)
            "ABC1234",      // 7 chars (too long)
            "",             // empty
            "abc123",       // lowercase (must be normalized first)
            "ABC 12",       // contains space
            "ABC!@#",       // special chars
            "ABC_12"        // underscore
        )

        for (code in invalidCodes) {
            val result = InputValidator.validateBattleCode(code)
            assertTrue("Expected Invalid for code '$code'", result is ValidationResult.Invalid)
        }
    }

    @Test
    fun `normalizing battle code trims and capitalizes for valid submission`() {
        val rawInput = "  abc123  "
        val normalized = InputValidator.normalizeBattleCode(rawInput)

        assertEquals("ABC123", normalized)
        val validation = InputValidator.validateBattleCode(normalized)
        assertTrue("Normalized code must be Valid", validation is ValidationResult.Valid)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Duplicate / Rapid Submission Protection Logic
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `rapid tap debounce protection prevents duplicate submissions within threshold`() {
        class ActionDebouncer(private val thresholdMs: Long = 500L) {
            private var lastActionTimeMs: Long = 0L

            fun canPerformAction(currentTimeMs: Long): Boolean {
                if (currentTimeMs - lastActionTimeMs < thresholdMs) {
                    return false
                }
                lastActionTimeMs = currentTimeMs
                return true
            }
        }

        val debouncer = ActionDebouncer(thresholdMs = 500L)

        // First tap at t=1000ms: allowed
        assertTrue(debouncer.canPerformAction(1000L))

        // Rapid double tap at t=1100ms (100ms later): rejected
        assertFalse(debouncer.canPerformAction(1100L))

        // Rapid triple tap at t=1200ms: rejected
        assertFalse(debouncer.canPerformAction(1200L))

        // Legitimate subsequent action at t=1600ms (600ms later): allowed
        assertTrue(debouncer.canPerformAction(1600L))
    }
}

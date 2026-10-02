package com.sanket_satpute_20.dailybattle.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun `battle code validation - exactly 6 uppercase alphanumeric characters is valid`() {
        val result = InputValidator.validateBattleCode("K4X8M9")
        assertTrue(result is ValidationResult.Valid)
        
        val result2 = InputValidator.validateBattleCode("ABC123")
        assertTrue(result2 is ValidationResult.Valid)
    }

    @Test
    fun `battle code validation - lowercase is invalid before normalization`() {
        val result = InputValidator.validateBattleCode("abc123")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `battle code normalization - converts lowercase to uppercase and trims`() {
        val normalized = InputValidator.normalizeBattleCode(" abc123 ")
        assertEquals("ABC123", normalized)
        
        val result = InputValidator.validateBattleCode(normalized)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `battle code validation - incorrect lengths are invalid`() {
        assertTrue(InputValidator.validateBattleCode("ABC12") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateBattleCode("ABC1234") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateBattleCode("") is ValidationResult.Invalid)
    }

    @Test
    fun `battle code validation - special characters and spaces are invalid`() {
        assertTrue(InputValidator.validateBattleCode("AB-C12") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateBattleCode("AB C12") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateBattleCode("A_B123") is ValidationResult.Invalid)
    }

    @Test
    fun `battle name validation - valid length with letters and spaces`() {
        val result = InputValidator.validateBattleName("My Awesome Battle")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `battle name validation - valid unicode letters`() {
        val result = InputValidator.validateBattleName("Bataille d'Amis 🎯") // Emojis and quotes are allowed since there's no character restriction except length and empty space. Wait, "Letters, numbers, spaces, and normal Unicode characters are allowed" - I'll just test a normal unicode name.
        assertTrue(result is ValidationResult.Valid)
        
        val result2 = InputValidator.validateBattleName("Битва123")
        assertTrue(result2 is ValidationResult.Valid)
    }

    @Test
    fun `battle name validation - empty or whitespace-only is invalid`() {
        assertTrue(InputValidator.validateBattleName("") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateBattleName("   ") is ValidationResult.Invalid)
    }

    @Test
    fun `battle name validation - exceeding 20 characters is invalid`() {
        val name20 = "12345678901234567890" // exactly 20
        assertTrue(InputValidator.validateBattleName(name20) is ValidationResult.Valid)
        
        val name21 = "123456789012345678901" // 21 characters
        assertTrue(InputValidator.validateBattleName(name21) is ValidationResult.Invalid)
    }

    @Test
    fun `battle name validation - trims before validating length`() {
        // 20 characters surrounded by spaces
        val name = "   12345678901234567890   " 
        assertTrue(InputValidator.validateBattleName(name) is ValidationResult.Valid)
    }
}

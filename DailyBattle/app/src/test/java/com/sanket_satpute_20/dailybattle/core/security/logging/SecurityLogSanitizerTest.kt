package com.sanket_satpute_20.dailybattle.core.security.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityLogSanitizerTest {

    @Test
    fun `sanitizeHeader redacts Authorization and Cookie headers`() {
        val authSanitized = SecurityLogSanitizer.sanitizeHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsIn...")
        assertEquals(SecurityLogSanitizer.REDACTED, authSanitized)

        val cookieSanitized = SecurityLogSanitizer.sanitizeHeader("Cookie", "session_id=12345; auth=xyz")
        assertEquals(SecurityLogSanitizer.REDACTED, cookieSanitized)

        val setCookieSanitized = SecurityLogSanitizer.sanitizeHeader("Set-Cookie", "jwt=abc")
        assertEquals(SecurityLogSanitizer.REDACTED, setCookieSanitized)
    }

    @Test
    fun `sanitizeHeader preserves non sensitive headers`() {
        val contentType = SecurityLogSanitizer.sanitizeHeader("Content-Type", "application/json")
        assertEquals("application/json", contentType)

        val accept = SecurityLogSanitizer.sanitizeHeader("Accept", "application/json")
        assertEquals("application/json", accept)
    }

    @Test
    fun `sanitizeUrl redacts sensitive query parameters`() {
        val rawUrl = "https://api.dailybattle.com/v1/battles?token=secret12345&date=2026-10-14&password=myPass"
        val sanitized = SecurityLogSanitizer.sanitizeUrl(rawUrl)

        assertTrue(sanitized.contains("token=[REDACTED]"))
        assertTrue(sanitized.contains("password=[REDACTED]"))
        assertTrue(sanitized.contains("date=2026-10-14"))
        assertFalse(sanitized.contains("secret12345"))
        assertFalse(sanitized.contains("myPass"))
    }

    @Test
    fun `sanitizeUrl preserves URLs with no query parameters`() {
        val url = "https://api.dailybattle.com/v1/battles/today"
        assertEquals(url, SecurityLogSanitizer.sanitizeUrl(url))
    }

    @Test
    fun `sanitizeText redacts Bearer tokens`() {
        val log = "Request failed with header: Bearer token_secret_value_12345678"
        val sanitized = SecurityLogSanitizer.sanitizeText(log)
        assertTrue(sanitized.contains("Bearer [REDACTED]"))
        assertFalse(sanitized.contains("token_secret_value_12345678"))
    }

    @Test
    fun `sanitizeText redacts JWT tokens`() {
        val fakeJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val log = "User authenticated with JWT: $fakeJwt"
        val sanitized = SecurityLogSanitizer.sanitizeText(log)
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains(fakeJwt))
    }

    @Test
    fun `sanitizeText redacts password field values`() {
        val log = "Invalid input: password=mySecretPassword123"
        val sanitized = SecurityLogSanitizer.sanitizeText(log)
        assertTrue(sanitized.contains("password=[REDACTED]"))
        assertFalse(sanitized.contains("mySecretPassword123"))
    }

    @Test
    fun `sanitizeText redacts emails and phone numbers`() {
        val log = "User contact email=player@example.com phone=555-123-4567"
        val sanitized = SecurityLogSanitizer.sanitizeText(log)
        assertFalse(sanitized.contains("player@example.com"))
        assertFalse(sanitized.contains("555-123-4567"))
        assertTrue(sanitized.contains("[REDACTED]"))
    }
}

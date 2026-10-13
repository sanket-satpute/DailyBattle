package com.sanket_satpute_20.dailybattle.core.analytics.privacy

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent

/**
 * Enforces strict data minimization and privacy protection across all analytics events.
 *
 * Implements Sprint 20.4 — Privacy per `ROADMAP.md` and `14_ANALYTICS.md` §22, §46, §48, §52-§56:
 * - Data minimization: prevents arbitrary, oversized, or unauthorized payload leakage.
 * - No unnecessary personal data: redacts emails, phone numbers, PII, and hardware identifiers.
 * - No sensitive data leakage: scrubs credit cards, passwords, API secrets, and exact locations.
 * - No credentials or tokens: detects and eliminates JWTs, auth tokens, and session secrets.
 * - Battle Code privacy (§55): raw Battle Codes are never logged.
 */
object AnalyticsPrivacySanitizer {

    const val REDACTED_PLACEHOLDER = "[REDACTED]"
    const val MAX_PROPERTY_VALUE_LENGTH = 500
    const val MAX_PROPERTY_COUNT = 30

    // Sensitive Value Patterns
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val JWT_TOKEN_REGEX = Regex("ey[a-zA-Z0-9_-]{8,}\\.[a-zA-Z0-9_-]{8,}\\.[a-zA-Z0-9_-]{8,}")
    private val CREDIT_CARD_REGEX = Regex("\\b(?:\\d[ -]*?){13,19}\\b")
    private val PHONE_NUMBER_REGEX = Regex("\\b(?:\\+?\\d{1,3}[- .]?)?\\(?\\d{3}\\)?[- .]?\\d{3}[- .]?\\d{4}\\b")

    /**
     * Determines whether a property key is forbidden for privacy reasons.
     */
    fun isForbiddenKey(key: String): Boolean {
        val lowerKey = key.lowercase().trim()
        if (lowerKey in AnalyticsEvent.FORBIDDEN_SENSITIVE_KEYS) {
            return true
        }
        // Substring checks for dangerous key suffixes or prefixes
        val sensitiveSuffixes = listOf(
            "_token",
            "_secret",
            "_password",
            "_pwd",
            "_pin",
            "_key",
            "_credential",
            "_battle_code",
            "_friend_code",
            "_invite_code"
        )
        val sensitivePrefixes = listOf("auth_", "secret_", "token_", "private_", "pin_")
        if (sensitiveSuffixes.any { lowerKey.endsWith(it) }) {
            return true
        }
        if (sensitivePrefixes.any { lowerKey.startsWith(it) }) {
            return true
        }
        return false
    }

    /**
     * Sanitizes an individual property value, redacting any leaked tokens, emails, or PII.
     */
    fun sanitizeValue(value: Any?): Any? {
        if (value == null) return null
        if (value !is String) return value

        var sanitized = value

        // 1. JWT / Bearer Token redaction
        if (JWT_TOKEN_REGEX.containsMatchIn(sanitized)) {
            sanitized = JWT_TOKEN_REGEX.replace(sanitized, REDACTED_PLACEHOLDER)
        }

        // 2. Email redaction
        if (EMAIL_REGEX.containsMatchIn(sanitized)) {
            sanitized = EMAIL_REGEX.replace(sanitized, REDACTED_PLACEHOLDER)
        }

        // 3. Credit Card number redaction
        if (CREDIT_CARD_REGEX.containsMatchIn(sanitized)) {
            sanitized = CREDIT_CARD_REGEX.replace(sanitized, REDACTED_PLACEHOLDER)
        }

        // 4. Phone number redaction
        if (PHONE_NUMBER_REGEX.containsMatchIn(sanitized)) {
            sanitized = PHONE_NUMBER_REGEX.replace(sanitized, REDACTED_PLACEHOLDER)
        }

        // 5. Length bounding (prevents stack traces or memory dumps leaking)
        if (sanitized.length > MAX_PROPERTY_VALUE_LENGTH) {
            sanitized = sanitized.take(MAX_PROPERTY_VALUE_LENGTH) + "...[TRUNCATED]"
        }

        return sanitized
    }

    /**
     * Deeply sanitizes an [AnalyticsEvent]:
     * - Strips all forbidden sensitive keys
     * - Redacts sensitive patterns in string values
     * - Enforces maximum property count limit
     */
    fun sanitize(event: AnalyticsEvent): AnalyticsEvent {
        val sanitizedProperties = mutableMapOf<String, Any?>()

        for ((key, value) in event.properties) {
            // Drop forbidden keys
            if (isForbiddenKey(key)) {
                continue
            }

            // Cap maximum property count
            if (sanitizedProperties.size >= MAX_PROPERTY_COUNT) {
                break
            }

            sanitizedProperties[key] = sanitizeValue(value)
        }

        return event.copy(properties = sanitizedProperties)
    }

    /**
     * Sanitizes a user property key/value pair. Returns null if key is forbidden.
     */
    fun sanitizeUserProperty(key: String, value: String?): Pair<String, String?>? {
        if (isForbiddenKey(key)) {
            return null
        }
        val sanitizedValue = sanitizeValue(value) as? String
        return key to sanitizedValue
    }
}

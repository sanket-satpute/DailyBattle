package com.sanket_satpute_20.dailybattle.core.security.logging

/**
 * Sanitizes application and network log messages, headers, and URLs to eliminate
 * credentials, tokens, and sensitive personal information per:
 * - `09_OFFLINE_AND_ERROR_HANDLING.md` §62 (Error Logging)
 * - `15_ANTIGRAVITY_RULES.md` Rule 73 (Logging Rule)
 * - `07_TECHNICAL_ARCHITECTURE.md` §41 (Logging)
 * - `ROADMAP.md` Sprint 24.6 (Security — Logging)
 */
object SecurityLogSanitizer {

    const val REDACTED = "[REDACTED]"

    // Patterns matching sensitive tokens and credentials
    private val JWT_REGEX = Regex("ey[a-zA-Z0-9_-]{8,}\\.[a-zA-Z0-9_-]{8,}\\.[a-zA-Z0-9_-]{8,}")
    private val BEARER_REGEX = Regex("(?i)Bearer\\s+[a-zA-Z0-9._~+/-]+=*")
    private val PASSWORD_VALUE_REGEX = Regex("(?i)(password|passwd|pwd)\\s*[:=]\\s*[^\\s,;\"']+")
    private val CREDIT_CARD_REGEX = Regex("\\b(?:\\d[ -]*?){13,19}\\b")
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val PHONE_REGEX = Regex("\\b(?:\\+?\\d{1,3}[- .]?)?\\(?\\d{3}\\)?[- .]?\\d{3}[- .]?\\d{4}\\b")

    // Sensitive Header names
    private val SENSITIVE_HEADERS = setOf(
        "authorization",
        "cookie",
        "set-cookie",
        "proxy-authorization",
        "x-auth-token",
        "x-api-key",
        "x-dailybattle-auth-required"
    )

    // Sensitive Query parameter names
    private val SENSITIVE_QUERY_PARAMS = setOf(
        "token",
        "access_token",
        "refresh_token",
        "auth",
        "password",
        "pwd",
        "secret",
        "key",
        "api_key"
    )

    /**
     * Sanitizes a header value. If the header name is in [SENSITIVE_HEADERS], returns [REDACTED].
     */
    fun sanitizeHeader(name: String, value: String): String {
        val lower = name.lowercase().trim()
        return if (SENSITIVE_HEADERS.contains(lower)) {
            REDACTED
        } else {
            sanitizeText(value)
        }
    }

    /**
     * Sanitizes a URL by redacting query parameters that match sensitive keys.
     */
    fun sanitizeUrl(url: String): String {
        val queryStart = url.indexOf('?')
        if (queryStart == -1) return url

        val base = url.substring(0, queryStart)
        val query = url.substring(queryStart + 1)

        val pairs = query.split("&").map { pair ->
            val equalsIndex = pair.indexOf('=')
            if (equalsIndex != -1) {
                val paramKey = pair.substring(0, equalsIndex)
                if (SENSITIVE_QUERY_PARAMS.contains(paramKey.lowercase())) {
                    "$paramKey=$REDACTED"
                } else {
                    pair
                }
            } else {
                pair
            }
        }

        return "$base?${pairs.joinToString("&")}"
    }

    /**
     * Sanitizes arbitrary text messages (log outputs, error details) to eliminate credentials,
     * JWT tokens, passwords, credit card numbers, and PII.
     */
    fun sanitizeText(message: String?): String {
        if (message.isNullOrBlank()) return ""

        var sanitized = message

        // 1. Bearer tokens
        if (BEARER_REGEX.containsMatchIn(sanitized)) {
            sanitized = BEARER_REGEX.replace(sanitized, "Bearer $REDACTED")
        }

        // 2. Raw JWTs
        if (JWT_REGEX.containsMatchIn(sanitized)) {
            sanitized = JWT_REGEX.replace(sanitized, REDACTED)
        }

        // 3. Password field values
        if (PASSWORD_VALUE_REGEX.containsMatchIn(sanitized)) {
            sanitized = PASSWORD_VALUE_REGEX.replace(sanitized) { matchResult ->
                val field = matchResult.groupValues[1]
                "$field=$REDACTED"
            }
        }

        // 4. Credit card numbers
        if (CREDIT_CARD_REGEX.containsMatchIn(sanitized)) {
            sanitized = CREDIT_CARD_REGEX.replace(sanitized, REDACTED)
        }

        // 5. Emails
        if (EMAIL_REGEX.containsMatchIn(sanitized)) {
            sanitized = EMAIL_REGEX.replace(sanitized, REDACTED)
        }

        // 6. Phone numbers
        if (PHONE_REGEX.containsMatchIn(sanitized)) {
            sanitized = PHONE_REGEX.replace(sanitized, REDACTED)
        }

        return sanitized
    }
}

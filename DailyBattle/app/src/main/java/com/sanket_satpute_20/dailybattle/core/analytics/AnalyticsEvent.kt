package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * Platform-neutral, immutable analytics event model.
 *
 * Per `14_ANALYTICS.md` §6-§7:
 * - Event names follow `<domain>_<action>` format in lowercase with underscores.
 * - Properties are typed, minimal, stable, and useful (§45).
 * - Sensitive keys (passwords, tokens, raw battle codes) are forbidden (§54, §55).
 * - Mode distinction (`battle_mode`: "official" | "practice") is mandatory (§13, REQ-AN-002).
 */
data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, Any?> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) {
    init {
        require(isValidEventName(name)) {
            "Invalid event name '$name'. Event names must be lowercase alphanumeric with underscores."
        }
    }

    /**
     * Returns a copy of this event with additional or replaced properties.
     */
    fun withProperty(key: String, value: Any?): AnalyticsEvent {
        return copy(properties = properties + (key to value))
    }

    /**
     * Returns a copy of this event with multiple additional properties.
     */
    fun withProperties(additional: Map<String, Any?>): AnalyticsEvent {
        return copy(properties = properties + additional)
    }

    companion object {
        private val EVENT_NAME_REGEX = Regex("^[a-z0-9]+(_[a-z0-9]+)*$")

        // Standard Common Parameter Keys
        const val PARAM_TIMESTAMP = "timestamp"
        const val PARAM_ENVIRONMENT = "environment"
        const val PARAM_APP_VERSION = "app_version"
        const val PARAM_BATTLE_MODE = "battle_mode"
        const val PARAM_SESSION_ID = "battle_session_id"
        const val PARAM_USER_ID = "user_id"
        const val PARAM_ERROR_CATEGORY = "error_category"
        const val PARAM_ERROR_CODE = "error_code"

        // Standard Mode Values
        const val VALUE_MODE_OFFICIAL = "official"
        const val VALUE_MODE_PRACTICE = "practice"

        // Sensitive Keys that must never be recorded (Data Minimization / Privacy §48, §54, §55, §56)
        val FORBIDDEN_SENSITIVE_KEYS = setOf(
            // Credentials & Authentication
            "password",
            "pwd",
            "token",
            "auth_token",
            "access_token",
            "refresh_token",
            "id_token",
            "secret",
            "api_key",
            "credential",
            "jwt",
            "bearer",
            // Battle Codes & Invitations (§55)
            "battle_code",
            "friend_code",
            "invite_code",
            // Payment & Financial (§54)
            "credit_card",
            "card_number",
            "cvv",
            "cvc",
            "pin",
            "user_pin",
            "pin_code",
            "payment_method",
            // Personal Identifiable Information (PII) (§46, §56)
            "email",
            "phone",
            "phone_number",
            "telephone",
            "mobile",
            "contact",
            "contacts",
            "address",
            "ssn",
            "real_name",
            // Hardware Identifiers (§48)
            "imei",
            "mac_address",
            "hardware_id",
            "android_id",
            "serial_number",
            "device_id",
            // Exact Location (§54)
            "latitude",
            "longitude",
            "lat",
            "lng",
            "gps",
            "location",
            "exact_location"
        )

        /**
         * Validates event name formatting per §7.
         */
        fun isValidEventName(name: String): Boolean {
            return name.isNotBlank() && EVENT_NAME_REGEX.matches(name)
        }

        /**
         * Creates an event with sensitive keys stripped out per data minimization requirements.
         */
        fun createSanitized(
            name: String,
            properties: Map<String, Any?> = emptyMap(),
            timestamp: Long = System.currentTimeMillis()
        ): AnalyticsEvent {
            val sanitized = properties.filterKeys { key ->
                key.lowercase() !in FORBIDDEN_SENSITIVE_KEYS
            }
            return AnalyticsEvent(name = name, properties = sanitized, timestamp = timestamp)
        }
    }
}

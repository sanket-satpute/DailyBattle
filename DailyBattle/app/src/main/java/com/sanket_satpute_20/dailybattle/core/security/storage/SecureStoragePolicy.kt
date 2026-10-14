package com.sanket_satpute_20.dailybattle.core.security.storage

/**
 * Enforces the required storage security tier for each data classification.
 *
 * Rules:
 * 1. Sensitive keys (tokens, credentials, secrets) MUST require [StorageSecurityLevel.ENCRYPTED_KEYSTORE].
 * 2. General user settings and non-sensitive local caches may use [StorageSecurityLevel.APP_PRIVATE_SANDBOX].
 * 3. NO data may ever use [StorageSecurityLevel.EXTERNAL_OR_WORLD_ACCESSIBLE].
 */
object SecureStoragePolicy {

    private val SENSITIVE_KEY_PATTERNS = listOf(
        "token",
        "secret",
        "credential",
        "password",
        "private_key",
        "session_secret",
        "auth_key",
        "bearer",
        "api_key"
    )

    /**
     * Determines whether a given storage key represents sensitive data requiring encryption.
     */
    fun isSensitiveKey(key: String): Boolean {
        val lower = key.lowercase().trim()
        return SENSITIVE_KEY_PATTERNS.any { pattern -> lower.contains(pattern) }
    }

    /**
     * Resolves the minimum required [StorageSecurityLevel] for a given key or domain descriptor.
     */
    fun requiredSecurityLevel(key: String): StorageSecurityLevel {
        return if (isSensitiveKey(key)) {
            StorageSecurityLevel.ENCRYPTED_KEYSTORE
        } else {
            StorageSecurityLevel.APP_PRIVATE_SANDBOX
        }
    }

    /**
     * Verifies that the provided storage level meets or exceeds the required level.
     *
     * @return true if permitted; false if storage tier is insecure for the given data.
     */
    fun isStoragePermitted(key: String, actualLevel: StorageSecurityLevel): Boolean {
        if (actualLevel == StorageSecurityLevel.EXTERNAL_OR_WORLD_ACCESSIBLE) {
            return false // External storage is strictly prohibited for all app data
        }

        val required = requiredSecurityLevel(key)
        return when (required) {
            StorageSecurityLevel.ENCRYPTED_KEYSTORE -> actualLevel == StorageSecurityLevel.ENCRYPTED_KEYSTORE
            StorageSecurityLevel.APP_PRIVATE_SANDBOX -> actualLevel == StorageSecurityLevel.ENCRYPTED_KEYSTORE ||
                    actualLevel == StorageSecurityLevel.APP_PRIVATE_SANDBOX
            StorageSecurityLevel.EXTERNAL_OR_WORLD_ACCESSIBLE -> true
        }
    }
}

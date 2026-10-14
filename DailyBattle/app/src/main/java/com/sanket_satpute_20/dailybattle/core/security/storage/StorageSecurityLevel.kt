package com.sanket_satpute_20.dailybattle.core.security.storage

/**
 * Storage security tiers for Daily Battle local persistence.
 *
 * Implements Sprint 24.6 Secure Storage verification per:
 * - `07_TECHNICAL_ARCHITECTURE.md` §35 (Security — Sensitive Local Data)
 * - `ROADMAP.md` Sprint 24.6 (Secure Storage)
 */
enum class StorageSecurityLevel {
    /**
     * Hardware-backed or platform-encrypted storage (KeyStore, EncryptedSharedPreferences/EncryptedDataStore).
     * Mandatory for auth tokens, session secrets, private credentials, or cryptographic keys.
     */
    ENCRYPTED_KEYSTORE,

    /**
     * App-internal private sandbox storage (Room SQLite, DataStore Preferences in `/data/data/<pkg>/`
     * with standard Linux sandbox UID isolation and `Context.MODE_PRIVATE`).
     * Permitted for non-sensitive user settings, local cached game definitions, and offline battle history.
     */
    APP_PRIVATE_SANDBOX,

    /**
     * External, shared, or world-accessible storage (e.g. `/sdcard/`, external cache, world-readable prefs).
     * Strictly FORBIDDEN for any sensitive, authentication, session, or score data.
     */
    EXTERNAL_OR_WORLD_ACCESSIBLE,
}

package com.sanket_satpute_20.dailybattle.core.security.audit

/**
 * The 9 mandatory security verification categories defined in `ROADMAP.md` Sprint 24.6.
 */
enum class SecurityAuditCategory(val displayName: String) {
    AUTHENTICATION("Authentication"),
    AUTHORIZATION("Authorization"),
    SECURE_STORAGE("Secure Storage"),
    API_VALIDATION("API Validation"),
    SCORE_AUTHORITY("Score Authority"),
    BATTLE_OWNERSHIP("Battle Ownership"),
    SECRETS("Secrets"),
    DEBUG_ENDPOINTS("Debug Endpoints"),
    LOGGING("Logging"),
}

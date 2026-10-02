package com.sanket_satpute_20.dailybattle.core.config

/**
 * The application's current runtime environment. No backend endpoint, credential, or secret is
 * associated with a value; concrete per-environment configuration remains a pending decision.
 */
enum class AppEnvironment {
    Development,
    Testing,
    Production,
}

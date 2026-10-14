package com.sanket_satpute_20.dailybattle.core.security.secrets

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Scans and verifies that no embedded API secrets, private keys, production credentials,
 * or signing keys exist in application configuration or source structures per:
 * - `07_TECHNICAL_ARCHITECTURE.md` §35 (Security)
 * - `15_ANTIGRAVITY_RULES.md` Rule 49 (Security Rule)
 * - `ROADMAP.md` Sprint 24.6 (Security — Secrets)
 */
class SecretsAudit {

    private val PEM_PRIVATE_KEY_REGEX = Regex("-----BEGIN (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----")
    private val AWS_ACCESS_KEY_REGEX = Regex("\\bAKIA[0-9A-Z]{16}\\b")
    private val GOOGLE_API_KEY_REGEX = Regex("\\bAIza[0-9A-Za-z\\-_]{30,45}\\b")
    private val GENERIC_SECRET_ASSIGNMENT_REGEX = Regex("(?i)(?:api_secret|client_secret|signing_key|private_key|service_account_key)\\s*[:=]\\s*[\"'][^\"']{8,}[\"']")
    private val EMBEDDED_PASSWORD_REGEX = Regex("(?i)(?:password|passwd|pwd)\\s*[:=]\\s*[\"'][^\"']{4,}[\"']")

    /**
     * Scans arbitrary text content (e.g. config files, serialized payloads) for forbidden hardcoded secrets.
     *
     * @return [DomainResult.Success] if clean; [DomainResult.Failure] with [AppError.Validation] if secrets detected.
     */
    fun scanTextForSecrets(content: String): DomainResult<Unit> {
        if (PEM_PRIVATE_KEY_REGEX.containsMatchIn(content)) {
            return DomainResult.Failure(AppError.Validation)
        }
        if (AWS_ACCESS_KEY_REGEX.containsMatchIn(content)) {
            return DomainResult.Failure(AppError.Validation)
        }
        if (GOOGLE_API_KEY_REGEX.containsMatchIn(content)) {
            return DomainResult.Failure(AppError.Validation)
        }
        if (GENERIC_SECRET_ASSIGNMENT_REGEX.containsMatchIn(content)) {
            return DomainResult.Failure(AppError.Validation)
        }
        if (EMBEDDED_PASSWORD_REGEX.containsMatchIn(content)) {
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }

    /**
     * Audits a map of configuration properties to ensure no sensitive credentials or keys are exposed.
     */
    fun auditConfigProperties(properties: Map<String, String>): DomainResult<Unit> {
        val forbiddenKeyTokens = listOf("secret", "private_key", "signing_key", "password", "service_account")
        for ((key, value) in properties) {
            val lowerKey = key.lowercase()
            if (forbiddenKeyTokens.any { lowerKey.contains(it) } && value.isNotBlank()) {
                return DomainResult.Failure(AppError.Validation)
            }
            if (scanTextForSecrets(value) is DomainResult.Failure) {
                return DomainResult.Failure(AppError.Validation)
            }
        }
        return DomainResult.Success(Unit)
    }

    /**
     * Verifies that the active [AppEnvironment] does not mix debug mock configs with production.
     */
    fun auditEnvironmentCompliance(
        environment: AppEnvironment,
        isMockModeActive: Boolean
    ): DomainResult<Unit> {
        if (environment == AppEnvironment.Production && isMockModeActive) {
            // Production environment must never allow mock or backdoor secrets
            return DomainResult.Failure(AppError.Validation)
        }
        return DomainResult.Success(Unit)
    }
}

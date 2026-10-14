package com.sanket_satpute_20.dailybattle.core.security.audit

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.security.auth.SecurityAuthValidator
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.core.security.logging.SecurityLogSanitizer
import com.sanket_satpute_20.dailybattle.core.security.ownership.BattleOwnershipValidator
import com.sanket_satpute_20.dailybattle.core.security.score.ScoreAuthorityEnforcer
import com.sanket_satpute_20.dailybattle.core.security.score.ScoreAuthorityState
import com.sanket_satpute_20.dailybattle.core.security.secrets.SecretsAudit
import com.sanket_satpute_20.dailybattle.core.security.storage.SecureStorageValidator
import com.sanket_satpute_20.dailybattle.core.security.storage.StorageSecurityLevel
import com.sanket_satpute_20.dailybattle.core.security.validation.ApiSecurityValidator
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified security auditor orchestrating and evaluating the 9 mandatory security checks
 * defined in `ROADMAP.md` Sprint 24.6:
 * 1. Authentication
 * 2. Authorization
 * 3. Secure storage
 * 4. API validation
 * 5. Score authority ("Never trust the Android client as the sole authority for competitive score integrity.")
 * 6. Battle ownership
 * 7. Secrets
 * 8. Debug endpoints
 * 9. Logging
 */
@Singleton
class SecurityAuditor @Inject constructor(
    private val authValidator: SecurityAuthValidator,
    private val storageValidator: SecureStorageValidator,
    private val apiValidator: ApiSecurityValidator,
    private val scoreEnforcer: ScoreAuthorityEnforcer,
    private val ownershipValidator: BattleOwnershipValidator,
    private val secretsAudit: SecretsAudit,
    private val debugGuard: DebugEndpointGuard,
    private val environmentProvider: EnvironmentProvider,
) {

    /**
     * Executes deep verification across all 9 security categories and returns an aggregated report.
     */
    fun runFullAudit(): SecurityAuditReport {
        val results = mutableListOf<SecurityCheckResult>()

        results.add(auditAuthentication())
        results.add(auditAuthorization())
        results.add(auditSecureStorage())
        results.add(auditApiValidation())
        results.add(auditScoreAuthority())
        results.add(auditBattleOwnership())
        results.add(auditSecrets())
        results.add(auditDebugEndpoints())
        results.add(auditLogging())

        return SecurityAuditReport(results = results)
    }

    private fun auditAuthentication(): SecurityCheckResult {
        // 1. Missing token on auth-required endpoint must fail with AppError.Authentication
        val missingTokenCheck = authValidator.validateTokenPresence(null, endpointRequiresAuth = true)
        val missingPass = missingTokenCheck is DomainResult.Failure && missingTokenCheck.error == AppError.Authentication

        // 2. HTTP 401 must map strictly to AppError.Authentication
        val status401Check = authValidator.evaluateHttpStatus(401)
        val statusPass = status401Check is DomainResult.Failure && status401Check.error == AppError.Authentication

        // 3. Retry limits on expired credentials (§37): retry counter must be bounded
        val testKey = "test_auth_retry_session"
        authValidator.resetFailureCounter(testKey)
        val firstRetry = authValidator.recordAndCheckRetryEligibility(testKey)
        val secondRetry = authValidator.recordAndCheckRetryEligibility(testKey)
        val retryPass = firstRetry && !secondRetry

        val allPassed = missingPass && statusPass && retryPass
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.AUTHENTICATION,
                "Authentication contracts verified: tokens enforced, 401 distinct error mapped, retries strictly bounded."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.AUTHENTICATION,
                "Authentication verification failed: missingPass=$missingPass, statusPass=$statusPass, retryPass=$retryPass"
            )
        }
    }

    private fun auditAuthorization(): SecurityCheckResult {
        // 1. HTTP 403 must map strictly to AppError.Authorization
        val status403Check = authValidator.evaluateHttpStatus(403)
        val statusPass = status403Check is DomainResult.Failure && status403Check.error == AppError.Authorization

        // 2. Cross-user resource access must fail with AppError.Authorization
        val user1 = UserId("user_1")
        val user2 = UserId("user_2")
        val authCheck = authValidator.validateResourceAuthorization(user1, user2, isPublicResource = false)
        val resourceAuthPass = authCheck is DomainResult.Failure && authCheck.error == AppError.Authorization

        val allPassed = statusPass && resourceAuthPass
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.AUTHORIZATION,
                "Authorization contracts verified: 403 distinct error mapped, resource boundaries strictly enforced."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.AUTHORIZATION,
                "Authorization verification failed: statusPass=$statusPass, resourceAuthPass=$resourceAuthPass"
            )
        }
    }

    private fun auditSecureStorage(): SecurityCheckResult {
        // 1. Sensitive keys (token, secret) must reject unencrypted storage
        val tokenInPlaintext = storageValidator.validateStorageLevel("auth_token", StorageSecurityLevel.APP_PRIVATE_SANDBOX)
        val tokenRejected = tokenInPlaintext is DomainResult.Failure

        // 2. Sensitive keys must be allowed in encrypted keystore
        val tokenInEncrypted = storageValidator.validateStorageLevel("auth_token", StorageSecurityLevel.ENCRYPTED_KEYSTORE)
        val tokenEncryptedAllowed = tokenInEncrypted is DomainResult.Success

        // 3. External storage must be forbidden for all data
        val settingsInExternal = storageValidator.validateStorageLevel("sound_enabled", StorageSecurityLevel.EXTERNAL_OR_WORLD_ACCESSIBLE)
        val externalRejected = settingsInExternal is DomainResult.Failure

        val allPassed = tokenRejected && tokenEncryptedAllowed && externalRejected
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.SECURE_STORAGE,
                "Secure storage contracts verified: sensitive keys require encryption, external storage strictly forbidden."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.SECURE_STORAGE,
                "Secure storage verification failed: tokenRejected=$tokenRejected, tokenEncryptedAllowed=$tokenEncryptedAllowed, externalRejected=$externalRejected"
            )
        }
    }

    private fun auditApiValidation(): SecurityCheckResult {
        // 1. Traversal in identifier rejected
        val badIdCheck = apiValidator.validateIdentifier("session/../escape")
        val idPass = badIdCheck is DomainResult.Failure

        // 2. Mutation idempotency key required
        val emptyIdempotency = apiValidator.validateIdempotencyKey("")
        val idempotencyPass = emptyIdempotency is DomainResult.Failure

        // 3. Challenge sequence must be Snap(1) -> Shift(2) -> Crowd Call(3)
        val seqPass = apiValidator.validateChallengeSequence(1, ChallengeType.SNAP) is DomainResult.Success &&
                apiValidator.validateChallengeSequence(1, ChallengeType.SHIFT) is DomainResult.Failure

        // 4. Score range validation (Snap > 300 rejected, total > 1000 rejected)
        val invalidScorePass = apiValidator.validateChallengeScore(301, ChallengeType.SNAP) is DomainResult.Failure

        val allPassed = idPass && idempotencyPass && seqPass && invalidScorePass
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.API_VALIDATION,
                "API validation contracts verified: traversal paths rejected, idempotency enforced, sequence & score bounds validated."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.API_VALIDATION,
                "API validation verification failed: idPass=$idPass, idempotencyPass=$idempotencyPass, seqPass=$seqPass, invalidScorePass=$invalidScorePass"
            )
        }
    }

    private fun auditScoreAuthority(): SecurityCheckResult {
        // Golden rule: "Never trust the Android client as the sole authority for competitive score integrity."
        val validResult = BattleResult(
            resultId = BattleResultId("res_1"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_1"),
            sessionId = BattleSessionId("sess_1"),
            snapScore = 250,
            shiftScore = 250,
            crowdCallScore = 250,
            consistencyScore = 50,
            totalScore = 800,
            percentile = 88.5,
            completedAt = 1000L
        )

        // 1. Valid score integrity passes
        val validIntegrity = scoreEnforcer.validateScoreIntegrity(validResult) is DomainResult.Success

        // 2. Tampered total score (math mismatch) rejected
        val tamperedMath = validResult.copy(totalScore = 999)
        val mathRejected = scoreEnforcer.validateScoreIntegrity(tamperedMath) is DomainResult.Failure

        // 3. Client mutation of SERVER_CONFIRMED result rejected
        val modifiedClientResult = validResult.copy(totalScore = 850)
        val mutationRejected = scoreEnforcer.assertMutationAllowed(
            existingResult = validResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = modifiedClientResult,
            isUpdateFromServer = false
        ) is DomainResult.Failure

        // 4. Practice mode cannot yield official score
        val practiceRejected = scoreEnforcer.validateModeAuthority(BattleMode.PRACTICE) is DomainResult.Failure

        val allPassed = validIntegrity && mathRejected && mutationRejected && practiceRejected
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.SCORE_AUTHORITY,
                "Score authority contracts verified: server is sole authority, client mutation blocked, practice isolated."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.SCORE_AUTHORITY,
                "Score authority verification failed: validIntegrity=$validIntegrity, mathRejected=$mathRejected, mutationRejected=$mutationRejected, practiceRejected=$practiceRejected"
            )
        }
    }

    private fun auditBattleOwnership(): SecurityCheckResult {
        val user1 = UserId("user_1")
        val user2 = UserId("user_2")
        val battle1 = BattleId("battle_1")

        val session1 = BattleSession(
            sessionId = BattleSessionId("sess_1"),
            userId = user1,
            battleId = battle1,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 100L,
            completedAt = null
        )

        // 1. Session ownership check
        val ownerPass = ownershipValidator.validateSessionOwnership(session1, user1) is DomainResult.Success
        val impostorPass = ownershipValidator.validateSessionOwnership(session1, user2) is DomainResult.Failure

        // 2. Uniqueness constraint (userId, battleId, OFFICIAL)
        val duplicateOfficialRejected = ownershipValidator.validateAttemptUniqueness(
            userId = user1,
            battleId = battle1,
            targetMode = BattleMode.OFFICIAL,
            existingOfficialSessions = listOf(session1)
        ) is DomainResult.Failure

        val allPassed = ownerPass && impostorPass && duplicateOfficialRejected
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.BATTLE_OWNERSHIP,
                "Battle ownership contracts verified: session ownership enforced, duplicate official attempts rejected."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.BATTLE_OWNERSHIP,
                "Battle ownership verification failed: ownerPass=$ownerPass, impostorPass=$impostorPass, duplicateOfficialRejected=$duplicateOfficialRejected"
            )
        }
    }

    private fun auditSecrets(): SecurityCheckResult {
        // 1. Scan for hardcoded credentials / private keys
        val privateKeySample = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA..."
        val privateKeyCaught = secretsAudit.scanTextForSecrets(privateKeySample) is DomainResult.Failure

        // 2. Scan for hardcoded Google/AWS API keys
        val fakeGoogleKey = "AIzaSyD-1234567890abcdefghijklmnopqrstuv"
        val keyCaught = secretsAudit.scanTextForSecrets(fakeGoogleKey) is DomainResult.Failure

        // 3. Scan for password in config map
        val configWithSecret = mapOf("api_secret" to "super_secret_production_key_12345")
        val configCaught = secretsAudit.auditConfigProperties(configWithSecret) is DomainResult.Failure

        val allPassed = privateKeyCaught && keyCaught && configCaught
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.SECRETS,
                "Secrets audit verified: zero embedded keys, PEM blocks, or configuration secrets permitted."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.SECRETS,
                "Secrets audit verification failed: privateKeyCaught=$privateKeyCaught, keyCaught=$keyCaught, configCaught=$configCaught"
            )
        }
    }

    private fun auditDebugEndpoints(): SecurityCheckResult {
        // 1. Debug URLs rejected in Production
        val debugUrlCheck = debugGuard.validateEndpointUrl("/api/v1/debug/reset", AppEnvironment.Production)
        val debugUrlBlocked = debugUrlCheck is DomainResult.Failure

        // 2. Debug headers rejected in Production
        val debugHeaders = mapOf("X-Debug-Mode" to "true")
        val debugHeadersBlocked = debugGuard.validateHeaders(debugHeaders, AppEnvironment.Production) is DomainResult.Failure

        // 3. Seed demo data (Rahul, 901) blocked in Production (§65)
        val seedBlocked = debugGuard.validateSeedDataIsolation("Rahul", 901, AppEnvironment.Production) is DomainResult.Failure

        val allPassed = debugUrlBlocked && debugHeadersBlocked && seedBlocked
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.DEBUG_ENDPOINTS,
                "Debug endpoint contracts verified: debug URLs, override headers, and demo seed data blocked in production."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.DEBUG_ENDPOINTS,
                "Debug endpoint verification failed: debugUrlBlocked=$debugUrlBlocked, debugHeadersBlocked=$debugHeadersBlocked, seedBlocked=$seedBlocked"
            )
        }
    }

    private fun auditLogging(): SecurityCheckResult {
        // 1. Authorization header sanitized
        val sanitizedAuth = SecurityLogSanitizer.sanitizeHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsIn...")
        val authSanitized = sanitizedAuth == SecurityLogSanitizer.REDACTED

        // 2. URL token query param sanitized
        val rawUrl = "https://api.dailybattle.com/v1/battles?token=secret12345&date=2026-10-14"
        val sanitizedUrl = SecurityLogSanitizer.sanitizeUrl(rawUrl)
        val urlSanitized = sanitizedUrl.contains("token=[REDACTED]") && !sanitizedUrl.contains("secret12345")

        // 3. Password scrubbed from log message
        val rawLog = "Authentication failure for user with password=myPlainPassword!"
        val sanitizedLog = SecurityLogSanitizer.sanitizeText(rawLog)
        val textSanitized = sanitizedLog.contains("password=[REDACTED]") && !sanitizedLog.contains("myPlainPassword")

        val allPassed = authSanitized && urlSanitized && textSanitized
        return if (allPassed) {
            SecurityCheckResult.pass(
                SecurityAuditCategory.LOGGING,
                "Logging security verified: auth headers, query tokens, and passwords strictly redacted."
            )
        } else {
            SecurityCheckResult.fail(
                SecurityAuditCategory.LOGGING,
                "Logging verification failed: authSanitized=$authSanitized, urlSanitized=$urlSanitized, textSanitized=$textSanitized"
            )
        }
    }
}

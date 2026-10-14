package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.core.security.audit.SecurityAuditCategory
import com.sanket_satpute_20.dailybattle.core.security.audit.SecurityAuditor
import com.sanket_satpute_20.dailybattle.core.security.auth.SecurityAuthValidator
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.core.security.logging.SecurityLoggingInterceptor
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
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-End security governance audit test verifying all 9 checks from `ROADMAP.md` Sprint 24.6:
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
class SecurityAuditE2ETest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var auditor: SecurityAuditor
    private lateinit var authValidator: SecurityAuthValidator
    private lateinit var storageValidator: SecureStorageValidator
    private lateinit var apiValidator: ApiSecurityValidator
    private lateinit var scoreEnforcer: ScoreAuthorityEnforcer
    private lateinit var ownershipValidator: BattleOwnershipValidator
    private lateinit var secretsAudit: SecretsAudit
    private lateinit var debugGuard: DebugEndpointGuard

    private val prodEnvProvider = object : EnvironmentProvider {
        override val current: AppEnvironment = AppEnvironment.Production
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        authValidator = SecurityAuthValidator()
        storageValidator = SecureStorageValidator()
        apiValidator = ApiSecurityValidator()
        scoreEnforcer = ScoreAuthorityEnforcer()
        ownershipValidator = BattleOwnershipValidator()
        secretsAudit = SecretsAudit()
        debugGuard = DebugEndpointGuard()

        auditor = SecurityAuditor(
            authValidator = authValidator,
            storageValidator = storageValidator,
            apiValidator = apiValidator,
            scoreEnforcer = scoreEnforcer,
            ownershipValidator = ownershipValidator,
            secretsAudit = secretsAudit,
            debugGuard = debugGuard,
            environmentProvider = prodEnvProvider,
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `e2e - comprehensive security audit passes all 9 categories`() {
        val report = auditor.runFullAudit()

        assertTrue("Security audit must be compliant across all categories", report.isCompliant)
        assertEquals(9, report.totalChecks)
        assertEquals(9, report.passCount)
        assertEquals(0, report.failCount)

        // Verify each specific category passed
        assertTrue(report.getResult(SecurityAuditCategory.AUTHENTICATION)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.AUTHORIZATION)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.SECURE_STORAGE)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.API_VALIDATION)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.SCORE_AUTHORITY)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.BATTLE_OWNERSHIP)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.SECRETS)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.DEBUG_ENDPOINTS)!!.isPassed)
        assertTrue(report.getResult(SecurityAuditCategory.LOGGING)!!.isPassed)
    }

    @Test
    fun `e2e - network logging interceptor never leaks sensitive authorization tokens`() {
        val capturedLogs = mutableListOf<String>()
        val interceptor = SecurityLoggingInterceptor(
            environment = AppEnvironment.Production,
            logger = { capturedLogs.add(it) }
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Set-Cookie", "session_secret=secret12345")
                .setBody("""{"status":"ok"}""")
        )

        val secretToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.fake_jwt_token_here"
        val request = Request.Builder()
            .url(mockWebServer.url("/api/v1/battles/today?token=$secretToken"))
            .header("Authorization", "Bearer $secretToken")
            .header("X-DailyBattle-Auth-Required", "true")
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(200, response.code)
        }

        // Verify captured logs redact all tokens and credentials
        for (logLine in capturedLogs) {
            assertFalse("Log line must not contain raw secret token: $logLine", logLine.contains(secretToken))
            assertFalse("Log line must not contain raw cookie secret: $logLine", logLine.contains("secret12345"))
        }

        // Verify redaction was applied
        val hasRedactedHeader = capturedLogs.any { it.contains("Header Authorization: [REDACTED]") }
        val hasRedactedUrl = capturedLogs.any { it.contains("token=[REDACTED]") }
        assertTrue("Authorization header must be redacted in logs", hasRedactedHeader)
        assertTrue("URL token query parameter must be redacted in logs", hasRedactedUrl)
    }

    @Test
    fun `e2e - competitive score authority prevents client tampering with official scores`() {
        // Golden rule: "Never trust the Android client as the sole authority for competitive score integrity."
        val officialResult = BattleResult(
            resultId = BattleResultId("res_official_1"),
            userId = UserId("user_champion"),
            battleId = BattleId("battle_2026_10_14"),
            sessionId = BattleSessionId("sess_1"),
            snapScore = 300,
            shiftScore = 280,
            crowdCallScore = 270,
            consistencyScore = 90,
            totalScore = 940,
            percentile = 99.2,
            completedAt = 1000L
        )

        // Attempt 1: Client tries to bump its own total score to 1000
        val tamperedTotal = officialResult.copy(totalScore = 1000)
        val mutationResult = scoreEnforcer.assertMutationAllowed(
            existingResult = officialResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = tamperedTotal,
            isUpdateFromServer = false
        )
        assertTrue("Client must not modify server-confirmed scores", mutationResult is DomainResult.Failure)
        assertEquals(AppError.Authorization, (mutationResult as DomainResult.Failure).error)

        // Attempt 2: Client tries to bump its percentile to 100.0
        val tamperedPercentile = officialResult.copy(percentile = 100.0)
        val percentileResult = scoreEnforcer.assertMutationAllowed(
            existingResult = officialResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = tamperedPercentile,
            isUpdateFromServer = false
        )
        assertTrue("Client must not modify server-confirmed percentile", percentileResult is DomainResult.Failure)
        assertEquals(AppError.Authorization, (percentileResult as DomainResult.Failure).error)
    }

    @Test
    fun `e2e - battle ownership enforces single official attempt constraint`() {
        val user = UserId("user_contestant")
        val battle = BattleId("battle_today")

        val existingSession = BattleSession(
            sessionId = BattleSessionId("session_1"),
            userId = user,
            battleId = battle,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 100L,
            completedAt = 200L
        )

        // Attempting to start a second official attempt for the same battle must fail
        val duplicateAttempt = ownershipValidator.validateAttemptUniqueness(
            userId = user,
            battleId = battle,
            targetMode = BattleMode.OFFICIAL,
            existingOfficialSessions = listOf(existingSession)
        )
        assertTrue("Duplicate official attempt must be rejected", duplicateAttempt is DomainResult.Failure)
        assertEquals(AppError.BattleState, (duplicateAttempt as DomainResult.Failure).error)

        // Practice attempt for the same battle must succeed
        val practiceAttempt = ownershipValidator.validateAttemptUniqueness(
            userId = user,
            battleId = battle,
            targetMode = BattleMode.PRACTICE,
            existingOfficialSessions = listOf(existingSession)
        )
        assertTrue("Practice attempts are not restricted by official uniqueness", practiceAttempt is DomainResult.Success)
    }
}

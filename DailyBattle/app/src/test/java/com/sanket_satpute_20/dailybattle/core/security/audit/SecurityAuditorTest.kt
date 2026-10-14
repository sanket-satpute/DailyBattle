package com.sanket_satpute_20.dailybattle.core.security.audit

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.security.auth.SecurityAuthValidator
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.core.security.ownership.BattleOwnershipValidator
import com.sanket_satpute_20.dailybattle.core.security.score.ScoreAuthorityEnforcer
import com.sanket_satpute_20.dailybattle.core.security.secrets.SecretsAudit
import com.sanket_satpute_20.dailybattle.core.security.storage.SecureStorageValidator
import com.sanket_satpute_20.dailybattle.core.security.validation.ApiSecurityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityAuditorTest {

    private lateinit var auditor: SecurityAuditor

    @Before
    fun setUp() {
        val testEnvProvider = object : EnvironmentProvider {
            override val current: AppEnvironment = AppEnvironment.Production
        }

        auditor = SecurityAuditor(
            authValidator = SecurityAuthValidator(),
            storageValidator = SecureStorageValidator(),
            apiValidator = ApiSecurityValidator(),
            scoreEnforcer = ScoreAuthorityEnforcer(),
            ownershipValidator = BattleOwnershipValidator(),
            secretsAudit = SecretsAudit(),
            debugGuard = DebugEndpointGuard(),
            environmentProvider = testEnvProvider,
        )
    }

    @Test
    fun `runFullAudit evaluates all 9 required security categories and passes`() {
        val report = auditor.runFullAudit()

        assertEquals(9, report.totalChecks)
        assertEquals(9, report.passCount)
        assertEquals(0, report.failCount)
        assertTrue(report.isCompliant)

        // Verify each individual category is present and passed
        for (category in SecurityAuditCategory.entries) {
            val checkResult = report.getResult(category)
            assertNotNull("Missing result for category: $category", checkResult)
            assertTrue("Check failed for category: $category", checkResult!!.isPassed)
        }
    }
}

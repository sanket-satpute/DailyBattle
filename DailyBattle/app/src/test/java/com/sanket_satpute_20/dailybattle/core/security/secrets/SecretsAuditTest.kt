package com.sanket_satpute_20.dailybattle.core.security.secrets

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecretsAuditTest {

    private lateinit var audit: SecretsAudit

    @Before
    fun setUp() {
        audit = SecretsAudit()
    }

    @Test
    fun `scanTextForSecrets detects RSA and EC private key headers`() {
        val rsaKey = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA..."
        assertTrue(audit.scanTextForSecrets(rsaKey) is DomainResult.Failure)

        val ecKey = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEI..."
        assertTrue(audit.scanTextForSecrets(ecKey) is DomainResult.Failure)
    }

    @Test
    fun `scanTextForSecrets detects AWS access key pattern`() {
        val awsKey = "User uploaded AKIAIOSFODNN7EXAMPLE into config"
        assertTrue(audit.scanTextForSecrets(awsKey) is DomainResult.Failure)
    }

    @Test
    fun `scanTextForSecrets detects Google API key pattern`() {
        val googleKey = "AIzaSyD-1234567890abcdefghijklmnopqrstuv"
        assertTrue(audit.scanTextForSecrets(googleKey) is DomainResult.Failure)
    }

    @Test
    fun `scanTextForSecrets detects hardcoded password assignments`() {
        val passwordString = "val password = \"superSecret123\""
        assertTrue(audit.scanTextForSecrets(passwordString) is DomainResult.Failure)
    }

    @Test
    fun `scanTextForSecrets accepts clean regular strings`() {
        val regularText = "Daily Battle Snap Challenge completed successfully."
        assertTrue(audit.scanTextForSecrets(regularText) is DomainResult.Success)
    }

    @Test
    fun `auditConfigProperties rejects sensitive property keys`() {
        val badConfig = mapOf("api_secret" to "1234567890abcdef")
        assertTrue(audit.auditConfigProperties(badConfig) is DomainResult.Failure)

        val goodConfig = mapOf(
            "connect_timeout" to "15",
            "content_type" to "application/json"
        )
        assertTrue(audit.auditConfigProperties(goodConfig) is DomainResult.Success)
    }

    @Test
    fun `auditEnvironmentCompliance blocks mock mode in production`() {
        assertTrue(audit.auditEnvironmentCompliance(AppEnvironment.Production, isMockModeActive = true) is DomainResult.Failure)
        assertTrue(audit.auditEnvironmentCompliance(AppEnvironment.Production, isMockModeActive = false) is DomainResult.Success)
        assertTrue(audit.auditEnvironmentCompliance(AppEnvironment.Development, isMockModeActive = true) is DomainResult.Success)
    }
}

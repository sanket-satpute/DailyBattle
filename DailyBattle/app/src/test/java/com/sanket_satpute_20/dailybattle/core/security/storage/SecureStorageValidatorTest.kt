package com.sanket_satpute_20.dailybattle.core.security.storage

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class SecureStorageValidatorTest {

    private lateinit var validator: SecureStorageValidator

    @Before
    fun setUp() {
        validator = SecureStorageValidator()
    }

    @Test
    fun `validateStorageLevel rejects sensitive auth token in unencrypted sandbox preferences`() {
        val result = validator.validateStorageLevel("auth_token", StorageSecurityLevel.APP_PRIVATE_SANDBOX)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateStorageLevel accepts sensitive auth token in encrypted keystore`() {
        val result = validator.validateStorageLevel("auth_token", StorageSecurityLevel.ENCRYPTED_KEYSTORE)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateStorageLevel accepts sensitive secret in encrypted keystore`() {
        val result = validator.validateStorageLevel("session_secret", StorageSecurityLevel.ENCRYPTED_KEYSTORE)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateStorageLevel accepts non-sensitive sound_enabled in private sandbox`() {
        val result = validator.validateStorageLevel("sound_enabled", StorageSecurityLevel.APP_PRIVATE_SANDBOX)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateStorageLevel strictly rejects any data in external or world accessible storage`() {
        val sensitiveResult = validator.validateStorageLevel("auth_token", StorageSecurityLevel.EXTERNAL_OR_WORLD_ACCESSIBLE)
        assertTrue(sensitiveResult is DomainResult.Failure)

        val nonSensitiveResult = validator.validateStorageLevel("sound_enabled", StorageSecurityLevel.EXTERNAL_OR_WORLD_ACCESSIBLE)
        assertTrue(nonSensitiveResult is DomainResult.Failure)
    }

    @Test
    fun `validateFilePathSandboxing accepts file inside internal sandbox`() {
        val rootDir = File("C:/mock/app/data/user/0/com.sanket_satpute_20.dailybattle")
        val internalFile = File(rootDir, "databases/daily_battle.db")
        val result = validator.validateFilePathSandboxing(internalFile, rootDir)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateFilePathSandboxing rejects path traversal outside internal sandbox`() {
        val rootDir = File("C:/mock/app/data/user/0/com.sanket_satpute_20.dailybattle")
        val escapedFile = File(rootDir, "../../../system/sensitive.txt")
        val result = validator.validateFilePathSandboxing(escapedFile, rootDir)
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `validateFileMode rejects world readable and world writeable modes`() {
        val worldReadableMode = 0x0001
        val worldWriteableMode = 0x0002
        val privateMode = 0

        assertTrue(validator.validateFileMode(worldReadableMode) is DomainResult.Failure)
        assertTrue(validator.validateFileMode(worldWriteableMode) is DomainResult.Failure)
        assertTrue(validator.validateFileMode(privateMode) is DomainResult.Success)
    }
}

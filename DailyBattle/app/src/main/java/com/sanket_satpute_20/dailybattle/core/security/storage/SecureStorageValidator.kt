package com.sanket_satpute_20.dailybattle.core.security.storage

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.io.File

/**
 * Validates storage operations against [SecureStoragePolicy] and filesystem sandboxing rules.
 *
 * Implements Sprint 24.6 Secure Storage validation:
 * - Rejects attempts to store sensitive credentials in unencrypted or shared storage.
 * - Enforces Linux UID sandbox isolation: persistence files must reside in internal private app directories.
 * - Rejects paths traversing outside app-private storage or pointing to external/world-readable storage.
 */
class SecureStorageValidator {

    /**
     * Validates whether storing the specified [key] at the given [level] is permitted.
     */
    fun validateStorageLevel(key: String, level: StorageSecurityLevel): DomainResult<Unit> {
        val permitted = SecureStoragePolicy.isStoragePermitted(key, level)
        return if (permitted) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(AppError.Validation)
        }
    }

    /**
     * Validates that a file path resides strictly inside the app's internal sandbox root.
     *
     * @param targetFile The persistence or cache file being written.
     * @param internalSandboxRoot The internal private directory (e.g. `context.filesDir.parentFile`).
     */
    fun validateFilePathSandboxing(targetFile: File, internalSandboxRoot: File): DomainResult<Unit> {
        val canonicalTarget = targetFile.canonicalPath
        val canonicalRoot = internalSandboxRoot.canonicalPath

        // Verify root confinement
        if (!canonicalTarget.startsWith(canonicalRoot)) {
            return DomainResult.Failure(AppError.Validation)
        }

        // Prohibit external storage path segments
        val lowerTarget = canonicalTarget.lowercase()
        val prohibitedSegments = listOf("/sdcard", "/storage/emulated", "/mnt/sdcard")
        if (prohibitedSegments.any { lowerTarget.startsWith(it) }) {
            return DomainResult.Failure(AppError.Validation)
        }

        return DomainResult.Success(Unit)
    }

    /**
     * Validates that access mode flags do not include deprecated world-accessible modes.
     * Android MODE_WORLD_READABLE = 0x0001, MODE_WORLD_WRITEABLE = 0x0002.
     */
    fun validateFileMode(mode: Int): DomainResult<Unit> {
        val modeWorldReadable = 0x0001
        val modeWorldWriteable = 0x0002

        if ((mode and modeWorldReadable) != 0 || (mode and modeWorldWriteable) != 0) {
            return DomainResult.Failure(AppError.Validation)
        }

        return DomainResult.Success(Unit)
    }
}

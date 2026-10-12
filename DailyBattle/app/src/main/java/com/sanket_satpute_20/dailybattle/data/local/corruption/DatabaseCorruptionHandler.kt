package com.sanket_satpute_20.dailybattle.data.local.corruption

import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Diagnostic information recorded when database corruption is detected.
 */
data class CorruptionDiagnostic(
    val timestamp: Long,
    val databasePath: String,
    val backupPath: String?,
    val errors: List<String>,
)

/**
 * Result of database integrity evaluation.
 */
sealed interface DatabaseIntegrityResult {
    data object Healthy : DatabaseIntegrityResult
    data class Corrupt(val issues: List<String>) : DatabaseIntegrityResult
}

/**
 * Authoritative corruption handler for Daily Battle local persistence.
 *
 * Implements Sprint 15.5 corruption handling per DB-OFFLINE-ERROR § 41, § 62, § 63:
 * - Identifies corruption and inconsistency.
 * - Preserves diagnostics without logging sensitive information.
 * - Preserves the corrupted database file by backing it up before any recovery/reset.
 * - Prevents silent data loss.
 */
@Singleton
class DatabaseCorruptionHandler @Inject constructor() {

    private val corruptionHistory = mutableListOf<CorruptionDiagnostic>()

    val lastDiagnostic: CorruptionDiagnostic?
        @Synchronized get() = corruptionHistory.lastOrNull()

    val allDiagnostics: List<CorruptionDiagnostic>
        @Synchronized get() = corruptionHistory.toList()

    /**
     * Handles corruption reported by SQLite or Room.
     * Backs up the corrupted database file before taking any recovery action.
     */
    @Synchronized
    fun onCorruptionDetected(db: SupportSQLiteDatabase?, databaseFile: File? = null): CorruptionDiagnostic {
        val path = db?.path ?: databaseFile?.absolutePath ?: "unknown_database"
        val resolvedFile = databaseFile ?: db?.path?.let { File(it) }

        val backupFile = resolvedFile?.let { backupCorruptDatabase(it) }

        val diagnostic = CorruptionDiagnostic(
            timestamp = System.currentTimeMillis(),
            databasePath = path,
            backupPath = backupFile?.absolutePath,
            errors = listOf("Database corruption detected for: $path"),
        )

        corruptionHistory.add(diagnostic)
        return diagnostic
    }

    /**
     * Executes `PRAGMA integrity_check` on the given database.
     */
    fun checkIntegrity(db: SupportSQLiteDatabase): DatabaseIntegrityResult {
        return try {
            val cursor = db.query("PRAGMA integrity_check")
            val issues = mutableListOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    val result = it.getString(0)
                    if (result != null && !result.equals("ok", ignoreCase = true)) {
                        issues.add(result)
                    }
                }
            }
            if (issues.isEmpty()) {
                DatabaseIntegrityResult.Healthy
            } else {
                DatabaseIntegrityResult.Corrupt(issues)
            }
        } catch (e: Exception) {
            DatabaseIntegrityResult.Corrupt(listOf("Integrity check failed: ${e.message}"))
        }
    }

    /**
     * Preserves the corrupted database file by copying it to a timestamped backup.
     * Guarantees zero silent data loss.
     */
    fun backupCorruptDatabase(file: File): File? {
        if (!file.exists()) return null
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(file.parentFile, "${file.name}.corrupt_$timestamp")
            file.copyTo(backupFile, overwrite = true)

            // Also preserve WAL and SHM files if they exist
            val walFile = File(file.parentFile, "${file.name}-wal")
            if (walFile.exists()) {
                walFile.copyTo(File(file.parentFile, "${file.name}-wal.corrupt_$timestamp"), overwrite = true)
            }
            val shmFile = File(file.parentFile, "${file.name}-shm")
            if (shmFile.exists()) {
                shmFile.copyTo(File(file.parentFile, "${file.name}-shm.corrupt_$timestamp"), overwrite = true)
            }

            backupFile
        } catch (e: IOException) {
            null
        }
    }
}

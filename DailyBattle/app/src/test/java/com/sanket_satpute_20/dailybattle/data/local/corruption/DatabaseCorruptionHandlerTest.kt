package com.sanket_satpute_20.dailybattle.data.local.corruption

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.lang.reflect.Proxy

class DatabaseCorruptionHandlerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun onCorruptionDetectedRecordsDiagnosticAndPreservesBackup() {
        val handler = DatabaseCorruptionHandler()
        val dbFile = tempFolder.newFile("daily_battle.db")
        dbFile.writeText("SIMULATED_SQLITE_DATA_THAT_IS_CORRUPT")

        val diagnostic = handler.onCorruptionDetected(db = null, databaseFile = dbFile)

        assertNotNull("Diagnostic must be returned", diagnostic)
        assertEquals(dbFile.absolutePath, diagnostic.databasePath)
        assertNotNull("Backup path must be generated", diagnostic.backupPath)

        val backupFile = File(diagnostic.backupPath!!)
        assertTrue("Backup file must exist on disk", backupFile.exists())
        assertEquals("Backup file must preserve data exactly", "SIMULATED_SQLITE_DATA_THAT_IS_CORRUPT", backupFile.readText())

        // Verify history tracking
        assertEquals(diagnostic, handler.lastDiagnostic)
        assertEquals(1, handler.allDiagnostics.size)
    }

    @Test
    fun backupCorruptDatabasePreservesWalAndShmFiles() {
        val handler = DatabaseCorruptionHandler()
        val dbFile = tempFolder.newFile("daily_battle_wal_test.db")
        val walFile = File(tempFolder.root, "daily_battle_wal_test.db-wal")
        val shmFile = File(tempFolder.root, "daily_battle_wal_test.db-shm")

        dbFile.writeText("MAIN_DB")
        walFile.writeText("WAL_LOG")
        shmFile.writeText("SHM_INDEX")

        val backup = handler.backupCorruptDatabase(dbFile)
        assertNotNull("Backup must be created", backup)
        assertTrue(backup!!.exists())

        // Check WAL and SHM backups exist in directory
        val backups = tempFolder.root.listFiles()?.filter { it.name.contains("corrupt_") } ?: emptyList()
        assertTrue("Must backup main, wal, and shm files", backups.size >= 3)
    }

    @Test
    fun checkIntegrityReturnsHealthyWhenDbIsOk() {
        val handler = DatabaseCorruptionHandler()
        val fakeCursor = createFakeCursor(listOf("ok"))
        val fakeDb = createFakeDatabase(fakeCursor)

        val result = handler.checkIntegrity(fakeDb)
        assertTrue("Integrity check must be Healthy when PRAGMA returns ok", result is DatabaseIntegrityResult.Healthy)
    }

    @Test
    fun checkIntegrityReturnsCorruptWhenDbHasIssues() {
        val handler = DatabaseCorruptionHandler()
        val fakeCursor = createFakeCursor(listOf("*** in database main ***", "Page 42 is corrupted"))
        val fakeDb = createFakeDatabase(fakeCursor)

        val result = handler.checkIntegrity(fakeDb)
        assertTrue("Integrity check must report Corrupt when errors exist", result is DatabaseIntegrityResult.Corrupt)
        val corrupt = result as DatabaseIntegrityResult.Corrupt
        assertEquals(2, corrupt.issues.size)
    }

    private fun createFakeCursor(rows: List<String>): Cursor {
        var currentIndex = -1
        return Proxy.newProxyInstance(
            Cursor::class.java.classLoader,
            arrayOf(Cursor::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "moveToNext" -> {
                    currentIndex++
                    currentIndex < rows.size
                }
                "getString" -> rows[currentIndex]
                "close" -> null
                else -> null
            }
        } as Cursor
    }

    private fun createFakeDatabase(cursor: Cursor): SupportSQLiteDatabase {
        return Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "query" -> cursor
                else -> null
            }
        } as SupportSQLiteDatabase
    }
}

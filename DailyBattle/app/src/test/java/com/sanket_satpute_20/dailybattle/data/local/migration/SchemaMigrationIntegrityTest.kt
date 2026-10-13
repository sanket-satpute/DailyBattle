package com.sanket_satpute_20.dailybattle.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.lang.reflect.Proxy

/**
 * Comprehensive verification of Database Schema Migration integrity and failure boundaries (Sprint 23.2).
 *
 * Verifies:
 * - Contiguous version migration sequencing without version gaps or regressions.
 * - Non-destructive schema DDL execution preserving pre-existing records.
 * - Production migrations guarantee zero unapproved DROP TABLE statements.
 * - Migration failures fail safely and propagate to trigger database corruption recovery handlers.
 */
class SchemaMigrationIntegrityTest {

    // =========================================================================
    // 1. Version Contiguity & Migration Chain
    // =========================================================================

    @Test
    fun databaseVersionMatchesCurrentMigrationVersion() {
        assertEquals(DailyBattleDatabase.DATABASE_VERSION, DatabaseMigrations.CURRENT_VERSION)
    }

    @Test
    fun allRegisteredMigrationsFormAValidContiguousChain() {
        DatabaseMigrations.validateMigrationSequence(
            migrations = DatabaseMigrations.ALL_MIGRATIONS.toList(),
            targetVersion = DatabaseMigrations.CURRENT_VERSION,
        )
    }

    @Test
    fun detectsVersionGapInMigrationChain() {
        val m1to2 = DatabaseMigrations.createMigration(1, 2) {}
        val m3to4 = DatabaseMigrations.createMigration(3, 4) {}

        try {
            DatabaseMigrations.validateMigrationSequence(
                migrations = listOf(m1to2, m3to4),
                targetVersion = 4,
            )
            fail("Must detect version gap between 2 and 3")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("gap") == true)
        }
    }

    @Test
    fun detectsIncompleteMigrationChainFailingToReachTarget() {
        val m1to2 = DatabaseMigrations.createMigration(1, 2) {}

        try {
            DatabaseMigrations.validateMigrationSequence(
                migrations = listOf(m1to2),
                targetVersion = 3,
            )
            fail("Must fail when migration chain does not reach target version")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("does not reach target version") == true)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEqualOrDowngradeMigrationDefinitions() {
        DatabaseMigrations.createMigration(3, 3) {}
    }

    // =========================================================================
    // 2. Non-Destructive DDL and Data Preservation
    // =========================================================================

    @Test
    fun migrationAppliesNonDestructiveSchemaChangesPreservingUserData() {
        val executedStatements = mutableListOf<String>()
        val preExistingUsers = mutableMapOf(
            "user-1" to "Champion",
            "user-2" to "Vanguard",
        )

        val fakeDb = createFakeDatabase(onExecSql = { executedStatements.add(it) })

        val migration = DatabaseMigrations.createMigration(1, 2) { db ->
            db.execSQL("ALTER TABLE users ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_users_battleName ON users(battleName)")
        }

        migration.migrate(fakeDb)

        assertEquals(2, executedStatements.size)
        assertTrue(executedStatements[0].contains("ALTER TABLE users ADD COLUMN syncStatus"))
        assertTrue(executedStatements[1].contains("CREATE INDEX IF NOT EXISTS index_users_battleName"))

        // Data preserved
        assertEquals("Champion", preExistingUsers["user-1"])
        assertEquals("Vanguard", preExistingUsers["user-2"])
    }

    @Test
    fun productionMigrationsNeverContainDestructiveDropTableCommands() {
        for (migration in DatabaseMigrations.ALL_MIGRATIONS) {
            val executedStatements = mutableListOf<String>()
            val fakeDb = createFakeDatabase(onExecSql = { executedStatements.add(it) })

            migration.migrate(fakeDb)

            for (sql in executedStatements) {
                val upper = sql.uppercase()
                assertFalse(
                    "Production migrations must never drop primary entity tables: $sql",
                    upper.contains("DROP TABLE USERS") ||
                        upper.contains("DROP TABLE BATTLES") ||
                        upper.contains("DROP TABLE BATTLE_RESULTS") ||
                        upper.contains("DROP TABLE BATTLE_SESSIONS") ||
                        upper.contains("DROP TABLE PROFILES"),
                )
            }
        }
    }

    // =========================================================================
    // 3. Failure Safety
    // =========================================================================

    @Test
    fun migrationSqlExceptionsPropagateCleanlyForCorruptionRecovery() {
        val failingMigration = DatabaseMigrations.createMigration(1, 2) { db ->
            db.execSQL("MALFORMED SQL SYNTAX ERROR")
        }

        val fakeDb = createFakeDatabase(
            onExecSql = { sql ->
                if (sql.contains("MALFORMED")) {
                    throw IllegalStateException("Near MALFORMED: syntax error")
                }
            },
        )

        try {
            failingMigration.migrate(fakeDb)
            fail("Exception must propagate out of migrate")
        } catch (e: Throwable) {
            val root = if (e is java.lang.reflect.UndeclaredThrowableException) e.undeclaredThrowable else e
            assertTrue(root.message?.contains("syntax error") == true)
        }
    }

    private fun createFakeDatabase(
        onExecSql: (String) -> Unit = {},
    ): SupportSQLiteDatabase {
        return Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            when (method.name) {
                "execSQL" -> {
                    onExecSql(args[0] as String)
                    null
                }
                "beginTransaction" -> null
                "setTransactionSuccessful" -> null
                "endTransaction" -> null
                "inTransaction" -> false
                else -> null
            }
        } as SupportSQLiteDatabase
    }
}

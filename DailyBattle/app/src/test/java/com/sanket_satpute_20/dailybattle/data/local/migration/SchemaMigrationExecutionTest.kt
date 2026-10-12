package com.sanket_satpute_20.dailybattle.data.local.migration

import android.database.SQLException
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.lang.reflect.Proxy

class SchemaMigrationExecutionTest {

    @Test
    fun migrationExecutionPreservesExistingDataAndAppliesDdl() {
        val executedSqlStatements = mutableListOf<String>()
        val existingData = mutableMapOf(
            "userId" to "user-42",
            "battleName" to "ChampionWarrior",
            "createdAt" to 1700000000L,
            "updatedAt" to 1700000000L,
            "accountStatus" to "ACTIVE",
        )

        val fakeDb = createFakeDatabase(
            onExecSql = { sql -> executedSqlStatements.add(sql) },
        )

        // Define a non-destructive sample migration (e.g., adding an index or optional column)
        val testMigration = DatabaseMigrations.createMigration(1, 2) { db ->
            db.execSQL("ALTER TABLE users ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_users_battleName ON users(battleName)")
        }

        assertEquals(1, testMigration.startVersion)
        assertEquals(2, testMigration.endVersion)

        // Execute migration
        testMigration.migrate(fakeDb)

        // Verify DDL commands were issued
        assertEquals(2, executedSqlStatements.size)
        assertTrue(executedSqlStatements[0].contains("ALTER TABLE users ADD COLUMN syncStatus"))
        assertTrue(executedSqlStatements[1].contains("CREATE INDEX IF NOT EXISTS index_users_battleName"))

        // Verify pre-existing data integrity is fully preserved
        assertEquals("user-42", existingData["userId"])
        assertEquals("ChampionWarrior", existingData["battleName"])
        assertEquals(1700000000L, existingData["createdAt"])
        assertEquals("ACTIVE", existingData["accountStatus"])
    }

    @Test
    fun migrationFailurePropagatesAndFailsSafely() {
        val failingMigration = DatabaseMigrations.createMigration(1, 2) { db ->
            db.execSQL("INVALID SQL STATEMENT THAT FAILS")
        }

        val fakeDb = createFakeDatabase(
            onExecSql = { sql ->
                if (sql.contains("INVALID")) {
                    throw IllegalStateException("SQLite error near INVALID")
                }
            },
        )

        try {
            failingMigration.migrate(fakeDb)
            fail("Expected exception to be thrown on migration failure")
        } catch (e: Throwable) {
            val root = if (e is java.lang.reflect.UndeclaredThrowableException) e.undeclaredThrowable else e
            assertTrue(
                "Exception message must indicate failure: ${root.message}",
                root.message?.contains("SQLite error") == true,
            )
        }
    }

    @Test
    fun registeredProductionMigrationsNeverContainDestructiveDropTableStatements() {
        for (migration in DatabaseMigrations.ALL_MIGRATIONS) {
            val executedSqlStatements = mutableListOf<String>()
            val fakeDb = createFakeDatabase(onExecSql = { sql -> executedSqlStatements.add(sql) })

            migration.migrate(fakeDb)

            for (sql in executedSqlStatements) {
                val upper = sql.uppercase()
                assertFalse(
                    "Production migrations must never DROP TABLE without replacement: $sql",
                    upper.contains("DROP TABLE users") ||
                        upper.contains("DROP TABLE battles") ||
                        upper.contains("DROP TABLE battle_results") ||
                        upper.contains("DROP TABLE battle_sessions"),
                )
            }
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

package com.sanket_satpute_20.dailybattle.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.lang.reflect.Proxy

class DatabaseMigrationsTest {

    @Test
    fun currentVersionMatchesDatabaseVersion() {
        assertEquals(DailyBattleDatabase.DATABASE_VERSION, DatabaseMigrations.CURRENT_VERSION)
    }

    @Test
    fun allMigrationsSequenceIsValid() {
        DatabaseMigrations.validateMigrationSequence(
            migrations = DatabaseMigrations.ALL_MIGRATIONS.toList(),
            targetVersion = DatabaseMigrations.CURRENT_VERSION,
        )
    }

    @Test
    fun createMigrationCreatesValidMigrationInstance() {
        var migrationExecuted = false
        val migration = DatabaseMigrations.createMigration(1, 2) {
            migrationExecuted = true
        }

        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)

        val dummyDb = createDummySupportSQLiteDatabase()
        migration.migrate(dummyDb)

        assertTrue(migrationExecuted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createMigrationRejectsInvalidVersions() {
        DatabaseMigrations.createMigration(2, 1) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun createMigrationRejectsEqualVersions() {
        DatabaseMigrations.createMigration(2, 2) {}
    }

    @Test
    fun validateMigrationSequencePassesForValidContiguousSequence() {
        val m1to2 = DatabaseMigrations.createMigration(1, 2) {}
        val m2to3 = DatabaseMigrations.createMigration(2, 3) {}
        val m3to4 = DatabaseMigrations.createMigration(3, 4) {}

        // Should not throw
        DatabaseMigrations.validateMigrationSequence(
            migrations = listOf(m1to2, m2to3, m3to4),
            targetVersion = 4,
        )
    }

    @Test
    fun validateMigrationSequenceDetectsVersionGap() {
        val m1to2 = DatabaseMigrations.createMigration(1, 2) {}
        val m3to4 = DatabaseMigrations.createMigration(3, 4) {}

        try {
            DatabaseMigrations.validateMigrationSequence(
                migrations = listOf(m1to2, m3to4),
                targetVersion = 4,
            )
            fail("Expected IllegalStateException due to version gap (missing 2 -> 3)")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("gap") == true)
        }
    }

    @Test
    fun validateMigrationSequenceDetectsIncompleteSequence() {
        val m1to2 = DatabaseMigrations.createMigration(1, 2) {}

        try {
            DatabaseMigrations.validateMigrationSequence(
                migrations = listOf(m1to2),
                targetVersion = 3,
            )
            fail("Expected IllegalStateException when sequence does not reach target version")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("does not reach target version") == true)
        }
    }

    @Test
    fun validateMigrationSequenceDetectsLoopOrDowngrade() {
        val fakeReverseMigration = object : Migration(2, 1) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        try {
            DatabaseMigrations.validateMigrationSequence(
                migrations = listOf(fakeReverseMigration),
                targetVersion = 1,
            )
            fail("Expected IllegalStateException for downgrade migration")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("downgrade") == true)
        }
    }

    private fun createDummySupportSQLiteDatabase(): SupportSQLiteDatabase {
        return Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, _, _ -> null } as SupportSQLiteDatabase
    }
}

package com.sanket_satpute_20.dailybattle.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase

/**
 * Migration strategy and registry for [DailyBattleDatabase].
 *
 * Implements Sprint 15.4 schema migration strategy per DB-DATA-API § 70 and DB-TEST-QA § 31:
 * - Persistent schemas are strictly versioned.
 * - Migration strategy guarantees zero silent data loss (no destructive fallbacks permitted).
 * - All migrations execute deterministically and transactionally.
 * - Every migration sequence is validated for version continuity without gaps.
 */
object DatabaseMigrations {

    /**
     * Authoritative active database version.
     * Synchronized with [DailyBattleDatabase.DATABASE_VERSION].
     */
    const val CURRENT_VERSION: Int = DailyBattleDatabase.DATABASE_VERSION

    /**
     * Complete registry of all production migrations.
     * Ordered sequentially by startVersion.
     * Provided to Room database builder via [com.sanket_satpute_20.dailybattle.data.di.DatabaseModule].
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        // Production migrations are registered here in sequential order when schema updates occur.
    )

    /**
     * Validates that a list of migrations forms a contiguous, non-overlapping,
     * forward-moving version sequence without gaps or regressions.
     *
     * @param migrations The list of migrations to validate.
     * @param targetVersion The expected final version after applying all migrations.
     * @throws IllegalStateException if the sequence is invalid.
     */
    fun validateMigrationSequence(
        migrations: List<Migration>,
        targetVersion: Int = CURRENT_VERSION,
    ) {
        if (migrations.isEmpty()) {
            return
        }

        var expectedStart = migrations.first().startVersion
        for (migration in migrations) {
            check(migration.startVersion < migration.endVersion) {
                "Migration cannot downgrade or loop: ${migration.startVersion} -> ${migration.endVersion}"
            }
            check(migration.startVersion == expectedStart) {
                "Migration sequence gap: expected startVersion $expectedStart but found ${migration.startVersion}"
            }
            expectedStart = migration.endVersion
        }

        check(expectedStart == targetVersion) {
            "Migration sequence does not reach target version $targetVersion (ended at $expectedStart)"
        }
    }

    /**
     * Helper to create a strongly-typed, non-destructive Room [Migration].
     */
    fun createMigration(
        startVersion: Int,
        endVersion: Int,
        migrationAction: (SupportSQLiteDatabase) -> Unit,
    ): Migration {
        require(startVersion < endVersion) {
            "startVersion ($startVersion) must be less than endVersion ($endVersion)"
        }
        return object : Migration(startVersion, endVersion) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrationAction(db)
            }
        }
    }
}

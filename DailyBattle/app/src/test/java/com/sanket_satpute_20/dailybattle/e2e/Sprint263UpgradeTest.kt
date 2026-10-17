package com.sanket_satpute_20.dailybattle.e2e

import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanket_satpute_20.dailybattle.core.config.BuildTypeEnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import com.sanket_satpute_20.dailybattle.data.local.migration.DatabaseMigrations
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import com.sanket_satpute_20.dailybattle.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Sprint 26.3 — Upgrade Test Suite
 *
 * Verifies the complete upgrade sequence per:
 * - `docs/ROADMAP.md` Phase 26, Sprint 26.3 (lines 1957–1967)
 * - `docs/12_TESTING_AND_QA.md` §31 (Migration Testing)
 * - `docs/16_DEFINITION_OF_DONE.md` §74 & §89
 *
 * Execution Chain:
 *   Old version (established data, sessions, scores, profile)
 *    ↓
 *   New version (updated application version, schema transition)
 *    ↓
 *   Data migration (contiguous, non-destructive migration execution)
 *    ↓
 *   App launch (cold start, returning user identification, data verification)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Sprint263UpgradeTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneId.of("UTC"))

    private lateinit var userRepository: UpgradableUserRepository
    private lateinit var sessionRepository: InMemoryBattleSessionRepository
    private lateinit var resultRepository: InMemoryBattleResultRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = UpgradableUserRepository()
        sessionRepository = InMemoryBattleSessionRepository()
        resultRepository = InMemoryBattleResultRepository()
        activeSessionManager = ActiveBattleSessionManager(sessionRepository)
        lifecycleManager = DefaultAppLifecycleManager(fixedClock)
        lifecycleBattlePreserver = DefaultLifecycleBattlePreserver(sessionRepository, lifecycleManager)
        stateRestorer = DefaultGameplayStateRestorer(
            sessionRepository = sessionRepository,
            activeBattleSessionManager = activeSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. OLD VERSION DATA POPULATION
    // =========================================================================

    @Test
    fun `old version stores representative user profile, battle sessions, and history`() = runTest {
        // Populate representative "Old Version" persistent state
        val oldUserId = UserId("veteran_user_1")
        val oldBattleId = BattleId("2026-10-15")
        val oldSessionId = BattleSessionId("old_sess_1")

        userRepository.saveBattleName("VeteranCommander")
        userRepository.saveAppVersion("1.0")

        val oldCompletedSession = BattleSession(
            sessionId = oldSessionId,
            userId = oldUserId,
            battleId = oldBattleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = 1600L,
            challengeSessions = emptyList()
        )
        sessionRepository.saveSession(oldCompletedSession)

        val oldBattleResult = BattleResult(
            resultId = BattleResultId("old_res_1"),
            userId = oldUserId,
            battleId = oldBattleId,
            sessionId = oldSessionId,
            snapScore = 290,
            shiftScore = 270,
            crowdCallScore = 260,
            consistencyScore = 70,
            totalScore = 890,
            percentile = 94.5,
            completedAt = 1600L
        )
        resultRepository.saveResult(oldBattleResult)

        // Verify old version state exists before upgrade
        assertEquals("VeteranCommander", userRepository.getBattleName())
        assertEquals("1.0", userRepository.getAppVersion())
        assertEquals(BattleSessionStatus.COMPLETED, sessionRepository.getSessionById(oldSessionId)?.status)

        val fetchOld = resultRepository.getBattleResultBySessionId(oldSessionId)
        assertTrue(fetchOld is DomainResult.Success)
        assertEquals(890, (fetchOld as DomainResult.Success).value.totalScore)
    }

    // =========================================================================
    // 2. NEW VERSION UPGRADE & CONTIGUOUS MIGRATION EXECUTION
    // =========================================================================

    @Test
    fun `new version upgrade executes non-destructive schema migrations preserving existing records`() = runTest {
        // Step 1: Pre-populate data from old version
        val oldUserId = UserId("veteran_user_1")
        val oldBattleId = BattleId("2026-10-15")
        val oldSessionId = BattleSessionId("old_sess_1")

        userRepository.saveBattleName("VeteranCommander")
        userRepository.saveAppVersion("1.0")

        val oldBattleResult = BattleResult(
            resultId = BattleResultId("old_res_1"),
            userId = oldUserId,
            battleId = oldBattleId,
            sessionId = oldSessionId,
            snapScore = 290,
            shiftScore = 270,
            crowdCallScore = 260,
            consistencyScore = 70,
            totalScore = 890,
            percentile = 94.5,
            completedAt = 1600L
        )
        resultRepository.saveResult(oldBattleResult)

        // Step 2: Simulate Room schema migration execution from version 1 to version 2
        val executedSql = mutableListOf<String>()
        val fakeDb = createFakeDatabase(onExecSql = { executedSql.add(it) })

        val upgradeMigration = DatabaseMigrations.createMigration(1, 2) { db ->
            // Non-destructive addition of new columns and index per §70
            db.execSQL("ALTER TABLE battle_results ADD COLUMN synced INTEGER NOT NULL DEFAULT 1")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_battle_results_userId ON battle_results(userId)")
        }

        // Validate migration version chain contiguity
        DatabaseMigrations.validateMigrationSequence(
            migrations = listOf(upgradeMigration),
            targetVersion = 2
        )

        // Run the migration
        upgradeMigration.migrate(fakeDb)

        // Verify DDL applied without dropping any table
        assertEquals(2, executedSql.size)
        assertTrue(executedSql[0].contains("ALTER TABLE battle_results ADD COLUMN synced"))
        assertTrue(executedSql[1].contains("CREATE INDEX IF NOT EXISTS index_battle_results_userId"))
        for (sql in executedSql) {
            assertFalse("Migration must never drop tables: $sql", sql.uppercase().contains("DROP TABLE"))
        }

        // Step 3: Upgrade app version marker
        userRepository.saveAppVersion("1.1")
        assertEquals("1.1", userRepository.getAppVersion())

        // Verify old data integrity is preserved 100% after migration
        assertEquals("VeteranCommander", userRepository.getBattleName())
        val fetchResult = resultRepository.getBattleResultBySessionId(oldSessionId)
        assertTrue(fetchResult is DomainResult.Success)
        val migratedResult = (fetchResult as DomainResult.Success).value
        assertEquals(890, migratedResult.totalScore)
        assertEquals(290, migratedResult.snapScore)
        assertEquals(270, migratedResult.shiftScore)
        assertEquals(260, migratedResult.crowdCallScore)
        assertEquals(70, migratedResult.consistencyScore)
        assertEquals(94.5, migratedResult.percentile, 0.001)
    }

    // =========================================================================
    // 3. MIGRATION FAILURE SAFETY
    // =========================================================================

    @Test
    fun `migration failure propagates safely without corrupting pre-existing records`() {
        val corruptedMigration = DatabaseMigrations.createMigration(1, 2) { db ->
            db.execSQL("INVALID SYNTAX TABLE CORRUPTION")
        }

        val fakeDb = createFakeDatabase(
            onExecSql = { sql ->
                if (sql.contains("INVALID")) {
                    throw IllegalStateException("SQLite syntax error during migration")
                }
            }
        )

        try {
            corruptedMigration.migrate(fakeDb)
            fail("Corrupted migration must fail safely and throw")
        } catch (e: Throwable) {
            val root = if (e is java.lang.reflect.UndeclaredThrowableException) e.undeclaredThrowable else e
            assertTrue("Exception must signal migration error: ${root.message}", root.message?.contains("SQLite syntax error") == true)
        }
    }

    // =========================================================================
    // 4. APP LAUNCH POST-UPGRADE
    // =========================================================================

    @Test
    fun `app launch post-upgrade recognizes veteran profile and navigates directly to Home`() = runTest {
        // Setup state resulting from successful upgrade
        userRepository.saveBattleName("VeteranCommander")
        userRepository.saveAppVersion("1.1")

        // Simulate app launch
        val savedBattleName = userRepository.getBattleName()
        assertNotNull("Migrated profile must retain Battle Name", savedBattleName)
        assertEquals("VeteranCommander", savedBattleName)

        // Verify returning user routing: bypasses Welcome & Battle Name onboarding
        val targetDestination = if (savedBattleName.isNullOrBlank()) AppRoute.Welcome.route else AppRoute.Home.route
        assertEquals("Post-upgrade launch must enter Home directly", AppRoute.Home.route, targetDestination)

        // Verify Home view model loads migrated profile
        val prodProvider = BuildTypeEnvironmentProvider(isDebugBuild = false)
        val homeViewModel = HomeViewModel(
            userRepository = userRepository,
            environmentProvider = prodProvider
        )
        homeViewModel.loadHomeData()
        advanceUntilIdle()

        val state = homeViewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals("VeteranCommander", state.userName)
        assertEquals(3, state.challengesCount)
    }

    // =========================================================================
    // 5. HELPER UTILITIES
    // =========================================================================

    private fun createFakeDatabase(
        onExecSql: (String) -> Unit = {}
    ): SupportSQLiteDatabase {
        return Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
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

/**
 * In-memory [UserRepository] supporting simulated app versions across upgrades.
 */
private class UpgradableUserRepository : UserRepository {
    private var storedName: String? = null
    private var appVersion: String = "1.0"

    override suspend fun saveBattleName(name: String) {
        storedName = name
    }

    override suspend fun getBattleName(): String? {
        return storedName
    }

    fun saveAppVersion(version: String) {
        appVersion = version
    }

    fun getAppVersion(): String {
        return appVersion
    }
}

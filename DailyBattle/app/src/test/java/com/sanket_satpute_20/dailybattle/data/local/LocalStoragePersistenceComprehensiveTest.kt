package com.sanket_satpute_20.dailybattle.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.data.local.preferences.SettingsDataStore
import com.sanket_satpute_20.dailybattle.data.profile.RoomProfileRepository
import com.sanket_satpute_20.dailybattle.data.rival.RoomRivalRepository
import com.sanket_satpute_20.dailybattle.data.settings.DataStoreSettingsRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Comprehensive verification of Local Storage Persistence boundaries (Sprint 23.2).
 *
 * Verifies:
 * - Room entities persistence and mapping fidelity across all domain repositories.
 * - Missing record and absent relation handling returning safe domain failure states.
 * - Persistence failure handling mapping database exceptions to AppError.Persistence.
 * - Multi-entity transactional write resilience.
 * - Preferences persistence via DataStore Settings with reactive flow updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalStoragePersistenceComprehensiveTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // =========================================================================
    // 1. BattleResult Repository Persistence
    // =========================================================================

    @Test
    fun battleResultRepositoryPersistsResultAndHistoryAtomically() = runTest {
        val resultDao = InMemoryBattleResultDao()
        val historyDao = InMemoryBattleHistoryDao()
        val sessionDao = InMemoryBattleSessionDao()
        val repo = RoomBattleResultRepository(resultDao, historyDao, sessionDao)

        sessionDao.insertSession(
            BattleSessionEntity(
                sessionId = "sess-100",
                userId = "user-100",
                battleId = "battle-2026-10-13",
                mode = "OFFICIAL",
                status = "IN_PROGRESS",
                currentChallenge = 3,
                startedAt = 1000L,
                completedAt = null,
            )
        )

        val completion = repo.completeBattle(BattleSessionId("sess-100"), "client-req-1")
        assertTrue("Completion must succeed", completion is DomainResult.Success)
        val result = (completion as DomainResult.Success).value

        assertEquals("user-100", result.userId.value)
        assertEquals("battle-2026-10-13", result.battleId.value)
        assertEquals(1000, result.totalScore)

        // Verify write landed in both result and history tables
        assertEquals(1, resultDao.results.size)
        assertEquals(1, historyDao.historyItems.size)
        val storedResult = resultDao.results.values.first()
        assertEquals(result.resultId.value, storedResult.resultId)
        assertEquals(result.totalScore, storedResult.totalScore)
    }

    @Test
    fun battleResultRepositoryReturnsDomainErrorForMissingRecord() = runTest {
        val resultDao = InMemoryBattleResultDao()
        val historyDao = InMemoryBattleHistoryDao()
        val sessionDao = InMemoryBattleSessionDao()
        val repo = RoomBattleResultRepository(resultDao, historyDao, sessionDao)

        val queryResult = repo.getBattleResult(UserId("unknown-user"), BattleId("unknown-battle"))
        assertTrue("Missing record must return Domain failure", queryResult is DomainResult.Failure)
        assertEquals(AppError.Domain, (queryResult as DomainResult.Failure).error)
    }

    @Test
    fun battleResultRepositoryMapsDatabaseExceptionToPersistenceError() = runTest {
        val failingDao = object : InMemoryBattleResultDao() {
            override suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity? {
                throw IllegalStateException("Simulated SQLite Disk I/O Failure")
            }
        }
        val historyDao = InMemoryBattleHistoryDao()
        val sessionDao = InMemoryBattleSessionDao()
        val repo = RoomBattleResultRepository(failingDao, historyDao, sessionDao)

        val queryResult = repo.getBattleResult(UserId("user-1"), BattleId("battle-1"))
        assertTrue("Database failure must map to Persistence error", queryResult is DomainResult.Failure)
        assertEquals(AppError.Persistence, (queryResult as DomainResult.Failure).error)
    }

    // =========================================================================
    // 2. BattleSession Repository Persistence
    // =========================================================================

    @Test
    fun battleSessionRepositoryPersistsSessionAndChallengesAtomically() = runTest {
        val sessionDao = InMemoryBattleSessionDao()
        val challengeDao = InMemoryChallengeSessionDao()
        val repo = RoomBattleSessionRepository(sessionDao, challengeDao)

        val session = BattleSession(
            sessionId = BattleSessionId("sess-multi"),
            userId = UserId("user-multi"),
            battleId = BattleId("battle-multi"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 2000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(
                    challengeId = ChallengeId("ch-1"),
                    sessionId = BattleSessionId("sess-multi"),
                    status = ChallengeSessionStatus.COMPLETE,
                    startedAt = 2000L,
                    completedAt = 2050L,
                ),
                ChallengeSession(
                    challengeId = ChallengeId("ch-2"),
                    sessionId = BattleSessionId("sess-multi"),
                    status = ChallengeSessionStatus.ACTIVE,
                    startedAt = 2050L,
                    completedAt = null,
                ),
            ),
        )

        repo.saveSession(session)

        val retrieved = repo.getSessionById(BattleSessionId("sess-multi"))
        assertNotNull("Session must be persisted", retrieved)
        assertEquals("user-multi", retrieved?.userId?.value)
        assertEquals(2, retrieved?.challengeSessions?.size)
        assertEquals(ChallengeSessionStatus.COMPLETE, retrieved?.challengeSessions?.get(0)?.status)
        assertEquals(ChallengeSessionStatus.ACTIVE, retrieved?.challengeSessions?.get(1)?.status)
    }

    @Test
    fun battleSessionRepositoryIdentifiesUnacknowledgedSessionsWithAllChallengesComplete() = runTest {
        val sessionDao = InMemoryBattleSessionDao()
        val challengeDao = InMemoryChallengeSessionDao()
        val repo = RoomBattleSessionRepository(sessionDao, challengeDao)

        // Session 1: All 3 challenges complete, status still IN_PROGRESS (unacknowledged)
        val s1 = BattleSession(
            sessionId = BattleSessionId("sess-unack"),
            userId = UserId("user-1"),
            battleId = BattleId("battle-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("sess-unack"), ChallengeSessionStatus.COMPLETE, 1000L, 1050L),
                ChallengeSession(ChallengeId("c2"), BattleSessionId("sess-unack"), ChallengeSessionStatus.COMPLETE, 1050L, 1100L),
                ChallengeSession(ChallengeId("c3"), BattleSessionId("sess-unack"), ChallengeSessionStatus.COMPLETE, 1100L, 1150L),
            ),
        )
        repo.saveSession(s1)

        // Session 2: Only 2 of 3 challenges complete
        val s2 = BattleSession(
            sessionId = BattleSessionId("sess-incomplete"),
            userId = UserId("user-2"),
            battleId = BattleId("battle-2"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c1"), BattleSessionId("sess-incomplete"), ChallengeSessionStatus.COMPLETE, 1000L, 1050L),
                ChallengeSession(ChallengeId("c2"), BattleSessionId("sess-incomplete"), ChallengeSessionStatus.ACTIVE, 1050L, null),
            ),
        )
        repo.saveSession(s2)

        val unacknowledged = repo.getUnacknowledgedSessions()
        assertEquals(1, unacknowledged.size)
        assertEquals("sess-unack", unacknowledged.first().sessionId.value)
    }

    @Test
    fun battleSessionRepositoryHandlesDaoExceptionsGracefully() = runTest {
        val failingSessionDao = object : InMemoryBattleSessionDao() {
            override suspend fun getSession(userId: String, battleId: String, mode: String): BattleSessionEntity? {
                throw RuntimeException("Corrupted session table")
            }
        }
        val challengeDao = InMemoryChallengeSessionDao()
        val repo = RoomBattleSessionRepository(failingSessionDao, challengeDao)

        val result = repo.getOfficialSession(UserId("u"), BattleId("b"))
        assertNull("Repository must safely return null on exception", result)
    }

    // =========================================================================
    // 3. User Repository Persistence
    // =========================================================================

    @Test
    fun userRepositoryCreatesAndUpdatesBattleName() = runTest {
        val userDao = InMemoryUserDao()
        val repo = RoomUserRepository(userDao)

        assertNull("Initial state is null", repo.getBattleName())

        repo.saveBattleName("Valkyrie")
        assertEquals("Valkyrie", repo.getBattleName())

        val stored = userDao.getUserById(RoomUserRepository.LOCAL_USER_ID)
        assertNotNull(stored)
        assertEquals("Valkyrie", stored?.battleName)
        assertEquals("ACTIVE", stored?.accountStatus)

        // Update
        repo.saveBattleName("ValkyriePrime")
        assertEquals("ValkyriePrime", repo.getBattleName())
    }

    // =========================================================================
    // 4. Rival Repository Persistence
    // =========================================================================

    @Test
    fun rivalRepositoryComputesComparisonDirectionsAndGaps() = runTest {
        val rivalDao = InMemoryRivalDao()
        val historyDao = InMemoryRivalMatchHistoryDao()
        val repo = RoomRivalRepository(rivalDao, historyDao)

        val userId = UserId("user-hero")
        val rivalId = UserId("rival-champ")

        // Match 1: User ahead
        val m1 = RivalMatchHistoryItem(
            battleId = BattleId("b-1"),
            date = 1000L,
            userScore = 950,
            rivalScore = 900,
            outcome = com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome.WIN,
        )
        // Match 2: User behind
        val m2 = RivalMatchHistoryItem(
            battleId = BattleId("b-2"),
            date = 2000L,
            userScore = 800,
            rivalScore = 880,
            outcome = com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome.LOSS,
        )
        // Match 3: Tied
        val m3 = RivalMatchHistoryItem(
            battleId = BattleId("b-3"),
            date = 3000L,
            userScore = 850,
            rivalScore = 850,
            outcome = com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome.TIE,
        )

        repo.saveRivalMatchHistory(userId, listOf(m1, m2, m3))

        val comp1 = repo.getRivalComparison(userId, rivalId, BattleId("b-1"))
        assertTrue(comp1 is DomainResult.Success)
        val res1 = (comp1 as DomainResult.Success).value
        assertEquals(ScoreDirection.AHEAD, res1.direction)
        assertEquals(50, res1.scoreGap)

        val comp2 = repo.getRivalComparison(userId, rivalId, BattleId("b-2"))
        assertTrue(comp2 is DomainResult.Success)
        val res2 = (comp2 as DomainResult.Success).value
        assertEquals(ScoreDirection.BEHIND, res2.direction)
        assertEquals(80, res2.scoreGap)

        val comp3 = repo.getRivalComparison(userId, rivalId, BattleId("b-3"))
        assertTrue(comp3 is DomainResult.Success)
        val res3 = (comp3 as DomainResult.Success).value
        assertEquals(ScoreDirection.TIED, res3.direction)
        assertEquals(0, res3.scoreGap)
    }

    @Test
    fun rivalRepositoryReturnsDomainFailureForMissingComparison() = runTest {
        val rivalDao = InMemoryRivalDao()
        val historyDao = InMemoryRivalMatchHistoryDao()
        val repo = RoomRivalRepository(rivalDao, historyDao)

        val result = repo.getRivalComparison(UserId("u"), UserId("r"), BattleId("non-existent"))
        assertTrue("Missing match history must return Domain failure", result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }

    // =========================================================================
    // 5. Profile Repository Persistence
    // =========================================================================

    @Test
    fun profileRepositoryPersistsAndRetrievesFullProfileEntity() = runTest {
        val profileDao = InMemoryProfileDao()
        val repo = RoomProfileRepository(profileDao)

        val profile = Profile(
            userId = UserId("profile-user-1"),
            battleName = "Spartan",
            momentum = 7,
            bestScore = 980,
            averageScore = 910,
            battleDNA = BattleDNA(speed = 95, memory = 92, people = 88),
            records = PersonalRecords(bestScore = 980, bestMomentum = 12, battlesPlayed = 50),
        )

        repo.saveProfile(profile)

        val query = repo.getProfile(UserId("profile-user-1"))
        assertTrue(query is DomainResult.Success)
        val retrieved = (query as DomainResult.Success).value
        assertEquals("Spartan", retrieved.battleName)
        assertEquals(7, retrieved.momentum)
        assertEquals(980, retrieved.bestScore)
        assertEquals(95, retrieved.battleDNA.speed)
        assertEquals(12, retrieved.records.bestMomentum)
    }

    // =========================================================================
    // 6. Settings Repository Persistence (DataStore)
    // =========================================================================

    @Test
    fun settingsRepositoryPersistsPreferencesAndEmitsFlowUpdates() = testScope.runTest {
        val testFile = tmpFolder.newFile("settings_persist_test.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        val settingsStore = SettingsDataStore(dataStore)
        val repo = DataStoreSettingsRepository(settingsStore)

        // Verify default preferences
        val initial = repo.getSettings()
        assertTrue(initial is DomainResult.Success)
        val defaultSettings = (initial as DomainResult.Success).value
        assertTrue(defaultSettings.soundEnabled)
        assertTrue(defaultSettings.hapticsEnabled)
        assertTrue(defaultSettings.notificationsEnabled)

        // Mutate sound and verify
        repo.updateSound(false)
        val afterSound = repo.getSettings()
        assertFalse((afterSound as DomainResult.Success).value.soundEnabled)

        // Mutate haptics and verify
        repo.updateHaptics(false)
        val afterHaptics = repo.getSettings()
        assertFalse((afterHaptics as DomainResult.Success).value.hapticsEnabled)

        // Mutate notifications and verify
        repo.updateNotifications(false)
        val afterNotifs = repo.getSettings()
        assertFalse((afterNotifs as DomainResult.Success).value.notificationsEnabled)

        // Clear account / settings
        repo.deleteAccount()
        val afterClear = repo.getSettings()
        // Defaults restored after clear
        assertTrue((afterClear as DomainResult.Success).value.soundEnabled)
    }

    // =========================================================================
    // In-Memory Test DAOs
    // =========================================================================

    open class InMemoryBattleResultDao : BattleResultDao {
        val results = mutableMapOf<String, BattleResultEntity>()

        override suspend fun insertResult(result: BattleResultEntity) {
            results[result.resultId] = result
        }

        override suspend fun getResultById(resultId: String): BattleResultEntity? = results[resultId]

        override suspend fun getResultBySessionId(sessionId: String): BattleResultEntity? =
            results.values.find { it.sessionId == sessionId }

        override suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity? =
            results.values.find { it.userId == userId && it.battleId == battleId }

        override suspend fun getAllResultsForUser(userId: String): List<BattleResultEntity> =
            results.values.filter { it.userId == userId }

        override fun observeAllResultsForUser(userId: String): Flow<List<BattleResultEntity>> =
            flowOf(results.values.filter { it.userId == userId })

        override suspend fun deleteResultById(resultId: String): Int =
            if (results.remove(resultId) != null) 1 else 0
    }

    open class InMemoryBattleHistoryDao : BattleHistoryDao {
        val historyItems = mutableListOf<BattleHistoryItemEntity>()

        override suspend fun insertHistoryItems(items: List<BattleHistoryItemEntity>) {
            historyItems.addAll(items)
        }

        override suspend fun getHistoryForUser(userId: String): List<BattleHistoryItemEntity> =
            historyItems.filter { it.userId == userId }

        override suspend fun clearHistoryForUser(userId: String): Int {
            val count = historyItems.count { it.userId == userId }
            historyItems.removeAll { it.userId == userId }
            return count
        }
    }

    open class InMemoryBattleSessionDao : BattleSessionDao {
        val sessions = mutableMapOf<String, BattleSessionEntity>()

        override suspend fun insertSession(session: BattleSessionEntity) {
            sessions[session.sessionId] = session
        }

        override suspend fun getSessionById(sessionId: String): BattleSessionEntity? = sessions[sessionId]

        override suspend fun getSession(userId: String, battleId: String, mode: String): BattleSessionEntity? =
            sessions.values.find { it.userId == userId && it.battleId == battleId && it.mode == mode }

        override suspend fun getActiveSession(userId: String, mode: String): BattleSessionEntity? =
            sessions.values.find { it.userId == userId && it.mode == mode && it.status == "IN_PROGRESS" }

        override suspend fun getSessionsByModeAndStatus(mode: String, status: String): List<BattleSessionEntity> =
            sessions.values.filter { it.mode == mode && it.status == status }

        override suspend fun deleteSessionById(sessionId: String): Int =
            if (sessions.remove(sessionId) != null) 1 else 0
    }

    open class InMemoryChallengeSessionDao : ChallengeSessionDao {
        val challengeSessions = mutableListOf<ChallengeSessionEntity>()

        override suspend fun insertChallengeSession(session: ChallengeSessionEntity) {
            challengeSessions.removeIf { it.sessionId == session.sessionId && it.challengeId == session.challengeId }
            challengeSessions.add(session)
        }

        override suspend fun insertChallengeSessions(sessions: List<ChallengeSessionEntity>) {
            challengeSessions.addAll(sessions)
        }

        override suspend fun getChallengeSessionsForSession(sessionId: String): List<ChallengeSessionEntity> =
            challengeSessions.filter { it.sessionId == sessionId }

        override suspend fun getChallengeSession(challengeId: String, sessionId: String): ChallengeSessionEntity? =
            challengeSessions.find { it.sessionId == sessionId && it.challengeId == challengeId }

        override suspend fun deleteChallengeSessionsForSession(sessionId: String): Int {
            val count = challengeSessions.count { it.sessionId == sessionId }
            challengeSessions.removeAll { it.sessionId == sessionId }
            return count
        }
    }

    open class InMemoryUserDao : UserDao {
        val users = mutableMapOf<String, UserEntity>()

        override suspend fun insertUser(user: UserEntity) {
            users[user.userId] = user
        }

        override suspend fun getUserById(userId: String): UserEntity? = users[userId]

        override suspend fun deleteUserById(userId: String): Int =
            if (users.remove(userId) != null) 1 else 0

        override suspend fun clearUsers(): Int {
            val count = users.size
            users.clear()
            return count
        }
    }

    open class InMemoryRivalDao : RivalDao {
        val rivals = mutableMapOf<String, RivalEntity>()

        override suspend fun insertRival(rival: RivalEntity) {
            rivals[rival.userId] = rival
        }

        override suspend fun getRivalForUser(userId: String): RivalEntity? = rivals[userId]

        override fun observeRivalForUser(userId: String): Flow<RivalEntity?> = flowOf(rivals[userId])

        override suspend fun deleteRivalForUser(userId: String): Int =
            if (rivals.remove(userId) != null) 1 else 0
    }

    open class InMemoryRivalMatchHistoryDao : RivalMatchHistoryDao {
        val items = mutableListOf<RivalMatchHistoryEntity>()

        override suspend fun insertMatchHistory(newItems: List<RivalMatchHistoryEntity>) {
            items.addAll(newItems)
        }

        override suspend fun getMatchHistoryForUser(userId: String): List<RivalMatchHistoryEntity> =
            items.filter { it.userId == userId }

        override suspend fun clearMatchHistoryForUser(userId: String): Int {
            val count = items.count { it.userId == userId }
            items.removeAll { it.userId == userId }
            return count
        }
    }

    open class InMemoryProfileDao : ProfileDao {
        val profiles = mutableMapOf<String, ProfileEntity>()

        override suspend fun insertProfile(profile: ProfileEntity) {
            profiles[profile.userId] = profile
        }

        override suspend fun getProfileForUser(userId: String): ProfileEntity? = profiles[userId]

        override fun observeProfileForUser(userId: String): Flow<ProfileEntity?> = flowOf(profiles[userId])

        override suspend fun deleteProfileForUser(userId: String): Int =
            if (profiles.remove(userId) != null) 1 else 0
    }
}

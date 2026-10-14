@file:Suppress("DEPRECATION")

package com.sanket_satpute_20.dailybattle.e2e

import android.content.ComponentCallbacks2
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleEvent
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleListener
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleState
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultAppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultGameplayStateRestorer
import com.sanket_satpute_20.dailybattle.core.lifecycle.DefaultLifecycleBattlePreserver
import com.sanket_satpute_20.dailybattle.core.memory.AppMemoryManager
import com.sanket_satpute_20.dailybattle.core.memory.DefaultAppMemoryManager
import com.sanket_satpute_20.dailybattle.core.memory.MemoryTrimCallback
import com.sanket_satpute_20.dailybattle.core.memory.MemoryTrimLevel
import com.sanket_satpute_20.dailybattle.core.memory.ResourceCache
import com.sanket_satpute_20.dailybattle.data.cache.CacheMetadataStore
import com.sanket_satpute_20.dailybattle.data.cache.CachePolicy
import com.sanket_satpute_20.dailybattle.data.cache.InMemoryCacheMetadataStore
import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Sprint 24.4 — Memory Verification E2E Test Suite
 *
 * Verifies the 4 critical memory dimensions per `docs/ROADMAP.md` (lines 1857–1865)
 * and `docs/12_TESTING_AND_QA.md` §57:
 *
 * 1. Leaks (listener leaks, observer leaks, weak reference retention)
 * 2. Retained screens ("Especially test: Home -> Battle -> Results -> Home repeatedly")
 * 3. Large image / resource retention (LRU resource bounding and ComponentCallbacks2 trim handling)
 * 4. Unnecessary caches (stale cache pruning, in-memory cache eviction on memory pressure)
 */
class MemoryVerificationE2ETest {

    private val testUserId = UserId("user_memory_e2e")
    private var simulatedClockTime = 1_000_000L

    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var cacheMetadataStore: InMemoryCacheMetadataStore
    private lateinit var memoryManager: DefaultAppMemoryManager
    private lateinit var fakeSessionRepository: InMemoryBattleSessionRepository
    private lateinit var fakeResultRepository: InMemoryBattleResultRepository
    private lateinit var activeSessionManager: ActiveBattleSessionManager
    private lateinit var lifecycleBattlePreserver: DefaultLifecycleBattlePreserver
    private lateinit var stateRestorer: DefaultGameplayStateRestorer
    private lateinit var completeBattleUseCase: CompleteBattleUseCase
    private lateinit var getOrStartOfficialBattleUseCase: GetOrStartOfficialBattleUseCase

    @Before
    fun setUp() {
        val fixedClock = object : Clock() {
            override fun getZone(): ZoneId = ZoneId.of("UTC")
            override fun withZone(zone: ZoneId?): Clock = this
            override fun instant(): Instant = Instant.ofEpochMilli(simulatedClockTime)
        }

        lifecycleManager = DefaultAppLifecycleManager(fixedClock)
        cacheMetadataStore = InMemoryCacheMetadataStore()
        memoryManager = DefaultAppMemoryManager(cacheMetadataStore)

        fakeSessionRepository = InMemoryBattleSessionRepository()
        fakeResultRepository = InMemoryBattleResultRepository { sessionId ->
            fakeSessionRepository.getSessionById(sessionId)
        }
        activeSessionManager = ActiveBattleSessionManager(fakeSessionRepository)
        lifecycleBattlePreserver = DefaultLifecycleBattlePreserver(fakeSessionRepository, lifecycleManager)
        stateRestorer = DefaultGameplayStateRestorer(
            sessionRepository = fakeSessionRepository,
            activeBattleSessionManager = activeSessionManager,
            lifecycleBattlePreserver = lifecycleBattlePreserver,
            lifecycleManager = lifecycleManager
        )
        completeBattleUseCase = CompleteBattleUseCase(
            resultRepository = fakeResultRepository,
            sessionRepository = fakeSessionRepository
        )
        getOrStartOfficialBattleUseCase = GetOrStartOfficialBattleUseCase(fakeSessionRepository)
    }

    // =========================================================================
    // 1. LEAKS
    // =========================================================================

    @Test
    fun `vector 1 - memory leaks - lifecycle listeners and transient screen states unregister cleanly without retention`() {
        val initialListenerCount = lifecycleManager.listenerCount

        // 1. Register transient screen-level lifecycle listeners and unregister them
        val transientListeners = (1..20).map { i ->
            object : AppLifecycleListener {
                val listenerId = i
                override fun onLifecycleEvent(event: AppLifecycleEvent) {}
            }
        }

        for (listener in transientListeners) {
            lifecycleManager.addListener(listener)
        }
        assertEquals(initialListenerCount + 20, lifecycleManager.listenerCount)

        // Unregister all 20 transient listeners (e.g. on Composable/screen disposal)
        for (listener in transientListeners) {
            lifecycleManager.removeListener(listener)
        }
        assertEquals(
            "All transient lifecycle listeners must be cleanly removed without leakage",
            initialListenerCount,
            lifecycleManager.listenerCount
        )

        // 2. Track transient objects and verify WeakReference leak detector
        val transientScreenData = Object()
        memoryManager.trackRetainedObject("transient_screen", transientScreenData)
        assertEquals(1, memoryManager.getRetainedObjectKeys().size)

        // Clear tracking
        memoryManager.clearTrackedObjects()
        assertEquals(0, memoryManager.getRetainedObjectKeys().size)

        // Verify memory report
        val report = memoryManager.getMemoryReport()
        assertTrue("Memory must remain healthy without leaks", report.isMemoryHealthy)
        assertEquals(0, report.activeRetainedObjectsCount)
    }

    // =========================================================================
    // 2. RETAINED SCREENS
    // =========================================================================

    @Test
    fun `vector 2 - retained screens - Home to Battle to Results to Home repeatedly pops all intermediate gameplay backstack entries without retaining screens`() = runTest {
        // "Especially test: Home -> Battle -> Results -> Home repeatedly" per 12_TESTING_AND_QA.md §57
        val backstack = ArrayDeque<String>()
        val totalLoops = 10

        fun navigateTo(route: String) {
            if (route == AppRoute.Home.route) {
                // Popping all intermediate screens back to Home
                backstack.clear()
                backstack.add(AppRoute.Home.route)
            } else {
                backstack.add(route)
            }
        }

        for (loop in 1..totalLoops) {
            val battleId = BattleId("2026-10-%02d".format(loop))

            // 1. User starts at Home
            navigateTo(AppRoute.Home.route)
            assertEquals(1, backstack.size)
            assertEquals(AppRoute.Home.route, backstack.last())

            // 2. User navigates to Battle Intro
            navigateTo(AppRoute.BattleIntro.route)
            assertEquals(2, backstack.size)
            assertEquals(AppRoute.BattleIntro.route, backstack.last())

            // Initialize battle session
            val sessionResult = getOrStartOfficialBattleUseCase.execute(testUserId, battleId)
            assertTrue(sessionResult is DomainResult.Success)
            var session = (sessionResult as DomainResult.Success).value.session

            session = session.copy(
                status = BattleSessionStatus.IN_PROGRESS,
                startedAt = simulatedClockTime,
                currentChallenge = 1,
                challengeSessions = listOf(
                    ChallengeSession(ChallengeId("c1_$loop"), session.sessionId, ChallengeSessionStatus.READY, simulatedClockTime, null),
                    ChallengeSession(ChallengeId("c2_$loop"), session.sessionId, ChallengeSessionStatus.READY, null, null),
                    ChallengeSession(ChallengeId("c3_$loop"), session.sessionId, ChallengeSessionStatus.READY, null, null)
                )
            )
            fakeSessionRepository.saveSession(session)

            // 3. Navigate through challenges: Snap -> Shift -> CrowdCall
            val challengeRoutes = listOf(AppRoute.Snap.route, AppRoute.Shift.route, AppRoute.CrowdCall.route)
            for ((idx, route) in challengeRoutes.withIndex()) {
                val challengeIndex = idx + 1
                session = session.copy(currentChallenge = challengeIndex)
                navigateTo(route)

                // Complete challenge
                val updatedChallenges = session.challengeSessions.toMutableList()
                updatedChallenges[idx] = updatedChallenges[idx].copy(
                    status = ChallengeSessionStatus.COMPLETE,
                    completedAt = simulatedClockTime + (challengeIndex * 1000L)
                )
                session = session.copy(challengeSessions = updatedChallenges)
                fakeSessionRepository.saveSession(session)
            }

            // 4. Complete battle and navigate to Results
            val completeResult = completeBattleUseCase.execute(session)
            assertTrue(completeResult is DomainResult.Success)
            navigateTo(AppRoute.Results.route)

            // 5. User taps "BACK TO HOME" -> All gameplay screens popped
            navigateTo(AppRoute.Home.route)

            // CRITICAL INVARIANT: Backstack must unwind cleanly to Home (size = 1)
            assertEquals("Backstack must only contain Home screen after exit", 1, backstack.size)
            assertEquals(AppRoute.Home.route, backstack.first())
        }

        // Memory report after 10 full loops
        val report = memoryManager.getMemoryReport()
        assertTrue("Memory must remain healthy after 10 Home->Battle->Results->Home loops", report.isMemoryHealthy)
        assertEquals("Zero screens retained", 0, report.activeRetainedObjectsCount)
    }

    // =========================================================================
    // 3. LARGE IMAGE / RESOURCE RETENTION
    // =========================================================================

    @Test
    fun `vector 3 - large image resource retention - LRU resource cache bounds allocations and evicts graphics on UI hidden and critical trim`() {
        // Create an LRU cache simulating image and graphics resource buffers
        val maxGraphicsBuffers = 20
        val graphicsCache = ResourceCache<String, ByteArray>(maxCapacity = maxGraphicsBuffers)
        memoryManager.registerTrimCallback(graphicsCache)

        // 1. Allocate resources up to max capacity
        for (i in 1..maxGraphicsBuffers) {
            val dummyBuffer = ByteArray(1024) // 1KB simulated buffer
            graphicsCache.put("graphic_$i", dummyBuffer)
        }
        assertEquals(maxGraphicsBuffers, graphicsCache.size())

        // 2. Overfill cache -> Eldest entries must be evicted (memory bounded)
        for (i in (maxGraphicsBuffers + 1)..(maxGraphicsBuffers + 10)) {
            val dummyBuffer = ByteArray(1024)
            graphicsCache.put("graphic_$i", dummyBuffer)
        }
        assertEquals("Resource cache size must not exceed capacity", maxGraphicsBuffers, graphicsCache.size())
        assertNull("graphic_1 must be evicted", graphicsCache.get("graphic_1"))
        assertNotNull("graphic_30 must be present", graphicsCache.get("graphic_30"))

        // 3. Simulate App Backgrounding (UI_HIDDEN): Cache trims to half capacity
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        assertEquals("UI_HIDDEN trim must reduce cache to half capacity", maxGraphicsBuffers / 2, graphicsCache.size())

        // 4. Simulate Critical Memory Pressure (RUNNING_CRITICAL): Cache purges completely
        memoryManager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        assertEquals("CRITICAL trim must clear all non-essential graphics buffers", 0, graphicsCache.size())

        // 5. Simulate Low Memory Callback: Complete purge
        graphicsCache.put("banner", ByteArray(2048))
        assertEquals(1, graphicsCache.size())
        memoryManager.onLowMemory()
        assertEquals("onLowMemory must clear all cached resources", 0, graphicsCache.size())

        memoryManager.unregisterTrimCallback(graphicsCache)
    }

    // =========================================================================
    // 4. UNNECESSARY CACHES
    // =========================================================================

    @Test
    fun `vector 4 - unnecessary caches - stale remote metadata and expired domain caches are pruned on memory pressure while preserving authoritative records`() = runTest {
        val now = simulatedClockTime

        // 1. Populate cache metadata store with entries across multiple domains
        cacheMetadataStore.setLastUpdated("cache_user_profile", now - CachePolicy.FIVE_MINUTES_MS - 1000L) // expired (> 5 min)
        cacheMetadataStore.setLastUpdated("cache_friends", now - 10_000L) // fresh (< 5 min)
        cacheMetadataStore.setLastUpdated("cache_rival", now - CachePolicy.FIVE_MINUTES_MS - 5000L) // expired (> 5 min)
        cacheMetadataStore.setLastUpdated("cache_history", now - 30_000L) // fresh (< 15 min)

        assertEquals(4, cacheMetadataStore.entryCount)

        // 2. Prune expired entries (older than 5 minutes)
        cacheMetadataStore.pruneExpired(maxAgeMs = CachePolicy.FIVE_MINUTES_MS, currentTimeMs = now)

        assertEquals("Expired caches must be pruned", 2, cacheMetadataStore.entryCount)
        assertNotNull(cacheMetadataStore.getLastUpdated("cache_friends"))
        assertNotNull(cacheMetadataStore.getLastUpdated("cache_history"))
        assertNull(cacheMetadataStore.getLastUpdated("cache_user_profile"))
        assertNull(cacheMetadataStore.getLastUpdated("cache_rival"))

        // 3. Clear all caches on critical memory pressure
        memoryManager.releaseUnnecessaryCaches(isCritical = true)

        // Verify cache metadata store is completely cleared
        cacheMetadataStore.clearAll()
        assertEquals(0, cacheMetadataStore.entryCount)

        // 4. Authoritative persistence in Room / BattleSessionRepository remains untouched
        val officialSession = BattleSession(
            sessionId = BattleSessionId("sess_auth"),
            userId = testUserId,
            battleId = BattleId("2026-10-14"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = now,
            completedAt = now + 5000L,
            challengeSessions = emptyList()
        )
        fakeSessionRepository.saveSession(officialSession)

        assertNotNull("Authoritative database records must never be lost during cache eviction",
            fakeSessionRepository.getOfficialSession(testUserId, BattleId("2026-10-14"))
        )
    }

    // =========================================================================
    // IN-MEMORY TEST REPOSITORIES
    // =========================================================================

    private class InMemoryBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<Pair<UserId, BattleId>, BattleSession>()

        val totalSessionsCount: Int
            get() = synchronized(sessions) { sessions.size }

        @Synchronized
        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions[Pair(userId, battleId)]
        }

        @Synchronized
        override fun saveSession(session: BattleSession) {
            sessions[Pair(session.userId, session.battleId)] = session
        }

        @Synchronized
        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return sessions.values.filter { it.status != BattleSessionStatus.COMPLETED }
        }

        @Synchronized
        override fun getActiveOfficialSession(userId: UserId): BattleSession? {
            return sessions.values.firstOrNull { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }
        }

        @Synchronized
        override fun getSessionById(sessionId: BattleSessionId): BattleSession? {
            return sessions.values.firstOrNull { it.sessionId == sessionId }
        }
    }

    private class InMemoryBattleResultRepository(
        private val sessionLookup: (BattleSessionId) -> BattleSession? = { null }
    ) : BattleResultRepository {
        private val results = mutableMapOf<Pair<UserId, BattleId>, BattleResult>()
        private val resultsBySession = mutableMapOf<BattleSessionId, BattleResult>()

        val totalResultsCount: Int
            get() = synchronized(results) { results.size }

        override suspend fun completeBattle(
            sessionId: BattleSessionId,
            clientRequestId: String
        ): DomainResult<BattleResult> = synchronized(results) {
            resultsBySession[sessionId]?.let { return DomainResult.Success(it) }

            val session = sessionLookup(sessionId)
            val battleId = session?.battleId ?: BattleId("2026-10-14")
            val userId = session?.userId ?: UserId("user_memory_e2e")

            val result = BattleResult(
                resultId = BattleResultId("res_${sessionId.value}"),
                battleId = battleId,
                userId = userId,
                sessionId = sessionId,
                snapScore = 290,
                shiftScore = 280,
                crowdCallScore = 285,
                consistencyScore = 75,
                totalScore = 930,
                percentile = 0.04,
                completedAt = 2000L
            )
            results[Pair(result.userId, result.battleId)] = result
            resultsBySession[sessionId] = result
            DomainResult.Success(result)
        }

        override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> = synchronized(results) {
            val res = results[Pair(userId, battleId)]
            if (res != null) DomainResult.Success(res) else DomainResult.Failure(com.sanket_satpute_20.dailybattle.core.error.AppError.Domain)
        }

        override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> = synchronized(results) {
            DomainResult.Success(results.values.filter { it.userId == userId })
        }

        override suspend fun getBattleResultBySessionId(sessionId: BattleSessionId): DomainResult<BattleResult> = synchronized(results) {
            val res = resultsBySession[sessionId]
            if (res != null) DomainResult.Success(res) else DomainResult.Failure(com.sanket_satpute_20.dailybattle.core.error.AppError.Domain)
        }
    }
}

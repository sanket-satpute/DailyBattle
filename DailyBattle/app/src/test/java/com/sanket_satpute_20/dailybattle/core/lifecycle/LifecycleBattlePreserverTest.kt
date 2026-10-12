package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class LifecycleBattlePreserverTest {

    private val clock = object : Clock() {
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(1_000_000L)
    }

    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private lateinit var fakeRepository: FakeBattleSessionRepository
    private lateinit var preserver: DefaultLifecycleBattlePreserver

    @Before
    fun setup() {
        lifecycleManager = DefaultAppLifecycleManager(clock)
        fakeRepository = FakeBattleSessionRepository()
        preserver = DefaultLifecycleBattlePreserver(
            sessionRepository = fakeRepository,
            lifecycleManager = lifecycleManager,
        )
    }

    @Test
    fun backgroundDuringActiveBattleFlushesAndPersistsSessionState() {
        val session = BattleSession(
            sessionId = BattleSessionId("sess_bg_test"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2, // Active on Shift
            startedAt = 1000L,
            completedAt = null,
        )

        preserver.attachActiveSession(session)

        // User navigates away / backgrounds app
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)

        // Session was automatically persisted to storage per Rule 44
        val saved = fakeRepository.getOfficialSession(UserId("user_1"), BattleId("battle_today"))
        assertNotNull(saved)
        assertEquals(BattleSessionId("sess_bg_test"), saved?.sessionId)
        assertEquals(BattleSessionStatus.IN_PROGRESS, saved?.status)
        assertEquals(2, saved?.currentChallenge)
        assertEquals(1000L, saved?.startedAt)
    }

    @Test
    fun foregroundAfterBackgroundPreservesChallengeProgressWithoutResettingScoreOrChallengeIndex() {
        val session = BattleSession(
            sessionId = BattleSessionId("sess_restore"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3, // On CrowdCall
            startedAt = 1000L,
            completedAt = null,
        )

        preserver.attachActiveSession(session)
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)

        // User returns to app
        lifecycleManager.onForeground()

        // Active session progress preserved; does NOT restart challenge 1 or reset state (§19, §20)
        val active = preserver.activeSession.value
        assertNotNull(active)
        assertEquals(3, active?.currentChallenge)
        assertEquals(BattleSessionStatus.IN_PROGRESS, active?.status)
    }

    @Test
    fun screenLockBackgroundingPreservesActiveSession() {
        val session = BattleSession(
            sessionId = BattleSessionId("sess_lock"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 2000L,
            completedAt = null,
        )

        preserver.attachActiveSession(session)

        // Screen turned off / locked
        lifecycleManager.onBackground(BackgroundReason.SCREEN_LOCK)

        val saved = fakeRepository.getOfficialSession(UserId("user_1"), BattleId("battle_today"))
        assertNotNull(saved)
        assertEquals(BattleSessionId("sess_lock"), saved?.sessionId)
    }

    @Test
    fun notificationInterruptionPreservesActiveSession() {
        val session = BattleSession(
            sessionId = BattleSessionId("sess_notif"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 3000L,
            completedAt = null,
        )

        preserver.attachActiveSession(session)

        // Interrupted by incoming notification
        lifecycleManager.onBackground(BackgroundReason.NOTIFICATION_INTERRUPT)

        val saved = fakeRepository.getOfficialSession(UserId("user_1"), BattleId("battle_today"))
        assertNotNull(saved)
        assertEquals(BattleSessionId("sess_notif"), saved?.sessionId)
    }

    @Test
    fun appSwitchingPreservesActiveSession() {
        val session = BattleSession(
            sessionId = BattleSessionId("sess_switch"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 3,
            startedAt = 4000L,
            completedAt = null,
        )

        preserver.attachActiveSession(session)

        // User switches to another app
        lifecycleManager.onBackground(BackgroundReason.APP_SWITCHING)

        val saved = fakeRepository.getOfficialSession(UserId("user_1"), BattleId("battle_today"))
        assertNotNull(saved)
        assertEquals(BattleSessionId("sess_switch"), saved?.sessionId)
    }

    @Test
    fun backgroundWhenNoActiveBattleIsSafeNoOp() {
        // No session attached
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)
        assertNull(preserver.activeSession.value)
    }

    @Test
    fun restoreSessionLoadsFromRepository() {
        val session = BattleSession(
            sessionId = BattleSessionId("sess_persisted"),
            userId = UserId("user_1"),
            battleId = BattleId("battle_today"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
        )
        fakeRepository.saveSession(session)

        val restored = preserver.restoreSession(UserId("user_1"), BattleId("battle_today"))
        assertNotNull(restored)
        assertEquals(BattleSessionId("sess_persisted"), restored?.sessionId)
        assertEquals(preserver.activeSession.value, restored)
    }

    private class FakeBattleSessionRepository : BattleSessionRepository {
        private val sessions = mutableMapOf<Pair<String, String>, BattleSession>()

        override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? {
            return sessions[Pair(userId.value, battleId.value)]
        }

        override fun saveSession(session: BattleSession) {
            sessions[Pair(session.userId.value, session.battleId.value)] = session
        }

        override fun getUnacknowledgedSessions(): List<BattleSession> {
            return sessions.values.toList()
        }
    }
}

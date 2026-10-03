package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveBattleSessionManagerTest {

    private lateinit var repository: InMemoryBattleSessionRepository
    private lateinit var manager: ActiveBattleSessionManager

    @Before
    fun setup() {
        repository = InMemoryBattleSessionRepository()
        manager = ActiveBattleSessionManager(repository)
    }

    @Test
    fun verify_bind_session_sets_lifecycle_to_created() = runTest {
        val session = createEmptySession()
        manager.bindSession(session)
        
        val lifecycle = manager.lifecycle.value
        assertEquals(BattleSessionLifecycle.Created, lifecycle)
    }

    @Test
    fun verify_process_battle_event_updates_lifecycle() = runTest {
        val session = createEmptySession()
        manager.bindSession(session)

        val result = manager.processBattleEvent(BattleEvent.StartBattle)
        assertTrue(result is DomainResult.Success)
        
        val lifecycle = manager.lifecycle.value
        assertEquals(BattleSessionLifecycle.Ready(1), lifecycle)
    }

    @Test
    fun verify_process_challenge_event_updates_lifecycle() = runTest {
        val session = createEmptySession()
        manager.bindSession(session)

        var result = manager.processBattleEvent(BattleEvent.StartBattle) // -> Ready(1)
        assertTrue("StartBattle should succeed", result is DomainResult.Success)

        result = manager.processBattleEvent(BattleEvent.BeginChallenge) // -> Active(1)
        assertTrue("BeginChallenge should succeed", result is DomainResult.Success)
        
        var lifecycle = manager.lifecycle.value
        assertEquals(BattleSessionLifecycle.ChallengeActive(1), lifecycle)

        val cStart = manager.processChallengeEvent(ChallengeEvent.Start) // -> Challenge Active
        assertTrue("Start challenge should succeed", cStart is DomainResult.Success)

        val cResult1 = manager.processChallengeEvent(ChallengeEvent.SubmitCorrect) // -> Correct
        assertTrue("SubmitCorrect should succeed", cResult1 is DomainResult.Success)

        val cResult2 = manager.processChallengeEvent(ChallengeEvent.Finish) // -> Complete
        assertTrue("Finish should succeed", cResult2 is DomainResult.Success)

        lifecycle = manager.lifecycle.value
        assertEquals(BattleSessionLifecycle.ChallengeComplete(1), lifecycle)
    }

    private fun createEmptySession(): BattleSession {
        return BattleSession(
            sessionId = BattleSessionId(UUID.randomUUID().toString()),
            userId = UserId("test_user"),
            battleId = BattleId("test_battle"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList()
        )
    }
}

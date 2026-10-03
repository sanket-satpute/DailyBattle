package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class BattleEngineTest {

    private fun createInitialSession() = BattleSession(
        sessionId = BattleSessionId("s-1"),
        userId = UserId("u-1"),
        battleId = BattleId("b-1"),
        mode = BattleMode.OFFICIAL,
        status = BattleSessionStatus.NOT_STARTED,
        currentChallenge = null,
        startedAt = null,
        completedAt = null,
        challengeSessions = emptyList()
    )

    @Test
    fun `full happy path flow completes successfully`() {
        val engine = BattleEngine(createInitialSession(), clock = { 1000L })
        
        // Start battle
        engine.processBattleEvent(BattleEvent.StartBattle)
        assertEquals(BattleSessionStatus.IN_PROGRESS, engine.session.status)
        assertEquals(1, engine.session.currentChallenge)
        
        // Start challenge 1
        engine.processBattleEvent(BattleEvent.BeginChallenge)
        engine.processChallengeEvent(ChallengeEvent.Start)
        engine.processChallengeEvent(ChallengeEvent.SubmitCorrect)
        engine.processChallengeEvent(ChallengeEvent.Finish)
        
        // Engine should auto-progress battle state to ChallengeComplete(1)
        assertEquals(1, engine.session.currentChallenge)
        assertEquals(ChallengeSessionStatus.COMPLETE, engine.session.challengeSessions[0].status)
        
        // Next challenge
        engine.processBattleEvent(BattleEvent.Continue)
        engine.processBattleEvent(BattleEvent.PrepareNext)
        assertEquals(2, engine.session.currentChallenge)
        
        // Start challenge 2
        engine.processBattleEvent(BattleEvent.BeginChallenge)
        engine.processChallengeEvent(ChallengeEvent.Start)
        engine.processChallengeEvent(ChallengeEvent.SubmitCorrect)
        engine.processChallengeEvent(ChallengeEvent.Finish)
        
        // Next challenge
        engine.processBattleEvent(BattleEvent.Continue)
        engine.processBattleEvent(BattleEvent.PrepareNext)
        assertEquals(3, engine.session.currentChallenge)
        
        // Start challenge 3
        engine.processBattleEvent(BattleEvent.BeginChallenge)
        engine.processChallengeEvent(ChallengeEvent.Start)
        engine.processChallengeEvent(ChallengeEvent.SubmitCorrect)
        engine.processChallengeEvent(ChallengeEvent.Finish)
        
        // Finish battle
        engine.processBattleEvent(BattleEvent.Continue)
        engine.processBattleEvent(BattleEvent.ShowResults)
        
        assertEquals(BattleSessionStatus.COMPLETED, engine.session.status)
        assertEquals(null, engine.session.currentChallenge)
    }

    @Test
    fun `state restored from disk recreates exact internal state`() {
        // Assume process killed when user was in the middle of Challenge 2 (Active)
        val sessionFromDisk = BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = UserId("u-1"),
            battleId = BattleId("b-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 2,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(ChallengeId("c-1"), BattleSessionId("s-1"), ChallengeSessionStatus.COMPLETE, 1000L, 2000L),
                ChallengeSession(ChallengeId("c-2"), BattleSessionId("s-1"), ChallengeSessionStatus.ACTIVE, 3000L, null)
            )
        )
        
        val engine = BattleEngine(sessionFromDisk, clock = { 4000L })
        
        // Since we are ACTIVE in Challenge 2, we can submit a correct answer
        val result = engine.processChallengeEvent(ChallengeEvent.SubmitCorrect)
        assertTrue(result is DomainResult.Success)
        
        assertEquals(ChallengeSessionStatus.CORRECT, engine.session.challengeSessions[1].status)
    }

    @Test
    fun `invalid state transition is rejected safely`() {
        val engine = BattleEngine(createInitialSession())
        
        // Trying to skip the READY state
        val result = engine.processBattleEvent(BattleEvent.BeginChallenge)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.BattleState, (result as DomainResult.Failure).error)
    }

    @Test
    fun `duplicate event is rejected cleanly`() {
        val engine = BattleEngine(createInitialSession())
        engine.processBattleEvent(BattleEvent.StartBattle)
        
        // Duplicate start event
        val result = engine.processBattleEvent(BattleEvent.StartBattle)
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `simultaneous state events are serialized securely`() {
        val engine = BattleEngine(createInitialSession())
        engine.processBattleEvent(BattleEvent.StartBattle)
        engine.processBattleEvent(BattleEvent.BeginChallenge)
        engine.processChallengeEvent(ChallengeEvent.Start)
        
        val latch = CountDownLatch(2)
        var result1: DomainResult<*>? = null
        var result2: DomainResult<*>? = null

        // Two threads trying to submit answers at the same time
        thread {
            result1 = engine.processChallengeEvent(ChallengeEvent.SubmitCorrect)
            latch.countDown()
        }
        thread {
            result2 = engine.processChallengeEvent(ChallengeEvent.SubmitIncorrect)
            latch.countDown()
        }
        
        latch.await()
        
        // One must succeed, one must fail (because state machine moves from Active)
        val successCount = listOf(result1, result2).count { it is DomainResult.Success }
        val failCount = listOf(result1, result2).count { it is DomainResult.Failure }
        
        assertEquals(1, successCount)
        assertEquals(1, failCount)
    }

    @Test
    fun `repeated completion is rejected`() {
        val engine = BattleEngine(createInitialSession())
        engine.processBattleEvent(BattleEvent.StartBattle)
        engine.processBattleEvent(BattleEvent.BeginChallenge)
        engine.processChallengeEvent(ChallengeEvent.Start)
        engine.processChallengeEvent(ChallengeEvent.Finish) // Challenge 1 Complete
        
        // Next Challenge
        engine.processBattleEvent(BattleEvent.Continue)
        engine.processBattleEvent(BattleEvent.PrepareNext)
        
        // Trying to finish challenge 1 again - but wait, challenge events map to active challenge.
        // The active challenge is now challenge 2 (Ready). 
        // Sending Finish should fail because it's in Ready state.
        val result = engine.processChallengeEvent(ChallengeEvent.Finish)
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `stale state modifying completed results is rejected`() {
        val sessionFromDisk = BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = UserId("u-1"),
            battleId = BattleId("b-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            startedAt = 1000L,
            completedAt = 5000L,
            challengeSessions = emptyList() // Doesn't matter for this test
        )
        
        val engine = BattleEngine(sessionFromDisk)
        
        // Engine should reject StartBattle (modifying completed official result)
        val result = engine.processBattleEvent(BattleEvent.StartBattle)
        assertTrue(result is DomainResult.Failure)
    }
}

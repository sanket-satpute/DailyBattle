package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PracticeEngineAndManagerTest {

    private lateinit var repository: InMemoryPracticeSessionRepository
    private lateinit var manager: ActivePracticeSessionManager
    private val userId = UserId("user-test-1")

    @Before
    fun setup() {
        repository = InMemoryPracticeSessionRepository()
        manager = ActivePracticeSessionManager(repository)
    }

    @Test
    fun `full practice lifecycle through engine`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-1"),
            userId = userId,
            configuration = PracticeConfiguration.STANDARD
        )
        val engine = PracticeEngine(session) { 1000L }

        assertEquals(PracticeSessionLifecycle.Created, engine.lifecycle)

        // 1. Start Practice
        val startResult = engine.processEvent(PracticeEvent.StartPractice(1000L))
        assertTrue(startResult is DomainResult.Success)
        assertEquals(PracticeSessionStatus.IN_PROGRESS, engine.session.status)
        assertEquals(1, engine.session.currentChallengeIndex)
        assertEquals(1000L, engine.session.startedAt)
        assertEquals(PracticeSessionLifecycle.Ready(1), engine.lifecycle)

        // 2. Begin Challenge 1 (Snap)
        val beginResult = engine.processEvent(PracticeEvent.BeginChallenge(1, 1050L))
        assertTrue(beginResult is DomainResult.Success)
        assertEquals(PracticeSessionLifecycle.ChallengeActive(1), engine.lifecycle)
        assertEquals(ChallengeSessionStatus.ACTIVE, engine.session.challengeSessions[0].status)

        // 3. Finish Challenge 1
        val finishResult = engine.processEvent(PracticeEvent.FinishChallenge(1, 2000L))
        assertTrue(finishResult is DomainResult.Success)
        assertEquals(PracticeSessionLifecycle.ChallengeComplete(1), engine.lifecycle)
        assertEquals(ChallengeSessionStatus.COMPLETE, engine.session.challengeSessions[0].status)

        // 4. Continue to Next Challenge (Shift)
        val nextResult1 = engine.processEvent(PracticeEvent.ContinueNext(2100L))
        assertTrue(nextResult1 is DomainResult.Success)
        assertEquals(2, engine.session.currentChallengeIndex)
        assertEquals(PracticeSessionLifecycle.Ready(2), engine.lifecycle)

        // 5. Begin and Finish Challenge 2
        engine.processEvent(PracticeEvent.BeginChallenge(2, 2200L))
        engine.processEvent(PracticeEvent.FinishChallenge(2, 3000L))
        assertEquals(PracticeSessionLifecycle.ChallengeComplete(2), engine.lifecycle)

        // 6. Continue to Next Challenge (Crowd Call)
        engine.processEvent(PracticeEvent.ContinueNext(3100L))
        assertEquals(3, engine.session.currentChallengeIndex)
        engine.processEvent(PracticeEvent.BeginChallenge(3, 3200L))
        engine.processEvent(PracticeEvent.FinishChallenge(3, 4000L))
        assertEquals(PracticeSessionLifecycle.ChallengeComplete(3), engine.lifecycle)

        // 7. Complete Practice Session
        val completeResult = engine.processEvent(PracticeEvent.ContinueNext(4100L))
        assertTrue(completeResult is DomainResult.Success)
        assertEquals(PracticeSessionStatus.COMPLETED, engine.session.status)
        assertNull(engine.session.currentChallengeIndex)
        assertEquals(4100L, engine.session.completedAt)
        assertEquals(PracticeSessionLifecycle.PracticeComplete, engine.lifecycle)
    }

    @Test
    fun `interrupt practice session transitions to INTERRUPTED`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-interrupt"),
            userId = userId,
            status = PracticeSessionStatus.IN_PROGRESS,
            currentChallengeIndex = 2
        )
        val engine = PracticeEngine(session)

        val result = engine.processEvent(PracticeEvent.InterruptPractice(reason = "App backgrounded", now = 5000L))
        assertTrue(result is DomainResult.Success)
        assertEquals(PracticeSessionStatus.INTERRUPTED, engine.session.status)
        assertEquals(5000L, engine.session.completedAt)
        assertTrue(engine.lifecycle is PracticeSessionLifecycle.Interrupted)
    }

    @Test
    fun `repeat practice session increments repeatCount and resets progress`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-repeat"),
            userId = userId,
            status = PracticeSessionStatus.COMPLETED,
            repeatCount = 1
        )
        val engine = PracticeEngine(session)

        val result = engine.processEvent(PracticeEvent.RepeatPractice(now = 6000L))
        assertTrue(result is DomainResult.Success)
        assertEquals(PracticeSessionStatus.IN_PROGRESS, engine.session.status)
        assertEquals(2, engine.session.repeatCount)
        assertEquals(1, engine.session.currentChallengeIndex)
        assertTrue(engine.session.challengeSessions.isEmpty())
        assertNull(engine.session.completedAt)
    }

    @Test
    fun `manager starts, progresses, and persists practice session`() {
        val startResult = manager.startPractice(userId, PracticeConfiguration.STANDARD, now = 1000L)
        assertTrue(startResult is DomainResult.Success)

        val active = manager.activeSession.value
        assertNotNull(active)
        assertEquals(BattleMode.PRACTICE, active?.mode)
        assertEquals(PracticeSessionStatus.IN_PROGRESS, active?.status)

        // Check repository was updated
        val fromRepo = repository.getActivePracticeSession(userId)
        assertNotNull(fromRepo)
        assertEquals(active?.sessionId, fromRepo?.sessionId)

        // Complete through manager
        manager.beginChallenge(1, 1100L)
        manager.finishChallenge(1, 1500L)
        manager.continueNext(1600L)
        manager.beginChallenge(2, 1700L)
        manager.finishChallenge(2, 2000L)
        manager.continueNext(2100L)
        manager.beginChallenge(3, 2200L)
        manager.finishChallenge(3, 2500L)
        manager.continueNext(2600L)

        assertEquals(PracticeSessionStatus.COMPLETED, manager.activeSession.value?.status)
        assertEquals(PracticeSessionLifecycle.PracticeComplete, manager.lifecycle.value)

        // Completed session is no longer active in repository
        assertNull(repository.getActivePracticeSession(userId))
        // But is available in recent history
        val recent = repository.getRecentPracticeSessions(userId)
        assertEquals(1, recent.size)
        assertEquals(PracticeSessionStatus.COMPLETED, recent[0].status)
    }

    @Test
    fun `manager rejects invalid configuration with AppError Validation`() {
        val invalidConfig = PracticeConfiguration(challengeTypes = emptyList())
        val result = manager.startPractice(userId, invalidConfig)

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
        assertNull(manager.activeSession.value)
    }
}

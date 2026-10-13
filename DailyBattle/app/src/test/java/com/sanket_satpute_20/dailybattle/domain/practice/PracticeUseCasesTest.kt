package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PracticeUseCasesTest {

    private lateinit var repository: InMemoryPracticeSessionRepository
    private lateinit var manager: ActivePracticeSessionManager
    private lateinit var startUseCase: StartPracticeSessionUseCase
    private lateinit var interruptUseCase: InterruptPracticeSessionUseCase
    private lateinit var repeatUseCase: RepeatPracticeSessionUseCase
    private lateinit var completeUseCase: CompletePracticeSessionUseCase
    private lateinit var getActiveUseCase: GetActivePracticeSessionUseCase

    private val userId = UserId("user-usecase-test")

    @Before
    fun setup() {
        repository = InMemoryPracticeSessionRepository()
        manager = ActivePracticeSessionManager(repository)
        startUseCase = StartPracticeSessionUseCase(manager)
        interruptUseCase = InterruptPracticeSessionUseCase(manager)
        repeatUseCase = RepeatPracticeSessionUseCase(manager)
        completeUseCase = CompletePracticeSessionUseCase(manager)
        getActiveUseCase = GetActivePracticeSessionUseCase(repository, manager)
    }

    @Test
    fun `start practice use case success with standard config`() {
        val result = startUseCase.execute(userId)
        assertTrue(result is DomainResult.Success)

        val session = (result as DomainResult.Success).value
        assertEquals(BattleMode.PRACTICE, session.mode)
        assertEquals(PracticeSessionStatus.IN_PROGRESS, session.status)
        assertEquals(1, session.currentChallengeIndex)

        val active = getActiveUseCase.execute(userId)
        assertNotNull(active)
        assertEquals(session.sessionId, active?.sessionId)
    }

    @Test
    fun `start practice with invalid config returns Validation error`() {
        val invalidConfig = PracticeConfiguration(challengeTypes = emptyList())
        val result = startUseCase.execute(userId, invalidConfig)

        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
        assertNull(getActiveUseCase.execute(userId))
    }

    @Test
    fun `interrupt practice use case marks session INTERRUPTED`() {
        startUseCase.execute(userId)

        val interruptResult = interruptUseCase.execute(reason = "User exited")
        assertTrue(interruptResult is DomainResult.Success)

        val session = (interruptResult as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.INTERRUPTED, session.status)
    }

    @Test
    fun `repeat practice use case increments repeatCount`() {
        startUseCase.execute(userId)
        completeUseCase.execute()

        val repeatResult = repeatUseCase.execute()
        assertTrue(repeatResult is DomainResult.Success)

        val session = (repeatResult as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.IN_PROGRESS, session.status)
        assertEquals(1, session.repeatCount)
    }

    @Test
    fun `complete practice use case finishes session without official side-effects`() {
        startUseCase.execute(userId)

        val completeResult = completeUseCase.execute()
        assertTrue(completeResult is DomainResult.Success)

        val session = (completeResult as DomainResult.Success).value
        assertEquals(PracticeSessionStatus.COMPLETED, session.status)
        assertNull(session.currentChallengeIndex)
    }
}

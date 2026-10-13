package com.sanket_satpute_20.dailybattle.data.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSession
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryPracticeSessionRepositoryTest {

    private lateinit var repository: InMemoryPracticeSessionRepository
    private val userId = UserId("user-repo-test")

    @Before
    fun setup() {
        repository = InMemoryPracticeSessionRepository()
    }

    @Test
    fun `no practice data returns null safely`() {
        assertNull(repository.getPracticeSession(PracticeSessionId("non-existent")))
        assertNull(repository.getActivePracticeSession(userId))
        assertTrue(repository.getRecentPracticeSessions(userId).isEmpty())
    }

    @Test
    fun `save and retrieve practice session by id`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-100"),
            userId = userId,
            status = PracticeSessionStatus.IN_PROGRESS
        )

        repository.savePracticeSession(session)

        val retrieved = repository.getPracticeSession(PracticeSessionId("ps-100"))
        assertNotNull(retrieved)
        assertEquals(session.sessionId, retrieved?.sessionId)
        assertEquals(session.userId, retrieved?.userId)
    }

    @Test
    fun `active session is tracked and untracked upon completion`() {
        val inProgress = PracticeSession(
            sessionId = PracticeSessionId("ps-active"),
            userId = userId,
            status = PracticeSessionStatus.IN_PROGRESS
        )
        repository.savePracticeSession(inProgress)
        assertEquals(inProgress.sessionId, repository.getActivePracticeSession(userId)?.sessionId)

        // Complete the session
        val completed = inProgress.copy(status = PracticeSessionStatus.COMPLETED)
        repository.savePracticeSession(completed)

        assertNull(repository.getActivePracticeSession(userId))
        val recent = repository.getRecentPracticeSessions(userId)
        assertEquals(1, recent.size)
        assertEquals(PracticeSessionStatus.COMPLETED, recent[0].status)
    }

    @Test
    fun `recent practice sessions ordered by recency and respects limit`() {
        for (i in 1..5) {
            val session = PracticeSession(
                sessionId = PracticeSessionId("ps-$i"),
                userId = userId,
                status = PracticeSessionStatus.COMPLETED
            )
            repository.savePracticeSession(session)
        }

        val recentAll = repository.getRecentPracticeSessions(userId, limit = 10)
        assertEquals(5, recentAll.size)
        // Most recent first
        assertEquals("ps-5", recentAll[0].sessionId.value)
        assertEquals("ps-4", recentAll[1].sessionId.value)

        val recentLimit2 = repository.getRecentPracticeSessions(userId, limit = 2)
        assertEquals(2, recentLimit2.size)
        assertEquals("ps-5", recentLimit2[0].sessionId.value)
        assertEquals("ps-4", recentLimit2[1].sessionId.value)
    }

    @Test
    fun `delete practice session removes it from all indices`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-to-delete"),
            userId = userId,
            status = PracticeSessionStatus.IN_PROGRESS
        )
        repository.savePracticeSession(session)
        assertEquals(session.sessionId, repository.getActivePracticeSession(userId)?.sessionId)

        repository.deletePracticeSession(session.sessionId)

        assertNull(repository.getPracticeSession(session.sessionId))
        assertNull(repository.getActivePracticeSession(userId))
        assertTrue(repository.getRecentPracticeSessions(userId).isEmpty())
    }
}

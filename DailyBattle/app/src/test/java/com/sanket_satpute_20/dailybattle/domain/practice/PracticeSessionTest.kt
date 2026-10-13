package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionTest {

    @Test
    fun `default mode is PRACTICE`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-1"),
            userId = UserId("u-1")
        )
        assertEquals(BattleMode.PRACTICE, session.mode)
        assertEquals(PracticeSessionStatus.NOT_STARTED, session.status)
        assertTrue(session.isNotStarted)
        assertFalse(session.isInProgress)
        assertFalse(session.isCompleted)
        assertFalse(session.isInterrupted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `creating PracticeSession with OFFICIAL mode throws exception`() {
        PracticeSession(
            sessionId = PracticeSessionId("ps-1"),
            userId = UserId("u-1"),
            mode = BattleMode.OFFICIAL
        )
    }

    @Test
    fun `status flags reflect current status`() {
        val base = PracticeSession(
            sessionId = PracticeSessionId("ps-1"),
            userId = UserId("u-1")
        )

        val inProgress = base.copy(status = PracticeSessionStatus.IN_PROGRESS)
        assertTrue(inProgress.isInProgress)
        assertFalse(inProgress.isNotStarted)

        val completed = base.copy(status = PracticeSessionStatus.COMPLETED)
        assertTrue(completed.isCompleted)
        assertFalse(completed.isInProgress)

        val interrupted = base.copy(status = PracticeSessionStatus.INTERRUPTED)
        assertTrue(interrupted.isInterrupted)
        assertFalse(interrupted.isCompleted)
    }

    @Test
    fun `repeat count tracks repeated sessions`() {
        val session = PracticeSession(
            sessionId = PracticeSessionId("ps-1"),
            userId = UserId("u-1"),
            repeatCount = 3
        )
        assertEquals(3, session.repeatCount)
    }
}

package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BattleSessionTest {

    private fun createSession(
        mode: BattleMode = BattleMode.OFFICIAL,
        status: BattleSessionStatus = BattleSessionStatus.NOT_STARTED,
        currentChallenge: Int? = null,
        startedAt: Long? = null,
        completedAt: Long? = null,
    ) = BattleSession(
        sessionId = BattleSessionId("s-1"),
        userId = UserId("u-1"),
        battleId = BattleId("b-1"),
        mode = mode,
        status = status,
        currentChallenge = currentChallenge,
        startedAt = startedAt,
        completedAt = completedAt,
    )

    @Test
    fun `session holds all required fields`() {
        val session = createSession()
        assertEquals(BattleSessionId("s-1"), session.sessionId)
        assertEquals(UserId("u-1"), session.userId)
        assertEquals(BattleId("b-1"), session.battleId)
        assertEquals(BattleMode.OFFICIAL, session.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, session.status)
        assertNull(session.currentChallenge)
        assertNull(session.startedAt)
        assertNull(session.completedAt)
    }

    @Test
    fun `official and practice sessions with same ids are not equal`() {
        val official = createSession(mode = BattleMode.OFFICIAL)
        val practice = createSession(mode = BattleMode.PRACTICE)
        assertNotEquals(official, practice)
    }

    @Test
    fun `session progresses through lifecycle states via copy`() {
        val notStarted = createSession(status = BattleSessionStatus.NOT_STARTED)
        val inProgress = notStarted.copy(
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
        )
        val completed = inProgress.copy(
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = null,
            completedAt = 9000L,
        )

        assertEquals(BattleSessionStatus.NOT_STARTED, notStarted.status)
        assertEquals(BattleSessionStatus.IN_PROGRESS, inProgress.status)
        assertEquals(1, inProgress.currentChallenge)
        assertEquals(BattleSessionStatus.COMPLETED, completed.status)
        assertNull(completed.currentChallenge)
        assertEquals(9000L, completed.completedAt)
    }

    @Test
    fun `current challenge can be 1 2 or 3 for three MVP challenges`() {
        for (challengeIndex in 1..3) {
            val session = createSession(
                status = BattleSessionStatus.IN_PROGRESS,
                currentChallenge = challengeIndex,
            )
            assertEquals(challengeIndex, session.currentChallenge)
        }
    }

    @Test
    fun `session structural equality`() {
        val a = createSession()
        val b = createSession()
        assertEquals(a, b)
    }
}

package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeSessionTest {

    private fun createSession(
        status: ChallengeSessionStatus = ChallengeSessionStatus.READY,
        startedAt: Long? = null,
        completedAt: Long? = null,
    ) = ChallengeSession(
        challengeId = ChallengeId("c-1"),
        sessionId = BattleSessionId("s-1"),
        status = status,
        startedAt = startedAt,
        completedAt = completedAt,
    )

    @Test
    fun `session holds all required fields`() {
        val session = createSession()
        assertEquals(ChallengeId("c-1"), session.challengeId)
        assertEquals(BattleSessionId("s-1"), session.sessionId)
        assertEquals(ChallengeSessionStatus.READY, session.status)
        assertNull(session.startedAt)
        assertNull(session.completedAt)
    }

    @Test
    fun `session progresses through lifecycle states via copy`() {
        val ready = createSession(status = ChallengeSessionStatus.READY)
        val active = ready.copy(
            status = ChallengeSessionStatus.ACTIVE,
            startedAt = 1000L,
        )
        val complete = active.copy(
            status = ChallengeSessionStatus.COMPLETE,
            completedAt = 5000L,
        )

        assertEquals(ChallengeSessionStatus.READY, ready.status)
        assertEquals(ChallengeSessionStatus.ACTIVE, active.status)
        assertEquals(1000L, active.startedAt)
        assertEquals(ChallengeSessionStatus.COMPLETE, complete.status)
        assertEquals(5000L, complete.completedAt)
    }

    @Test
    fun `sessions with different statuses are not equal`() {
        val ready = createSession(status = ChallengeSessionStatus.READY)
        val active = createSession(status = ChallengeSessionStatus.ACTIVE)
        assertNotEquals(ready, active)
    }
}

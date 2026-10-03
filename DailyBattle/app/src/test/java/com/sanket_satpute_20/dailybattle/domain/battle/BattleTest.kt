package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BattleTest {

    private fun snapChallenge() = Challenge(
        challengeId = ChallengeId("c-1"),
        battleId = BattleId("b-1"),
        type = ChallengeType.SNAP,
        order = 1,
        version = 1,
    )

    private fun shiftChallenge() = Challenge(
        challengeId = ChallengeId("c-2"),
        battleId = BattleId("b-1"),
        type = ChallengeType.SHIFT,
        order = 2,
        version = 1,
    )

    private fun crowdCallChallenge() = Challenge(
        challengeId = ChallengeId("c-3"),
        battleId = BattleId("b-1"),
        type = ChallengeType.CROWD_CALL,
        order = 3,
        version = 1,
    )

    private fun createBattle() = Battle(
        battleId = BattleId("b-1"),
        battleDate = 20261001L,
        status = BattleStatus.AVAILABLE,
        version = 1,
        challenges = listOf(snapChallenge(), shiftChallenge(), crowdCallChallenge()),
    )

    @Test
    fun `battle holds all required fields`() {
        val battle = createBattle()
        assertEquals(BattleId("b-1"), battle.battleId)
        assertEquals(20261001L, battle.battleDate)
        assertEquals(BattleStatus.AVAILABLE, battle.status)
        assertEquals(1, battle.version)
        assertEquals(3, battle.challenges.size)
    }

    @Test
    fun `battle challenges are in official order Snap then Shift then CrowdCall`() {
        val battle = createBattle()
        assertEquals(ChallengeType.SNAP, battle.challenges[0].type)
        assertEquals(1, battle.challenges[0].order)
        assertEquals(ChallengeType.SHIFT, battle.challenges[1].type)
        assertEquals(2, battle.challenges[1].order)
        assertEquals(ChallengeType.CROWD_CALL, battle.challenges[2].type)
        assertEquals(3, battle.challenges[2].order)
    }

    @Test
    fun `battle challenges all reference same battleId`() {
        val battle = createBattle()
        battle.challenges.forEach { challenge ->
            assertEquals(battle.battleId, challenge.battleId)
        }
    }

    @Test
    fun `battle structural equality`() {
        val a = createBattle()
        val b = createBattle()
        assertEquals(a, b)
    }

    @Test
    fun `battles with different statuses are not equal`() {
        val available = createBattle()
        val completed = available.copy(status = BattleStatus.COMPLETED)
        assertNotEquals(available, completed)
    }

    @Test
    fun `battle with no challenges is structurally valid model`() {
        // The model itself permits an empty list; composition enforcement
        // belongs to the use case layer, not the data class.
        val battle = createBattle().copy(challenges = emptyList())
        assertEquals(0, battle.challenges.size)
    }
}

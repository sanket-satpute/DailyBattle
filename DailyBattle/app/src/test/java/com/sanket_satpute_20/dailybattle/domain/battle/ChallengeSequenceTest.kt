package com.sanket_satpute_20.dailybattle.domain.battle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId

class ChallengeSequenceTest {

    // ── typeForIndex ────────────────────────────────────────────────

    @Test
    fun `index 1 maps to SNAP`() {
        assertEquals(ChallengeType.SNAP, ChallengeSequence.typeForIndex(1))
    }

    @Test
    fun `index 2 maps to SHIFT`() {
        assertEquals(ChallengeType.SHIFT, ChallengeSequence.typeForIndex(2))
    }

    @Test
    fun `index 3 maps to CROWD_CALL`() {
        assertEquals(ChallengeType.CROWD_CALL, ChallengeSequence.typeForIndex(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `index 0 throws`() {
        ChallengeSequence.typeForIndex(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `index 4 throws`() {
        ChallengeSequence.typeForIndex(4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative index throws`() {
        ChallengeSequence.typeForIndex(-1)
    }

    // ── indexForType ────────────────────────────────────────────────

    @Test
    fun `SNAP maps to index 1`() {
        assertEquals(1, ChallengeSequence.indexForType(ChallengeType.SNAP))
    }

    @Test
    fun `SHIFT maps to index 2`() {
        assertEquals(2, ChallengeSequence.indexForType(ChallengeType.SHIFT))
    }

    @Test
    fun `CROWD_CALL maps to index 3`() {
        assertEquals(3, ChallengeSequence.indexForType(ChallengeType.CROWD_CALL))
    }

    // ── TOTAL_CHALLENGES ────────────────────────────────────────────

    @Test
    fun `total challenges is 3`() {
        assertEquals(3, ChallengeSequence.TOTAL_CHALLENGES)
    }

    // ── isValidSequence ─────────────────────────────────────────────

    @Test
    fun `valid official sequence returns true`() {
        val challenges = listOf(
            makeChallenge(ChallengeType.SNAP, 1),
            makeChallenge(ChallengeType.SHIFT, 2),
            makeChallenge(ChallengeType.CROWD_CALL, 3),
        )
        assertTrue(ChallengeSequence.isValidSequence(challenges))
    }

    @Test
    fun `swapped types returns false`() {
        // Snap and Shift swapped
        val challenges = listOf(
            makeChallenge(ChallengeType.SHIFT, 1),
            makeChallenge(ChallengeType.SNAP, 2),
            makeChallenge(ChallengeType.CROWD_CALL, 3),
        )
        assertFalse(ChallengeSequence.isValidSequence(challenges))
    }

    @Test
    fun `reversed order returns false`() {
        val challenges = listOf(
            makeChallenge(ChallengeType.CROWD_CALL, 1),
            makeChallenge(ChallengeType.SHIFT, 2),
            makeChallenge(ChallengeType.SNAP, 3),
        )
        assertFalse(ChallengeSequence.isValidSequence(challenges))
    }

    @Test
    fun `wrong order values returns false`() {
        // Correct types but wrong order numbers
        val challenges = listOf(
            makeChallenge(ChallengeType.SNAP, 2),
            makeChallenge(ChallengeType.SHIFT, 3),
            makeChallenge(ChallengeType.CROWD_CALL, 1),
        )
        assertFalse(ChallengeSequence.isValidSequence(challenges))
    }

    @Test
    fun `too few challenges returns false`() {
        val challenges = listOf(
            makeChallenge(ChallengeType.SNAP, 1),
            makeChallenge(ChallengeType.SHIFT, 2),
        )
        assertFalse(ChallengeSequence.isValidSequence(challenges))
    }

    @Test
    fun `too many challenges returns false`() {
        val challenges = listOf(
            makeChallenge(ChallengeType.SNAP, 1),
            makeChallenge(ChallengeType.SHIFT, 2),
            makeChallenge(ChallengeType.CROWD_CALL, 3),
            makeChallenge(ChallengeType.SNAP, 4),
        )
        assertFalse(ChallengeSequence.isValidSequence(challenges))
    }

    @Test
    fun `empty list returns false`() {
        assertFalse(ChallengeSequence.isValidSequence(emptyList()))
    }

    @Test
    fun `duplicate order values returns false`() {
        val challenges = listOf(
            makeChallenge(ChallengeType.SNAP, 1),
            makeChallenge(ChallengeType.SHIFT, 1),
            makeChallenge(ChallengeType.CROWD_CALL, 3),
        )
        assertFalse(ChallengeSequence.isValidSequence(challenges))
    }

    // ── Helper ──────────────────────────────────────────────────────

    private fun makeChallenge(type: ChallengeType, order: Int): Challenge {
        return Challenge(
            challengeId = ChallengeId("c-$order"),
            battleId = BattleId("b-1"),
            type = type,
            order = order,
            version = 1,
        )
    }
}

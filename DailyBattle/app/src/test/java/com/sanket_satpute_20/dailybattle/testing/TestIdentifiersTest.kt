package com.sanket_satpute_20.dailybattle.testing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TestIdentifiersTest {
    @Test
    fun `factories are deterministic for the same suffix`() {
        assertEquals(testUserId(1), testUserId(1))
        assertEquals(testBattleId(2), testBattleId(2))
    }

    @Test
    fun `factories are distinct for different suffixes`() {
        assertNotEquals(testUserId(1), testUserId(2))
        assertNotEquals(testFriendshipId(1), testFriendshipId(2))
    }

    @Test
    fun `default suffix produces a stable value`() {
        assertEquals("test-user-1", testUserId().value)
        assertEquals("test-battle-result-1", testBattleResultId().value)
    }
}

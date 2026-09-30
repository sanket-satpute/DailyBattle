package com.sanket_satpute_20.dailybattle.domain.identifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DomainIdentifiersTest {
    @Test
    fun `identifier preserves its opaque value`() {
        assertEquals("opaque-id", BattleId("opaque-id").value)
    }

    @Test
    fun `identifier types remain domain specific`() {
        assertNotEquals(BattleId("same-value"), ChallengeId("same-value"))
    }
}

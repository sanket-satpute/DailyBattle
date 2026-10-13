package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeConfigurationTest {

    @Test
    fun `standard configuration is valid`() {
        val config = PracticeConfiguration.STANDARD
        assertTrue(config.isValid())
        assertEquals(3, config.challengeTypes.size)
        assertEquals(PracticeDifficulty.STANDARD, config.difficulty)

        val result = config.validate()
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `empty challenge list is invalid configuration`() {
        val config = PracticeConfiguration(challengeTypes = emptyList())
        assertFalse(config.isValid())

        val result = config.validate()
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `more than 3 challenges is invalid configuration`() {
        val config = PracticeConfiguration(
            challengeTypes = listOf(
                ChallengeType.SNAP,
                ChallengeType.SHIFT,
                ChallengeType.CROWD_CALL,
                ChallengeType.SNAP
            )
        )
        assertFalse(config.isValid())

        val result = config.validate()
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `single challenge practice configuration is valid`() {
        val config = PracticeConfiguration(
            challengeTypes = listOf(ChallengeType.SNAP),
            targetBattleId = BattleId("b-123")
        )
        assertTrue(config.isValid())
        assertEquals(BattleId("b-123"), config.targetBattleId)
        assertTrue(config.validate() is DomainResult.Success)
    }
}

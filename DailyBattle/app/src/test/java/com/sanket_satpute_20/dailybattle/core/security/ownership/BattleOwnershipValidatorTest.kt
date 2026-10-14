package com.sanket_satpute_20.dailybattle.core.security.ownership

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BattleOwnershipValidatorTest {

    private lateinit var validator: BattleOwnershipValidator

    private val userA = UserId("user_A")
    private val userB = UserId("user_B")
    private val battle1 = BattleId("battle_1")

    private val sessionA = BattleSession(
        sessionId = BattleSessionId("sess_A"),
        userId = userA,
        battleId = battle1,
        mode = BattleMode.OFFICIAL,
        status = BattleSessionStatus.IN_PROGRESS,
        currentChallenge = 1,
        startedAt = 100L,
        completedAt = null
    )

    private val resultA = BattleResult(
        resultId = BattleResultId("res_A"),
        userId = userA,
        battleId = battle1,
        sessionId = BattleSessionId("sess_A"),
        snapScore = 200,
        shiftScore = 200,
        crowdCallScore = 200,
        consistencyScore = 50,
        totalScore = 650,
        percentile = 70.0,
        completedAt = 200L
    )

    @Before
    fun setUp() {
        validator = BattleOwnershipValidator()
    }

    @Test
    fun `validateSessionOwnership accepts caller matching session owner`() {
        val result = validator.validateSessionOwnership(sessionA, userA)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateSessionOwnership rejects caller different from session owner`() {
        val result = validator.validateSessionOwnership(sessionA, userB)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateResultOwnership accepts matching userId and battleId`() {
        val result = validator.validateResultOwnership(resultA, userA, battle1)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateResultOwnership rejects mismatched userId with Authorization error`() {
        val result = validator.validateResultOwnership(resultA, userB, battle1)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateResultOwnership rejects mismatched battleId with Validation error`() {
        val otherBattle = BattleId("battle_different")
        val result = validator.validateResultOwnership(resultA, userA, otherBattle)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateAttemptUniqueness rejects duplicate official attempt for user and battle`() {
        val result = validator.validateAttemptUniqueness(
            userId = userA,
            battleId = battle1,
            targetMode = BattleMode.OFFICIAL,
            existingOfficialSessions = listOf(sessionA)
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.BattleState, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateAttemptUniqueness permits new official attempt if no prior official attempt exists`() {
        val result = validator.validateAttemptUniqueness(
            userId = userA,
            battleId = battle1,
            targetMode = BattleMode.OFFICIAL,
            existingOfficialSessions = emptyList()
        )
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateAttemptUniqueness permits practice sessions even when official session exists`() {
        val result = validator.validateAttemptUniqueness(
            userId = userA,
            battleId = battle1,
            targetMode = BattleMode.PRACTICE,
            existingOfficialSessions = listOf(sessionA)
        )
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateSessionResultAlignment accepts aligned session and result`() {
        val result = validator.validateSessionResultAlignment(sessionA, resultA)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateSessionResultAlignment rejects mismatched sessionId`() {
        val mismatched = resultA.copy(sessionId = BattleSessionId("sess_different"))
        val result = validator.validateSessionResultAlignment(sessionA, mismatched)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }
}

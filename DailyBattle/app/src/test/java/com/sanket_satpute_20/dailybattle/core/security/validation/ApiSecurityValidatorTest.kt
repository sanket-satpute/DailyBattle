package com.sanket_satpute_20.dailybattle.core.security.validation

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApiSecurityValidatorTest {

    private lateinit var validator: ApiSecurityValidator

    @Before
    fun setUp() {
        validator = ApiSecurityValidator()
    }

    @Test
    fun `validateIdentifier accepts canonical alphanumeric and UUID strings`() {
        assertTrue(validator.validateIdentifier("battle-2026-10-14") is DomainResult.Success)
        assertTrue(validator.validateIdentifier("d3b07384-d113-4632-a567-27b2b8c548f0") is DomainResult.Success)
        assertTrue(validator.validateIdentifier("user_12345") is DomainResult.Success)
    }

    @Test
    fun `validateIdentifier rejects blank or null identifiers`() {
        assertTrue(validator.validateIdentifier(null) is DomainResult.Failure)
        assertTrue(validator.validateIdentifier("") is DomainResult.Failure)
        assertTrue(validator.validateIdentifier("   ") is DomainResult.Failure)
    }

    @Test
    fun `validateIdentifier rejects path traversal and control characters`() {
        assertTrue(validator.validateIdentifier("../etc/passwd") is DomainResult.Failure)
        assertTrue(validator.validateIdentifier("session/123") is DomainResult.Failure)
        assertTrue(validator.validateIdentifier("id\u0000injected") is DomainResult.Failure)
        assertTrue(validator.validateIdentifier("id\ninjected") is DomainResult.Failure)
        assertTrue(validator.validateIdentifier("id%20injected") is DomainResult.Failure)
    }

    @Test
    fun `validateIdempotencyKey requires non blank clientRequestId`() {
        assertTrue(validator.validateIdempotencyKey("req_abc_123") is DomainResult.Success)
        assertTrue(validator.validateIdempotencyKey(null) is DomainResult.Failure)
        assertTrue(validator.validateIdempotencyKey("") is DomainResult.Failure)
        assertTrue(validator.validateIdempotencyKey("  ") is DomainResult.Failure)
    }

    @Test
    fun `validateChallengeSequence enforces strict order Snap then Shift then Crowd Call`() {
        // Step 1 must be Snap
        assertTrue(validator.validateChallengeSequence(1, ChallengeType.SNAP) is DomainResult.Success)
        assertTrue(validator.validateChallengeSequence(1, ChallengeType.SHIFT) is DomainResult.Failure)
        assertTrue(validator.validateChallengeSequence(1, ChallengeType.CROWD_CALL) is DomainResult.Failure)

        // Step 2 must be Shift
        assertTrue(validator.validateChallengeSequence(2, ChallengeType.SHIFT) is DomainResult.Success)
        assertTrue(validator.validateChallengeSequence(2, ChallengeType.SNAP) is DomainResult.Failure)

        // Step 3 must be Crowd Call
        assertTrue(validator.validateChallengeSequence(3, ChallengeType.CROWD_CALL) is DomainResult.Success)
        assertTrue(validator.validateChallengeSequence(3, ChallengeType.SHIFT) is DomainResult.Failure)

        // Step 4 is invalid index
        assertTrue(validator.validateChallengeSequence(4, ChallengeType.SNAP) is DomainResult.Failure)
    }

    @Test
    fun `validateChallengeScore accepts scores in 0 to 300 and rejects out of bounds`() {
        assertTrue(validator.validateChallengeScore(0, ChallengeType.SNAP) is DomainResult.Success)
        assertTrue(validator.validateChallengeScore(300, ChallengeType.SNAP) is DomainResult.Success)
        assertTrue(validator.validateChallengeScore(150, ChallengeType.SHIFT) is DomainResult.Success)

        // Negative score
        val neg = validator.validateChallengeScore(-1, ChallengeType.SNAP)
        assertTrue(neg is DomainResult.Failure)
        assertEquals(AppError.Validation, (neg as DomainResult.Failure).error)

        // Exceeding maximum
        val over = validator.validateChallengeScore(301, ChallengeType.CROWD_CALL)
        assertTrue(over is DomainResult.Failure)
        assertEquals(AppError.Validation, (over as DomainResult.Failure).error)
    }

    @Test
    fun `validateBattleScoreBreakdown enforces mathematical consistency and 1000 max`() {
        // Valid 1000 total score
        val perfect = validator.validateBattleScoreBreakdown(
            snapScore = 300,
            shiftScore = 300,
            crowdCallScore = 300,
            consistencyScore = 100,
            totalScore = 1000,
            mode = BattleMode.OFFICIAL
        )
        assertTrue(perfect is DomainResult.Success)

        // Valid mid-range score
        val mid = validator.validateBattleScoreBreakdown(
            snapScore = 200,
            shiftScore = 150,
            crowdCallScore = 250,
            consistencyScore = 50,
            totalScore = 650,
            mode = BattleMode.OFFICIAL
        )
        assertTrue(mid is DomainResult.Success)

        // Mathematical mismatch: sum is 650 but total reported as 700
        val sumMismatch = validator.validateBattleScoreBreakdown(
            snapScore = 200,
            shiftScore = 150,
            crowdCallScore = 250,
            consistencyScore = 50,
            totalScore = 700,
            mode = BattleMode.OFFICIAL
        )
        assertTrue(sumMismatch is DomainResult.Failure)

        // Consistency exceeding 100
        val badConsistency = validator.validateBattleScoreBreakdown(
            snapScore = 200,
            shiftScore = 200,
            crowdCallScore = 200,
            consistencyScore = 101,
            totalScore = 701,
            mode = BattleMode.OFFICIAL
        )
        assertTrue(badConsistency is DomainResult.Failure)
    }

    @Test
    fun `validatePercentile accepts 0 to 100 and rejects outside`() {
        assertTrue(validator.validatePercentile(0.0) is DomainResult.Success)
        assertTrue(validator.validatePercentile(50.5) is DomainResult.Success)
        assertTrue(validator.validatePercentile(100.0) is DomainResult.Success)

        assertTrue(validator.validatePercentile(-0.1) is DomainResult.Failure)
        assertTrue(validator.validatePercentile(100.1) is DomainResult.Failure)
    }
}

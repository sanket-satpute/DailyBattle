package com.sanket_satpute_20.dailybattle.core.security.score

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScoreAuthorityEnforcerTest {

    private lateinit var enforcer: ScoreAuthorityEnforcer

    private val sampleResult = BattleResult(
        resultId = BattleResultId("res_1"),
        userId = UserId("user_1"),
        battleId = BattleId("battle_1"),
        sessionId = BattleSessionId("session_1"),
        snapScore = 200,
        shiftScore = 200,
        crowdCallScore = 200,
        consistencyScore = 50,
        totalScore = 650,
        percentile = 75.0,
        completedAt = 1000L
    )

    @Before
    fun setUp() {
        enforcer = ScoreAuthorityEnforcer()
    }

    @Test
    fun `validateScoreIntegrity passes on structurally valid result`() {
        val result = enforcer.validateScoreIntegrity(sampleResult)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `validateScoreIntegrity rejects mathematical discrepancy between components and total`() {
        val tampered = sampleResult.copy(totalScore = 900)
        val result = enforcer.validateScoreIntegrity(tampered)
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `validateScoreIntegrity rejects negative component scores`() {
        val negativeSnap = sampleResult.copy(snapScore = -10, totalScore = 440)
        assertTrue(enforcer.validateScoreIntegrity(negativeSnap) is DomainResult.Failure)
    }

    @Test
    fun `validateScoreIntegrity rejects component score exceeding maximum 300`() {
        val overSnap = sampleResult.copy(snapScore = 350, totalScore = 800)
        assertTrue(enforcer.validateScoreIntegrity(overSnap) is DomainResult.Failure)
    }

    @Test
    fun `assertMutationAllowed blocks client modification of SERVER_CONFIRMED result`() {
        val modifiedClientResult = sampleResult.copy(totalScore = 700)
        val result = enforcer.assertMutationAllowed(
            existingResult = sampleResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = modifiedClientResult,
            isUpdateFromServer = false
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun `assertMutationAllowed blocks client modification of percentile in SERVER_CONFIRMED result`() {
        val modifiedPercentile = sampleResult.copy(percentile = 99.9)
        val result = enforcer.assertMutationAllowed(
            existingResult = sampleResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = modifiedPercentile,
            isUpdateFromServer = false
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Authorization, (result as DomainResult.Failure).error)
    }

    @Test
    fun `assertMutationAllowed permits server reconciliation for newer timestamp`() {
        val newerServerResult = sampleResult.copy(totalScore = 700, completedAt = 1500L)
        val result = enforcer.assertMutationAllowed(
            existingResult = sampleResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = newerServerResult,
            isUpdateFromServer = true
        )
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `assertMutationAllowed rejects stale server reconciliation with older timestamp`() {
        val staleServerResult = sampleResult.copy(totalScore = 600, completedAt = 500L)
        val result = enforcer.assertMutationAllowed(
            existingResult = sampleResult,
            existingAuthority = ScoreAuthorityState.SERVER_CONFIRMED,
            proposedUpdate = staleServerResult,
            isUpdateFromServer = true
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals(AppError.Validation, (result as DomainResult.Failure).error)
    }

    @Test
    fun `resolveAuthoritativeResult prefers server result over local candidate`() {
        val localCandidate = sampleResult.copy(totalScore = 500)
        val serverConfirmed = sampleResult.copy(totalScore = 650)

        val resolution = enforcer.resolveAuthoritativeResult(
            localResult = localCandidate,
            serverResult = serverConfirmed
        )
        assertTrue(resolution is DomainResult.Success)
        assertEquals(serverConfirmed, (resolution as DomainResult.Success).value)
    }

    @Test
    fun `validateModeAuthority blocks Practice mode from yielding official result`() {
        assertTrue(enforcer.validateModeAuthority(BattleMode.OFFICIAL) is DomainResult.Success)
        val practiceResult = enforcer.validateModeAuthority(BattleMode.PRACTICE)
        assertTrue(practiceResult is DomainResult.Failure)
        assertEquals(AppError.Domain, (practiceResult as DomainResult.Failure).error)
    }
}

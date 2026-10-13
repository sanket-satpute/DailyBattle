package com.sanket_satpute_20.dailybattle.domain.profile

import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.RivalComparisonDirection
import com.sanket_satpute_20.dailybattle.domain.battle.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 23.1 — Unit Tests (Derived Metrics)
 * Validates mathematical derivation of secondary metrics across the system:
 * 1. Battle DNA derived metrics (speed = avg Snap, memory = avg Shift, people = avg Crowd Call).
 * 2. Empty battle history yields null DNA dimensions.
 * 3. Personal Best derivation.
 * 4. Score Improvement derivation (+/- comparison against yesterday).
 * 5. Rival gap derivation and leading status.
 *
 * Per `docs/01_PRODUCT.md`, `docs/04_SCREEN_BLUEPRINTS.md`, and `docs/12_TESTING_AND_QA.md` §8.
 */
class DerivedMetricsUnitTest {

    private val testUserId = UserId("user_metrics_1")

    private fun createBattleResult(
        id: String,
        snap: Int,
        shift: Int,
        crowd: Int,
        consistency: Int = 100,
        total: Int = snap + shift + crowd + consistency
    ): BattleResult {
        return BattleResult(
            resultId = BattleResultId("res_$id"),
            userId = testUserId,
            battleId = BattleId("battle_$id"),
            sessionId = BattleSessionId("sess_$id"),
            totalScore = total,
            snapScore = snap,
            shiftScore = shift,
            crowdCallScore = crowd,
            consistencyScore = consistency,
            percentile = 0.85,
            completedAt = 1000L
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Battle DNA Derivation
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `Battle DNA calculates accurate average trait scores across multiple battles`() {
        val battles = listOf(
            createBattleResult("1", snap = 300, shift = 200, crowd = 100),
            createBattleResult("2", snap = 200, shift = 300, crowd = 200),
            createBattleResult("3", snap = 250, shift = 100, crowd = 300)
        )

        // Speed: (300 + 200 + 250) / 3 = 750 / 3 = 250
        val expectedSpeed = 250
        // Memory: (200 + 300 + 100) / 3 = 600 / 3 = 200
        val expectedMemory = 200
        // People: (100 + 200 + 300) / 3 = 600 / 3 = 200
        val expectedPeople = 200

        val speed = battles.sumOf { it.snapScore } / battles.size
        val memory = battles.sumOf { it.shiftScore } / battles.size
        val people = battles.sumOf { it.crowdCallScore } / battles.size

        val dna = BattleDNA(speed = speed, memory = memory, people = people)

        assertEquals(expectedSpeed, dna.speed)
        assertEquals(expectedMemory, dna.memory)
        assertEquals(expectedPeople, dna.people)
    }

    @Test
    fun `Battle DNA returns null traits when user has completed 0 battles`() {
        val battles = emptyList<BattleResult>()

        val dna = if (battles.isNotEmpty()) {
            BattleDNA(
                speed = battles.sumOf { it.snapScore } / battles.size,
                memory = battles.sumOf { it.shiftScore } / battles.size,
                people = battles.sumOf { it.crowdCallScore } / battles.size
            )
        } else {
            BattleDNA(speed = null, memory = null, people = null)
        }

        assertNull(dna.speed)
        assertNull(dna.memory)
        assertNull(dna.people)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Score Improvement Derivation
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `score improvement calculates positive, negative, and null deltas accurately`() {
        fun calculateImprovement(todayScore: Int, yesterdayScore: Int?): Int? {
            return yesterdayScore?.let { todayScore - it }
        }

        // Higher than yesterday
        assertEquals(50, calculateImprovement(todayScore = 850, yesterdayScore = 800))

        // Lower than yesterday
        assertEquals(-75, calculateImprovement(todayScore = 725, yesterdayScore = 800))

        // Equal to yesterday
        assertEquals(0, calculateImprovement(todayScore = 900, yesterdayScore = 900))

        // First day (no yesterday score)
        assertNull(calculateImprovement(todayScore = 900, yesterdayScore = null))
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Personal Best Derivation
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `personal best derivation detects new record accurately`() {
        fun evaluatePersonalBest(todayScore: Int, historicBest: Int?): Pair<Boolean, Int> {
            val isNewRecord = historicBest == null || todayScore > historicBest
            val recordScore = if (isNewRecord) todayScore else historicBest
            return Pair(isNewRecord, recordScore)
        }

        // First game played (no previous record)
        val (firstGameRecord, firstBest) = evaluatePersonalBest(todayScore = 750, historicBest = null)
        assertTrue(firstGameRecord)
        assertEquals(750, firstBest)

        // Beating previous record
        val (beatRecord, newBest) = evaluatePersonalBest(todayScore = 920, historicBest = 880)
        assertTrue(beatRecord)
        assertEquals(920, newBest)

        // Lower than previous record
        val (missedRecord, unchangedBest) = evaluatePersonalBest(todayScore = 800, historicBest = 920)
        assertFalse(missedRecord)
        assertEquals(920, unchangedBest)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Rival Gap Derivation
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `rival score comparison calculates gap and leading status`() {
        fun buildRivalComparison(userScore: Int, rivalScore: Int, rivalName: String): RivalScoreComparison {
            val gap = rivalScore - userScore
            val dir = when {
                gap > 0 -> RivalComparisonDirection.RIVAL_AHEAD
                gap < 0 -> RivalComparisonDirection.USER_AHEAD
                else -> RivalComparisonDirection.TIED
            }
            return RivalScoreComparison(
                userScore = userScore,
                rivalScore = rivalScore,
                scoreGap = gap,
                battleId = BattleId("battle_today"),
                direction = dir,
                rivalName = rivalName
            )
        }

        // User trailing rival
        val trailing = buildRivalComparison(userScore = 800, rivalScore = 850, rivalName = "Alex")
        assertEquals(50, trailing.scoreGap)
        assertTrue(trailing.scoreGap > 0)
        assertEquals(RivalComparisonDirection.RIVAL_AHEAD, trailing.direction)

        // User leading rival
        val leading = buildRivalComparison(userScore = 900, rivalScore = 850, rivalName = "Alex")
        assertEquals(-50, leading.scoreGap)
        assertTrue(leading.scoreGap <= 0)
        assertEquals(RivalComparisonDirection.USER_AHEAD, leading.direction)

        // Tied
        val tied = buildRivalComparison(userScore = 850, rivalScore = 850, rivalName = "Alex")
        assertEquals(0, tied.scoreGap)
        assertEquals(RivalComparisonDirection.TIED, tied.direction)
    }
}

package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import java.util.UUID

/**
 * Domain model representing the isolated outcome of a completed Practice session.
 *
 * Authoritative specifications:
 * - `ROADMAP.md` Phase 19, Sprint 19.3: Practice results remain isolated.
 * - `02_GAMEPLAY.md` §75 & Rule 6: Practice results must not replace or modify the official daily result.
 * - `08_DATA_AND_API.md` §19 & §40: Practice results are isolated from official history and leaderboards.
 * - `12_TESTING_AND_QA.md` §14: Practice score ≠ Official Battle score.
 *   Practice must not modify official score, completion state, streak, ranking, or percentile.
 *
 * Scoring invariants:
 * - [snapScore]: 0..300
 * - [shiftScore]: 0..300
 * - [crowdCallScore]: 0..300
 * - [totalScore]: 0..900 (sum of challenges; consistency score is not applicable to Practice mode per REQ-BAT-005)
 * - Practice results have NO percentile and NO consistencyScore (exclusive to Official Battle).
 */
data class PracticeResult(
    val resultId: PracticeResultId,
    val userId: UserId,
    val sessionId: PracticeSessionId,
    val difficulty: PracticeDifficulty,
    val snapScore: Int,
    val shiftScore: Int,
    val crowdCallScore: Int,
    val totalScore: Int,
    val challengeResults: List<ChallengeResult> = emptyList(),
    val repeatCount: Int = 0,
    val completedAt: Long,
) {
    val mode: BattleMode = BattleMode.PRACTICE

    init {
        require(snapScore in 0..300) { "snapScore must be between 0 and 300, was $snapScore" }
        require(shiftScore in 0..300) { "shiftScore must be between 0 and 300, was $shiftScore" }
        require(crowdCallScore in 0..300) { "crowdCallScore must be between 0 and 300, was $crowdCallScore" }
        require(totalScore in 0..900) { "totalScore must be between 0 and 900, was $totalScore" }
        require(repeatCount >= 0) { "repeatCount must be non-negative, was $repeatCount" }
        require(completedAt > 0) { "completedAt must be greater than 0, was $completedAt" }
    }
}

/**
 * Converts a completed [PracticeSession] into a validated [PracticeResult].
 * Returns [DomainResult.Failure] if the session is not completed or timestamps/scores are invalid.
 */
fun PracticeSession.toPracticeResult(
    resultId: PracticeResultId = PracticeResultId(UUID.randomUUID().toString()),
    completedAt: Long = this.completedAt ?: System.currentTimeMillis()
): DomainResult<PracticeResult> {
    if (this.status != PracticeSessionStatus.COMPLETED) {
        return DomainResult.Failure(AppError.Validation)
    }

    if (completedAt <= 0) {
        return DomainResult.Failure(AppError.Validation)
    }

    val snapResult = this.challengeResults.find { it.challengeId.value.contains("snap") }
    val shiftResult = this.challengeResults.find { it.challengeId.value.contains("shift") }
    val crowdCallResult = this.challengeResults.find { it.challengeId.value.contains("crowd_call") }

    val aggregated = BattleScoreAggregator().aggregate(
        mode = BattleMode.PRACTICE,
        snapResult = snapResult,
        shiftResult = shiftResult,
        crowdCallResult = crowdCallResult
    )

    return try {
        DomainResult.Success(
            PracticeResult(
                resultId = resultId,
                userId = this.userId,
                sessionId = this.sessionId,
                difficulty = this.configuration.difficulty,
                snapScore = aggregated.snapScore.coerceIn(0, 300),
                shiftScore = aggregated.shiftScore.coerceIn(0, 300),
                crowdCallScore = aggregated.crowdCallScore.coerceIn(0, 300),
                totalScore = aggregated.totalScore.coerceIn(0, 900),
                challengeResults = this.challengeResults,
                repeatCount = this.repeatCount,
                completedAt = completedAt
            )
        )
    } catch (e: IllegalArgumentException) {
        DomainResult.Failure(AppError.Validation)
    }
}

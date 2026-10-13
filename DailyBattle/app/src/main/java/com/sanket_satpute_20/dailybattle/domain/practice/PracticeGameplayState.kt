package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeState
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.scoring.AggregatedBattleScore

/**
 * Unified representation of the active practice gameplay state.
 */
data class PracticeGameplayState(
    val session: PracticeSession?,
    val currentChallengeIndex: Int?,
    val totalChallenges: Int,
    val currentChallengeType: ChallengeType?,
    val challengeState: ChallengeState?,
    val challengeResults: Map<ChallengeType, ChallengeResult>,
    val aggregatedScore: AggregatedBattleScore?,
    val isPracticeComplete: Boolean,
    val isInterrupted: Boolean,
)

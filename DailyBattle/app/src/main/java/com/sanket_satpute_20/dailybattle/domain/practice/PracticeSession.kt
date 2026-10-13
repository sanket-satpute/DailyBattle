package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Domain model representing a separate practice session.
 *
 * Requirements:
 * - `08_DATA_AND_API.md` §19: Practice sessions are separate from Official sessions,
 *   may be repeated, do not modify official results, rankings, or streaks.
 * - `15_ANTIGRAVITY_RULES.md` Rule 26: Practice Isolation Rule.
 * - `ROADMAP.md` Sprint 19.1: "Create a separate practice session type."
 *
 * Invariants:
 * - [mode] is immutable and strictly [BattleMode.PRACTICE].
 * - [repeatCount] tracks how many times practice has been repeated (practice repeated edge case).
 */
data class PracticeSession(
    val sessionId: PracticeSessionId,
    val userId: UserId,
    val configuration: PracticeConfiguration = PracticeConfiguration.STANDARD,
    val mode: BattleMode = BattleMode.PRACTICE,
    val status: PracticeSessionStatus = PracticeSessionStatus.NOT_STARTED,
    val currentChallengeIndex: Int? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val challengeSessions: List<ChallengeSession> = emptyList(),
    val challengeResults: List<ChallengeResult> = emptyList(),
    val repeatCount: Int = 0,
) {
    init {
        require(mode == BattleMode.PRACTICE) {
            "PracticeSession must always have mode = BattleMode.PRACTICE. Found: $mode"
        }
    }

    val isNotStarted: Boolean get() = status == PracticeSessionStatus.NOT_STARTED
    val isInProgress: Boolean get() = status == PracticeSessionStatus.IN_PROGRESS
    val isCompleted: Boolean get() = status == PracticeSessionStatus.COMPLETED
    val isInterrupted: Boolean get() = status == PracticeSessionStatus.INTERRUPTED
}

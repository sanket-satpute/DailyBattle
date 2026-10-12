package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType

/**
 * Distinguishes the exact recovery stage for an in-progress or restored BattleSession.
 *
 * Conforms to:
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §20 (Gameplay State Recovery):
 *   - Challenge not started
 *   - Challenge active
 *   - Challenge completed
 *   - Battle completed
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §19 (Screen Recreation):
 *   - Screen recreation must not restart a challenge, reset a score, create a new session,
 *     submit an answer twice, or navigate to the wrong screen.
 * - 06_NAVIGATION_AND_FLOWS.md §55 (Screen Recreation):
 *   - Configuration changes or process recreation must not silently create a new Battle,
 *     reset official attempt, duplicate a result, or lose a completed challenge.
 */
sealed interface GameplayRecoveryState {

    /**
     * Session exists but no challenge has begun yet.
     */
    data object NotStarted : GameplayRecoveryState

    /**
     * A challenge is currently active and in-progress.
     *
     * @param challengeIndex 1-based challenge index (1=Snap, 2=Shift, 3=Crowd Call).
     * @param challengeType The official [ChallengeType] for this index.
     * @param isFirstChallenge True if this is the first challenge of the battle sequence.
     */
    data class ChallengeActive(
        val challengeIndex: Int,
        val challengeType: ChallengeType,
        val isFirstChallenge: Boolean,
    ) : GameplayRecoveryState

    /**
     * A challenge has just completed, and next challenge is pending.
     *
     * @param challengeIndex 1-based index of the completed challenge.
     * @param challengeType The [ChallengeType] just completed.
     * @param nextChallengeIndex The 1-based index of the next challenge, or null if all completed.
     * @param nextChallengeType The [ChallengeType] of the next challenge, or null if all completed.
     */
    data class ChallengeCompleted(
        val challengeIndex: Int,
        val challengeType: ChallengeType,
        val nextChallengeIndex: Int?,
        val nextChallengeType: ChallengeType?,
    ) : GameplayRecoveryState

    /**
     * All challenges in the battle have completed, awaiting or displaying results.
     */
    data object BattleCompleted : GameplayRecoveryState
}

/**
 * Result of a gameplay state restoration attempt.
 */
sealed interface GameplayRestorationResult {

    /**
     * No existing session found for the user and battle ID.
     */
    data object NoSession : GameplayRestorationResult

    /**
     * Session successfully restored and verified from authoritative persistence.
     *
     * @param session The authoritative restored [BattleSession].
     * @param recoveryState Granular recovery classification ([GameplayRecoveryState]).
     * @param targetRoute The authoritative route to navigate to (§19, §20, §55).
     * @param isDuplicateSubmissionPrevented True if guards are armed against duplicate submission.
     */
    data class Restored(
        val session: BattleSession,
        val recoveryState: GameplayRecoveryState,
        val targetRoute: String,
        val isDuplicateSubmissionPrevented: Boolean = true,
    ) : GameplayRestorationResult
}

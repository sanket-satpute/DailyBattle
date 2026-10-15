package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates gameplay state restoration and navigation route recovery across screen recreation,
 * configuration changes, and process recreation.
 *
 * Conforms to:
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §19 (Screen Recreation):
 *   - Screen recreation must not restart a challenge, reset a score, create a new session,
 *     submit an answer twice, or navigate to the wrong screen.
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §20 (Gameplay State Recovery):
 *   - Distinguishes: Challenge not started, Challenge active, Challenge completed, Battle completed.
 *   - Example: SNAP = COMPLETE, SHIFT = ACTIVE, CROWD CALL = NOT_STARTED means the app must
 *     never return the user to Snap.
 * - 06_NAVIGATION_AND_FLOWS.md §55 (Screen Recreation):
 *   - Configuration changes or process recreation must not silently create a new Battle,
 *     reset official attempt, duplicate a result, or lose a completed challenge.
 * - 07_TECHNICAL_ARCHITECTURE.md §28 (Lifecycle Handling) & §47 (State Restoration):
 *   - Important screen and battle state survives Activity recreation and process recreation.
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (Idempotency & Duplicate Prevention), Rule 43 (Lifecycle), Rule 44 (Persistence).
 */
interface GameplayStateRestorer {

    /**
     * Inspects persistent storage and restores the authoritative BattleSession state
     * into memory without creating a new session, restarting challenges, or resetting scores.
     */
    fun restoreGameplayState(userId: UserId, battleId: BattleId): GameplayRestorationResult

    /**
     * Inspects persistent storage for any in-progress official BattleSession for the given user,
     * and restores it into memory without creating a new session or resetting state (§16, §17).
     */
    fun restoreActiveGameplayState(userId: UserId): GameplayRestorationResult

    /**
     * Determines the authoritative target navigation route based on the reconstructed
     * session state (§19, §20, §55).
     *
     * Example:
     * - SNAP = COMPLETE, SHIFT = ACTIVE -> Returns Shift route (never Snap).
     * - All Complete -> Returns Results route.
     */
    fun determineRestoredRoute(session: BattleSession): String

    /**
     * Evaluates the granular recovery stage for the session.
     */
    fun evaluateRecoveryState(session: BattleSession): GameplayRecoveryState

    /**
     * Guards against duplicate challenge submission during screen recreation or retries (§19, Rule 42).
     * Returns true only if the specified challenge index is currently active and not yet completed.
     */
    fun canSubmitChallenge(session: BattleSession, challengeIndex: Int): Boolean
}

@Singleton
class DefaultGameplayStateRestorer @Inject constructor(
    private val sessionRepository: BattleSessionRepository,
    private val activeBattleSessionManager: ActiveBattleSessionManager,
    private val lifecycleBattlePreserver: LifecycleBattlePreserver,
    private val lifecycleManager: AppLifecycleManager,
) : GameplayStateRestorer, AppLifecycleListener {

    init {
        lifecycleManager.addListener(this)
    }

    override fun restoreGameplayState(userId: UserId, battleId: BattleId): GameplayRestorationResult {
        // Retrieve authoritative session from repository (or attached memory if already synchronized)
        val session = sessionRepository.getOfficialSession(userId, battleId)
            ?: lifecycleBattlePreserver.activeSession.value?.takeIf { it.userId == userId && it.battleId == battleId }
            ?: return GameplayRestorationResult.NoSession

        // Reconstruct engine and in-memory holders without creating a duplicate session or resetting scores
        activeBattleSessionManager.bindSession(session)
        lifecycleBattlePreserver.attachActiveSession(session)

        val recoveryState = evaluateRecoveryState(session)
        val targetRoute = determineRestoredRoute(session)

        return GameplayRestorationResult.Restored(
            session = session,
            recoveryState = recoveryState,
            targetRoute = targetRoute,
            isDuplicateSubmissionPrevented = true,
        )
    }

    override fun restoreActiveGameplayState(userId: UserId): GameplayRestorationResult {
        val session = sessionRepository.getActiveOfficialSession(userId)
            ?: lifecycleBattlePreserver.activeSession.value?.takeIf { it.userId == userId && it.status == BattleSessionStatus.IN_PROGRESS }
            ?: return GameplayRestorationResult.NoSession

        activeBattleSessionManager.bindSession(session)
        lifecycleBattlePreserver.attachActiveSession(session)

        val recoveryState = evaluateRecoveryState(session)
        val targetRoute = determineRestoredRoute(session)

        return GameplayRestorationResult.Restored(
            session = session,
            recoveryState = recoveryState,
            targetRoute = targetRoute,
            isDuplicateSubmissionPrevented = true,
        )
    }

    override fun evaluateRecoveryState(session: BattleSession): GameplayRecoveryState {
        return when (session.status) {
            BattleSessionStatus.NOT_STARTED -> GameplayRecoveryState.NotStarted
            BattleSessionStatus.COMPLETED -> GameplayRecoveryState.BattleCompleted
            BattleSessionStatus.IN_PROGRESS -> {
                val currentIdx = session.currentChallenge ?: 1
                val challengeSessions = session.challengeSessions
                val cSession = challengeSessions.getOrNull(currentIdx - 1)

                val challengeType = ChallengeSequence.typeForIndex(currentIdx)

                if (cSession != null && cSession.status == ChallengeSessionStatus.COMPLETE) {
                    if (currentIdx < ChallengeSequence.TOTAL_CHALLENGES) {
                        val nextIdx = currentIdx + 1
                        val nextType = ChallengeSequence.typeForIndex(nextIdx)
                        GameplayRecoveryState.ChallengeCompleted(
                            challengeIndex = currentIdx,
                            challengeType = challengeType,
                            nextChallengeIndex = nextIdx,
                            nextChallengeType = nextType,
                        )
                    } else {
                        // All 3 challenges are completed
                        GameplayRecoveryState.BattleCompleted
                    }
                } else {
                    // Current challenge is active or ready
                    GameplayRecoveryState.ChallengeActive(
                        challengeIndex = currentIdx,
                        challengeType = challengeType,
                        isFirstChallenge = (currentIdx == 1),
                    )
                }
            }
        }
    }

    override fun determineRestoredRoute(session: BattleSession): String {
        val recoveryState = evaluateRecoveryState(session)
        return when (recoveryState) {
            is GameplayRecoveryState.NotStarted -> GameplayNavigationRoutes.BATTLE_INTRO
            is GameplayRecoveryState.BattleCompleted -> GameplayNavigationRoutes.RESULTS
            is GameplayRecoveryState.ChallengeActive -> {
                routeForChallengeType(recoveryState.challengeType)
            }
            is GameplayRecoveryState.ChallengeCompleted -> {
                val nextType = recoveryState.nextChallengeType
                if (nextType != null) {
                    routeForChallengeType(nextType)
                } else {
                    GameplayNavigationRoutes.RESULTS
                }
            }
        }
    }

    override fun canSubmitChallenge(session: BattleSession, challengeIndex: Int): Boolean {
        // If battle is completed, submissions are strictly prohibited
        if (session.status == BattleSessionStatus.COMPLETED) {
            return false
        }

        // If this challenge index does not match the active challenge pointer, prohibit
        if (session.currentChallenge != challengeIndex) {
            return false
        }

        // If this challenge was already marked COMPLETE, prohibit duplicate submission (§19)
        val cSession = session.challengeSessions.getOrNull(challengeIndex - 1)
        if (cSession != null && cSession.status == ChallengeSessionStatus.COMPLETE) {
            return false
        }

        return true
    }

    override fun onLifecycleEvent(event: AppLifecycleEvent) {
        if (event is AppLifecycleEvent.Recreated) {
            // Upon screen recreation or process restoration, verify active session integrity
            val current = lifecycleBattlePreserver.activeSession.value
            if (current != null) {
                val persisted = sessionRepository.getOfficialSession(current.userId, current.battleId)
                if (persisted != null) {
                    // Synchronize and rebind without creating duplicate attempt or resetting attempt state
                    activeBattleSessionManager.bindSession(persisted)
                    lifecycleBattlePreserver.attachActiveSession(persisted)
                }
            }
        }
    }

    private fun routeForChallengeType(type: ChallengeType): String {
        return when (type) {
            ChallengeType.SNAP -> GameplayNavigationRoutes.SNAP
            ChallengeType.SHIFT -> GameplayNavigationRoutes.SHIFT
            ChallengeType.CROWD_CALL -> GameplayNavigationRoutes.CROWD_CALL
        }
    }
}

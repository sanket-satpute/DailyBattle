package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.RecoverUnacknowledgedResultUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.navigation.AppRoute
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of recovering an interrupted battle after process death.
 */
sealed interface ProcessDeathRecoveryResult {
    /**
     * No active battle session was in progress when process death occurred.
     */
    data object NoActiveBattle : ProcessDeathRecoveryResult

    /**
     * An active official battle session was successfully recovered from persisted state.
     *
     * @param session The authoritative recovered [BattleSession].
     * @param recoveryState The granular stage of gameplay recovered (Snap active, Shift active, etc.).
     * @param targetRoute The authoritative route to navigate to (§17, §19, §20, §57).
     */
    data class Recovered(
        val session: BattleSession,
        val recoveryState: GameplayRecoveryState,
        val targetRoute: String,
    ) : ProcessDeathRecoveryResult

    /**
     * The battle had already been completed prior to process death.
     *
     * @param session The completed [BattleSession].
     */
    data class BattleCompleted(
        val session: BattleSession,
    ) : ProcessDeathRecoveryResult
}

/**
 * Coordinates recovery of active battle state following Android process death.
 *
 * Conforms to:
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §16 (Official Battle Session Recovery):
 *   App launch checks for active official session; if found, restores rather than creates another.
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §17 (Process Death):
 *   Process death must not create a new official attempt. App recovers from persisted state.
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §20 (Gameplay State Recovery):
 *   Distinguishes Snap active, Shift active, Crowd Call active, Battle complete.
 * - 06_NAVIGATION_AND_FLOWS.md §57 (App Re-entry):
 *   If battle in progress, resume appropriate battle state.
 * - 12_TESTING_AND_QA.md E2E-006 (Process death recovery).
 * - 15_ANTIGRAVITY_RULES.md Rule 42, Rule 43, Rule 44.
 */
interface ProcessDeathRecoverer {
    /**
     * Recovers any active official battle for the user after process death.
     *
     * If [battleId] is provided, looks up that specific battle session.
     * If [battleId] is null, looks up any in-progress official battle session.
     */
    fun recoverBattleAfterProcessDeath(
        userId: UserId,
        battleId: BattleId? = null,
    ): ProcessDeathRecoveryResult
}

@Singleton
class DefaultProcessDeathRecoverer @Inject constructor(
    private val sessionRepository: BattleSessionRepository,
    private val gameplayStateRestorer: GameplayStateRestorer,
    private val activeBattleSessionManager: ActiveBattleSessionManager,
    private val lifecycleBattlePreserver: LifecycleBattlePreserver,
    private val recoverUnacknowledgedResultUseCase: RecoverUnacknowledgedResultUseCase,
) : ProcessDeathRecoverer {

    override fun recoverBattleAfterProcessDeath(
        userId: UserId,
        battleId: BattleId?,
    ): ProcessDeathRecoveryResult {
        // 1. Look up persisted session
        val session = if (battleId != null) {
            sessionRepository.getOfficialSession(userId, battleId)
        } else {
            sessionRepository.getActiveOfficialSession(userId)
        } ?: return ProcessDeathRecoveryResult.NoActiveBattle

        // 2. If session status is COMPLETED, no new attempt can be created (§17, §18)
        if (session.status == BattleSessionStatus.COMPLETED) {
            lifecycleBattlePreserver.attachActiveSession(session)
            activeBattleSessionManager.bindSession(session)
            return ProcessDeathRecoveryResult.BattleCompleted(session)
        }

        // 3. If session status is IN_PROGRESS, reconstruct and restore state
        val restorationResult = gameplayStateRestorer.restoreGameplayState(session.userId, session.battleId)
        return when (restorationResult) {
            is GameplayRestorationResult.Restored -> {
                if (restorationResult.recoveryState == GameplayRecoveryState.BattleCompleted) {
                    ProcessDeathRecoveryResult.BattleCompleted(restorationResult.session)
                } else {
                    ProcessDeathRecoveryResult.Recovered(
                        session = restorationResult.session,
                        recoveryState = restorationResult.recoveryState,
                        targetRoute = restorationResult.targetRoute,
                    )
                }
            }
            is GameplayRestorationResult.NoSession -> {
                ProcessDeathRecoveryResult.NoActiveBattle
            }
        }
    }
}

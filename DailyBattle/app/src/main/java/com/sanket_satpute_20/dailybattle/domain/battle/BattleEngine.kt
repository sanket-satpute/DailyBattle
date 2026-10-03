package com.sanket_satpute_20.dailybattle.domain.battle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Orchestrator enforcing attempt integrity for a single BattleSession.
 *
 * Implements Sprint 5.5 protections:
 * - Duplicate event rejection
 * - Out of order event rejection (via state machines)
 * - State restoration from disk (via reconstruction)
 * - Simultaneous state events (via synchronization lock)
 * - Modifying completed results (rejected natively by terminal states)
 */
class BattleEngine(
    initialSession: BattleSession,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val lock = Any()
    
    var session: BattleSession = initialSession
        private set

    private val battleStateMachine = BattleStateMachine(reconstructBattleState(initialSession))
    private var challengeStateMachine: ChallengeStateMachine? = reconstructChallengeState(initialSession)

    val currentBattleState: BattleState
        get() = battleStateMachine.currentState

    fun processBattleEvent(event: BattleEvent): DomainResult<BattleSession> = synchronized(lock) {
        val result = battleStateMachine.transition(event)
        if (result is DomainResult.Failure) return result

        val newState = (result as DomainResult.Success).value
        session = updateSessionFromBattleState(session, newState, clock())
        
        // Synchronize inner challenge machine lifecycle
        if (newState is BattleState.Active) {
            val csmState = challengeStateMachine?.currentState
            if (csmState == null || csmState == ChallengeState.Complete) {
                challengeStateMachine = ChallengeStateMachine(ChallengeState.Ready)
            }
        } else if (newState is BattleState.ChallengeComplete || newState is BattleState.NextChallenge || newState is BattleState.BattleComplete || newState is BattleState.Results) {
            challengeStateMachine = null
        }
        
        return DomainResult.Success(session)
    }

    fun processChallengeEvent(event: ChallengeEvent): DomainResult<BattleSession> = synchronized(lock) {
        val csm = challengeStateMachine ?: return DomainResult.Failure(AppError.Domain)
        
        val result = csm.transition(event)
        if (result is DomainResult.Failure) return result

        val newChallengeState = (result as DomainResult.Success).value
        session = updateSessionFromChallengeState(session, newChallengeState, clock())
        
        // Auto-progress BattleStateMachine when Challenge completes
        if (newChallengeState is ChallengeState.Complete) {
            val bResult = battleStateMachine.transition(BattleEvent.FinishChallenge)
            if (bResult is DomainResult.Success) {
                session = updateSessionFromBattleState(session, bResult.value, clock())
            }
        }
        
        return DomainResult.Success(session)
    }

    private fun updateSessionFromBattleState(
        current: BattleSession, 
        state: BattleState, 
        now: Long
    ): BattleSession {
        return when (state) {
            is BattleState.NotStarted -> current
            is BattleState.Ready -> {
                val newStatus = if (current.status == BattleSessionStatus.NOT_STARTED) BattleSessionStatus.IN_PROGRESS else current.status
                val newStartedAt = current.startedAt ?: now
                current.copy(status = newStatus, startedAt = newStartedAt, currentChallenge = state.challengeIndex)
            }
            is BattleState.Active -> current.copy(currentChallenge = state.challengeIndex)
            is BattleState.ChallengeComplete -> current.copy(currentChallenge = state.challengeIndex)
            is BattleState.NextChallenge -> current.copy(currentChallenge = state.challengeIndex)
            is BattleState.BattleComplete -> current.copy(currentChallenge = null)
            is BattleState.Results -> current.copy(
                status = BattleSessionStatus.COMPLETED, 
                currentChallenge = null,
                completedAt = current.completedAt ?: now
            )
        }
    }

    private fun updateSessionFromChallengeState(
        current: BattleSession, 
        state: ChallengeState, 
        now: Long
    ): BattleSession {
        val idx = current.currentChallenge ?: return current
        val currentChallenges = current.challengeSessions.toMutableList()
        val cSessionIndex = idx - 1
        
        val status = when (state) {
            is ChallengeState.Ready -> ChallengeSessionStatus.READY
            is ChallengeState.Active -> ChallengeSessionStatus.ACTIVE
            is ChallengeState.Correct -> ChallengeSessionStatus.CORRECT
            is ChallengeState.Incorrect -> ChallengeSessionStatus.INCORRECT
            is ChallengeState.Complete -> ChallengeSessionStatus.COMPLETE
        }

        if (cSessionIndex < currentChallenges.size) {
            val existing = currentChallenges[cSessionIndex]
            val startedAt = if (status == ChallengeSessionStatus.ACTIVE && existing.startedAt == null) now else existing.startedAt
            val completedAt = if (status == ChallengeSessionStatus.COMPLETE && existing.completedAt == null) now else existing.completedAt
            currentChallenges[cSessionIndex] = existing.copy(status = status, startedAt = startedAt, completedAt = completedAt)
        } else {
            // New challenge session
            val startedAt = if (status == ChallengeSessionStatus.ACTIVE) now else null
            val completedAt = if (status == ChallengeSessionStatus.COMPLETE) now else null
            currentChallenges.add(
                ChallengeSession(
                    challengeId = ChallengeId("c-$idx"), 
                    sessionId = current.sessionId,
                    status = status,
                    startedAt = startedAt,
                    completedAt = completedAt
                )
            )
        }
        
        return current.copy(challengeSessions = currentChallenges)
    }

    companion object {
        fun reconstructBattleState(session: BattleSession): BattleState {
            return when (session.status) {
                BattleSessionStatus.NOT_STARTED -> BattleState.NotStarted
                BattleSessionStatus.COMPLETED -> BattleState.Results
                BattleSessionStatus.IN_PROGRESS -> {
                    val idx = session.currentChallenge ?: return BattleState.NotStarted
                    val cSession = session.challengeSessions.getOrNull(idx - 1)
                    if (cSession == null) {
                        BattleState.Ready(idx)
                    } else {
                        when (cSession.status) {
                            ChallengeSessionStatus.COMPLETE -> BattleState.ChallengeComplete(idx)
                            else -> BattleState.Active(idx)
                        }
                    }
                }
            }
        }

        fun reconstructChallengeState(session: BattleSession): ChallengeStateMachine? {
            if (session.status != BattleSessionStatus.IN_PROGRESS) return null
            val idx = session.currentChallenge ?: return null
            val cSession = session.challengeSessions.getOrNull(idx - 1) ?: return null
            
            val state = when (cSession.status) {
                ChallengeSessionStatus.READY -> ChallengeState.Ready
                ChallengeSessionStatus.ACTIVE -> ChallengeState.Active
                ChallengeSessionStatus.CORRECT -> ChallengeState.Correct
                ChallengeSessionStatus.INCORRECT -> ChallengeState.Incorrect
                ChallengeSessionStatus.COMPLETE -> ChallengeState.Complete
            }
            return ChallengeStateMachine(state)
        }
    }
}

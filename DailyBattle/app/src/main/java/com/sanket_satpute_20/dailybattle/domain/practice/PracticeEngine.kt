package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeEvent
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeState
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeStateMachine
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.AggregatedBattleScore
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreConstants
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreValidator
import java.util.UUID

/**
 * State machine orchestrator enforcing lifecycle and state transitions for a [PracticeSession].
 *
 * Implements Sprint 19.2 — Practice Gameplay:
 * - Safely reuses [ChallengeStateMachine], [ChallengeEvent], and [ChallengeState].
 * - Manages challenge-level gameplay transitions (Ready -> Active -> Correct/Incorrect -> Complete).
 * - Enforces immediate gameplay feedback per REQ-GAME-016.
 * - Safely scores practice challenges via [ChallengeScoreValidator] and [BattleScoreAggregator].
 * - Invariant: Completely isolated from official battle state and official backend submission.
 */
class PracticeEngine(
    initialSession: PracticeSession,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val lock = Any()
    private val scoreValidator = ChallengeScoreValidator()
    private val scoreAggregator = BattleScoreAggregator()

    var session: PracticeSession = initialSession
        private set

    private var challengeStateMachine: ChallengeStateMachine? = reconstructChallengeState(initialSession)

    val currentChallengeStateMachine: ChallengeStateMachine?
        get() = challengeStateMachine

    val currentChallengeState: ChallengeState?
        get() = challengeStateMachine?.currentState

    val currentChallengeType: ChallengeType?
        get() {
            val idx = session.currentChallengeIndex ?: return null
            return session.configuration.challengeTypes.getOrNull(idx - 1)
        }

    val lifecycle: PracticeSessionLifecycle
        get() = when (session.status) {
            PracticeSessionStatus.NOT_STARTED -> PracticeSessionLifecycle.Created
            PracticeSessionStatus.COMPLETED -> PracticeSessionLifecycle.PracticeComplete
            PracticeSessionStatus.INTERRUPTED -> PracticeSessionLifecycle.Interrupted()
            PracticeSessionStatus.IN_PROGRESS -> {
                val idx = session.currentChallengeIndex ?: 1
                val cState = challengeStateMachine?.currentState
                when (cState) {
                    is ChallengeState.Ready -> PracticeSessionLifecycle.Ready(idx)
                    is ChallengeState.Active,
                    is ChallengeState.Correct,
                    is ChallengeState.Incorrect -> PracticeSessionLifecycle.ChallengeActive(idx)
                    is ChallengeState.Complete -> PracticeSessionLifecycle.ChallengeComplete(idx)
                    null -> {
                        val cSession = session.challengeSessions.getOrNull(idx - 1)
                        when {
                            cSession == null || cSession.status == ChallengeSessionStatus.READY -> PracticeSessionLifecycle.Ready(idx)
                            cSession.status == ChallengeSessionStatus.COMPLETE -> PracticeSessionLifecycle.ChallengeComplete(idx)
                            else -> PracticeSessionLifecycle.ChallengeActive(idx)
                        }
                    }
                }
            }
        }

    /**
     * Processes individual gameplay events for the active practice challenge.
     * Reuses [ChallengeStateMachine] safely per Sprint 19.2.
     */
    fun processChallengeEvent(event: ChallengeEvent): DomainResult<PracticeSession> = synchronized(lock) {
        if (session.status != PracticeSessionStatus.IN_PROGRESS) {
            return DomainResult.Failure(AppError.Domain)
        }

        val csm = challengeStateMachine ?: return DomainResult.Failure(AppError.Domain)
        val transitionResult = csm.transition(event)
        if (transitionResult is DomainResult.Failure) {
            return transitionResult
        }

        val newChallengeState = (transitionResult as DomainResult.Success).value
        session = updateSessionFromChallengeState(session, newChallengeState, clock())

        return DomainResult.Success(session)
    }

    /**
     * Records a score outcome for a completed practice challenge, enforcing structural score boundaries.
     */
    fun recordChallengeResult(
        challengeType: ChallengeType,
        score: Int,
        metadata: Map<String, String>? = null
    ): DomainResult<ChallengeResult> = synchronized(lock) {
        val rawResult = ChallengeResult(
            resultId = ChallengeResultId(UUID.randomUUID().toString()),
            challengeId = ChallengeId("practice-${challengeType.name.lowercase()}"),
            sessionId = BattleSessionId(session.sessionId.value),
            score = score,
            maxScore = ChallengeScoreConstants.maxScoreFor(challengeType),
            completedAt = clock(),
            metadata = metadata
        )

        val validResult = scoreValidator.validate(rawResult, challengeType)
        val updatedResults = session.challengeResults.filter { it.challengeId != validResult.challengeId } + validResult
        session = session.copy(challengeResults = updatedResults)

        return DomainResult.Success(validResult)
    }

    /**
     * Aggregates current practice challenge results using the domain [BattleScoreAggregator].
     * In Practice mode, Consistency score is always 0.
     */
    fun getAggregatedScore(): AggregatedBattleScore = synchronized(lock) {
        val snapResult = session.challengeResults.find { it.challengeId.value.contains("snap") }
        val shiftResult = session.challengeResults.find { it.challengeId.value.contains("shift") }
        val crowdCallResult = session.challengeResults.find { it.challengeId.value.contains("crowd_call") }

        return scoreAggregator.aggregate(
            mode = BattleMode.PRACTICE,
            snapResult = snapResult,
            shiftResult = shiftResult,
            crowdCallResult = crowdCallResult
        )
    }

    /**
     * Processes high-level practice flow events.
     */
    fun processEvent(event: PracticeEvent): DomainResult<PracticeSession> = synchronized(lock) {
        val totalChallenges = session.configuration.challengeTypes.size

        when (event) {
            is PracticeEvent.StartPractice -> {
                if (session.status != PracticeSessionStatus.NOT_STARTED) {
                    return DomainResult.Failure(AppError.Domain)
                }
                session = session.copy(
                    status = PracticeSessionStatus.IN_PROGRESS,
                    startedAt = event.now,
                    currentChallengeIndex = 1,
                )
                challengeStateMachine = ChallengeStateMachine(ChallengeState.Ready)
                DomainResult.Success(session)
            }

            is PracticeEvent.BeginChallenge -> {
                if (session.status != PracticeSessionStatus.IN_PROGRESS) {
                    return DomainResult.Failure(AppError.Domain)
                }
                val idx = event.challengeIndex
                if (idx < 1 || idx > totalChallenges) {
                    return DomainResult.Failure(AppError.Validation)
                }

                if (challengeStateMachine == null || challengeStateMachine?.currentState == ChallengeState.Complete) {
                    challengeStateMachine = ChallengeStateMachine(ChallengeState.Ready)
                }
                val startResult = challengeStateMachine?.transition(ChallengeEvent.Start)
                if (startResult is DomainResult.Failure) {
                    return startResult
                }

                session = updateSessionFromChallengeState(
                    session.copy(currentChallengeIndex = idx),
                    ChallengeState.Active,
                    event.now
                )
                DomainResult.Success(session)
            }

            is PracticeEvent.FinishChallenge -> {
                if (session.status != PracticeSessionStatus.IN_PROGRESS) {
                    return DomainResult.Failure(AppError.Domain)
                }
                val idx = event.challengeIndex
                if (idx < 1 || idx > totalChallenges) {
                    return DomainResult.Failure(AppError.Validation)
                }

                if (challengeStateMachine?.currentState is ChallengeState.Active ||
                    challengeStateMachine?.currentState is ChallengeState.Correct ||
                    challengeStateMachine?.currentState is ChallengeState.Incorrect
                ) {
                    challengeStateMachine?.transition(ChallengeEvent.Finish)
                } else if (challengeStateMachine == null) {
                    challengeStateMachine = ChallengeStateMachine(ChallengeState.Complete)
                }

                session = updateSessionFromChallengeState(
                    session.copy(currentChallengeIndex = idx),
                    ChallengeState.Complete,
                    event.now
                )
                DomainResult.Success(session)
            }

            is PracticeEvent.ContinueNext -> {
                if (session.status != PracticeSessionStatus.IN_PROGRESS) {
                    return DomainResult.Failure(AppError.Domain)
                }
                val currentIdx = session.currentChallengeIndex ?: 1

                if (currentIdx < totalChallenges) {
                    val nextIdx = currentIdx + 1
                    session = session.copy(
                        currentChallengeIndex = nextIdx
                    )
                    challengeStateMachine = ChallengeStateMachine(ChallengeState.Ready)
                } else {
                    // All challenges complete -> complete practice session
                    session = session.copy(
                        status = PracticeSessionStatus.COMPLETED,
                        completedAt = event.now,
                        currentChallengeIndex = null
                    )
                    challengeStateMachine = null
                }
                DomainResult.Success(session)
            }

            is PracticeEvent.InterruptPractice -> {
                if (session.status != PracticeSessionStatus.IN_PROGRESS && session.status != PracticeSessionStatus.NOT_STARTED) {
                    return DomainResult.Failure(AppError.Domain)
                }
                session = session.copy(
                    status = PracticeSessionStatus.INTERRUPTED,
                    completedAt = event.now,
                    currentChallengeIndex = null
                )
                challengeStateMachine = null
                DomainResult.Success(session)
            }

            is PracticeEvent.RepeatPractice -> {
                session = session.copy(
                    status = PracticeSessionStatus.IN_PROGRESS,
                    currentChallengeIndex = 1,
                    startedAt = event.now,
                    completedAt = null,
                    challengeSessions = emptyList(),
                    challengeResults = emptyList(),
                    repeatCount = session.repeatCount + 1
                )
                challengeStateMachine = ChallengeStateMachine(ChallengeState.Ready)
                DomainResult.Success(session)
            }

            is PracticeEvent.CompletePractice -> {
                if (session.status != PracticeSessionStatus.IN_PROGRESS) {
                    return DomainResult.Failure(AppError.Domain)
                }
                session = session.copy(
                    status = PracticeSessionStatus.COMPLETED,
                    completedAt = event.now,
                    currentChallengeIndex = null
                )
                challengeStateMachine = null
                DomainResult.Success(session)
            }
        }
    }

    private fun updateSessionFromChallengeState(
        current: PracticeSession,
        state: ChallengeState,
        now: Long
    ): PracticeSession {
        val idx = current.currentChallengeIndex ?: return current
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
            currentChallenges[cSessionIndex] = existing.copy(
                status = status,
                startedAt = startedAt,
                completedAt = completedAt
            )
        } else {
            val startedAt = if (status == ChallengeSessionStatus.ACTIVE) now else null
            val completedAt = if (status == ChallengeSessionStatus.COMPLETE) now else null
            currentChallenges.add(
                ChallengeSession(
                    challengeId = ChallengeId("practice-$idx"),
                    sessionId = BattleSessionId(current.sessionId.value),
                    status = status,
                    startedAt = startedAt,
                    completedAt = completedAt
                )
            )
        }

        return current.copy(challengeSessions = currentChallenges)
    }

    companion object {
        fun reconstructChallengeState(session: PracticeSession): ChallengeStateMachine? {
            if (session.status != PracticeSessionStatus.IN_PROGRESS) return null
            val idx = session.currentChallengeIndex ?: return null
            val cSession = session.challengeSessions.getOrNull(idx - 1) ?: return ChallengeStateMachine(ChallengeState.Ready)

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

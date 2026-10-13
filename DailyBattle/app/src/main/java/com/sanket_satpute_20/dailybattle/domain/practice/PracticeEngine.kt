package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * State machine orchestrator enforcing lifecycle and state transitions for a [PracticeSession].
 *
 * Distinct and isolated from [com.sanket_satpute_20.dailybattle.domain.battle.BattleEngine].
 */
class PracticeEngine(
    initialSession: PracticeSession,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val lock = Any()

    var session: PracticeSession = initialSession
        private set

    val lifecycle: PracticeSessionLifecycle
        get() = when (session.status) {
            PracticeSessionStatus.NOT_STARTED -> PracticeSessionLifecycle.Created
            PracticeSessionStatus.COMPLETED -> PracticeSessionLifecycle.PracticeComplete
            PracticeSessionStatus.INTERRUPTED -> PracticeSessionLifecycle.Interrupted()
            PracticeSessionStatus.IN_PROGRESS -> {
                val idx = session.currentChallengeIndex ?: 1
                val cSession = session.challengeSessions.getOrNull(idx - 1)
                when {
                    cSession == null -> PracticeSessionLifecycle.Ready(idx)
                    cSession.status == ChallengeSessionStatus.COMPLETE -> PracticeSessionLifecycle.ChallengeComplete(idx)
                    else -> PracticeSessionLifecycle.ChallengeActive(idx)
                }
            }
        }

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

                val currentChallenges = session.challengeSessions.toMutableList()
                val cSessionIndex = idx - 1

                if (cSessionIndex < currentChallenges.size) {
                    val existing = currentChallenges[cSessionIndex]
                    currentChallenges[cSessionIndex] = existing.copy(
                        status = ChallengeSessionStatus.ACTIVE,
                        startedAt = existing.startedAt ?: event.now
                    )
                } else {
                    currentChallenges.add(
                        ChallengeSession(
                            challengeId = ChallengeId("practice-$idx"),
                            sessionId = BattleSessionId(session.sessionId.value),
                            status = ChallengeSessionStatus.ACTIVE,
                            startedAt = event.now,
                            completedAt = null
                        )
                    )
                }

                session = session.copy(
                    currentChallengeIndex = idx,
                    challengeSessions = currentChallenges
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

                val currentChallenges = session.challengeSessions.toMutableList()
                val cSessionIndex = idx - 1

                if (cSessionIndex < currentChallenges.size) {
                    val existing = currentChallenges[cSessionIndex]
                    currentChallenges[cSessionIndex] = existing.copy(
                        status = ChallengeSessionStatus.COMPLETE,
                        completedAt = existing.completedAt ?: event.now
                    )
                } else {
                    currentChallenges.add(
                        ChallengeSession(
                            challengeId = ChallengeId("practice-$idx"),
                            sessionId = BattleSessionId(session.sessionId.value),
                            status = ChallengeSessionStatus.COMPLETE,
                            startedAt = event.now,
                            completedAt = event.now
                        )
                    )
                }

                session = session.copy(
                    currentChallengeIndex = idx,
                    challengeSessions = currentChallenges
                )
                DomainResult.Success(session)
            }

            is PracticeEvent.ContinueNext -> {
                if (session.status != PracticeSessionStatus.IN_PROGRESS) {
                    return DomainResult.Failure(AppError.Domain)
                }
                val currentIdx = session.currentChallengeIndex ?: 1

                if (currentIdx < totalChallenges) {
                    session = session.copy(
                        currentChallengeIndex = currentIdx + 1
                    )
                } else {
                    // All challenges complete -> complete practice session
                    session = session.copy(
                        status = PracticeSessionStatus.COMPLETED,
                        completedAt = event.now,
                        currentChallengeIndex = null
                    )
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
                DomainResult.Success(session)
            }

            is PracticeEvent.RepeatPractice -> {
                // Allows repeated practice runs (Practice Repeated edge case)
                session = session.copy(
                    status = PracticeSessionStatus.IN_PROGRESS,
                    currentChallengeIndex = 1,
                    startedAt = event.now,
                    completedAt = null,
                    challengeSessions = emptyList(),
                    repeatCount = session.repeatCount + 1
                )
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
                DomainResult.Success(session)
            }
        }
    }
}

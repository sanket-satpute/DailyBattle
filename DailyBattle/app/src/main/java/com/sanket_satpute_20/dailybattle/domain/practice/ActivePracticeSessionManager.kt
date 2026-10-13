package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeEvent
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeState
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.scoring.AggregatedBattleScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinator managing the active practice session for the app runtime.
 *
 * Implements Sprint 19.2 — Practice Gameplay:
 * - Exposes reactive state for the active challenge, challenge state, and challenge scoring.
 * - Bridges [ChallengeEvent]s to [PracticeEngine].
 * - Enforces Practice Isolation Rule (Rule 26) — completely isolated from [com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager].
 */
@Singleton
class ActivePracticeSessionManager @Inject constructor(
    private val repository: PracticeSessionRepository
) {
    private var engine: PracticeEngine? = null

    private val _activeSession = MutableStateFlow<PracticeSession?>(null)
    val activeSession: StateFlow<PracticeSession?> = _activeSession.asStateFlow()

    private val _lifecycle = MutableStateFlow<PracticeSessionLifecycle?>(null)
    val lifecycle: StateFlow<PracticeSessionLifecycle?> = _lifecycle.asStateFlow()

    private val _currentChallengeState = MutableStateFlow<ChallengeState?>(null)
    val currentChallengeState: StateFlow<ChallengeState?> = _currentChallengeState.asStateFlow()

    private val _currentChallengeType = MutableStateFlow<ChallengeType?>(null)
    val currentChallengeType: StateFlow<ChallengeType?> = _currentChallengeType.asStateFlow()

    private val _challengeResults = MutableStateFlow<Map<ChallengeType, ChallengeResult>>(emptyMap())
    val challengeResults: StateFlow<Map<ChallengeType, ChallengeResult>> = _challengeResults.asStateFlow()

    private val _aggregatedScore = MutableStateFlow<AggregatedBattleScore?>(null)
    val aggregatedScore: StateFlow<AggregatedBattleScore?> = _aggregatedScore.asStateFlow()

    fun bindSession(session: PracticeSession) {
        val newEngine = PracticeEngine(session)
        engine = newEngine
        updateStateFromEngine(newEngine, session)
    }

    fun startPractice(
        userId: UserId,
        configuration: PracticeConfiguration = PracticeConfiguration.STANDARD,
        now: Long = System.currentTimeMillis()
    ): DomainResult<PracticeSession> {
        val validation = configuration.validate()
        if (validation is DomainResult.Failure) {
            return validation
        }

        val newSession = PracticeSession(
            sessionId = PracticeSessionId(UUID.randomUUID().toString()),
            userId = userId,
            configuration = configuration,
            status = PracticeSessionStatus.NOT_STARTED,
            currentChallengeIndex = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList(),
            challengeResults = emptyList(),
            repeatCount = 0
        )

        val newEngine = PracticeEngine(newSession)
        val startResult = newEngine.processEvent(PracticeEvent.StartPractice(now))
        if (startResult is DomainResult.Failure) {
            return startResult
        }

        val startedSession = (startResult as DomainResult.Success).value
        engine = newEngine
        updateStateFromEngine(newEngine, startedSession)

        repository.savePracticeSession(startedSession)
        return DomainResult.Success(startedSession)
    }

    /**
     * Processes individual challenge events (Start, SubmitCorrect, SubmitIncorrect, Continue, Finish)
     * during practice gameplay.
     */
    fun processChallengeEvent(event: ChallengeEvent): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processChallengeEvent(event)
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    /**
     * Records a score outcome for a completed practice challenge.
     */
    fun recordPracticeChallengeScore(
        challengeType: ChallengeType,
        score: Int,
        metadata: Map<String, String>? = null
    ): DomainResult<ChallengeResult> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.recordChallengeResult(challengeType, score, metadata)
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, currentEngine.session)
            repository.savePracticeSession(currentEngine.session)
        }
        return result
    }

    fun beginChallenge(challengeIndex: Int, now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.BeginChallenge(challengeIndex, now))
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun finishChallenge(challengeIndex: Int, now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.FinishChallenge(challengeIndex, now))
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun continueNext(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.ContinueNext(now))
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun interruptPractice(reason: String? = null, now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.InterruptPractice(reason, now))
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun repeatPractice(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.RepeatPractice(now))
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun completePractice(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.CompletePractice(now))
        if (result is DomainResult.Success) {
            updateStateFromEngine(currentEngine, result.value)
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun getAggregatedPracticeScore(): AggregatedBattleScore? {
        return engine?.getAggregatedScore()
    }

    fun clearActivePractice() {
        val currentSession = _activeSession.value
        if (currentSession != null) {
            repository.clearActivePracticeSession(currentSession.userId)
        }
        engine = null
        _activeSession.value = null
        _lifecycle.value = null
        _currentChallengeState.value = null
        _currentChallengeType.value = null
        _challengeResults.value = emptyMap()
        _aggregatedScore.value = null
    }

    private fun updateStateFromEngine(currentEngine: PracticeEngine, session: PracticeSession) {
        _activeSession.value = session
        _lifecycle.value = currentEngine.lifecycle
        _currentChallengeState.value = currentEngine.currentChallengeState
        _currentChallengeType.value = currentEngine.currentChallengeType

        val resultsMap = session.challengeResults.associateBy { res ->
            when {
                res.challengeId.value.contains("snap") -> ChallengeType.SNAP
                res.challengeId.value.contains("shift") -> ChallengeType.SHIFT
                res.challengeId.value.contains("crowd_call") -> ChallengeType.CROWD_CALL
                else -> ChallengeType.SNAP
            }
        }
        _challengeResults.value = resultsMap
        _aggregatedScore.value = currentEngine.getAggregatedScore()
    }
}

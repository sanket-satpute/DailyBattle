package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinator managing the active practice session for the app runtime.
 *
 * Requirements:
 * - Isolated from [com.sanket_satpute_20.dailybattle.domain.battle.ActiveBattleSessionManager].
 * - Supports starting, progressing, repeating, and interrupting practice sessions.
 * - Enforces Practice Isolation Rule (Rule 26) - never touches official battle state.
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

    fun bindSession(session: PracticeSession) {
        engine = PracticeEngine(session)
        _activeSession.value = session
        _lifecycle.value = engine?.lifecycle
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
            repeatCount = 0
        )

        val newEngine = PracticeEngine(newSession)
        val startResult = newEngine.processEvent(PracticeEvent.StartPractice(now))
        if (startResult is DomainResult.Failure) {
            return startResult
        }

        val startedSession = (startResult as DomainResult.Success).value
        engine = newEngine
        _activeSession.value = startedSession
        _lifecycle.value = newEngine.lifecycle

        repository.savePracticeSession(startedSession)
        return DomainResult.Success(startedSession)
    }

    fun beginChallenge(challengeIndex: Int, now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.BeginChallenge(challengeIndex, now))
        if (result is DomainResult.Success) {
            _activeSession.value = result.value
            _lifecycle.value = currentEngine.lifecycle
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun finishChallenge(challengeIndex: Int, now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.FinishChallenge(challengeIndex, now))
        if (result is DomainResult.Success) {
            _activeSession.value = result.value
            _lifecycle.value = currentEngine.lifecycle
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun continueNext(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.ContinueNext(now))
        if (result is DomainResult.Success) {
            _activeSession.value = result.value
            _lifecycle.value = currentEngine.lifecycle
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun interruptPractice(reason: String? = null, now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.InterruptPractice(reason, now))
        if (result is DomainResult.Success) {
            _activeSession.value = result.value
            _lifecycle.value = currentEngine.lifecycle
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun repeatPractice(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.RepeatPractice(now))
        if (result is DomainResult.Success) {
            _activeSession.value = result.value
            _lifecycle.value = currentEngine.lifecycle
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun completePractice(now: Long = System.currentTimeMillis()): DomainResult<PracticeSession> {
        val currentEngine = engine ?: return DomainResult.Failure(AppError.Domain)
        val result = currentEngine.processEvent(PracticeEvent.CompletePractice(now))
        if (result is DomainResult.Success) {
            _activeSession.value = result.value
            _lifecycle.value = currentEngine.lifecycle
            repository.savePracticeSession(result.value)
        }
        return result
    }

    fun clearActivePractice() {
        val currentSession = _activeSession.value
        if (currentSession != null) {
            repository.clearActivePracticeSession(currentSession.userId)
        }
        engine = null
        _activeSession.value = null
        _lifecycle.value = null
    }
}

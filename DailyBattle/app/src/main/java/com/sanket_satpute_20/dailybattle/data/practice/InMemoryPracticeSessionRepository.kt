package com.sanket_satpute_20.dailybattle.data.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSession
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSessionRepository
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeSessionStatus
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe in-memory implementation of [PracticeSessionRepository].
 *
 * Implements the architecture established in `DefaultDomainAuthorityRegistry`:
 * - domain = DomainType.PRACTICE_SESSION
 * - primaryAuthority = DataAuthority.Local
 * - storageTier = StorageTier.IN_MEMORY
 * - isLocallyWritable = true
 * - isRemoteAuthoritative = false
 * - requiresSynchronization = false
 * - allowsOfflineRead = true
 * - allowsOfflineMutation = true
 * - conflictResolutionRule = "Local-only; practice plays are never submitted to remote leaderboards or competitive records."
 */
@Singleton
class InMemoryPracticeSessionRepository @Inject constructor() : PracticeSessionRepository {

    private val sessionsById = ConcurrentHashMap<String, PracticeSession>()
    private val activeByUserId = ConcurrentHashMap<String, String>() // userId -> sessionId
    private val userSessionHistory = ConcurrentHashMap<String, MutableList<String>>() // userId -> list of sessionIds

    override fun savePracticeSession(session: PracticeSession) {
        val sId = session.sessionId.value
        val uId = session.userId.value

        sessionsById[sId] = session

        // Track active session if not completed or interrupted
        if (session.status == PracticeSessionStatus.IN_PROGRESS || session.status == PracticeSessionStatus.NOT_STARTED) {
            activeByUserId[uId] = sId
        } else if (activeByUserId[uId] == sId) {
            activeByUserId.remove(uId)
        }

        // Maintain history list
        val history = userSessionHistory.computeIfAbsent(uId) { mutableListOf() }
        synchronized(history) {
            if (!history.contains(sId)) {
                history.add(0, sId)
            }
        }
    }

    override fun getPracticeSession(sessionId: PracticeSessionId): PracticeSession? {
        return sessionsById[sessionId.value]
    }

    override fun getActivePracticeSession(userId: UserId): PracticeSession? {
        val activeId = activeByUserId[userId.value] ?: return null
        return sessionsById[activeId]
    }

    override fun getRecentPracticeSessions(userId: UserId, limit: Int): List<PracticeSession> {
        val history = userSessionHistory[userId.value] ?: return emptyList()
        val sessionIds = synchronized(history) { history.take(limit) }
        return sessionIds.mapNotNull { sessionsById[it] }
    }

    override fun clearActivePracticeSession(userId: UserId) {
        activeByUserId.remove(userId.value)
    }

    override fun deletePracticeSession(sessionId: PracticeSessionId) {
        val session = sessionsById.remove(sessionId.value)
        if (session != null) {
            val uId = session.userId.value
            if (activeByUserId[uId] == sessionId.value) {
                activeByUserId.remove(uId)
            }
            val history = userSessionHistory[uId]
            if (history != null) {
                synchronized(history) {
                    history.remove(sessionId.value)
                }
            }
        }
    }
}
